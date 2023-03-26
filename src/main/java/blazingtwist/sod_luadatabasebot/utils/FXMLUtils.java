package blazingtwist.sod_luadatabasebot.utils;

import blazingtwist.sod_luadatabasebot.MainApplication;
import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;

public class FXMLUtils {

	public static <Controller> Controller tryLoadFxml(String path) {
		URL fxmlResource = MainApplication.class.getResource(path);
		if (fxmlResource == null) {
			System.err.println("Failed to load " + path);
			return null;
		}

		FXMLLoader loader = new FXMLLoader(fxmlResource);
		try {
			loader.load();
		} catch (IOException e) {
			System.err.println("failed to load node. Exception: " + e);
			e.printStackTrace();
			return null;
		}

		return loader.getController();
	}
}
