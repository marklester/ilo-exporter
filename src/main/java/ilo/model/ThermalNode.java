package ilo.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

public class ThermalNode {
	JsonNode node;

	public ThermalNode(JsonNode node) {
		this.node = node;
	}

	public Set<FanNode> getFans() {
		Set<FanNode> fans = new HashSet<FanNode>();
		for (JsonNode node : node.get("Fans")) {
			fans.add(new FanNode(node));
		}
		return fans;
	}
	
	public Set<TemperatureNode> getTempuratures() {
		var temps = new HashSet<TemperatureNode>();
		for (JsonNode node : node.get("Temperatures")) {
			temps.add(new TemperatureNode(node));
		}
		return temps;	
	}
	
}
