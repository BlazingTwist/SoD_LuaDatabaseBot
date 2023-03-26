package blazingtwist.sod_luadatabasebot.ui;

import blazingtwist.sod_luadatabasebot.MainApplication;
import blazingtwist.sod_luadatabasebot.utils.FXMLUtils;
import blazingtwist.sod_luadatabasebot.utils.StringUtils;
import blazingtwist.sod_luadatabasebot.yamlmapper.MapperManager;
import blazingtwist.sod_luadatabasebot.yamlmapper.YamlFileKey;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class SourceFileEntry {

	public static final String fxmlPath = "/fxml-nodes/source-file-entry.fxml";

	public static SourceFileEntry load() {
		SourceFileEntry controller = FXMLUtils.tryLoadFxml(fxmlPath);
		assert controller != null;
		return controller;
	}

	@FXML
	public VBox container;
	@FXML
	public Label fileName;
	@FXML
	public HBox detailsHBox;
	@FXML
	public LedButton selectButton;
	@FXML
	public Label selectedFileLabel;
	@FXML
	public Tooltip selectedFileTooltip;
	@FXML
	public Rectangle dividerLine;

	private YamlFileKey yamlFileKey;
	private final List<Animation> animations = new ArrayList<>();

	@FXML
	public void onFileDragOver(DragEvent dragEvent) {
		Object source = dragEvent.getGestureSource();
		if (source != fileName && source != detailsHBox
				&& dragEvent.getDragboard().hasFiles()) {
			dragEvent.acceptTransferModes(TransferMode.LINK);
		}
		dragEvent.consume();
	}

	@FXML
	public void onFileDropped(DragEvent dragEvent) {
		Dragboard dragboard = dragEvent.getDragboard();
		boolean success = false;
		if (dragboard.hasFiles()) {
			checkFile(dragboard.getFiles().get(0));
			success = true;
		}
		dragEvent.setDropCompleted(success);
		dragEvent.consume();
	}

	public void init(YamlFileKey fileKey) {
		this.yamlFileKey = fileKey;
		fileName.setText(fileKey.getDisplayName());
		selectButton.setButtonText("Select File");
		selectButton.setLed(false);
		selectButton.setOnclickCallback(this::openFileDialog);

		String filePath = MainApplication.getMainConfig().getYamlFilePath(fileKey);
		if (filePath != null) {
			checkFile(new File(filePath));
		}
	}

	public void openFileDialog(MouseEvent event) {
		FileChooser fileChooser = new FileChooser();
		String previousPath = MainApplication.getMainConfig().getSearchFilePath(yamlFileKey);
		if (previousPath != null) {
			File previousFile = new File(previousPath);
			if (previousFile.isDirectory()) {
				fileChooser.setInitialDirectory(previousFile);
			} else {
				fileChooser.setInitialDirectory(previousFile.getParentFile());
			}
		}
		File selectedFile = fileChooser.showOpenDialog(null);
		checkFile(selectedFile);
	}

	public void checkFile(File file) {
		if (file == null) {
			selectButton.setLed(false);
			selectedFileLabel.setText("Please select a file");
			selectedFileTooltip.setText("Despite receiving a File Event, the program did not receive any File.");
			MainApplication.getMainController().updateFilesSelected();
			return;
		}

		MainApplication.getMainConfig().setYamlFilePath(yamlFileKey, file.getAbsolutePath());

		List<Exception> exceptions = MapperManager.getInstance().loadFile(yamlFileKey, file);
		if (exceptions == null || exceptions.isEmpty()) {
			selectButton.setLed(true);
			selectedFileLabel.setText(file.getName());
			selectedFileTooltip.setText(String.join("\n",
					StringUtils.SplitWithMaxLength(file.getAbsolutePath(), 64, StringUtils.SplitCharRule.Prepend,
							'\\', '/', File.separatorChar, File.pathSeparatorChar)
			));
		} else {
			selectButton.setLed(false);
			selectedFileLabel.setText("Parsing File Failed!");
			String errorString = exceptions.stream()
					.map(e -> e.toString().lines()
							.map(line -> String.join("\n  ", StringUtils.SplitWithMaxLength(
									line, 64, StringUtils.SplitCharRule.Remove, ' ')
							)).collect(Collectors.joining("\n"))
					).collect(Collectors.joining("\n---\n"));
			selectedFileTooltip.setText(errorString);
		}
		MainApplication.getMainController().updateFilesSelected();
	}

	public void showDivider(Duration animDuration) {
		if (animDuration == null) {
			dividerLine.setOpacity(1);
		} else if (dividerLine.getOpacity() <= 0.01) {
			FadeTransition fadeTransition = new FadeTransition(animDuration, dividerLine);
			fadeTransition.setFromValue(0);
			fadeTransition.setToValue(1);
			fadeTransition.playFromStart();
		}
	}

	public void hideDivider(Duration animDuration) {
		if (animDuration == null) {
			dividerLine.setOpacity(0);
		} else if (dividerLine.getOpacity() >= 0.99) {
			FadeTransition fadeTransition = new FadeTransition(animDuration, dividerLine);
			fadeTransition.setFromValue(1);
			fadeTransition.setToValue(0);
			fadeTransition.playFromStart();
		}
	}

	public void cancelCurrentAnimationEvents() {
		for (int i = animations.size() - 1; i >= 0; i--) {
			Animation anim = animations.get(i);
			anim.setOnFinished(null);
			if (anim.getStatus() == Animation.Status.STOPPED) {
				animations.remove(i);
			}
		}
	}

	public void registerAnimations(Animation... animations) {
		this.animations.addAll(Arrays.asList(animations));
	}
}
