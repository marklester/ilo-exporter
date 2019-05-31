package ilo.model;

import java.util.List;

import com.google.common.base.Splitter;

public class DiskLocation {
	public static DiskLocation parseLocationString(String locationString) {
		List<String> parts = Splitter.on(":").splitToList(locationString);
		return new DiskLocation(parts.get(0), parts.get(1), parts.get(2));
	}

	private final String controllerPort;
	private final String box;
	private final String bay;

	public DiskLocation(String containerPort, String box, String bay) {
		this.controllerPort = containerPort;
		this.box = box;
		this.bay = bay;
	}

	public String getControllerPort() {
		return controllerPort;
	}

	public String getBox() {
		return box;
	}

	public String getBay() {
		return bay;
	}

	@Override
	public String toString() {
		return "DiskLocation [controllerPort=" + controllerPort + ", box=" + box + ", bay=" + bay + "]";
	}
}
