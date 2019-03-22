package ilo.model;

import com.fasterxml.jackson.databind.JsonNode;

public class ChassisNode {
	private final JsonNode node;
	private final ThermalNode thermalNode;
	private final PowerNode powerNode;
	private final SystemNode systemNode;

	public ChassisNode(JsonNode node, ThermalNode thermalNode, PowerNode powerNode, SystemNode systemNode) {
		this.node = node;
		this.thermalNode = thermalNode;
		this.powerNode = powerNode;
		this.systemNode = systemNode;
	}

	public PowerNode getPowerNode() {
		return powerNode;
	}

	public ThermalNode getThermalNode() {
		return thermalNode;
	}

	public JsonNode getNode() {
		return node;
	}

	public String toString() {
		return node.toString();
	}

	public String getHostName() {
		return systemNode.getHostName();
	}

	public SystemNode getSystemNode() {
		return systemNode;
	}
}
