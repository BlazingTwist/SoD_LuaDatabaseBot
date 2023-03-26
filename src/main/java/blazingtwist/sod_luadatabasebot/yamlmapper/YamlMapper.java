package blazingtwist.sod_luadatabasebot.yamlmapper;

import blazingtwist.sod_luadatabasebot.config.MainConfig;
import blazingtwist.sod_luadatabasebot.utils.FieldAccessor;
import java.util.List;
import org.spongepowered.configurate.ConfigurationNode;

public abstract class YamlMapper {

	protected boolean enabled;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public abstract String getDisplayName();

	public abstract YamlMapperCategory getMapperCategory();

	public abstract FieldAccessor<?, String> getWikiPathAccessor(MainConfig configInstance);

	public abstract List<YamlFileKey> getRequiredYamlFiles();

	public abstract Exception loadSourceFile(YamlFileKey fileKey, ConfigurationNode rootNode);

	public abstract List<String> buildLuaLines();
}
