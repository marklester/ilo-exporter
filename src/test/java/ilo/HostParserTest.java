package ilo;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HostParserTest {

	@Test
	public void testParseHosts() {
		HostParser parser = new HostParser();
		List<String> hosts = parser.parseHosts("191-192.168.2.2-3,192.168.2.4");
		Assertions.assertEquals(5, hosts.size());
	}

	@Test
	public void testParseHost() {
		HostParser parser = new HostParser();
		List<String> hosts = parser.parseHost("191-192.168.2.1-3");
		Assertions.assertEquals(6, hosts.size());
	}

	@Test
	public void testExpand() {
		HostParser parser = new HostParser();
		List<List<String>> expanded = parser.expand("191-192.168.2.1-3");
		Assertions.assertEquals(4, expanded.size());
		Assertions.assertEquals(2, expanded.get(0).size());
		Assertions.assertEquals(1, expanded.get(1).size());
		Assertions.assertEquals(1, expanded.get(2).size());
		Assertions.assertEquals(3, expanded.get(3).size());
		List<String> reduce = parser.reduce(expanded);
		Assertions.assertEquals(6, reduce.size());
	}

	@Test
	public void testExpandInts() {
		HostParser parser = new HostParser();
		List<String> hosts = parser.expandInt("1-3");
		Assertions.assertEquals(3, hosts.size());
		hosts = parser.expandInt("1");
		Assertions.assertEquals(1, hosts.size());
	}

	@Test
	public void testMoreDigits() {
		HostParser parser = new HostParser();
		List<String> hosts = parser.expandInt("191-193");
		Assertions.assertEquals(3, hosts.size());
	}

//	@Test
//	public void getUPSstats() throws IOException, InterruptedException {
//
//		var creds = new Credentials("", "");
//		var chasis = URI.create("https://nimbus-ups");
//		Shell shell = new Ssh("nimbus-ups", 22, "username", "key...");
//		String stdout = new Shell.Plain(shell).exec("echo 'Hello, world!'");
//	}

}
