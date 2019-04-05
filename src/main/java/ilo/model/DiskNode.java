package ilo.model;

import com.fasterxml.jackson.databind.JsonNode;

public class DiskNode {

	private final JsonNode diskJson;
	private final DiskStatus diskStatus;
	private final DiskLocation location;

	public DiskNode(JsonNode diskNode) {
		this.diskJson = diskNode;
		String status = diskJson.get("Status").get("Health").asText();
		String state = diskJson.get("Status").get("State").asText();
		String reason = diskJson.get("DiskDriveStatusReasons").get(0).asText();
		diskStatus = new DiskStatus(status,state, reason);
		var locationString= diskJson.get("Location").asText();
		location = DiskLocation.parseLocationString(locationString);
	}

	public DiskLocation getLocation() {
		return location;
	}

	public DiskStatus getHealth() {
		return diskStatus;
	}

	public JsonNode toJson() {
		return diskJson;
	}

	public String toString() {
		return getLocation() + ":" + getHealth();
	}
}
