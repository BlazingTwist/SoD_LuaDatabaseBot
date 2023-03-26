package blazingtwist.sod_luadatabasebot.yamlmapper;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class LuaTypeDefinition {
	public String key;
	private final ArrayList<LuaTypeDefinition.LuaPoolReference> types = new ArrayList<>();

	public LuaTypeDefinition(String key) {
		this.key = key;
	}

	public LuaTypeDefinition withKeyRef(String keyName) {
		types.add(LuaTypeDefinition.LuaPoolReference.keyReference(keyName));
		return this;
	}

	public LuaTypeDefinition withTypeRef(String typeName) {
		types.add(LuaTypeDefinition.LuaPoolReference.typeReference(typeName));
		return this;
	}

	public String subTypesToLuaString(Map<String, Integer> keyIndices, Map<String, Integer> typeIndices) {
		return types.stream()
				.map(subType -> subType.toLuaString(keyIndices, typeIndices))
				.collect(Collectors.joining(", "));
	}

	public static class LuaPoolReference {
		public static LuaTypeDefinition.LuaPoolReference keyReference(String name) {
			return new LuaTypeDefinition.LuaPoolReference(name, null);
		}

		public static LuaTypeDefinition.LuaPoolReference typeReference(String name) {
			return new LuaTypeDefinition.LuaPoolReference(null, name);
		}

		public String keyName;
		public String typeName;

		private LuaPoolReference(String keyName, String typeName) {
			this.keyName = keyName;
			this.typeName = typeName;
		}

		public String toLuaString(Map<String, Integer> keyIndices, Map<String, Integer> typeIndices) {
			return keyName != null
					? "{ki = " + keyIndices.get(keyName) + "}"
					: "{ti = " + typeIndices.get(typeName) + "}";
		}
	}
}
