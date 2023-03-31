package blazingtwist.sod_luadatabasebot.ui;

import blazingtwist.sod_luadatabasebot.utils.FXMLUtils;
import blazingtwist.sod_luadatabasebot.utils.MediaWikiResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class UploadErrors {

	public static final String fxmlPath = "/fxml-nodes/upload-errors.fxml";

	public static final String cssUploadErrorsTitle = "upload-errors-title";
	public static final String cssUploadErrorsText = "upload-errors-text";

	public static UploadErrors load() {
		UploadErrors controller = FXMLUtils.tryLoadFxml(fxmlPath);
		assert controller != null;
		return controller;
	}

	@FXML
	public GridPane errorPane;
	@FXML
	public Label codeLabel;
	@FXML
	public Label infoLabel;

	private final List<Label> extraLabels = new ArrayList<>();
	private int currentRowIndex = 4;

	public Parent getRootNode() {
		return errorPane;
	}

	public void clear() {
		errorPane.getChildren().removeAll(extraLabels);
		extraLabels.clear();
		currentRowIndex = 4;
	}

	public void loadError(MediaWikiResponse.Error error) {
		ObservableList<Node> children = errorPane.getChildren();

		codeLabel.setText(error.code());
		infoLabel.setText(error.info());

		for (Map.Entry<String, String> extraEntry : error.additionalInfo().entrySet()) {
			Label keyLabel = new Label();
			keyLabel.setText(extraEntry.getKey());
			keyLabel.getStyleClass().add(cssUploadErrorsTitle);
			GridPane.setRowIndex(keyLabel, currentRowIndex);
			GridPane.setColumnIndex(keyLabel, 0);
			children.add(keyLabel);
			extraLabels.add(keyLabel);

			Label valueLabel = new Label();
			valueLabel.setText(extraEntry.getValue());
			valueLabel.getStyleClass().add(cssUploadErrorsText);
			GridPane.setRowIndex(valueLabel, currentRowIndex);
			GridPane.setColumnIndex(valueLabel, 1);
			children.add(valueLabel);
			extraLabels.add(valueLabel);

			currentRowIndex++;
		}
	}

}
