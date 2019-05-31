package ilo;

import java.io.IOException;
import java.net.URI;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IloHttpClient {
	private ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	private OkHttpClient client;
	private URI power;
	private URI thermal;
	private URI chassis;
	private URI system;
	private Credentials creds;
	private String ip;
	private String sessionToken;
	private LoadingCache<Request, JsonNode> responseCache;

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
		client = HttpClientBuilder.insecureOk();
		sessionUrl = URI.create(base + "SessionService/Sessions/");
		sessionToken = createSession(sessionUrl);
	}

	public void initCache() {
		Duration refreshRate = Duration.parse(System.getenv().getOrDefault("refresh.rate", "PT30s"));
		this.responseCache = CacheBuilder.newBuilder().refreshAfterWrite(refreshRate)
				.build(new CacheLoader<Request, JsonNode>() {

					@Override
					public JsonNode load(Request key) throws Exception {
						return getJsonInternal(key);
					}

				});
	}

	public URI getNodeUri() {
		return URI.create("https://" + ip);
	}

	public ChassisNode getChassisNode() {
		Request req = session(chassis);
		JsonNode node = getJson(req);
		return new ChassisNode(node, getThermalNode(), getPowerNode(), getSystemNode());
	}

	public JsonNode getJson(Request request) {
		return responseCache.getUnchecked(request);
	}

	private JsonNode getJsonInternal(Request request) {
		try (Response response = client.newCall(request).execute()) {
			if (response.code() == 401) {
				sessionToken = createSession(sessionUrl);
			}
			if (!response.isSuccessful()) {
				throw new IllegalStateException("could not retrieve json: " + response.toString());
			}
			return mapper.readTree(response.body().string());
		} catch (IOException e) {
			throw new IllegalStateException("could not retrieve json", e);
		}
	}

	public PowerNode getPowerNode() {
		Request req = session(power);

		JsonNode node = getJson(req);
		return new PowerNode(node);
	}

	public ThermalNode getThermalNode() {
		Request req = session(thermal);
		JsonNode node = getJson(req);
		return new ThermalNode(node);
	}

	public SystemNode getSystemNode() {
		Request req = session(system);
		JsonNode node = getJson(req);
		return new SystemNode(node, getStorageNode());
	}

	public Request session(URI uri) {
		return new Request.Builder().header("x-auth-token", sessionToken).url(uri.toString()).build();
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

	public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	private String createSession(URI sessionUrl) {
		Request req = new Request.Builder().header("Content-Type", "application/json").header("OData-Version", "4.0")
				.url(sessionUrl.toString()).post(RequestBody.create(JSON, creds.toJson())).build();
		try (Response resp = client.newCall(req).execute()) {
			return resp.headers().get("x-auth-token");
		} catch (IOException e) {
			throw new IllegalStateException("Could not create session", e);
		}
	}
}
