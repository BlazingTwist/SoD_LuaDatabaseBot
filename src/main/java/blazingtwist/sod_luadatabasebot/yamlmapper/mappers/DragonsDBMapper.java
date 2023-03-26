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
public class DragonsDBMapper extends YamlMapper {

	@ConfigSerializable
	private static class MetaEntry {
		private static final String keyPool_dragons = "dragons";
		private static final String keyPool_meta = "metaInfo";
		private static final String typePool_dragonsMap = "dragonsMap";

		public static List<String> buildLuaLines(List<MetaEntry> entries) {
			LuaDatabaseCompressor compressor = new LuaDatabaseCompressor()
					.addKeySequence(keyPool_dragons, entries.stream().map(x -> x.dragonName).collect(Collectors.toList()))
					.addKeySequence(keyPool_meta, "hasTitan")
					.addType(typePool_dragonsMap, new LuaTypeDefinition(keyPool_dragons).withKeyRef(keyPool_meta))
					.setRootType(LuaTypeDefinition.LuaPoolReference.typeReference(typePool_dragonsMap));
			return compressor.buildLuaLines(
					entries.stream().map(MetaEntry::buildLuaDataTableEntry).collect(Collectors.toList()),
					"\t"
			);
		}

		@NodeKey()
		public String dragonName;

		@Setting("GrowthStates:Name")
		@Required
		public List<String> growthStateList;

		private MetaEntry() {
		}

		public String buildLuaDataTableEntry() {
			boolean hasTitan = growthStateList.contains("Titan");
			return "{" + (hasTitan ? "true" : "false") + "},";
		}
	}

	private List<MetaEntry> metaEntries = null;

	@Override
	public String getDisplayName() {
		return "Dragon Names";
	}

	@Override
	public YamlMapperCategory getMapperCategory() {
		return YamlMapperCategory.Miscellaneous;
	}

	@Override
	public FieldAccessor<?, String> getWikiPathAccessor(MainConfig configInstance) {
		return FieldAccessor.reflectiveAccessor(configInstance.getWikiPaths(), WikiPathsConfig.DEFAULT, "dragonsDBPath", String.class);
	}

	@Override
	public List<YamlFileKey> getRequiredYamlFiles() {
		return Collections.singletonList(YamlFileKey.PetTypesSource);
	}

	@Override
	public Exception loadSourceFile(YamlFileKey fileKey, ConfigurationNode rootNode) {
		if (fileKey == YamlFileKey.PetTypesSource) {
			try {
				metaEntries = new ArrayList<>();
				for (ConfigurationNode dragonStats : rootNode.childrenMap().values()) {
					metaEntries.add(dragonStats.get(MetaEntry.class));
				}
			} catch (Exception e) {
				metaEntries = null;
				return e;
			}
		}
		return null;
	}

	@Override
	public List<String> buildLuaLines() {
		return MetaEntry.buildLuaLines(metaEntries);
	}
}
