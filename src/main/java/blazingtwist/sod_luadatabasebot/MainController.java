package blazingtwist.sod_luadatabasebot;

import blazingtwist.sod_luadatabasebot.utils.WebContext;
import blazingtwist.sod_luadatabasebot.utils.WikiUpload;
import java.io.IOException;
import me.blazingtwist.loadingspinner.IconKey;
import me.blazingtwist.loadingspinner.LoadingSpinner;
import blazingtwist.sod_luadatabasebot.config.MainConfig;
import blazingtwist.sod_luadatabasebot.ui.SourceFileEntry;
import blazingtwist.sod_luadatabasebot.ui.YamlMapperContainer;
import blazingtwist.sod_luadatabasebot.utils.FieldAccessor;
import blazingtwist.sod_luadatabasebot.utils.StringUtils;
import blazingtwist.sod_luadatabasebot.yamlmapper.MapperManager;
import blazingtwist.sod_luadatabasebot.yamlmapper.YamlFileKey;
import blazingtwist.sod_luadatabasebot.yamlmapper.YamlMapperCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class MainController {

	@FXML
	public StackPane rootNode;

	@FXML
	public GridPane yamlMapperGrid;

	@FXML
	public ScrollPane sourceFileListContainer;
	@FXML
	public AnchorPane sourceFileAnimPane;
	@FXML
	public VBox sourceFileVBox;
	@FXML
	public VBox sourceFileHeaderVBox;

	@FXML
	public ScrollPane uploadSettingsContainer;
	@FXML
	public Label wikiUrlLabel;
	@FXML
	public Tooltip wikiUrlTooltip;
	@FXML
	public Label botNameLabel;
	@FXML
	public Tooltip botNameTooltip;
	@FXML
	public Label botPasswordLabel;
	@FXML
	public Tooltip botPasswordTooltip;

	@FXML
	public StackPane uploadButtonContainer;
	@FXML
	public Button uploadButton;

	@FXML
	public Rectangle darkenOverlay;

	@FXML
	public VBox singleStringEditPanel;
	@FXML
	public Label singleStringEditHeader;
	@FXML
	public Label singleStringEditCurrentLabel;
	@FXML
	public TextField singleStringEditCurrentTextField;
	@FXML
	public Label singleStringEditNewLabel;
	@FXML
	public TextField singleStringEditNewTextField;

	@FXML
	public LoadingSpinner testLoadingSpinner;
	@FXML
	public LoadingSpinner testLoadingSpinner2;

	private boolean fileListVisible = false;
	private final List<YamlFileKey> displayedSourceFilesInOrder = new ArrayList<>();
	private final HashMap<YamlFileKey, SourceFileEntry> sourceFileEntryInstances = new HashMap<>();

	private boolean uploadConfigVisible = false;

	private FieldAccessor<?, String> currentSingleStringEditTarget = null;

	public void initialize() {
		registerYamlMappers();
		initUploadSettings();
	}

	private void registerYamlMappers() {
		ArrayList<YamlMapperCategory> categories = new ArrayList<>(Arrays.asList(YamlMapperCategory.values()));
		categories.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getDisplayName(), b.getDisplayName()));
		int rowIndex = 0;
		ObservableList<RowConstraints> rowConstraints = yamlMapperGrid.getRowConstraints();
		for (YamlMapperCategory category : categories) {
			if (MapperManager.getInstance().getMappers(category).size() <= 0) {
				continue;
			}
			if (rowIndex >= rowConstraints.size()) {
				// repeat last 2 rowConstraints (whitespace | content)
				for (int i = 0; i < 2; i++) {
					rowConstraints.add(rowConstraints.get(rowConstraints.size() - 2));
				}
			}
			YamlMapperContainer container = YamlMapperContainer.load(category);
			Parent mapperRootNode = container.getRootNode();
			GridPane.setRowIndex(mapperRootNode, rowIndex);
			rowIndex += 2;
			yamlMapperGrid.getChildren().add(mapperRootNode);
		}
	}

	private void initUploadSettings() {
		MainConfig mainConfig = MainApplication.getMainConfig();
		setWikiUrl(mainConfig.getWikiURL());
		setBotUserName(mainConfig.getBotUsername());
		setBotPassword(mainConfig.getBotPassword());
	}

	private void setWikiUrl(String url) {
		wikiUrlLabel.setText(url);
		wikiUrlTooltip.setText(url);
		checkUploadReady(0);
	}

	private void setBotUserName(String userName) {
		botNameLabel.setText(userName);
		botNameTooltip.setText(userName);
		checkUploadReady(0);
	}

	private void setBotPassword(String password) {
		botPasswordLabel.setText(password);
		botPasswordTooltip.setText(password);
		checkUploadReady(0);
	}

	private SourceFileEntry getSourceFileInstance(YamlFileKey fileKey) {
		return sourceFileEntryInstances.computeIfAbsent(fileKey, key -> {
			SourceFileEntry loadedEntry = SourceFileEntry.load();
			loadedEntry.init(key);
			return loadedEntry;
		});
	}

	public void updateDisplayedSourceFiles() {
		Set<YamlFileKey> requiredSourceFiles = MapperManager.getInstance().getRequiredSourceFiles();

		Set<YamlFileKey> filesToAdd = requiredSourceFiles.stream()
				.filter(file -> !displayedSourceFilesInOrder.contains(file))
				.collect(Collectors.toSet());
		Set<YamlFileKey> filesToRemove = displayedSourceFilesInOrder.stream()
				.filter(file -> !requiredSourceFiles.contains(file))
				.collect(Collectors.toSet());

		if (filesToAdd.isEmpty() && filesToRemove.isEmpty()) {
			return;
		}

		if (!fileListVisible) {
			showPopulatedFileList(requiredSourceFiles);
			return;
		}

		animateRemoveFiles(filesToRemove);
		animateAddFiles(filesToAdd);

		int remainingSourceFileCount = displayedSourceFilesInOrder.size();
		if (remainingSourceFileCount > 0) {
			Duration animDuration = Duration.millis(150);
			for (int i = 0; i < (remainingSourceFileCount - 1); i++) {
				getSourceFileInstance(displayedSourceFilesInOrder.get(i)).showDivider(animDuration);
			}
			getSourceFileInstance(displayedSourceFilesInOrder.get(remainingSourceFileCount - 1)).hideDivider(animDuration);
			updateFilesSelected();
		}
	}

	private void animateAddFiles(Set<YamlFileKey> filesToAdd) {
		if (filesToAdd.isEmpty()) {
			return;
		}

		Duration animDuration = Duration.millis(150);
		if (!displayedSourceFilesInOrder.isEmpty()) {
			YamlFileKey lastDisplayedFile = displayedSourceFilesInOrder.get(displayedSourceFilesInOrder.size() - 1);
			getSourceFileInstance(lastDisplayedFile).showDivider(animDuration);
		}

		double currentContainerHeight = sourceFileHeaderVBox.getHeight();
		for (YamlFileKey displayedFileKey : displayedSourceFilesInOrder) {
			currentContainerHeight += getSourceFileInstance(displayedFileKey).container.getHeight();
		}

		VBox nodeWrapper = new VBox();
		nodeWrapper.getStyleClass().add("mapper-anim-rounded-bottom");
		nodeWrapper.setScaleY(0);
		nodeWrapper.setScaleX(0.8);

		int remainingNodes = filesToAdd.size();
		for (YamlFileKey fileKey : filesToAdd) {
			SourceFileEntry entry = getSourceFileInstance(fileKey);

			remainingNodes--;
			if (remainingNodes > 0) {
				entry.showDivider(null);
			} else {
				entry.hideDivider(null);
			}

			nodeWrapper.getChildren().add(entry.container);
			displayedSourceFilesInOrder.add(fileKey);
		}

		sourceFileAnimPane.getChildren().add(0, nodeWrapper);

		rootNode.applyCss();
		rootNode.layout();

		double startY = currentContainerHeight - (nodeWrapper.getHeight() / 2);
		nodeWrapper.setTranslateY(startY);

		TranslateTransition translateTransition = new TranslateTransition(animDuration, nodeWrapper);
		translateTransition.setFromY(startY);
		translateTransition.setToY(currentContainerHeight);

		ScaleTransition scaleTransition = new ScaleTransition(animDuration, nodeWrapper);
		scaleTransition.setFromX(0.8);
		scaleTransition.setToX(1.0);
		scaleTransition.setFromY(0);
		scaleTransition.setToY(1);

		for (YamlFileKey fileKey : filesToAdd) {
			SourceFileEntry entry = getSourceFileInstance(fileKey);
			entry.cancelCurrentAnimationEvents();
			entry.registerAnimations(translateTransition, scaleTransition);
		}
		translateTransition.playFromStart();
		scaleTransition.playFromStart();

		scaleTransition.setOnFinished(event -> {
			sourceFileAnimPane.getChildren().remove(nodeWrapper);
			for (YamlFileKey addKey : filesToAdd) {
				int insertIndex = Math.min(displayedSourceFilesInOrder.indexOf(addKey), sourceFileVBox.getChildren().size() - 1);
				sourceFileVBox.getChildren().add(insertIndex + 1, getSourceFileInstance(addKey).container);
			}
		});
	}

	private void animateRemoveFiles(Set<YamlFileKey> filesToRemove) {
		if (filesToRemove.isEmpty()) {
			return;
		}

		Duration animDuration = Duration.millis(200);
		List<YamlFileKey> sourceFileOrderCopy = List.copyOf(displayedSourceFilesInOrder);
		double height = sourceFileHeaderVBox.getHeight();
		double removedHeight = 0;
		int animChildIndex = 0;
		for (int i = 0; i < sourceFileOrderCopy.size(); i++) {
			final YamlFileKey fileKey = sourceFileOrderCopy.get(i);
			SourceFileEntry sourceFileInstance = getSourceFileInstance(fileKey);
			double childHeight = sourceFileInstance.container.getHeight();
			final boolean shouldRemove = filesToRemove.contains(fileKey);
			final boolean shouldMoveUp = removedHeight > 0;

			if (shouldRemove) {
				displayedSourceFilesInOrder.remove(fileKey);
			}

			if (shouldRemove || shouldMoveUp) {
				VBox nodeWrapper = new VBox();
				if (i == (sourceFileOrderCopy.size() - 1)) {
					nodeWrapper.getStyleClass().add("mapper-anim-rounded-bottom");
				} else {
					nodeWrapper.getStyleClass().add("mapper-anim-square");
				}
				nodeWrapper.getChildren().add(sourceFileInstance.container);
				nodeWrapper.setTranslateY(height);
				sourceFileAnimPane.getChildren().add(animChildIndex++, nodeWrapper);

				// manage animation
				TranslateTransition translateTransition = new TranslateTransition(animDuration, nodeWrapper);
				if (shouldRemove) {
					translateTransition.setFromX(0);
					translateTransition.setToX(300);

					ScaleTransition scaleTransition = new ScaleTransition(animDuration, nodeWrapper);
					scaleTransition.setFromX(1);
					scaleTransition.setToX(0.8);
					scaleTransition.setFromY(1);
					scaleTransition.setToY(0);
					scaleTransition.setOnFinished(event -> sourceFileAnimPane.getChildren().remove(nodeWrapper));

					sourceFileInstance.cancelCurrentAnimationEvents();
					sourceFileInstance.registerAnimations(scaleTransition, translateTransition);
					scaleTransition.playFromStart();
				} else {
					// shift this node 'removedHeight' many pixels upwards | start at 'height'
					translateTransition.setFromY(height);
					translateTransition.setToY(height - removedHeight);
					translateTransition.setOnFinished(event -> {
						sourceFileAnimPane.getChildren().remove(nodeWrapper);
						if (displayedSourceFilesInOrder.contains(fileKey)) {
							int insertIndex = Math.min(displayedSourceFilesInOrder.indexOf(fileKey), sourceFileVBox.getChildren().size() - 1);
							sourceFileVBox.getChildren().add(insertIndex + 1, sourceFileInstance.container);
						}
					});
					sourceFileInstance.cancelCurrentAnimationEvents();
					sourceFileInstance.registerAnimations(translateTransition);
				}
				translateTransition.playFromStart();

				if (shouldRemove) {
					removedHeight += childHeight;
				}
			}
			height += childHeight;
		}
	}

	private void showPopulatedFileList(Set<YamlFileKey> requiredSourceFiles) {
		fileListVisible = true;
		for (YamlFileKey fileKey : requiredSourceFiles) {
			SourceFileEntry entry = getSourceFileInstance(fileKey);
			sourceFileVBox.getChildren().add(entry.container);
			displayedSourceFilesInOrder.add(fileKey);
		}
		windowSlideIn(sourceFileListContainer);
	}

	public void updateFilesSelected() {
		boolean allFilesReady = displayedSourceFilesInOrder.stream().allMatch(fileKey -> getSourceFileInstance(fileKey).selectButton.getLedIsOn());
		if (!uploadConfigVisible && allFilesReady) {
			uploadConfigVisible = true;
			windowSlideIn(uploadSettingsContainer);
			checkUploadReady(200);
			return;
		}
		checkUploadReady(0);
	}

	public void checkUploadReady(int delayMillis) {
		boolean allFilesReady = displayedSourceFilesInOrder.stream().allMatch(fileKey -> getSourceFileInstance(fileKey).selectButton.getLedIsOn());
		MainConfig mainConfig = MainApplication.getMainConfig();
		boolean urlProvided = !StringUtils.isNullOrWhitespace(mainConfig.getWikiURL());
		boolean botNameProvided = !StringUtils.isNullOrWhitespace(mainConfig.getBotUsername());
		boolean botPasswordProvided = !StringUtils.isNullOrWhitespace(mainConfig.getBotPassword());

		boolean shouldEnable = allFilesReady && urlProvided && botNameProvided && botPasswordProvided;
		if (shouldEnable == uploadButton.isDisable()) {
			if (shouldEnable) {
				enableUploadButtonWithAnimation(delayMillis);
			} else {
				disableUploadButtonWithAnimation(delayMillis);
			}
		}
	}

	private void enableUploadButtonWithAnimation(int delayMillis) {
		uploadButton.setDisable(false);
		FadeTransition fadeTransition = new FadeTransition(Duration.millis(100), uploadButton);
		fadeTransition.setFromValue(0);
		fadeTransition.setToValue(1);
		fadeTransition.setDelay(Duration.millis(delayMillis));
		fadeTransition.playFromStart();

		TranslateTransition jumpAnim1 = new TranslateTransition(Duration.millis(50), uploadButtonContainer);
		jumpAnim1.setFromY(0);
		jumpAnim1.setToY(-10);
		jumpAnim1.setDelay(Duration.millis(delayMillis + 100));

		TranslateTransition jumpAnim2 = new TranslateTransition(Duration.millis(50), uploadButtonContainer);
		jumpAnim2.setFromY(-10);
		jumpAnim2.setToY(-15);
		jumpAnim2.setDelay(Duration.millis(delayMillis + 150));

		TranslateTransition jumpAnim3 = new TranslateTransition(Duration.millis(50), uploadButtonContainer);
		jumpAnim3.setFromY(-15);
		jumpAnim3.setToY(-10);
		jumpAnim3.setDelay(Duration.millis(delayMillis + 200));

		TranslateTransition jumpAnim4 = new TranslateTransition(Duration.millis(50), uploadButtonContainer);
		jumpAnim4.setFromY(-10);
		jumpAnim4.setToY(0);
		jumpAnim4.setDelay(Duration.millis(delayMillis + 250));

		jumpAnim1.playFromStart();
		jumpAnim2.playFromStart();
		jumpAnim3.playFromStart();
		jumpAnim4.playFromStart();
	}

	private void disableUploadButtonWithAnimation(int delayMillis) {
		uploadButton.setDisable(true);
		FadeTransition fadeTransition = new FadeTransition(Duration.millis(100), uploadButton);
		fadeTransition.setFromValue(1);
		fadeTransition.setToValue(0);
		fadeTransition.setDelay(Duration.millis(delayMillis));
		fadeTransition.playFromStart();
	}

	private void windowSlideIn(Node node) {
		Duration animDuration = Duration.millis(100);
		TranslateTransition translateTransition = new TranslateTransition(animDuration, node);
		translateTransition.setFromX(-150);
		translateTransition.setToX(0);

		FadeTransition fadeTransition = new FadeTransition(animDuration, node);
		fadeTransition.setFromValue(0);
		fadeTransition.setToValue(1);

		ScaleTransition scaleTransition = new ScaleTransition(animDuration, node);
		scaleTransition.setFromX(0.6);
		scaleTransition.setToX(1.0);

		translateTransition.playFromStart();
		fadeTransition.playFromStart();
		scaleTransition.playFromStart();
		node.setDisable(false);

	}

	@FXML
	protected void onclick_panelBackground() {
		closeActivePanel();
	}

	@FXML
	protected void onclick_SingleStringEditSave() {
		currentSingleStringEditTarget.setValue(singleStringEditNewTextField.getText());
		currentSingleStringEditTarget = null;
		closeActivePanel();
	}

	@FXML
	protected void onclick_SingleStringEditReset() {
		singleStringEditNewTextField.setText(currentSingleStringEditTarget.getDefaultValue());
	}

	@FXML
	protected void onclick_SingleStringEditCancel() {
		closeActivePanel();
	}

	private void closeActivePanel() {
		darkenOverlay.setDisable(true);
		singleStringEditPanel.setDisable(true);

		Duration transitionDuration = Duration.millis(100);
		if (darkenOverlay.getOpacity() >= 0.49) {
			FadeTransition transition = new FadeTransition(transitionDuration, darkenOverlay);
			transition.setFromValue(0.5);
			transition.setToValue(0.0);
			transition.playFromStart();
		}

		if (singleStringEditPanel.getOpacity() > 0.99) {
			FadeTransition fadeTransition = new FadeTransition(transitionDuration, singleStringEditPanel);
			fadeTransition.setFromValue(1.0);
			fadeTransition.setToValue(0.0);
			fadeTransition.playFromStart();
			TranslateTransition translateTransition = new TranslateTransition(transitionDuration, singleStringEditPanel);
			translateTransition.setFromY(0);
			translateTransition.setToY(-50);
			translateTransition.playFromStart();
		}
	}

	public void openSingleStringEditPanel(String headerText, String currentValueLabelText, String newValueLabelText,
										  FieldAccessor<?, String> targetField) {
		currentSingleStringEditTarget = targetField;
		darkenOverlay.setDisable(false);
		singleStringEditPanel.setDisable(false);

		String currentValue = targetField.getCurrentValue();
		singleStringEditHeader.setText(headerText);
		singleStringEditCurrentLabel.setText(currentValueLabelText);
		singleStringEditNewLabel.setText(newValueLabelText);
		singleStringEditCurrentTextField.setText(currentValue);
		singleStringEditNewTextField.setText(currentValue);

		Duration transitionDuration = Duration.millis(100);
		if (darkenOverlay.getOpacity() <= 0.01) {
			FadeTransition transition = new FadeTransition(transitionDuration, darkenOverlay);
			transition.setFromValue(0.0);
			transition.setToValue(0.5);
			transition.playFromStart();
		}

		if (singleStringEditPanel.getOpacity() <= 0.01) {
			FadeTransition fadeTransition = new FadeTransition(transitionDuration, singleStringEditPanel);
			fadeTransition.setFromValue(0.0);
			fadeTransition.setToValue(1.0);
			fadeTransition.playFromStart();
			TranslateTransition translateTransition = new TranslateTransition(transitionDuration, singleStringEditPanel);
			translateTransition.setFromY(-50);
			translateTransition.setToY(0);
			translateTransition.playFromStart();
		}
	}

	@FXML
	public void onEditWikiUrl() {
		openSingleStringEditPanel("Edit Wiki URL", "Current URL", "New URL",
				new FieldAccessor<>(MainApplication.getMainConfig(), MainConfig.DEFAULT, MainConfig::getWikiURL, MainConfig::setWikiURL)
						.addSetterListener(rawInput -> setWikiUrl(MainApplication.getMainConfig().getWikiURL())));
	}

	@FXML
	public void onEditBotName() {
		openSingleStringEditPanel("Edit Bot Name", "Current Name", "New Name",
				new FieldAccessor<>(MainApplication.getMainConfig(), MainConfig.DEFAULT, MainConfig::getBotUsername, MainConfig::setBotUsername)
						.addSetterListener(rawInput -> setBotUserName(MainApplication.getMainConfig().getBotUsername())));
	}

	@FXML
	public void onEditBotPassword() {
		openSingleStringEditPanel("Edit Bot Password", "Current Password", "New Password",
				new FieldAccessor<>(MainApplication.getMainConfig(), MainConfig.DEFAULT, MainConfig::getBotPassword, MainConfig::setBotPassword)
						.addSetterListener(rawInput -> setBotPassword(MainApplication.getMainConfig().getBotPassword())));
	}

	@FXML
	public void onUploadPressed() {
		// TODO
		// open panel containing each file to upload:
		//   | <file name> | <loading-spinner> |
		//   | <file name> | <loading-spinner> |
		//                              [Close]
		try {
			MainConfig mainConfig = MainApplication.getMainConfig();
			WikiUpload wikiUpload = new WikiUpload(mainConfig.getWikiURL());

			WebContext.WebResponse response = wikiUpload.acquireLoginToken();
			System.out.println("loginToken response: " + response.responseText());

			response = wikiUpload.logIn(mainConfig.getBotUsername(), mainConfig.getBotPassword());
			System.out.println("login response: " + response.responseText());

			response = wikiUpload.acquireCsrfToken();
			System.out.println("csrf response: " + response.responseText());

			MapperManager.getInstance().getActiveMappers().forEach(mapper -> {
				String wikiPath = mapper.getWikiPathAccessor(mainConfig).getCurrentValue();
				String mapperOutput = String.join("\n", mapper.buildLuaLines());
				try {
					WebContext.WebResponse uploadResponse = wikiUpload.uploadFile(wikiPath, mapperOutput);
					System.out.println("path: " + wikiPath);
					System.out.println("\t response: " + uploadResponse.responseText());
				} catch (IOException e) {
					System.out.println("web error! " + e);
				}
			});
		} catch (IOException e) {
			System.out.println("web error! " + e);
		}
	}

	@FXML
	public void onAnimateSpinner() {
		LoadingSpinner[] spinners = new LoadingSpinner[]{testLoadingSpinner, testLoadingSpinner2};
		for (LoadingSpinner spinner : spinners) {
			IconKey displayedIcon = spinner.getDisplayedIcon();
			if (displayedIcon == null) {
				spinner.setDisplayedIcon(IconKey.getByIndex(0));
			} else {
				int newIndex = (displayedIcon.index + 1) % spinner.getIconSequence().size();
				if (newIndex == 0) {
					spinner.setDisplayedIcon(null);
				} else {
					spinner.setDisplayedIcon(IconKey.getByIndex(newIndex));
				}
			}
		}
	}
}