package ilo;

import java.io.IOException;

import io.prometheus.client.exporter.HTTPServer;

public class IloExporter {
	static final IloCollector COLLECTOR = new IloCollector().register();

	public static void main(String[] args) throws IOException {
		String port = System.getenv().getOrDefault("ilo.port", "9416");
		System.out.println("Starting server on port: " + port);
		new HTTPServer(Integer.parseInt(port));	}

}
