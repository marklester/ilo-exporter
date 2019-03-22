package ilo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import ilo.model.ChassisNode;
import ilo.model.FanNode;
import ilo.model.TemperatureNode;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

public class IloCollector extends Collector {
	private List<IloHttpClient> clients;
	LoadingCache<IloHttpClient, ChassisNode> nodes = CacheBuilder.newBuilder().refreshAfterWrite(Duration.ofSeconds(30))
			.build(new CacheLoader<IloHttpClient, ChassisNode>() {

				@Override
				public ChassisNode load(IloHttpClient key) throws Exception {
					return key.getChassisNode();
				}
			});

	public IloCollector() {
		Credentials creds = Credentials.fromEnvironment();
		List<String> servers = Splitter.on(",").omitEmptyStrings().splitToList(System.getenv("ilo.hosts"));
		clients = servers.stream().map(s -> new IloHttpClient(creds, s)).collect(Collectors.toList());
	}

	@Override
	public List<MetricFamilySamples> collect() {
		List<MetricFamilySamples> samples = new ArrayList<>();
		GaugeMetricFamily powerSamples = new GaugeMetricFamily("ilo_chassis_power_in_watts", "power in watts",
				Arrays.asList("hostname"));
		GaugeMetricFamily fanSamples = new GaugeMetricFamily("ilo_chassis_fan_percent", "percent fans",
				Arrays.asList("hostname", "fan"));
		GaugeMetricFamily tempSamples = new GaugeMetricFamily("ilo_chassis_temp", "tempurature (C)",
				Arrays.asList("hostname", "temp"));
		for (IloHttpClient client : clients) {
			ChassisNode node;
			try {
				node = nodes.get(client);
				String hostname = node.getHostName();
				powerSamples.addMetric(Arrays.asList(hostname), node.getPowerNode().getValue());
				for (FanNode fanNode : node.getThermalNode().getFans()) {
					fanSamples.addMetric(Arrays.asList(hostname, fanNode.getLabel()), fanNode.getValue());
				}
				for (TemperatureNode tempNode : node.getThermalNode().getTempuratures()) {
					tempSamples.addMetric(Arrays.asList(hostname, tempNode.getLabel()), tempNode.getValue());
				}
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}
		samples.add(powerSamples);
		samples.add(fanSamples);
		samples.add(tempSamples);
		return samples;
	}

}
