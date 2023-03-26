package blazingtwist.sod_luadatabasebot.ui;

import blazingtwist.sod_luadatabasebot.utils.FXMLUtils;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public class LedButton extends Group {

	public static final String fxmlPath = "/fxml-nodes/flat-led-button.fxml";
	public static final String cssLedOn = "button-led-on";
	public static final String cssLedOff = "button-led-off";

	public static LedButton load() {
		LedButton controller = FXMLUtils.tryLoadFxml(fxmlPath);
		assert controller != null;
		controller.getChildren().add(controller.container);
		return controller;
	}

	@FXML
	protected StackPane container;

	@FXML
	public Button button;

	@FXML
	public Circle led;

	protected Consumer<MouseEvent> onclickCallback = null;

	public Parent getContainer() {
		return container;
	}

	public boolean getLedIsOn() {
		return led.getStyleClass().contains(cssLedOn);
	}

	public void setLed(boolean enabled) {
		boolean ledIsOn = getLedIsOn();
		if (enabled != ledIsOn) {
			if (ledIsOn) {
				led.getStyleClass().remove(cssLedOn);
				led.getStyleClass().add(cssLedOff);
			} else {
				led.getStyleClass().remove(cssLedOff);
				led.getStyleClass().add(cssLedOn);
			}
		}
	}

	public void setButtonText(String text) {
		button.setText(text);
	}

	public void setOnclickCallback(Consumer<MouseEvent> onclickCallback) {
		this.onclickCallback = onclickCallback;
	}

	@FXML
	protected void onButtonClicked(MouseEvent event) {
		if (onclickCallback != null) {
			onclickCallback.accept(event);
		}
	}
}
