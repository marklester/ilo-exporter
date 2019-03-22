package ilo;

import java.io.IOException;

import io.prometheus.client.exporter.HTTPServer;

//jib this bish
public class IloExporter {
	static final IloCollector COLLECTOR = new IloCollector().register();

	public static void main(String[] args) throws IOException {
		new HTTPServer(1234);
	}

}
