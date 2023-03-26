package blazingtwist.sod_luadatabasebot.yamlmapper;

public enum YamlFileKey {
	PetTypesSource("Sanctuary Pet Types Source"),
	FireballSource("Fireball Source"),
	FlightStatsSource("Flight Stats Source"),
	SquadTacticsSource("Squad Tactics Source"),
	WeaponNameSource("Weapon Name Source"),
	;

	private final String displayName;

	YamlFileKey(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
