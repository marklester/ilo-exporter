package ilo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import ilo.model.ArrayController;
import ilo.model.ChassisNode;
import ilo.model.DiskNode;
import ilo.model.FanNode;
import ilo.model.TemperatureNode;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

public class IloCollector extends Collector {
	private List<IloHttpClient> clients;
	LoadingCache<IloHttpClient, ChassisNode> nodeCache;
	private Duration refreshRate;

	public IloCollector() {
		Credentials creds = Credentials.fromEnvironment();
		Preconditions.checkNotNull(System.getenv("ilo.hosts"), "ilo.hosts environment variable is not set");
		List<String> servers = Splitter.on(",").omitEmptyStrings().splitToList(System.getenv("ilo.hosts"));
		clients = servers.stream().map(s -> new IloHttpClient(creds, s)).collect(Collectors.toList());
		initCache();
		System.out.println("Refresh rate set to: " + refreshRate);
		System.out.println("monitoring ilos: " + servers);
		System.out.println("using credentials: " + creds);
	}

	private void initCache() {
		refreshRate = Duration.parse(System.getenv().getOrDefault("refresh.rate", "PT30s"));
		nodeCache = CacheBuilder.newBuilder().refreshAfterWrite(refreshRate)
				.build(new CacheLoader<IloHttpClient, ChassisNode>() {

					@Override
					public ChassisNode load(IloHttpClient key) throws Exception {
						return key.getChassisNode();
					}
				});
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
		GaugeMetricFamily diskSamples = new GaugeMetricFamily("ilo_disk_status", "status of disks",
				Arrays.asList("hostname", "container_port", "box", "bay", "status","state", "reason"));
		for (IloHttpClient client : clients) {
			ChassisNode node;
			try {
				node = nodeCache.get(client);
				String hostname = node.getHostName();
				powerSamples.addMetric(Arrays.asList(hostname), node.getPowerNode().getValue());
				for (FanNode fanNode : node.getThermalNode().getFans()) {
					fanSamples.addMetric(Arrays.asList(hostname, fanNode.getLabel()), fanNode.getValue());
				}
				for (TemperatureNode tempNode : node.getThermalNode().getTempuratures()) {
					tempSamples.addMetric(Arrays.asList(hostname, tempNode.getLabel()), tempNode.getValue());
				}
				for (ArrayController array : node.getSystemNode().getStorageNode().getArrays()) {
					for (DiskNode disk : array.getDiskDrives()) {
						var health = disk.getHealth();
						double statusValue = health.getState().equals("Enabled") ? 1.0 : 0.0;

						var location = disk.getLocation();
						var port = location.getControllerPort();
						var box = location.getBox();
						var bay = location.getBay();
						diskSamples.addMetric(
								Arrays.asList(hostname, port, box, bay, health.getStatus(), health.getState(),health.getReason()),
								statusValue);
					}
				}
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}
		samples.add(powerSamples);
		samples.add(fanSamples);
		samples.add(tempSamples);
		samples.add(diskSamples);
		return samples;
	}

}
