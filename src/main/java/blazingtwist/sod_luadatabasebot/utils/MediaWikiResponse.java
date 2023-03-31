package blazingtwist.sod_luadatabasebot.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.configurate.ConfigurationNode;

public record MediaWikiResponse<T>(
		Status status,
		Optional<WebContext.WebResponse> rawResponse,
		Optional<T> result,
		Optional<Warnings> warnings,
		Optional<Error> errorInfo
) {

	public enum Status {
		Success,
		Warning,
		Error
	}

	public static record Warnings(Map<String, String[]> rawWarnings) {
		public static Warnings fromResponseNode(ConfigurationNode node) {
			HashMap<String, String[]> result = new HashMap<>();

			for (Map.Entry<Object, ? extends ConfigurationNode> childEntry : node.childrenMap().entrySet()) {
				ConfigurationNode messageNode = childEntry.getValue().node("*");
				if (messageNode.empty()) {
					continue;
				}

				String module = childEntry.getKey().toString();
				String messages = messageNode.getString();
				if (messages == null) {
					continue;
				}

				result.put(module, messages.split("\n"));
			}

			return new Warnings(result);
		}

		public Set<String> getModulesWithWarnings() {
			return rawWarnings.keySet();
		}

		public String[] getModuleWarnings(String moduleName) {
			return rawWarnings.getOrDefault(moduleName, new String[0]);
		}
	}

	public static record Error(String code, String info, Map<String, String> additionalInfo) {
		public static Error fromException(Exception e) {
			return new Error("Exception", e.getMessage(), Collections.emptyMap());
		}

		public static Error fromStatusCode(int statusCode) {
			String statusMessage = switch (statusCode) {
				case 100 -> "Continue";
				case 101 -> "Switching Protocols";
				case 102 -> "Processing";
				case 103 -> "Early Hints";
				case 300 -> "Multiple Choices";
				case 301 -> "Moved Permanently";
				case 302 -> "Found";
				case 303 -> "See Other";
				case 304 -> "Not Modified";
				case 305 -> "Use Proxy";
				case 307 -> "Temporary Redirect";
				case 308 -> "Permanent Redirect";
				case 400 -> "Bad Request";
				case 401 -> "Unauthorized";
				case 402 -> "Payment Required";
				case 403 -> "Forbidden";
				case 404 -> "Not Found";
				case 405 -> "Method Not Allowed";
				case 406 -> "Not Accessible";
				case 407 -> "Proxy Authentication Required";
				case 408 -> "Request Timeout";
				case 409 -> "Conflict";
				case 410 -> "Gone";
				case 411 -> "Length Required";
				case 412 -> "Precondition Failed";
				case 413 -> "Payload Too Large";
				case 414 -> "URI Too Long";
				case 415 -> "Unsupported Media Type";
				case 416 -> "Range Not Satisfiable";
				case 417 -> "Expectation Failed";
				case 418 -> "I'm a teapot";
				case 421 -> "Misdirected Request";
				case 422 -> "Unprocessable Content (WebDAV)";
				case 423 -> "Locked (WebDAV)";
				case 424 -> "Failed Dependency (WebDAV)";
				case 425 -> "Too Early Experimental";
				case 426 -> "Upgrade Required";
				case 428 -> "Precondition Required";
				case 429 -> "Too Many Requests";
				case 431 -> "Request Header Fields Too Large";
				case 451 -> "Unavailable For Legal Reasons";
				case 500 -> "Internal Server Error";
				case 501 -> "Not Implemented";
				case 502 -> "Bad Gateway";
				case 503 -> "Service Unavailable";
				case 504 -> "Gateway Timeout";
				case 505 -> "HTTP Version Not Supported";
				case 506 -> "Variant Also Negotiates";
				case 507 -> "Insufficient Storage (WebDAV)";
				case 508 -> "Loop Detected (WebDAV)";
				case 510 -> "Not Extended";
				case 511 -> "Network Authentication Required";
				default -> "Unknown";
			};
			return new Error("" + statusCode, statusMessage, Collections.emptyMap());
		}

		public static Error fromErrorNode(ConfigurationNode node) {
			String errorCode = null;
			String errorInfo = null;
			HashMap<String, String> additionalInfo = new HashMap<>();

			for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
				String key = entry.getKey().toString();
				String value = entry.getValue().getString();
				if (key.equalsIgnoreCase("code")) {
					errorCode = value;
				} else if (key.equalsIgnoreCase("info")) {
					errorInfo = value;
				} else {
					additionalInfo.put(key, value);
				}
			}

			return new Error(errorCode, errorInfo, additionalInfo);
		}
	}

}
