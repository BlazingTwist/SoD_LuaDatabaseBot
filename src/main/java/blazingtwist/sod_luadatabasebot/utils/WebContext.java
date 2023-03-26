package blazingtwist.sod_luadatabasebot.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;

public class WebContext {

	private static final Charset encoderCharset = StandardCharsets.UTF_16LE;

	private final HashMap<String, String> cookies = new HashMap<>();

	public String getCookieString() {
		return cookies.entrySet().stream()
				.map(entry -> URLEncoder.encode(entry.getKey(), encoderCharset) + "=" + URLEncoder.encode(entry.getValue(), encoderCharset))
				.collect(Collectors.joining("; "));
	}

	private WebResponse _sendRequest(WebRequest request, String contentType) throws IOException {
		URLConnection connection = request.toUrl().openConnection();
		if (request.bodyForm.size() > 0) {
			if (request.baseUrl.startsWith("https:/")) {
				HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
				httpsConnection.setRequestMethod("POST");
			} else if (request.baseUrl.startsWith("http:/")) {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setRequestMethod("POST");
			}
		} else {
			if (request.baseUrl.startsWith("https:/")) {
				HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
				httpsConnection.setRequestMethod("GET");
			} else if (request.baseUrl.startsWith("http:/")) {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setRequestMethod("GET");
			}
		}

		connection.setRequestProperty("Accept", "*/*");
		connection.setRequestProperty("Accept-Encoding", "gzip");
		connection.setRequestProperty("Connection", "keep-alive");
		connection.setRequestProperty("Content-Type", contentType);
		String cookieString = getCookieString();
		if (!StringUtils.isNullOrWhitespace(cookieString)) {
			connection.setRequestProperty("Cookie", cookieString);
		}
		if (request.bodyForm.size() > 0) {
			String bodyString = request.getBodyString();
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", "" + bodyString.length());
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(bodyString.getBytes(StandardCharsets.UTF_8));
			outputStream.flush();
			outputStream.close();
		}
		connection.connect();

		//new GZIPInputStream(connection.getInputStream()).readNBytes(64);
		byte[] responseBytes = new byte[Integer.parseInt(connection.getHeaderFields().get("Content-Length").get(0))];
		InputStream inputStream = connection.getInputStream();
		byte[] buffer = new byte[1024];
		int readBytes;
		int totalReadBytes = 0;
		while ((readBytes = inputStream.read(buffer)) > 0) {
			System.arraycopy(buffer, 0, responseBytes, totalReadBytes, readBytes);
			totalReadBytes += readBytes;
		}
		inputStream.close();
		GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(responseBytes));
		String responseString = new String(gzipInputStream.readAllBytes(), StandardCharsets.UTF_8);
		gzipInputStream.close();
		for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
			if ("set-cookie".equalsIgnoreCase(entry.getKey())) {
				for (String setCookie : entry.getValue()) {
					String[] cookieSplit = setCookie.split("; ")[0].split("=");
					cookies.put(URLDecoder.decode(cookieSplit[0], encoderCharset), URLDecoder.decode(cookieSplit[1], encoderCharset));
				}
			}
		}
		return new WebResponse(connection.getURL(), responseString, connection.getHeaderFields());
	}

	public WebResponse sendRequest(WebRequest request) throws IOException {
		return _sendRequest(request, "application/json");
	}

	public WebResponse sendRequestMultipart(WebRequest request) throws IOException {
		return _sendRequest(request, "multipart/form-data");
	}

	public record WebResponse(URL url, String responseText, Map<String, List<String>> headerFields) {
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

		public URL toUrl() {
			String urlString = baseUrl;
			if (params.size() > 0) {
				String paramString = params.entrySet().stream()
						.map(kvp -> URLEncoder.encode(kvp.getKey(), encoderCharset) + "=" + URLEncoder.encode(kvp.getValue(), encoderCharset))
						.collect(Collectors.joining("&"));
				urlString += ("?" + paramString);
			}
			try {
				return new URL(urlString);
			} catch (MalformedURLException e) {
				System.err.println("Failed to build URL. Exception: " + e);
				return null;
			}
		}

		public String getBodyString() {
			return bodyForm.entrySet().stream()
					.map(kvp -> URLEncoder.encode(kvp.getKey(), encoderCharset) + "=" + URLEncoder.encode(kvp.getValue(), encoderCharset))
					.collect(Collectors.joining("&"));
		}
	}
}
