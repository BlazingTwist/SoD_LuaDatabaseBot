package blazingtwist.sod_luadatabasebot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

public class WikiUpload {

	private final String wikiApiUrl;
	private final WebContext webContext;

	private String loginToken;
	private String csrfToken;

	public WikiUpload(String wikiUrl) {
		webContext = new WebContext();

		wikiUrl = wikiUrl.endsWith("/") ? wikiUrl : (wikiUrl + "/");
		wikiApiUrl = wikiUrl + "api.php";
	}

	public MediaWikiResponse<String> acquireLoginToken() {
		MediaWikiResponse<String> result = sendRequest(
				new WebContext.WebRequest(wikiApiUrl)
						.param("action", "query")
						.param("meta", "tokens")
						.param("format", "json")
						.param("type", "login"),
				rootNode -> rootNode.node("query", "tokens", "logintoken").getString(),
				rootNode -> {
					if (!rootNode.node("query", "tokens", "logintoken").empty()) {
						return null;
					}
					return new MediaWikiResponse.Error("Missing Token", "MediaWiki API returned no Token", Collections.emptyMap());
				}
		);
		loginToken = result.result().orElse("+\\");
		return result;
	}

	public MediaWikiResponse<Void> logIn(String userName, String password) {
		return sendRequest(
				new WebContext.WebRequest(wikiApiUrl)
						.param("format", "json")
						.param("action", "login")
						.body("lgname", userName)
						.body("lgpassword", password)
						.body("lgtoken", loginToken),
				resultErrorHandler("login")
		);
	}

	public MediaWikiResponse<String> acquireCsrfToken() {
		MediaWikiResponse<String> result = sendRequest(
				new WebContext.WebRequest(wikiApiUrl)
						.param("action", "query")
						.param("meta", "tokens")
						.param("format", "json")
						.param("type", "csrf"),
				rootNode -> rootNode.node("query", "tokens", "csrftoken").getString(),
				rootNode -> {
					if (!rootNode.node("query", "tokens", "csrftoken").empty()) {
						return null;
					}
					return new MediaWikiResponse.Error("Missing Token", "MediaWiki API returned no Token", Collections.emptyMap());
				}
		);
		csrfToken = result.result().orElse("+\\");
		return result;
	}

	public MediaWikiResponse<Void> uploadFile(String wikiFilePath, String fileData) {
		return sendRequest(
				new WebContext.WebRequest(wikiApiUrl)
						.param("action", "edit")
						.param("title", wikiFilePath)
						.param("summary", "upload from LuaDatabaseBot")
						.param("format", "json")
						.body("token", csrfToken)
						.body("text", fileData),
				resultErrorHandler("edit")
		);
	}

	public MediaWikiResponse<Void> sendRequest(WebContext.WebRequest request) {
		return sendRequest(request, node -> null, node -> null);
	}

	public MediaWikiResponse<Void> sendRequest(
			WebContext.WebRequest request,
			Function<BasicConfigurationNode, MediaWikiResponse.Error> specialErrorMapper) {
		return sendRequest(request, node -> null, specialErrorMapper);
	}

	public <T> MediaWikiResponse<T> sendRequest(
			WebContext.WebRequest request,
			Function<BasicConfigurationNode, T> resultMapper,
			Function<BasicConfigurationNode, MediaWikiResponse.Error> specialErrorMapper) {
		try {
			WebContext.WebResponse response = webContext.sendRequest(request);
			int statusCode = response.response().statusCode();
			if (statusCode < 200 || statusCode >= 300) {
				return errorResponse(response, MediaWikiResponse.Error.fromStatusCode(statusCode));
			}

			String moduleName = request.params.getOrDefault("action", null);
			MediaWikiResponse.Status status = MediaWikiResponse.Status.Success;
			MediaWikiResponse.Warnings warnings = null;
			MediaWikiResponse.Error error;
			BasicConfigurationNode rootNode = JacksonConfigurationLoader.builder()
					.source(() -> new BufferedReader(new StringReader(response.responseText())))
					.build()
					.load();

			BasicConfigurationNode warningsNode = rootNode.node("warnings");
			if (!warningsNode.empty()) {
				warnings = MediaWikiResponse.Warnings.fromResponseNode(warningsNode);
				if (warnings.getModuleWarnings(moduleName) != null) {
					status = MediaWikiResponse.Status.Warning;
				}
			}

			BasicConfigurationNode errorNode = rootNode.node("error");
			if (!errorNode.empty()) {
				status = MediaWikiResponse.Status.Error;
				error = MediaWikiResponse.Error.fromErrorNode(errorNode);
			} else {
				error = specialErrorMapper.apply(rootNode);
				if (error != null) {
					status = MediaWikiResponse.Status.Error;
				}
			}

			T result = null;
			if (status == MediaWikiResponse.Status.Success) {
				result = resultMapper.apply(rootNode);
			}

			return new MediaWikiResponse<>(
					status,
					Optional.of(response),
					Optional.ofNullable(result),
					Optional.ofNullable(warnings),
					Optional.ofNullable(error)
			);

		} catch (IOException | InterruptedException e) {
			return errorResponse(null, MediaWikiResponse.Error.fromException(e));
		}
	}

	private static <T> MediaWikiResponse<T> errorResponse(WebContext.WebResponse rawResponse, MediaWikiResponse.Error error) {
		return new MediaWikiResponse<>(
				MediaWikiResponse.Status.Error,
				Optional.ofNullable(rawResponse),
				Optional.empty(),
				Optional.empty(),
				Optional.of(error)
		);
	}

	private static Function<BasicConfigurationNode, MediaWikiResponse.Error> resultErrorHandler(String moduleName) {
		return rootNode -> {
			BasicConfigurationNode moduleNode = rootNode.node(moduleName);
			String resultString = moduleNode.node("result").getString();
			if (resultString != null && resultString.equalsIgnoreCase("Success")) {
				return null;
			}
			return unsuccessfulResultError(resultString, moduleNode);
		};
	}

	private static MediaWikiResponse.Error unsuccessfulResultError(String result, ConfigurationNode moduleNode) {
		HashMap<String, String> additionalInfo = new HashMap<>();
		for (Map.Entry<Object, ? extends ConfigurationNode> entry : moduleNode.childrenMap().entrySet()) {
			additionalInfo.put(entry.getKey().toString(), entry.getValue().getString());
		}
		return new MediaWikiResponse.Error(result, "Unsuccessful Result", additionalInfo);
	}

}
