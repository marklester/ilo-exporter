package ilo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import ilo.model.ChassisNode;
import ilo.model.PowerNode;
import ilo.model.StorageNode;
import ilo.model.SystemNode;
import ilo.model.ThermalNode;

public class IloHttpClient {
	private ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	private HttpClient client;
	private URI power;
	private URI thermal;
	private URI chassis;
	private URI system;
	private Credentials creds;
	private String ip;
	private String sessionToken;
	private LoadingCache<HttpRequest, JsonNode> responseCache;

	private URI sessionUrl;

	public IloHttpClient(Credentials creds, String ip) throws IllegalStateException {
		this.creds = creds;
		this.ip = ip;
		initCache();
		String base = String.format("https://%s/redfish/v1/", ip);
		system = URI.create(base + "systems/1/");

		chassis = URI.create(base + "chassis/1/");
		thermal = URI.create(chassis + "thermal/");
		power = URI.create(chassis + "power/");
		client = HttpClientBuilder.insecure();
		sessionUrl = URI.create(base + "SessionService/Sessions/");
		sessionToken = createSession(sessionUrl);
	}

	public void initCache() {
		var refreshRate = Duration.parse(System.getenv().getOrDefault("refresh.rate", "PT30s"));
		this.responseCache = CacheBuilder.newBuilder().refreshAfterWrite(refreshRate)
				.build(new CacheLoader<HttpRequest, JsonNode>() {

					@Override
					public JsonNode load(HttpRequest key) throws Exception {
						return getJsonInternal(key);
					}

				});
	}

	public URI getNodeUri() {
		return URI.create("https://" + ip);
	}

	public ChassisNode getChassisNode() {
		var req = session().uri(chassis).build();
		JsonNode node = getJson(req);
		return new ChassisNode(node, getThermalNode(), getPowerNode(), getSystemNode());
	}

	public JsonNode getJson(HttpRequest request) {
		return responseCache.getUnchecked(request);
	}

	private JsonNode getJsonInternal(HttpRequest request) {

		try {
			var response = client.send(request, BodyHandlers.ofString());
			if(response.statusCode()==401) {
				sessionToken = createSession(sessionUrl);
			}
			if (response.statusCode() != 200) {
				throw new IllegalStateException("could not retrieve json: " + response.toString());
			}
			
			return mapper.readTree(response.body());
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException("could not retrieve json", e);
		}
	}

	public PowerNode getPowerNode() {
		var req = session().uri(power).build();

		JsonNode node = getJson(req);
		return new PowerNode(node);
	}

	public ThermalNode getThermalNode() {
		var req = session().uri(thermal).build();
		JsonNode node = getJson(req);
		return new ThermalNode(node);
	}

	public SystemNode getSystemNode() {
		var req = session().uri(system).build();
		JsonNode node = getJson(req);
		return new SystemNode(node, getStorageNode());
	}

	public HttpRequest.Builder session() {
		return HttpRequest.newBuilder().header("x-auth-token", sessionToken);		
	}

	public HttpRequest.Builder basic() {
		return HttpRequest.newBuilder().header("Authorization", basicAuth(creds));
	}

	private String basicAuth(Credentials creds) {
		return HttpClientBuilder.basicAuth(creds);
	}

	public StorageNode getStorageNode() {
		StorageClient client = new StorageClient(this);
		return client.getStorageNode();
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

	private String createSession(URI sessionUrl) {
		HttpRequest req = HttpRequest.newBuilder().header("Content-Type", "application/json")
				.header("OData-Version", "4.0").uri(sessionUrl).POST(BodyPublishers.ofString(creds.toJson())).build();
		try {
			HttpResponse<String> resp = client.send(req, BodyHandlers.ofString());
			return resp.headers().firstValue("x-auth-token").get();
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException("Could not create session", e);
		}
	}
}
