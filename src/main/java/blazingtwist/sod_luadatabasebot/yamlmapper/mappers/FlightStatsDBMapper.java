package blazingtwist.sod_luadatabasebot.yamlmapper.mappers;

import blazingtwist.sod_luadatabasebot.config.MainConfig;
import blazingtwist.sod_luadatabasebot.config.WikiPathsConfig;
import blazingtwist.sod_luadatabasebot.utils.FieldAccessor;
import blazingtwist.sod_luadatabasebot.utils.ReflectivelyConstructed;
import blazingtwist.sod_luadatabasebot.yamlmapper.LuaDatabaseCompressor;
import blazingtwist.sod_luadatabasebot.yamlmapper.LuaTypeDefinition;
import blazingtwist.sod_luadatabasebot.yamlmapper.YamlFileKey;
import blazingtwist.sod_luadatabasebot.yamlmapper.YamlMapper;
import blazingtwist.sod_luadatabasebot.yamlmapper.YamlMapperCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.NodeKey;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ReflectivelyConstructed
public class FlightStatsDBMapper extends YamlMapper {

	@ConfigSerializable
	private static class FlightStatsEntry {
		private static final String keyPool_dragons = "dragons";
		private static final String keyPool_flightMode = "flightMode";
		private static final String keyPool_flightStats = "flightStats";
		private static final String typePool_dragonsMap = "dragonsMap";
		private static final String typePool_flightStatsType = "flightStatsType";

		private static final String[] flightStatOrder = {
				"RollTurnRate", "RollDampRate", "YawTurnRate", "YawTurnFactor", "PitchTurnRate", "PitchDampRate", "Speed:Min", "Speed:Max",
				"Acceleration", "ManualFlapAccel", "ManualFlapTimer", "SpeedDampRate", "BrakeDecel", "ClimbAccelRate", "DiveAccelRate",
				"SpeedModifierOnCollision", "BounceOnCollision", "GlideDownMult", "GravityModifier", "GravityClimbMult", "GravityDiveMult",
				"FlyMaxUpPitch", "FlyMaxDownPitch", "GlideMaxUpPitch", "GlideMaxDownPitch", "MaxRoll", "PositionBoost"
		};

		public static List<String> buildLuaLines(List<FlightStatsEntry> entries) {
			LuaDatabaseCompressor compressor = new LuaDatabaseCompressor()
					.addKeySequence(
							keyPool_dragons,
							entries.stream()
									.map(x -> x.dragonName.startsWith("PfDW") ? x.dragonName.substring("PfDW".length()) : x.dragonName)
									.collect(Collectors.toList()))
					.addKeySequence(keyPool_flightMode, "Gliding", "Flying", "Racing")
					.addKeySequence(keyPool_flightStats, flightStatOrder)
					.addType(typePool_dragonsMap, new LuaTypeDefinition(keyPool_dragons).withTypeRef(typePool_flightStatsType))
					.addType(typePool_flightStatsType, new LuaTypeDefinition(keyPool_flightMode).withKeyRef(keyPool_flightStats))
					.setRootType(LuaTypeDefinition.LuaPoolReference.typeReference(typePool_dragonsMap));
			return compressor.buildLuaLines(
					entries.stream().map(FlightStatsEntry::buildLuaDataTableEntry).collect(Collectors.toList()),
					"\t"
			);
		}

		@NodeKey()
		public String dragonName;

		@Setting(nodeFromParent = true)
		@Required
		public Map<String, List<String>> statValues;

		private FlightStatsEntry() {
		}

		public String buildLuaDataTableEntry() {
			List<String> typeOrder = statValues.get("FlightType");
			int glidingIndex = typeOrder.indexOf("Gliding");
			int flyingIndex = typeOrder.indexOf("Flying");
			int racingIndex = typeOrder.indexOf("Racing");

			List<List<String>> orderedStatValues = Arrays.stream(flightStatOrder)
					.map(statKey -> statValues.get(statKey))
					.collect(Collectors.toList());
			String glideStatString = orderedStatValues.stream().map(statList -> statList.get(glidingIndex)).collect(Collectors.joining(", "));
			String flyingStatString = orderedStatValues.stream().map(statList -> statList.get(flyingIndex)).collect(Collectors.joining(", "));
			String racingStatString = orderedStatValues.stream().map(statList -> statList.get(racingIndex)).collect(Collectors.joining(", "));

			return "{ {" + glideStatString + "}, {" + flyingStatString + "}, {" + racingStatString + "} },";
		}
	}

	private List<FlightStatsEntry> flightStatsEntries = null;

	@Override
	public String getDisplayName() {
		return "Flight Stats";
	}

	@Override
	public YamlMapperCategory getMapperCategory() {
		return YamlMapperCategory.Flight;
	}

	@Override
	public FieldAccessor<?, String> getWikiPathAccessor(MainConfig configInstance) {
		return FieldAccessor.reflectiveAccessor(configInstance.getWikiPaths(), WikiPathsConfig.DEFAULT, "flightStatsDBPath", String.class);
	}

	@Override
	public List<YamlFileKey> getRequiredYamlFiles() {
		return Collections.singletonList(YamlFileKey.FlightStatsSource);
	}

	@Override
	public Exception loadSourceFile(YamlFileKey fileKey, ConfigurationNode rootNode) {
		if (fileKey == YamlFileKey.FlightStatsSource) {
			try {
				flightStatsEntries = new ArrayList<>();
				for (ConfigurationNode dragonStats : rootNode.childrenMap().values()) {
					flightStatsEntries.add(dragonStats.get(FlightStatsEntry.class));
				}
			} catch (Exception e) {
				flightStatsEntries = null;
				return e;
			}
		}
		return null;
	}

	@Override
	public List<String> buildLuaLines() {
		return FlightStatsEntry.buildLuaLines(flightStatsEntries);
	}
}
