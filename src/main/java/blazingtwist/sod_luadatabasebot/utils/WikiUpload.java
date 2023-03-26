package blazingtwist.sod_luadatabasebot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.spongepowered.configurate.BasicConfigurationNode;
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

	public WebContext.WebResponse acquireLoginToken() throws IOException {
		WebContext.WebResponse loginTokenResponse = webContext.sendRequest(
				new WebContext.WebRequest(wikiApiUrl)
						.param("action", "query")
						.param("meta", "tokens")
						.param("format", "json")
						.param("type", "login")
		);
		BasicConfigurationNode loginTokenResponseRootNode = JacksonConfigurationLoader.builder()
				.source(() -> new BufferedReader(new StringReader(loginTokenResponse.responseText())))
				.build()
				.load();
		this.loginToken = loginTokenResponseRootNode.node("query", "tokens", "logintoken").get(String.class);
		return loginTokenResponse;
	}

	public WebContext.WebResponse logIn(String userName, String password) throws IOException {
		return webContext.sendRequest(
				new WebContext.WebRequest(wikiApiUrl)
						.param("format", "json")
						.param("action", "login")
						.body("lgname", userName)
						.body("lgpassword", password)
						.body("lgtoken", loginToken)
		);
	}

	public WebContext.WebResponse acquireCsrfToken() throws IOException {
		WebContext.WebResponse csrfResponse = webContext.sendRequest(
				new WebContext.WebRequest(wikiApiUrl)
						.param("action", "query")
						.param("meta", "tokens")
						.param("format", "json")
						.param("type", "csrf")
		);
		BasicConfigurationNode csrfResponseRootNode = JacksonConfigurationLoader.builder()
				.source(() -> new BufferedReader(new StringReader(csrfResponse.responseText())))
				.build().load();
		this.csrfToken = csrfResponseRootNode.node("query", "tokens", "csrftoken").get(String.class);
		return csrfResponse;
	}

	public WebContext.WebResponse uploadFile(String wikiFilePath, String fileData) throws IOException {
		return webContext.sendRequest(
				new WebContext.WebRequest(wikiApiUrl)
						.param("action", "edit")
						.param("title", wikiFilePath)
						.param("summary", "upload from LuaDatabaseBot")
						.param("format", "json")
						.body("token", csrfToken)
						.body("text", fileData)
		);
	}

}
