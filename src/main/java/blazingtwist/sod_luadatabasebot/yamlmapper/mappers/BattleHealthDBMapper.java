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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.NodeKey;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ReflectivelyConstructed
public class BattleHealthDBMapper extends YamlMapper {

	@ConfigSerializable
	private static class HealthEntry {
		private static final String keyPool_dragons = "dragons";

		public static List<String> buildLuaLines(List<HealthEntry> entries) {
			LuaDatabaseCompressor compressor = new LuaDatabaseCompressor()
					.addKeySequence(keyPool_dragons, entries.stream().map(x -> x.dragonName).collect(Collectors.toList()))
					.setRootType(LuaTypeDefinition.LuaPoolReference.keyReference(keyPool_dragons));
			return compressor.buildLuaLines(
					entries.stream().map(HealthEntry::buildLuaDataTableEntry).collect(Collectors.toList()),
					"\t"
			);
		}

		@NodeKey()
		public String dragonName;

		@Setting("Health")
		@Required
		public List<String> healthList;

		private HealthEntry() {
		}

		public String buildLuaDataTableEntry() {
			return healthList.get(0) + ",";
		}
	}

	private List<HealthEntry> healthEntries = null;

	@Override
	public String getDisplayName() {
		return "Health Stats";
	}

	@Override
	public YamlMapperCategory getMapperCategory() {
		return YamlMapperCategory.Battle;
	}

	@Override
	public FieldAccessor<?, String> getWikiPathAccessor(MainConfig configInstance) {
		return FieldAccessor.reflectiveAccessor(configInstance.getWikiPaths(), WikiPathsConfig.DEFAULT, "battleHealthDBPath", String.class);
	}

	@Override
	public List<YamlFileKey> getRequiredYamlFiles() {
		return Collections.singletonList(YamlFileKey.PetTypesSource);
	}

	@Override
	public Exception loadSourceFile(YamlFileKey fileKey, ConfigurationNode rootNode) {
		if (fileKey == YamlFileKey.PetTypesSource) {
			try {
				healthEntries = new ArrayList<>();
				for (ConfigurationNode dragonStats : rootNode.childrenMap().values()) {
					healthEntries.add(dragonStats.get(HealthEntry.class));
				}
			} catch (Exception e) {
				healthEntries = null;
				return e;
			}
		}
		return null;
	}

	@Override
	public List<String> buildLuaLines() {
		return HealthEntry.buildLuaLines(healthEntries);
	}
}
