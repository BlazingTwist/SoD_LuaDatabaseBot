package blazingtwist.sod_luadatabasebot.ui;

import blazingtwist.sod_luadatabasebot.utils.FXMLUtils;
import blazingtwist.sod_luadatabasebot.utils.MediaWikiResponse;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class UploadWarnings {

	public static final String fxmlPath = "/fxml-nodes/upload-warnings.fxml";

	public static final String cssUploadWarningsModule = "upload-warnings-module";
	public static final String cssUploadWarningsText = "upload-warnings-text";

	public static UploadWarnings load() {
		UploadWarnings controller = FXMLUtils.tryLoadFxml(fxmlPath);
		assert controller != null;
		return controller;
	}

	@FXML
	public GridPane rootPane;
	@FXML
	public GridPane warningsPane;

	private int currentRowIndex = 0;

	public Parent getRootNode() {
		return rootPane;
	}

	public void clear() {
		warningsPane.getChildren().clear();
		currentRowIndex = 0;
	}

	public void loadWarnings(MediaWikiResponse.Warnings warnings) {
		ObservableList<Node> children = warningsPane.getChildren();

		for (String module : warnings.getModulesWithWarnings()) {
			Label moduleLabel = new Label();
			moduleLabel.setText(module);
			moduleLabel.getStyleClass().add(cssUploadWarningsModule);
			GridPane.setRowIndex(moduleLabel, currentRowIndex);
			children.add(moduleLabel);

			for (String warning : warnings.getModuleWarnings(module)) {
				Label warningLabel = new Label();
				warningLabel.setText(warning);
				warningLabel.getStyleClass().add(cssUploadWarningsText);
				GridPane.setRowIndex(warningLabel, currentRowIndex);
				GridPane.setColumnIndex(warningLabel, 1);
				children.add(warningLabel);
				currentRowIndex++;
			}
		}
	}

}
