package org.commonprovenance.framework.nro.support.fixture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Signature;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.PropertyPlaceholderHelper;

public record ProvDocumentData(OrganizationData organization, String bundleId, String metaBundleId, String json, String signature) {

  public static final String GRAPH_FORMAT = "json";
  public static final LocalDateTime CREATED_ON = LocalDateTime.of(2026, 1, 1, 10, 0, 30);

  private static final String STORE_URL = "http://cpf-store.example.org/api/v1/";
  private static final String TEMPLATE = "fixtures/documents/main-activity-only.json";
  private static final PropertyPlaceholderHelper PLACEHOLDERS = new PropertyPlaceholderHelper("${", "}", null, null, false);

  public static ProvDocumentData newDocumentOf(OrganizationData organization) {
    String bundleId = UUID.randomUUID().toString();
    String metaBundleId = UUID.randomUUID().toString();
    String json = PLACEHOLDERS.replacePlaceholders(template(), Map.of(
        "documentsUrl", documentsUrl(organization),
        "metaBundlesUrl", STORE_URL + "documents/meta/",
        "bundleId", bundleId,
        "metaBundleId", metaBundleId)::get);
    return new ProvDocumentData(organization, bundleId, metaBundleId, json, signed(json, organization));
  }

  public String bundleIdentifier() {
    return documentsUrl(organization) + bundleId;
  }

  public String base64() {
    return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
  }

  public String digest() {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  public String createdOn() {
    return CREATED_ON.toString();
  }

  public long createdOnEpochSecond() {
    return CREATED_ON.toEpochSecond(ZoneOffset.UTC);
  }

  private static String documentsUrl(OrganizationData organization) {
    return STORE_URL + "organizations/" + organization.id() + "/documents/";
  }

  private static String signed(String json, OrganizationData organization) {
    try {
      Signature signature = Signature.getInstance("SHA256withECDSA");
      signature.initSign(organization.privateKey());
      signature.update(json.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(signature.sign());
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String template() {
    try {
      return new ClassPathResource(TEMPLATE).getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Can not read PROV document template '" + TEMPLATE + "'", e);
    }
  }
}
