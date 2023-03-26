package blazingtwist.sod_luadatabasebot.yamlmapper;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class MapperManager {
	private static MapperManager instance;

	public static MapperManager getInstance() {
		if (instance == null) {
			instance = new MapperManager();
		}
		return instance;
	}

	private final HashMap<YamlMapperCategory, List<YamlMapper>> yamlMappers = new HashMap<>();

	private MapperManager() {
		Reflections reflections = new Reflections("blazingtwist.sod_luadatabasebot.yamlmapper.mappers");
		for (Class<? extends YamlMapper> mapperClass : reflections.getSubTypesOf(YamlMapper.class)) {
			try {
				YamlMapper mapper = mapperClass.getDeclaredConstructor().newInstance();
				List<YamlMapper> yamlMappers = this.yamlMappers.computeIfAbsent(mapper.getMapperCategory(), k -> new ArrayList<>());
				yamlMappers.add(mapper);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				System.err.println("Failed to instantiate mapper '" + mapperClass.getCanonicalName() + "' | exception: " + e);
			}
		}
		for (List<YamlMapper> mapperList : yamlMappers.values()) {
			mapperList.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getDisplayName(), b.getDisplayName()));
		}
	}

	public List<YamlMapper> getMappers(YamlMapperCategory category) {
		return yamlMappers.computeIfAbsent(category, k -> new ArrayList<>());
	}

	public Set<YamlFileKey> getRequiredSourceFiles() {
		return yamlMappers.values().stream()
				.flatMap(Collection::stream)
				.filter(YamlMapper::isEnabled)
				.flatMap(mapper -> mapper.getRequiredYamlFiles().stream())
				.collect(Collectors.toSet());
	}

	public List<Exception> loadFile(YamlFileKey fileKey, File file) {
		List<Exception> exceptions = new ArrayList<>();
		try {
			ConfigurationNode rootNode = YamlConfigurationLoader.builder().file(file).build().load();
			exceptions.addAll(yamlMappers.values().stream()
					.flatMap(mappers -> mappers.stream()
							.filter(YamlMapper::isEnabled)
							.map(mapper -> mapper.loadSourceFile(fileKey, rootNode))
					)
					.filter(Objects::nonNull)
					.collect(Collectors.toList()));
		} catch (ConfigurateException e) {
			exceptions.add(e);
		}
		return exceptions;
	}

	public List<YamlMapper> getActiveMappers() {
		return yamlMappers.values().stream()
				.flatMap(Collection::stream)
				.filter(YamlMapper::isEnabled)
				.collect(Collectors.toList());
	}

}
