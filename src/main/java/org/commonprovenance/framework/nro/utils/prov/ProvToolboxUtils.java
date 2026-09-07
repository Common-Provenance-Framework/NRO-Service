package org.commonprovenance.framework.nro.utils.prov;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.interop.Formats;

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
      ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded.getBytes(StandardCharsets.UTF_8));
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
