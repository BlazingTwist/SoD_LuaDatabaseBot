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
public class STStatDBMapper extends YamlMapper {

	@ConfigSerializable
	private static class STStatEntry {
		private static final String keyPool_dragons = "dragons";
		private static final String keyPool_stStats = "stStats";
		private static final String keyPool_statInfo = "statInfo";
		private static final String typePool_dragonsMap = "dragonsMap";
		private static final String typePool_statType = "statType";

		public static List<String> buildLuaLines(List<STStatEntry> entries) {
			LuaDatabaseCompressor compressor = new LuaDatabaseCompressor()
					.addKeySequence(keyPool_dragons, entries.stream().map(x -> x.dragonName).collect(Collectors.toList()))
					.addKeySequence(keyPool_stStats, "Critical Chance", "Critical Damage", "Dodge Chance", "FirePower",
							"Healing Power", "Health", "Movement", "Strength")
					.addKeySequence(keyPool_statInfo, "base", "level", "limitMult", "limitMin", "limitMax", "round")
					.addType(typePool_dragonsMap, new LuaTypeDefinition(keyPool_dragons).withTypeRef(typePool_statType))
					.addType(typePool_statType, new LuaTypeDefinition(keyPool_stStats).withKeyRef(keyPool_statInfo))
					.setRootType(LuaTypeDefinition.LuaPoolReference.typeReference(typePool_dragonsMap));
			return compressor.buildLuaLines(
					entries.stream().map(STStatEntry::buildLuaDataTableEntry).collect(Collectors.toList()),
					"\t"
			);
		}

		@NodeKey()
		public String dragonName;

		@Setting(nodeFromParent = true)
		@Required
		public Map<String, List<String>> statValues;

		private STStatEntry() {
		}

		public String buildLuaDataTableEntry() {
			String[] statOrder = {"CriticalChance", "CriticalDamageMultiplier", "DodgeChance", "FirePower", "HealingPower", "Health", "Movement", "Strength"};
			String[] statInfoOrder = {"BaseValue", "LevelMultiplier", "LimitMultiplier", "Min", "Max", "Round"};
			String statListString = Arrays.stream(statOrder)
					.map(statKey -> {
						String infoString = Arrays.stream(statInfoOrder)
								.map(infoKey -> statValues.get(statKey + ":" + infoKey).get(0))
								.collect(Collectors.joining(", "));
						return "{" + infoString + "}";
					}).collect(Collectors.joining(", "));
			return "{" + statListString + "},";
		}
	}

	private List<STStatEntry> stStatEntries = null;

	@Override
	public String getDisplayName() {
		return "SquadTactics Stats";
	}

	@Override
	public YamlMapperCategory getMapperCategory() {
		return YamlMapperCategory.Combat;
	}

	@Override
	public FieldAccessor<?, String> getWikiPathAccessor(MainConfig configInstance) {
		return FieldAccessor.reflectiveAccessor(configInstance.getWikiPaths(), WikiPathsConfig.DEFAULT, "stStatDBPath", String.class);
	}

	@Override
	public List<YamlFileKey> getRequiredYamlFiles() {
		return Collections.singletonList(YamlFileKey.SquadTacticsSource);
	}

	@Override
	public Exception loadSourceFile(YamlFileKey fileKey, ConfigurationNode rootNode) {
		if (fileKey == YamlFileKey.SquadTacticsSource) {
			try {
				stStatEntries = new ArrayList<>();
				for (ConfigurationNode dragonStats : rootNode.childrenMap().values()) {
					stStatEntries.add(dragonStats.get(STStatEntry.class));
				}
			} catch (Exception e) {
				stStatEntries = null;
				return e;
			}
		}
		return null;
	}

	@Override
	public List<String> buildLuaLines() {
		return STStatEntry.buildLuaLines(stStatEntries);
	}
}
