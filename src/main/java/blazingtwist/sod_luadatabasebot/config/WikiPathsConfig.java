package blazingtwist.sod_luadatabasebot.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class WikiPathsConfig {

	public static final WikiPathsConfig DEFAULT = new WikiPathsConfig();

	@Setting("battleFireballDBPath")
	public String battleFireballDBPath = "Module:BlazingTwist/DragonStats/Database/BattleFireballDB";

	@Setting("battleFireballNameDBPath")
	public String battleFireballNameDBPath = "Module:BlazingTwist/DragonStats/Database/BattleFireballNameDB";

	@Setting("battleHealthDBPath")
	public String battleHealthDBPath = "Module:BlazingTwist/DragonStats/Database/BattleHealthDB";

	@Setting("dragonCardDBPath")
	public String dragonCardDBPath = "Module:BlazingTwist/DragonStats/Database/DragonCardDB";

	@Setting("dragonsDBPath")
	public String dragonsDBPath = "Module:BlazingTwist/DragonStats/Database/DragonsDB";

	@Setting("flightStatsDBPath")
	public String flightStatsDBPath = "Module:BlazingTwist/DragonStats/Database/FlightStatsDB";

	@Setting("stStatDBPath")
	public String stStatDBPath = "Module:BlazingTwist/DragonStats/Database/STStatDB";

}
