package blazingtwist.sod_luadatabasebot.config;

import blazingtwist.sod_luadatabasebot.yamlmapper.YamlFileKey;
import java.util.HashMap;
import java.util.Map;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class MainConfig {

	public static final MainConfig DEFAULT = new MainConfig();

	@Setting("window-width")
	protected int windowWidth = 1280;

	@Setting("window-height")
	protected int windowHeight = 720;

	@Setting("wikiPaths")
	protected WikiPathsConfig wikiPaths = new WikiPathsConfig();

	@Setting("mostRecentFilePath")
	protected String mostRecentFilePath = null;

	@Setting("previousYamlFilePaths")
	protected Map<YamlFileKey, String> previousYamlFilePaths = new HashMap<>();

	@Setting("wikiURL")
	protected String wikiURL = "https://dreamworks-school-of-dragons.fandom.com/";

	@Setting("bot-username")
	protected String botUsername = "ExampleUser@LuaDatabaseBot";

	@Setting("bot-password")
	protected String botPassword = "qyvhk9yyixlqvc4qu39kte2u3l6uh9e3";

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	public WikiPathsConfig getWikiPaths() {
		return wikiPaths;
	}

	public String getYamlFilePath(YamlFileKey targetFile) {
		return previousYamlFilePaths.getOrDefault(targetFile, null);
	}

	public String getSearchFilePath(YamlFileKey targetFile) {
		return previousYamlFilePaths.getOrDefault(targetFile, mostRecentFilePath);
	}

	public void setYamlFilePath(YamlFileKey targetFile, String absolutePath) {
		previousYamlFilePaths.put(targetFile, absolutePath);
		mostRecentFilePath = absolutePath;
	}

	public String getWikiURL() {
		return wikiURL;
	}

	public void setWikiURL(String wikiURL) {
		if (!wikiURL.endsWith("/")) {
			wikiURL = wikiURL + "/";
		}
		this.wikiURL = wikiURL;
	}

	public String getBotUsername() {
		return botUsername;
	}

	public void setBotUsername(String botUsername) {
		this.botUsername = botUsername;
	}

	public String getBotPassword() {
		return botPassword;
	}

	public void setBotPassword(String botPassword) {
		this.botPassword = botPassword;
	}
}
