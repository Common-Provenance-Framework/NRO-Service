package org.commonprovenance.framework.nro.support.trustedParty;

import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.util.List;

import org.commonprovenance.framework.nro.support.fixture.Pki;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.SignedJWT;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

public record TokenView(Header header, Claims claims, boolean signedByTrustedParty) {

  private static final JsonMapper STRICT = JsonMapper.builder()
      .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  public record Header(String typ, String alg, List<String> x5c, String trustedPartyUri) {
  }

  @JsonIgnoreProperties("iat")
  public record Claims(
      String iss,
      String sub,
      @JsonProperty("doc_digest") String documentDigest,
      @JsonProperty("hash_alg") String hashAlgorithm,
      @JsonProperty("doc_iat") long documentIssuedAt,
      @JsonProperty("org_id") String organizationId) {
  }

  @JsonCreator
  public static TokenView fromJwt(@JsonProperty("jwt") String jwt) {
    try {
      SignedJWT token = SignedJWT.parse(jwt);
      return new TokenView(
          STRICT.readValue(token.getParsedParts()[0].decodeToString(), Header.class),
          STRICT.readValue(token.getParsedParts()[1].decodeToString(), Claims.class),
          token.verify(new ECDSAVerifier((ECPublicKey) Pki.trustedPartyPublicKey())));
    } catch (ParseException | JOSEException e) {
      throw new IllegalArgumentException("Token can not be decoded: " + jwt, e);
    }
  }
}
