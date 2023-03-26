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
import java.util.stream.Stream;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.NodeKey;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ReflectivelyConstructed
public class DragonCardDBMapper extends YamlMapper {

	@ConfigSerializable
	private static class CardEntry {
		private static final String keyPool_dragons = "dragons";
		private static final String keyPool_barInfo = "barInfo";
		private static final String typePool_dragonsMap = "dragonsMap";

		public static List<String> buildLuaLines(List<CardEntry> entries) {
			LuaDatabaseCompressor compressor = new LuaDatabaseCompressor()
					.addKeySequence(keyPool_dragons, entries.stream().map(x -> x.dragonName).collect(Collectors.toList()))
					.addKeySequence(keyPool_barInfo, "MaxBarValue", "FirePower", "MaxSpeed", "PitchRate", "TurnRate", "Acceleration")
					.addType(typePool_dragonsMap, new LuaTypeDefinition(keyPool_dragons).withKeyRef(keyPool_barInfo))
					.setRootType(LuaTypeDefinition.LuaPoolReference.typeReference(typePool_dragonsMap));
			return compressor.buildLuaLines(
					entries.stream().map(CardEntry::buildLuaDataTableEntry).collect(Collectors.toList()),
					"\t"
			);
		}

		@NodeKey()
		public String dragonName;

		@Setting("Stats:MaxValue")
		@Required
		public List<String> maxBarList;

		@Setting("Stats:FirePower")
		@Required
		public List<String> firepowerList;

		@Setting("Stats:MaxSpeed")
		@Required
		public List<String> maxSpeedList;

		@Setting("Stats:PitchRate")
		@Required
		public List<String> pitchRateList;

		@Setting("Stats:TurnRate")
		@Required
		public List<String> TurnRateList;

		@Setting("Stats:Acceleration")
		@Required
		public List<String> AccelerationList;

		private CardEntry() {
		}

		public String buildLuaDataTableEntry() {
			Stream<List<String>> yamlLists = Stream.of(this.maxBarList, firepowerList, maxSpeedList, pitchRateList, TurnRateList, AccelerationList);
			return "{"
					+ yamlLists.map(list -> list.get(0)).collect(Collectors.joining(", "))
					+ "},";
		}
	}

	private List<CardEntry> cardEntries = null;

	@Override
	public String getDisplayName() {
		return "Dragon Card";
	}

	@Override
	public YamlMapperCategory getMapperCategory() {
		return YamlMapperCategory.Miscellaneous;
	}

	@Override
	public FieldAccessor<?, String> getWikiPathAccessor(MainConfig configInstance) {
		return FieldAccessor.reflectiveAccessor(configInstance.getWikiPaths(), WikiPathsConfig.DEFAULT, "dragonCardDBPath", String.class);
	}

	@Override
	public List<YamlFileKey> getRequiredYamlFiles() {
		return Collections.singletonList(YamlFileKey.PetTypesSource);
	}

	@Override
	public Exception loadSourceFile(YamlFileKey fileKey, ConfigurationNode rootNode) {
		if (fileKey == YamlFileKey.PetTypesSource) {
			try {
				cardEntries = new ArrayList<>();
				for (ConfigurationNode dragonStats : rootNode.childrenMap().values()) {
					cardEntries.add(dragonStats.get(CardEntry.class));
				}
			} catch (Exception e) {
				cardEntries = null;
				return e;
			}
		}
		return null;
	}

	@Override
	public List<String> buildLuaLines() {
		return CardEntry.buildLuaLines(cardEntries);
	}
}
