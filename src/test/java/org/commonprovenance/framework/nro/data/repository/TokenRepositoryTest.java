package org.commonprovenance.framework.nro.data.repository;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.commonprovenance.framework.nro.data.enums.CertificateType;
import org.commonprovenance.framework.nro.data.enums.GraphType;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.commonprovenance.framework.nro.data.model.Document;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.commonprovenance.framework.nro.data.model.Token;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TokenRepositoryTest {

  @Autowired
  private TokenRepository tokenRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  void findByDocument_existingDocument_returnsTokens() {
    Document documentA = saveDocument("doc-a");
    Document documentB = saveDocument("doc-b");

    saveToken(documentA, "hash-1");
    saveToken(documentA, "hash-2");
    saveToken(documentB, "hash-3");

    List<Token> result = tokenRepository.findByDocument(documentA);

    assertThat(result)
        .extracting(token -> token.getJwt())
        .containsExactlyInAnyOrder("jwt-hash-1", "jwt-hash-2");
  }

  @Test
  void findByDocument_existingDocumentWithoutTokens_returnsEmptyList() {
    Document document = saveDocument("doc-empty");

    List<Token> result = tokenRepository.findByDocument(document);

    assertThat(result).isEmpty();
  }

  @Test
  void findByDocument_nonExistingDocument_returnsEmptyList() {
    Document document = new Document();
    document.setId("missing-doc");

    List<Token> result = tokenRepository.findByDocument(document);

    assertThat(result).isEmpty();
  }

  @Test
  void save_validToken_persistsAndLoads() {
    Document document = saveDocument("doc-save");

    Token token = new Token();
    token.setDocument(document);
    token.setJwt("jwt-save");

    Token saved = tokenRepository.save(token);
    entityManager.flush();
    entityManager.clear();

    assertThat(saved.getId()).isNotNull();

    Token reloaded = tokenRepository.findById(requireNonNull(saved.getId())).orElseThrow();

    assertThat(reloaded.getJwt()).isEqualTo("jwt-save");
    assertThat(reloaded.getDocument().getIdentifier()).isEqualTo(buildIdentifier("doc-save", document.getOrganization()));
    assertThat(reloaded.getDocument().getId()).isEqualTo("doc-save");
  }

  private Document saveDocument(String id) {
    Organization organization = new Organization();
    organization.setId("org-" + id);
    entityManager.persist(organization);

    Certificate certificate = new Certificate();
    certificate.setCertDigest("cert-" + id);
    certificate.setCert("cert-body");
    certificate.setCertificateType(CertificateType.CLIENT);
    certificate.setIsRevoked(false);
    certificate.setReceived_on(LocalDateTime.now().minusDays(1));
    certificate.setOrganization(organization);
    entityManager.persist(certificate);

    Document document = new Document();
    document.setId(id);
    document.setIdentifier(buildIdentifier(id, organization));
    document.setGraphFormat("json");
    document.setCertificate(certificate);
    document.setOrganization(organization);
    document.setGraphType(GraphType.GRAPH);
    document.setGraph("{}");
    document.setCreatedOn(LocalDateTime.now());
    document.setSignature("sig-" + id);
    entityManager.persist(document);

    entityManager.flush();
    return document;
  }

  private Token saveToken(Document document, String hash) {
    Token token = new Token();
    token.setDocument(document);
    token.setJwt("jwt-" + hash);
    entityManager.persist(token);
    entityManager.flush();
    return token;
  }

  private String buildIdentifier(String id, Organization organization) {
    // http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/dc8efed0-0035-4029-9065-8c46667151db
    return "http://localhost:8080/api/v1/organizations/" + organization.getId() + "/documents/" + id;
  }
}
