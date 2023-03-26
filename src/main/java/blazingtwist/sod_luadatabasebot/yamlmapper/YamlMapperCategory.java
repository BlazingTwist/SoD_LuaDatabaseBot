package blazingtwist.sod_luadatabasebot.yamlmapper;

public enum YamlMapperCategory {
	Battle("Battle Stats"),
	Combat("Combat Stats"),
	Flight("Flight Stats"),
	Miscellaneous("Miscellaneous"),
	;

	private final String displayName;

	YamlMapperCategory(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
