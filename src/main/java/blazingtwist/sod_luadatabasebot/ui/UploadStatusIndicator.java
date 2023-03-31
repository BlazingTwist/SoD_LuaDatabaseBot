package blazingtwist.sod_luadatabasebot.ui;

import blazingtwist.sod_luadatabasebot.MainApplication;
import blazingtwist.sod_luadatabasebot.utils.MediaWikiResponse;
import java.util.Collection;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import me.blazingtwist.loadingspinner.LoadingSpinner;
import me.blazingtwist.loadingspinner.LoadingSpinnerAnimatedIcon;
import me.blazingtwist.loadingspinner.LoadingSpinnerPaintAnimationInfo;

public class UploadStatusIndicator {

	public static final String cssUploadStepLabel = "upload-step-label";
	public static final String cssFlatButton = "flat-button";
	public static final String cssButtonSmall = "button-small";

	public static UploadStatusIndicator load(String labelText, int row) {
		LoadingSpinner spinner = new LoadingSpinner();
		spinner.setIndeterminate(false);
		spinner.setStartAngle(0);
		spinner.setThickness(3);
		spinner.setProgressText(false);
		spinner.setRadius(8);
		spinner.setProgress(0);
		spinner.getPaintAnimationSequence().add(new LoadingSpinnerPaintAnimationInfo(Paint.valueOf("#4285f4"), Duration.ONE, Duration.ONE, Duration.ONE));
		spinner.getIconSequence().addAll(
				LoadingSpinnerAnimatedIcon.greenCheckMark,
				LoadingSpinnerAnimatedIcon.yellowExclamationMark,
				LoadingSpinnerAnimatedIcon.redCross
		);
		GridPane.setColumnIndex(spinner, 0);
		GridPane.setRowIndex(spinner, row);

		Label label = new Label();
		label.setText(labelText);
		label.getStyleClass().add(cssUploadStepLabel);
		GridPane.setColumnIndex(label, 1);
		GridPane.setRowIndex(label, row);

		Button responseButton = new Button();
		responseButton.setText("Response");
		responseButton.getStyleClass().addAll(cssFlatButton, cssButtonSmall);
		responseButton.setFocusTraversable(false);
		responseButton.setDisable(true);
		responseButton.setVisible(false);
		GridPane.setColumnIndex(responseButton, 2);
		GridPane.setRowIndex(responseButton, row);

		return new UploadStatusIndicator(spinner, label, responseButton);
	}

	public LoadingSpinner spinner;
	public Label label;
	public Button responseButton;
	private MediaWikiResponse<?> currentResponse = null;

	public UploadStatusIndicator(LoadingSpinner spinner, Label label, Button responseButton) {
		this.spinner = spinner;
		this.label = label;
		this.responseButton = responseButton;

		responseButton.setOnMouseClicked(this::onResponseClicked);
	}

	public Collection<Node> getNodes() {
		return List.of(spinner, label, responseButton);
	}

	public void setSpinnerLoading() {
		spinner.setIndeterminate(true);
		spinner.setProgress(0.75);
	}

	public void handleResponse(MediaWikiResponse<?> response) {
		currentResponse = response;
		String iconKey = switch (response.status()) {
			case Success -> "greenCheckMark";
			case Warning -> "yellowExclamationMark";
			case Error -> "redCross";
		};
		spinner.displayIconByKey(iconKey);

		if (response.status() != MediaWikiResponse.Status.Success || response.rawResponse().isPresent()) {
			responseButton.setVisible(true);
			responseButton.setDisable(false);
		}
	}

	protected void onResponseClicked(MouseEvent event) {
		MainApplication.getMainController().openUploadResponseInfo(currentResponse);
	}

}
