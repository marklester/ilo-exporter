package ilo.model;

import com.fasterxml.jackson.databind.JsonNode;

import ilo.Labels;

public class FanNode {
	JsonNode node;

	public FanNode(JsonNode jsonNode) {
		this.node = jsonNode;
	}

	public Double getValue() {
		return node.get("CurrentReading").asDouble();
	}

	public String getName() {
		return node.get("FanName").asText();
	}
	
	public String getLabel() {
		return Labels.from(getName());
	}

	@Override
	public String toString() {
		return getLabel()+"="+getValue();
	}
}
