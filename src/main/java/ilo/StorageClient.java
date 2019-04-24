package ilo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ilo.model.DiskNode;
import ilo.model.ArrayController;
import ilo.model.StorageNode;

public class StorageClient {
	private IloHttpClient client;

	StorageClient(IloHttpClient client) {
		this.client = client;
	}

	public List<DiskNode> getDiskDrives(JsonNode disksJson) {
		var set = new ArrayList<DiskNode>();
		for (JsonNode member : disksJson.get("links").get("Member")) {
			var link = member.get("href").asText();
			var diskUri = URI.create(client.getNodeUri().toString() + link);
			var diskJson = client.getJson(client.session().uri(diskUri).build());
			set.add(new DiskNode(diskJson));
		}
		return set;
	}

	public List<ArrayController> getArrays(JsonNode arrayControllers) {
		var arrays = new ArrayList<ArrayController>();
		for (JsonNode arrayMember : arrayControllers.get("links").get("Member")) {
			String arrayLink = arrayMember.get("href").asText();			
			var disksUri = URI.create(client.getNodeUri().toString() + arrayLink + "diskdrives/");
			//System.out.println("getting disks for "+disksUri);
			var disksJson = client.getJson(client.session().uri(disksUri).build());
			arrays.add(new ArrayController(getDiskDrives(disksJson)));
		}
		return arrays;
	}

	public StorageNode getStorageNode() {
		URI arrayControllersUri = URI.create(client.getSystemUri().toString() + "SmartStorage/ArrayControllers/");
		JsonNode arraysJson = client.getJson(client.session().uri(arrayControllersUri).build());
		return new StorageNode(arraysJson, getArrays(arraysJson));
	}
}
