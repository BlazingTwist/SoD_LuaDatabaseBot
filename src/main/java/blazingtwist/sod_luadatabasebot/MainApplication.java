package blazingtwist.sod_luadatabasebot;

import blazingtwist.sod_luadatabasebot.config.MainConfig;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class MainApplication extends Application {


	private static YamlConfigurationLoader mainConfigLoader;
	private static MainConfig mainConfig;

	private static Parent windowRootNode;
	private static MainController mainController;

	public static MainConfig getMainConfig() {
		return mainConfig;
	}

	public static MainController getMainController() {
		return mainController;
	}

	public static void saveMainConfigToFile() throws ConfigurateException {
		BasicConfigurationNode saveNode = BasicConfigurationNode.root();
		ObjectMapper.factory().get(MainConfig.class).save(mainConfig, saveNode);
		mainConfigLoader.save(saveNode);
	}

	@Override
	public void start(Stage stage) {
		Scene scene = new Scene(windowRootNode, mainConfig.getWindowWidth(), mainConfig.getWindowHeight());
		scene.widthProperty().addListener((observable, oldValue, newValue) -> mainConfig.setWindowWidth(newValue.intValue()));
		scene.heightProperty().addListener((observable, oldValue, newValue) -> mainConfig.setWindowHeight(newValue.intValue()));
		stage.setTitle("Lua Database Bot");

		URL styleSheet = MainApplication.class.getResource("/css/main.css");
		assert styleSheet != null;
		scene.getStylesheets().addAll(styleSheet.toExternalForm());

		stage.setScene(scene);
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		saveMainConfigToFile();
	}

	public static void main(String[] args) throws URISyntaxException, IOException {
		String mainConfigPath = new File(MainApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath()
				+ File.separator + "config.yaml";
		mainConfigLoader = YamlConfigurationLoader.builder()
				.nodeStyle(NodeStyle.BLOCK)
				.file(new File(mainConfigPath))
				.build();
		mainConfig = mainConfigLoader.load().get(MainConfig.class);

		FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
		windowRootNode = fxmlLoader.load();
		mainController = fxmlLoader.getController();
		mainController.initialize();

		launch();
	}
}
