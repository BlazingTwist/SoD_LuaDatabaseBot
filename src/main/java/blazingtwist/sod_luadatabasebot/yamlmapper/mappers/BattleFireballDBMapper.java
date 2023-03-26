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
public class BattleFireballDBMapper extends YamlMapper {

	@ConfigSerializable
	private static class FireballDBEntry {
		private static final String keyPool_dragons = "dragons";
		private static final String keyPool_fireball = "fireball";
		private static final String typePool_dragonsMap = "dragonsMap";

		public static List<String> buildLuaLines(List<FireballDBEntry> entries) {
			LuaDatabaseCompressor compressor = new LuaDatabaseCompressor()
					.addKeySequence(keyPool_dragons, entries.stream().map(x -> x.dragonName).collect(Collectors.toList()))
					.addKeySequence(keyPool_fireball,
							"Damage", "CritMultiplier", "CritChance", "TotalShots", "Cooldown", "Range", "RechargeMin", "RechargeMax")
					.addType(typePool_dragonsMap, new LuaTypeDefinition(keyPool_dragons).withKeyRef(keyPool_fireball))
					.setRootType(LuaTypeDefinition.LuaPoolReference.typeReference(typePool_dragonsMap));
			return compressor.buildLuaLines(
					entries.stream().map(FireballDBEntry::buildLuaDataTableEntry).collect(Collectors.toList()),
					"\t"
			);
		}

		@NodeKey()
		public String dragonName;

		@Setting("Damage")
		@Required
		public int damage;

		@Setting("CritMultiplier")
		@Required
		public float critMultiplier;

		@Setting("CritChance")
		@Required
		public float critRate;

		@Setting("TotalShots")
		@Required
		public int totalShots;

		@Setting("Cooldown")
		@Required
		public float cooldown;

		@Setting("Range")
		@Required
		public float range;

		@Setting("Recharge:Min")
		@Required
		public float rechargeMin;

		@Setting("Recharge:Max")
		@Required
		public float rechargeMax;

		private FireballDBEntry() {
		}

		public String buildLuaDataTableEntry() {
			return LuaDatabaseCompressor.numbersToLuaArray(
					Stream.of(damage, critMultiplier, critRate, totalShots, cooldown, range, rechargeMin, rechargeMax)
			) + ",";
		}
	}

	private List<FireballDBEntry> fireballDBEntries = null;

	@Override
	public String getDisplayName() {
		return "Fireball Stats";
	}

	@Override
	public YamlMapperCategory getMapperCategory() {
		return YamlMapperCategory.Battle;
	}

	@Override
	public FieldAccessor<?, String> getWikiPathAccessor(MainConfig configInstance) {
		return FieldAccessor.reflectiveAccessor(configInstance.getWikiPaths(), WikiPathsConfig.DEFAULT, "battleFireballDBPath", String.class);
	}

	@Override
	public List<YamlFileKey> getRequiredYamlFiles() {
		return Collections.singletonList(YamlFileKey.FireballSource);
	}

	@Override
	public Exception loadSourceFile(YamlFileKey fileKey, ConfigurationNode rootNode) {
		if (fileKey == YamlFileKey.FireballSource) {
			try {
				fireballDBEntries = new ArrayList<>();
				for (ConfigurationNode dragonStats : rootNode.childrenMap().values()) {
					fireballDBEntries.add(dragonStats.get(FireballDBEntry.class));
				}
			} catch (Exception e) {
				fireballDBEntries = null;
				return e;
			}
		}
		return null;
	}

	@Override
	public List<String> buildLuaLines() {
		return FireballDBEntry.buildLuaLines(fireballDBEntries);
	}
}
