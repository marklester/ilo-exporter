package ilo.model;

import com.fasterxml.jackson.databind.JsonNode;

public class SystemNode {
	private final JsonNode node;

	public SystemNode(JsonNode node) {
		this.node = node;
	}
	
	public JsonNode getNode() {
		return node;
	}
	
	public String getHostName() {
		return node.get("HostName").asText();
	}
	
	public String toString() {
		return node.toString();
	}
}
