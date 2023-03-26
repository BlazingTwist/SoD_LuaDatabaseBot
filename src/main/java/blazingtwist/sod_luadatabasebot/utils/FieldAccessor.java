package blazingtwist.sod_luadatabasebot.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class FieldAccessor<ConfigType, ValueType> {
	private final ConfigType currentConfig;
	private final ConfigType defaultConfig;
	private final Function<ConfigType, ValueType> fieldGetter;
	private final BiConsumer<ConfigType, ValueType> fieldSetter;
	private final List<Consumer<ValueType>> onSetCallbacks = new ArrayList<>();

	public static <CT, VT> FieldAccessor<CT, VT> reflectiveAccessor(
			CT instance, CT defaultInstance, String fieldName, Class<VT> valueClass) {
		Class<?> configClass = instance.getClass();
		try {
			Field field = configClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			return new FieldAccessor<>(instance, defaultInstance, c -> {
				try {
					Object value = field.get(c);
					if (valueClass.isAssignableFrom(value.getClass())) {
						return valueClass.cast(value);
					}
					return null;
				} catch (IllegalAccessException e) {
					System.err.println("Failed to get field value! Exception: " + e);
					return null;
				}
			}, (c, x) -> {
				try {
					field.set(c, x);
				} catch (IllegalAccessException e) {
					System.err.println("Failed to set field value! Exception: " + e);
				}
			});
		} catch (NoSuchFieldException e) {
			System.err.println("Unable to find field: " + fieldName + " in " + configClass.getSimpleName() + ". Exception: " + e);
			return null;
		}
	}

	public FieldAccessor(ConfigType currentConfig, ConfigType defaultConfig,
						 Function<ConfigType, ValueType> fieldGetter,
						 BiConsumer<ConfigType, ValueType> fieldSetter) {
		this.currentConfig = currentConfig;
		this.defaultConfig = defaultConfig;
		this.fieldGetter = fieldGetter;
		this.fieldSetter = fieldSetter;
	}

	public FieldAccessor<ConfigType, ValueType> addSetterListener(Consumer<ValueType> callback) {
		onSetCallbacks.add(callback);
		return this;
	}

	public ValueType getCurrentValue() {
		return fieldGetter.apply(currentConfig);
	}

	public ValueType getDefaultValue() {
		return fieldGetter.apply(defaultConfig);
	}

	public void setValue(ValueType value) {
		fieldSetter.accept(currentConfig, value);
		for (Consumer<ValueType> callback : onSetCallbacks) {
			callback.accept(value);
		}
	}
}
