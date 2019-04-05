package ilo.model;

public class DiskStatus {

	private final String status;
	private final String reason;
	private final String state;

	public DiskStatus(String status, String state, String reason) {
		this.status = status;
		this.reason = reason;
		this.state = state;
	}

	public String getStatus() {
		return status;
	}

	public String getReason() {
		return reason;
	}

	public String getState() {
		return state;
	}

	@Override
	public String toString() {
		return "DiskStatus [status=" + status + ", reason=" + reason + ", state=" + state + "]";
	}
}
