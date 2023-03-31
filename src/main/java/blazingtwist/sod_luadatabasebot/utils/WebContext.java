package blazingtwist.sod_luadatabasebot.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

public class WebContext {

	private static final Charset encoderCharset = StandardCharsets.UTF_8;

	private final HashMap<String, String> cookies = new HashMap<>();

	public String getCookieString() {
		return cookies.entrySet().stream()
				.map(entry -> URLEncoder.encode(entry.getKey(), encoderCharset) + "=" + URLEncoder.encode(entry.getValue(), encoderCharset))
				.collect(Collectors.joining("; "));
	}

	private WebResponse _sendRequest(WebRequest request, String contentType) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(request.toUri());

		if (request.bodyForm.size() > 0) {
			String bodyString = request.getBodyString();
			requestBuilder.header("Content-Type", "application/x-www-form-urlencoded");
			requestBuilder.POST(HttpRequest.BodyPublishers.ofString(bodyString, StandardCharsets.UTF_8));
		} else {
			requestBuilder.GET();
		}

		requestBuilder.header("Accept", "*/*");
		requestBuilder.header("Accept-Encoding", "gzip");
		requestBuilder.header("Content-Type", contentType);
		String cookieString = getCookieString();
		if (!StringUtils.isNullOrWhitespace(cookieString)) {
			requestBuilder.header("Cookie", cookieString);
		}

		HttpResponse<byte[]> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
		for (Map.Entry<String, List<String>> headerEntry : response.headers().map().entrySet()) {
			if ("set-cookie".equalsIgnoreCase(headerEntry.getKey())) {
				for (String setCookie : headerEntry.getValue()) {
					String[] cookieSplit = setCookie.split("; ")[0].split("=");
					cookies.put(URLDecoder.decode(cookieSplit[0], encoderCharset), URLDecoder.decode(cookieSplit[1], encoderCharset));
				}
			}
		}

		GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(response.body()));
		String string = new String(gzipInputStream.readAllBytes(), StandardCharsets.UTF_8);
		BasicConfigurationNode rootNode = JacksonConfigurationLoader.builder()
				.source(() -> new BufferedReader(new StringReader(string)))
				.build()
				.load();

		return new WebResponse(response, string, rootNode);
	}

	public WebResponse sendRequest(WebRequest request) throws IOException, InterruptedException {
		return _sendRequest(request, "application/json");
	}

	public WebResponse sendRequestMultipart(WebRequest request) throws IOException, InterruptedException {
		return _sendRequest(request, "multipart/form-data");
	}

	public record WebResponse(HttpResponse<?> response, String responseText, BasicConfigurationNode responseNode) {
	}

	public static class WebRequest {
		public final String baseUrl;
		public final Map<String, String> params = new HashMap<>();
		public final Map<String, String> bodyForm = new HashMap<>();

		public WebRequest(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public WebRequest param(String key, String value) {
			params.put(key, value);
			return this;
		}

		public WebRequest body(String key, String value) {
			bodyForm.put(key, value);
			return this;
		}

		public URI toUri() {
			String urlString = baseUrl;
			if (params.size() > 0) {
				String paramString = params.entrySet().stream()
						.map(kvp -> URLEncoder.encode(kvp.getKey(), encoderCharset) + "=" + URLEncoder.encode(kvp.getValue(), encoderCharset))
						.collect(Collectors.joining("&"));
				urlString += ("?" + paramString);
			}
			return URI.create(urlString);
		}

		public String getBodyString() {
			return bodyForm.entrySet().stream()
					.map(kvp -> URLEncoder.encode(kvp.getKey(), encoderCharset) + "=" + URLEncoder.encode(kvp.getValue(), encoderCharset))
					.collect(Collectors.joining("&"));
		}
	}
}
