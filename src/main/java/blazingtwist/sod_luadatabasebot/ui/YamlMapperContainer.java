package blazingtwist.sod_luadatabasebot.ui;

import blazingtwist.sod_luadatabasebot.utils.FXMLUtils;
import blazingtwist.sod_luadatabasebot.yamlmapper.MapperManager;
import blazingtwist.sod_luadatabasebot.yamlmapper.YamlMapper;
import blazingtwist.sod_luadatabasebot.yamlmapper.YamlMapperCategory;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class YamlMapperContainer {

	public static final String fxmlPath = "/fxml-nodes/yaml-mapper-container.fxml";

	public static YamlMapperContainer load(YamlMapperCategory mapperCategory) {
		YamlMapperContainer controller = FXMLUtils.tryLoadFxml(fxmlPath);
		assert controller != null;
		controller.loadContent(mapperCategory);
		return controller;
	}

	@FXML
	public GridPane mapperPane;
	@FXML
	public Label mapperHeader;

	public Parent getRootNode() {
		return mapperPane;
	}

	public void loadContent(YamlMapperCategory category) {
		mapperHeader.setText(category.getDisplayName());
		int rowIndex = 2;
		for (YamlMapper mapper : MapperManager.getInstance().getMappers(category)) {
			YamlMapperEntry mapperEntry = YamlMapperEntry.load(mapper, rowIndex > 2 ? (rowIndex++) : -1, rowIndex++, 300);
			mapperEntry.addNodesToList(mapperPane.getChildren());
		}
	}
}
