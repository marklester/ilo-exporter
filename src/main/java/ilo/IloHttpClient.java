package ilo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ilo.model.ChassisNode;
import ilo.model.PowerNode;
import ilo.model.StorageNode;
import ilo.model.SystemNode;
import ilo.model.ThermalNode;

public class IloHttpClient {
	private ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	private TrustManager[] trustAllCerts;
	private HttpClient client;
	private URI power;
	private URI thermal;
	private URI chassis;
	private URI system;
	private Credentials creds;
	private String ip;

	public IloHttpClient(Credentials creds, String ip) throws IllegalStateException {
		this.creds = creds;
		this.ip = ip;
		String base = String.format("https://%s/redfish/v1/", ip);
		system = URI.create(base + "systems/1/");

		chassis = URI.create(base + "chassis/1/");
		thermal = URI.create(chassis + "thermal/");
		power = URI.create(chassis + "power/");

		final Properties props = System.getProperties();
		props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());
		trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
		SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			client = HttpClient.newBuilder().sslContext(sc).build();
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new IllegalStateException("Could not create client", e);
		}

	}
	
	public URI getNodeUri() {
		return URI.create("https://"+ip);
	}
	
	public ChassisNode getChassisNode() {
		var req = HttpRequest.newBuilder().header("Authorization", basicAuth(creds)).uri(chassis).build();
		JsonNode node = getJson(req);
		return new ChassisNode(node, getThermalNode(), getPowerNode(), getSystemNode());
	}

	public JsonNode getJson(HttpRequest request) {

		try {
			var response = client.send(request, BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				throw new IllegalStateException("could not retrieve json: " + response.toString());
			}
			return mapper.readTree(response.body());
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException("could not retrieve json", e);
		}
	}

	public PowerNode getPowerNode() {
		var req = HttpRequest.newBuilder().header("Authorization", basicAuth(creds)).uri(power).build();

		JsonNode node = getJson(req);
		return new PowerNode(node);
	}

	public ThermalNode getThermalNode() {
		var req = HttpRequest.newBuilder().header("Authorization", basicAuth(creds)).uri(thermal).build();
		JsonNode node = getJson(req);
		return new ThermalNode(node);
	}

	public SystemNode getSystemNode() {
		var req = HttpRequest.newBuilder().header("Authorization", basicAuth(creds)).uri(system).build();
		JsonNode node = getJson(req);
		return new SystemNode(node,getStorageNode());
	}
	
	public HttpRequest.Builder reqBuilder(){
		return HttpRequest.newBuilder().header("Authorization", basicAuth(creds));
	}
	
	public StorageNode getStorageNode() {
		StorageClient client = new StorageClient(this);
		return client.getStorageNode();
	}

	private String basicAuth(Credentials creds) {
		return "Basic "
				+ Base64.getEncoder().encodeToString((creds.getUsername() + ":" + creds.getPassword()).getBytes());
	}

	@Override
	public int hashCode() {
		return Objects.hash(ip);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IloHttpClient other = (IloHttpClient) obj;
		return Objects.equals(ip, other.ip);
	}

	public URI getSystemUri() {
		return system;
	}
}
