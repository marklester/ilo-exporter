package ilo.model;

import java.util.List;

public class ArrayController {
	private List<DiskNode> diskNodes;

	public ArrayController(List<DiskNode> diskNodes) {
		this.diskNodes = diskNodes;
	}

	public List<DiskNode> getDiskDrives() {
		return diskNodes;
	}

	public String toString() {
		return diskNodes.toString();
	}
}
