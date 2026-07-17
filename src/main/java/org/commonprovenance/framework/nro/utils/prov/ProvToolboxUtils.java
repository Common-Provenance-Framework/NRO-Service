package org.commonprovenance.framework.nro.utils.prov;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.interop.Formats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ProvToolboxUtils {

  private ProvToolboxUtils() {
  }

  public static Document parseDocument(String base64Graph, String format) {
    try {
      if (base64Graph == null || base64Graph.isBlank()) {
        throw new IllegalArgumentException("Missing graph content.");
      }
      String toolboxFormat = normalizeFormat(format);

      String decoded = new String(Base64.getDecoder().decode(base64Graph), StandardCharsets.UTF_8);
      String preprocessed = ProvToolboxUtils.preprocessIncompatibleJsonForDeserialization(decoded, toolboxFormat, true);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(preprocessed.getBytes(StandardCharsets.UTF_8));
      InteropFramework interop = new InteropFramework();
      Formats.ProvFormat provFormat = interop.getTypeForFormat(toolboxFormat);
      if (provFormat == null) {
        throw new IllegalArgumentException("Unknown PROV format: " + toolboxFormat);
      }
      return interop.readDocument(inputStream, provFormat);
    } catch (Exception e) {
      throw new IllegalArgumentException("Unable to parse PROV graph.", e);
    }
  }

  private static String preprocessIncompatibleJsonForDeserialization(String json, String format, boolean prettyPrint) throws JsonProcessingException {
    if (!format.equals("json"))
      return json;

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(json);

    root = ProvToolboxUtils.addExplicitBundleId(root);
    root = ProvToolboxUtils.putTypedObjectsInArrays(root, mapper);
    root = ProvToolboxUtils.putStringValuesInArray(root, mapper, false);
    root = ProvToolboxUtils.stringifyValues(root, mapper);
    root = ProvToolboxUtils.copyOuterPrefixesIntoBundles(root, mapper);
    return prettyPrint
        ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
        : mapper.writeValueAsString(root);

  }

  private static JsonNode copyOuterPrefixesIntoBundles(JsonNode root, ObjectMapper mapper) {
    JsonNode outerPrefix = root.path("prefix");
    JsonNode bundleNode = root.path("bundle");
    if (outerPrefix.isObject() && bundleNode.isObject()) {
      bundleNode.propertyStream()
          .forEach((Map.Entry<String, JsonNode> bundleEntry) -> {
            JsonNode bundle = bundleEntry.getValue();

            ObjectNode bundlePrefix = bundle.isObject()
                && bundle.has("prefix")
                && bundle.get("prefix").isObject()
                    ? (ObjectNode) bundle.get("prefix")
                    : mapper.createObjectNode();

            outerPrefix
                .propertyStream()
                .forEach((Map.Entry<String, JsonNode> prefixEntry) -> bundlePrefix.set(
                    prefixEntry.getKey(),
                    prefixEntry.getValue()));

            ((ObjectNode) bundle).set("prefix", bundlePrefix);
          });
    }

    return root;
  }

  private static JsonNode stringifyValues(JsonNode node, ObjectMapper mapper) {
    if (node.isObject()) {
      ObjectNode obj = mapper.createObjectNode();
      node.propertyStream()
          .forEach((Map.Entry<String, JsonNode> entry) -> {
            String property = entry.getKey();
            obj.set(property, ProvToolboxUtils.stringifyValues(node.get(property), mapper));
          });
      return obj;
    } else if (node.isArray()) {
      ArrayNode arr = mapper.createArrayNode();
      node.forEach((JsonNode item) -> arr.add(ProvToolboxUtils.stringifyValues(item, mapper)));
      return arr;
    } else {
      return mapper.getNodeFactory().textNode(node.asText());
    }
  }

  private static JsonNode addExplicitBundleId(JsonNode root) {
    JsonNode bundleNode = root.path("bundle");
    if (bundleNode.isObject()) {
      bundleNode.propertyStream()
          .forEach((Map.Entry<String, JsonNode> entry) -> {
            String bundleId = entry.getKey();
            JsonNode bundle = entry.getValue();
            if (bundle.isObject() && !bundle.hasNonNull(bundleId)) {
              ((ObjectNode) bundle).put("@id", bundleId);
            }
          });
    }
    return root;
  }

  private static JsonNode putTypedObjectsInArrays(JsonNode node, ObjectMapper mapper) {
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;

      boolean hasDollar = obj.has("$");
      boolean hasType = obj.has("type");

      // If object matches {"$", "type"} → wrap in array
      if (hasDollar && hasType) {
        ArrayNode arr = mapper.createArrayNode();
        arr.add(obj);
        return arr;
      }

      // Otherwise recurse through fields
      ObjectNode newObj = mapper.createObjectNode();
      obj
          .propertyStream()
          .forEach((Map.Entry<String, JsonNode> entry) -> newObj.set(
              entry.getKey(),
              ProvToolboxUtils.putTypedObjectsInArrays(entry.getValue(), mapper)));
      return newObj;
    }

    return node;
  }

  private static JsonNode putStringValuesInArray(JsonNode node, ObjectMapper mapper, boolean insideTarget) {

    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;

      if (obj.has("$")) {
        return node;
      }

      obj.propertyStream()
          .forEach((Map.Entry<String, JsonNode> entry) -> {
            String property = entry.getKey();
            JsonNode value = entry.getValue();

            boolean nowInsideTarget = insideTarget
                || Set.of("entity", "activity", "agent").contains(property);

            if (nowInsideTarget) {
              if (value.isTextual()
                  && !property.equals("prov:startTime")
                  && !property.equals("prov:endTime")) {
                ArrayNode arr = mapper.getNodeFactory().arrayNode();
                arr.add(value);
                obj.set(property, arr);
              }
            }
            ProvToolboxUtils.putStringValuesInArray(value, mapper, nowInsideTarget);
          });
    }

    return node;
  }

  public static String serializeDocumentToBase64(Document document, String format) {
    try {
      InteropFramework interop = new InteropFramework();
      String toolboxFormat = normalizeFormat(format);
      Formats.ProvFormat provFormat = interop.getTypeForFormat(toolboxFormat);
      if (provFormat == null) {
        throw new IllegalArgumentException("Unknown PROV format: " + toolboxFormat);
      }
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      interop.writeDocument(outputStream, document, provFormat);
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    } catch (Exception e) {
      throw new IllegalArgumentException("Unable to serialize PROV document.", e);
    }
  }

  private static String normalizeFormat(String format) {
    if (format == null) {
      return "json";
    }
    return switch (format.toLowerCase()) {
      case "rdf", "trig" -> "trig";
      case "xml" -> "xml";
      case "provn" -> "provn";
      case "json" -> "json";
      default -> format.toLowerCase();
    };
  }
}
