package ilo;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class HttpClientBuilder {
	private static X509TrustManager[] trustAllCerts = new X509TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			 return new java.security.cert.X509Certificate[]{};
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}
	} };

	public static OkHttpClient insecureOk() {
		OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		}).sslSocketFactory(getSSLContextFactory(), trustAllCerts[0]).build();
		return client;
	}

	private static SSLSocketFactory getSSLContextFactory() {
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			return sc.getSocketFactory();
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			throw new IllegalStateException("Could not create client", e);
		}
	}

	public static String basicAuth(Credentials creds) {
		return "Basic "
				+ Base64.getEncoder().encodeToString((creds.getUsername() + ":" + creds.getPassword()).getBytes());
	}

}
