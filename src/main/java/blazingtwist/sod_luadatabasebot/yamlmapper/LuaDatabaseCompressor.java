package blazingtwist.sod_luadatabasebot.yamlmapper;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LuaDatabaseCompressor {

	public static String numbersToLuaArray(Stream<Number> numbers) {
		return "{" + numbers.map(Object::toString).collect(Collectors.joining(", ")) + "}";
	}

	public static String stringsToLuaArray(Stream<String> strings) {
		return "{\"" + strings.map(LuaDatabaseCompressor::sanitizeString).collect(Collectors.joining("\", \"")) + "\"}";
	}

	public static String sanitizeString(String str) {
		return str.replace("\"", "\\\"");
	}

	private final LinkedHashMap<String, ArrayList<String>> keyPool = new LinkedHashMap<>();
	private final LinkedHashMap<String, LuaTypeDefinition> typePool = new LinkedHashMap<>();
	private LuaTypeDefinition.LuaPoolReference rootType = null;

	public LuaDatabaseCompressor() {
	}

	public LuaDatabaseCompressor addKeySequence(String name, String... keys) {
		keyPool.put(name, new ArrayList<>(Arrays.asList(keys)));
		return this;
	}

	public LuaDatabaseCompressor addKeySequence(String name, List<String> keys) {
		keyPool.put(name, new ArrayList<>(keys));
		return this;
	}

	public LuaDatabaseCompressor addType(String name, LuaTypeDefinition typeDef) {
		typePool.put(name, typeDef);
		return this;
	}

	public LuaDatabaseCompressor setRootType(LuaTypeDefinition.LuaPoolReference rootType) {
		this.rootType = rootType;
		return this;
	}

	public List<String> buildLuaLines(List<String> dataTableLines, String indentationStr) {
		List<String> lines = new ArrayList<>();

		HashMap<String, Integer> keyPoolIndices = new HashMap<>();
		lines.add("local keyPool = {");
		{
			int keyIndex = 1;
			for (Map.Entry<String, ArrayList<String>> keyPoolEntry : keyPool.entrySet()) {
				lines.add(indentationStr + "--[[" + keyPoolEntry.getKey() + "--]] " + stringsToLuaArray(keyPoolEntry.getValue().stream()) + ",");
				keyPoolIndices.put(keyPoolEntry.getKey(), keyIndex++);
			}
		}
		lines.add("}");

		HashMap<String, Integer> typeIndices = new HashMap<>();
		ArrayList<Map.Entry<String, LuaTypeDefinition>> typeOrder = new ArrayList<>();
		{
			int typeIndex = 1;
			for (Map.Entry<String, LuaTypeDefinition> typeEntry : typePool.entrySet()) {
				typeIndices.put(typeEntry.getKey(), typeIndex++);
				typeOrder.add(typeEntry);
			}
		}
		lines.add("local typePool = {");
		for (Map.Entry<String, LuaTypeDefinition> typeEntry : typeOrder) {
			int keyIndex = keyPoolIndices.get(typeEntry.getValue().key);
			lines.add(indentationStr + "--[[" + typeEntry.getKey() + "--]] " +
					"{ key = " + keyIndex + ", types = { " +
					typeEntry.getValue().subTypesToLuaString(keyPoolIndices, typeIndices) +
					" } },");
		}
		lines.add("}");

		lines.add("local compressedDataTable = {");
		for (String dataTableLine : dataTableLines) {
			lines.add(indentationStr + dataTableLine);
		}
		lines.add("}");

		//language=lua
		String luaMapperFunction = """
				local function resolveTypeMapping(targetTable, dataType, dataTable)
				    if dataTable == nil then
				        return
				    end
				    
				    if dataType.ki ~= nil then
				        local keyOrder = keyPool[dataType.ki]
				        for i,key in ipairs(keyOrder) do
				            targetTable[key] = dataTable[i]
				        end
				    else
				        local pooledType = typePool[dataType.ti]
				        local keyOrder = keyPool[pooledType.key]
				        local subTypes = pooledType.types
				        local numTypes = #subTypes
				        local currentSubType = 1
				        
				        for i,key in ipairs(keyOrder) do
				            local value = {}
				            resolveTypeMapping(value, subTypes[currentSubType], dataTable[i])
				            targetTable[key] = value
				            currentSubType = (currentSubType % numTypes) + 1
				        end
				    end
				end""";
		lines.addAll(new BufferedReader(new StringReader(luaMapperFunction)).lines().collect(Collectors.toList()));

		lines.add("local resultTable = {}");
		lines.add("resolveTypeMapping(resultTable, " + rootType.toLuaString(keyPoolIndices, typeIndices) + ", compressedDataTable)");
		lines.add("return resultTable");

		return lines;
	}
}
