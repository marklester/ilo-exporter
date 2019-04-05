package ilo.model;

import com.fasterxml.jackson.databind.JsonNode;

public class SystemNode {
	private final JsonNode node;
	private StorageNode storageNode;

	public SystemNode(JsonNode node,StorageNode storageNode) {
		this.node = node;
		this.storageNode = storageNode;
	}
	
	public JsonNode getNode() {
		return node;
	}
	
	public String getHostName() {
		return node.get("HostName").asText();
	}
	
	public StorageNode getStorageNode(){
		return storageNode;
	}
	
	public String toString() {
		return node.toString();
	}
}
