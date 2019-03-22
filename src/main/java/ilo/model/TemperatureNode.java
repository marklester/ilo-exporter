package ilo.model;

import com.fasterxml.jackson.databind.JsonNode;

import ilo.Labels;

public class TemperatureNode {
	JsonNode node;

	public TemperatureNode(JsonNode jsonNode) {
		this.node = jsonNode;
	}

	public Double getValue() {
		return node.get("CurrentReading").asDouble();
	}

	public String getName() {
		return node.get("Name").asText();
	}
	
	public String getLabel() {
		return Labels.from(getName());
	}

	@Override
	public String toString() {
		return getLabel()+"="+getValue();
	}
}
