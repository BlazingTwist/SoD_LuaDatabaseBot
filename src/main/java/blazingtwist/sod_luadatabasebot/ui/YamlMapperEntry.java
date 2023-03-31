package blazingtwist.sod_luadatabasebot.ui;

import blazingtwist.sod_luadatabasebot.MainApplication;
import blazingtwist.sod_luadatabasebot.utils.StringUtils;
import blazingtwist.sod_luadatabasebot.yamlmapper.YamlMapper;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class YamlMapperEntry {

	public static final String cssMapperName = "mapper-name";
	public static final String cssMapperWikiPath = "mapper-wiki-path";
	public static final String cssFlatButton = "flat-button";
	public static final String cssButtonSmall = "button-small";
	public static final String cssSmallDivider = "small-divider";

	private static final int transitionDuration = 100;

	/**
	 * @param mapper       the mapper to create an entry for
	 * @param separatorRow the row for the separatorLine, or -1 to disable separator
	 * @param entryRow     the row for the entry
	 * @return object to be used to access the created nodes
	 */
	public static YamlMapperEntry load(YamlMapper mapper, int separatorRow, int entryRow, float gridWidth) {
		Label mapperNameLabel = new Label(mapper.getDisplayName());
		mapperNameLabel.getStyleClass().add(cssMapperName);
		GridPane.setRowIndex(mapperNameLabel, entryRow);
		mapperNameLabel.setOpacity(1);

		LedButton mapperSelectButton = LedButton.load();
		mapperSelectButton.setButtonText("Select");
		mapperSelectButton.setLed(false);
		StackPane selectButtonContainer = mapperSelectButton.container;
		selectButtonContainer.setPadding(new Insets(0, 16, 0, 0));
		GridPane.setRowIndex(selectButtonContainer, entryRow);
		GridPane.setColumnIndex(selectButtonContainer, 2);

		Label mapperWikiPathLabel = new Label("");
		mapperWikiPathLabel.getStyleClass().add(cssMapperWikiPath);
		GridPane.setRowIndex(mapperWikiPathLabel, entryRow);
		mapperWikiPathLabel.setOpacity(0);

		Button editButton = new Button("Edit");
		editButton.getStyleClass().addAll(cssFlatButton, cssButtonSmall);
		editButton.setFocusTraversable(false);
		GridPane.setRowIndex(editButton, entryRow);
		GridPane.setColumnIndex(editButton, 1);
		editButton.setOpacity(0);
		editButton.setDisable(true);

		Rectangle separator = null;
		if (separatorRow >= 0) {
			separator = new Rectangle(gridWidth, 2);
			separator.getStyleClass().add(cssSmallDivider);
			GridPane.setRowIndex(separator, separatorRow);
			GridPane.setColumnSpan(separator, 3);
		}

		return new YamlMapperEntry(mapper, mapperNameLabel, mapperWikiPathLabel, editButton, mapperSelectButton, separator);
	}

	private static FadeTransition createFadeOutTransition(Node targetNode) {
		FadeTransition transition = new FadeTransition(Duration.millis(transitionDuration), targetNode);
		transition.setFromValue(1.0);
		transition.setToValue(0.0);
		return transition;
	}

	private static FadeTransition createFadeInTransition(Node targetNode) {
		FadeTransition transition = new FadeTransition(Duration.millis(transitionDuration), targetNode);
		transition.setFromValue(0.0);
		transition.setToValue(1.0);
		return transition;
	}

	private static TranslateTransition createTranslateTransition(Node targetNode, double fromY, double toY) {
		TranslateTransition transition = new TranslateTransition(Duration.millis(transitionDuration), targetNode);
		transition.setFromY(fromY);
		transition.setToY(toY);
		return transition;
	}

	protected YamlMapper mapper;

	protected Label nameLabel;
	protected Label wikiPathLabel;
	protected Button editButton;
	protected LedButton selectButton;
	protected Rectangle separatorLine;

	protected FadeTransition nameLabelFadeOut;
	protected TranslateTransition nameLabelTranslateOut;
	protected FadeTransition nameLabelFadeIn;
	protected TranslateTransition nameLabelTranslateIn;

	protected FadeTransition wikiPathFadeOut;
	protected TranslateTransition wikiPathTranslateOut;
	protected FadeTransition wikiPathFadeIn;
	protected TranslateTransition wikiPathTranslateIn;

	protected FadeTransition editButtonFadeOut;
	protected TranslateTransition editButtonTranslateOut;
	protected FadeTransition editButtonFadeIn;
	protected TranslateTransition editButtonTranslateIn;


	public YamlMapperEntry(YamlMapper mapper, Label nameLabel, Label wikiPathLabel, Button editButton, LedButton selectButton, Rectangle separatorLine) {
		this.mapper = mapper;
		this.nameLabel = nameLabel;
		this.wikiPathLabel = wikiPathLabel;
		this.editButton = editButton;
		this.selectButton = selectButton;
		this.separatorLine = separatorLine;

		setWikiPath(mapper.getWikiPathAccessor(MainApplication.getMainConfig()).getCurrentValue());

		selectButton.setOnclickCallback(this::onSelectClicked);
		editButton.setOnMouseClicked(this::onEditClicked);

		nameLabelFadeOut = createFadeOutTransition(nameLabel);
		nameLabelTranslateOut = createTranslateTransition(nameLabel, 0, -20);
		nameLabelFadeIn = createFadeInTransition(nameLabel);
		nameLabelTranslateIn = createTranslateTransition(nameLabel, -20, 0);

		wikiPathFadeOut = createFadeOutTransition(wikiPathLabel);
		wikiPathTranslateOut = createTranslateTransition(wikiPathLabel, 0, 20);
		wikiPathFadeIn = createFadeInTransition(wikiPathLabel);
		wikiPathTranslateIn = createTranslateTransition(wikiPathLabel, 20, 0);

		editButtonFadeOut = createFadeOutTransition(editButton);
		editButtonTranslateOut = createTranslateTransition(editButton, 0, 20);
		editButtonFadeIn = createFadeInTransition(editButton);
		editButtonTranslateIn = createTranslateTransition(editButton, 20, 0);
	}

	private void setWikiPath(String path) {
		String wikiPathText = String.join("\n", StringUtils.SplitWithMaxLength(path, 32, StringUtils.SplitCharRule.Prepend, '/'));
		wikiPathLabel.setText(wikiPathText);
	}

	public void addNodesToList(List<Node> targetList) {
		targetList.addAll(List.of(nameLabel, wikiPathLabel, editButton, selectButton.getContainer()));
		if (separatorLine != null) {
			targetList.add(separatorLine);
		}
	}

	protected void onEditClicked(MouseEvent event) {
		MainApplication.getMainController().openSingleStringEditPanel(
				"Edit " + mapper.getDisplayName() + " Wiki-Path",
				"Current Path",
				"New Path",
				mapper.getWikiPathAccessor(MainApplication.getMainConfig()).addSetterListener(this::setWikiPath)
		);
	}

	protected void onSelectClicked(MouseEvent event) {
		boolean ledIsOn = selectButton.getLedIsOn();
		selectButton.setLed(!ledIsOn);
		mapper.setEnabled(!ledIsOn);

		MainApplication.getMainController().onStatUploadSelectionChanged();

		if (ledIsOn) {
			// animate: hide wikiPath and edit button
			// animate: show mapper name
			nameLabelFadeIn.playFromStart();
			nameLabelTranslateIn.playFromStart();

			wikiPathFadeOut.playFromStart();
			wikiPathTranslateOut.playFromStart();

			editButtonFadeOut.playFromStart();
			editButtonTranslateOut.playFromStart();
			editButton.setDisable(true);
		} else {
			// animate: hide mapper name
			// animate: show wikiPath and edit button
			nameLabelFadeOut.playFromStart();
			nameLabelTranslateOut.playFromStart();

			wikiPathFadeIn.playFromStart();
			wikiPathTranslateIn.playFromStart();

			editButtonFadeIn.playFromStart();
			editButtonTranslateIn.playFromStart();
			editButton.setDisable(false);
		}
	}
}
