package org.commonprovenance.framework.nro.data.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.nro.data.enums.CertificateType;
import org.commonprovenance.framework.nro.data.enums.GraphType;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.commonprovenance.framework.nro.data.model.Document;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class DocumentRepositoryTest {

  @Autowired
  private DocumentRepository documentRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  void save_validDocument_persistsAndLoads() {
    Organization organization = saveOrganization("org-save");
    Certificate certificate = saveCertificate(organization, "cert-save");

    Document document = buildDocument("doc-save", "json", GraphType.GRAPH, organization, certificate);

    documentRepository.save(document);

    Optional<Document> reloaded = documentRepository
        .findByIdAndGraphFormatAndOrganization("doc-save", "json", organization);

    assertThat(reloaded).isPresent();
    assertThat(reloaded.get().getGraphType()).isEqualTo(GraphType.GRAPH);
  }

  @Test
  void findByIdentifierAndGraphFormatAndOrganization_existingDocument_returnsDocument() {
    Organization organization = saveOrganization("org-a");
    Certificate certificate = saveCertificate(organization, "cert-a");
    saveDocument("doc-a", "json", GraphType.GRAPH, organization, certificate);

    Optional<Document> result = documentRepository
        .findByIdAndGraphFormatAndOrganization("doc-a", "json", organization);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo("doc-a");
    assertThat(result.get().getIdentifier()).isEqualTo(buildIdentifier("doc-a", organization));
  }

  @Test
  void findByIdentifierAndGraphFormatAndOrganization_nonExistingDocument_returnsEmpty() {
    Organization organization = saveOrganization("org-missing");

    Optional<Document> result = documentRepository
        .findByIdAndGraphFormatAndOrganization("doc-missing", "json", organization);

    assertThat(result).isEmpty();
  }

  @Test
  void findByIdentifierAndGraphFormatAndGraphTypeAndOrganization_existingDocument_returnsDocument() {
    Organization organization = saveOrganization("org-b");
    Certificate certificate = saveCertificate(organization, "cert-b");
    saveDocument("doc-b", "xml", GraphType.META, organization, certificate);

    Optional<Document> result = documentRepository
        .findByIdentifierAndGraphFormatAndGraphTypeAndOrganization(
            buildIdentifier("doc-b", organization),
            "xml",
            GraphType.META,
            organization);

    assertThat(result).isPresent();
    assertThat(result.get().getGraphFormat()).isEqualTo("xml");
  }

  @Test
  void findByIdentifierAndGraphFormatAndGraphTypeAndOrganization_wrongType_returnsEmpty() {
    Organization organization = saveOrganization("org-c");
    Certificate certificate = saveCertificate(organization, "cert-c");
    saveDocument("doc-c", "xml", GraphType.GRAPH, organization, certificate);

    Optional<Document> result = documentRepository
        .findByIdentifierAndGraphFormatAndGraphTypeAndOrganization(
            "doc-c",
            "xml",
            GraphType.META,
            organization);

    assertThat(result).isEmpty();
  }

  @Test
  void findByIdentifierAndGraphFormatAndGraphTypeAndOrganization_wrongFormat_returnsEmpty() {
    Organization organization = saveOrganization("org-format");
    Certificate certificate = saveCertificate(organization, "cert-format");
    saveDocument("doc-format", "json", GraphType.META, organization, certificate);

    Optional<Document> result = documentRepository
        .findByIdentifierAndGraphFormatAndGraphTypeAndOrganization(
            "doc-format",
            "xml",
            GraphType.META,
            organization);

    assertThat(result).isEmpty();
  }

  @Test
  void findByIdentifierAndGraphFormatAndGraphTypeAndOrganization_wrongOrganization_returnsEmpty() {
    Organization organization = saveOrganization("org-right");
    Organization otherOrganization = saveOrganization("org-wrong");
    Certificate certificate = saveCertificate(organization, "cert-org");
    saveDocument("doc-org", "xml", GraphType.GRAPH, organization, certificate);

    Optional<Document> result = documentRepository
        .findByIdentifierAndGraphFormatAndGraphTypeAndOrganization(
            "doc-org",
            "xml",
            GraphType.GRAPH,
            otherOrganization);

    assertThat(result).isEmpty();
  }

  @Test
  void findByIdentifierAndGraphFormatAndGraphTypeAndOrganization_wrongIdentifier_returnsEmpty() {
    Organization organization = saveOrganization("org-id");
    Certificate certificate = saveCertificate(organization, "cert-id");
    saveDocument("doc-id", "json", GraphType.META, organization, certificate);

    Optional<Document> result = documentRepository
        .findByIdentifierAndGraphFormatAndGraphTypeAndOrganization(
            "doc-id-wrong",
            "json",
            GraphType.META,
            organization);

    assertThat(result).isEmpty();
  }

  @Test
  void findByOrganization_existingOrganization_returnsDocuments() {
    Organization organization = saveOrganization("org-list");
    Organization otherOrganization = saveOrganization("org-other");
    Certificate certificate = saveCertificate(organization, "cert-list");
    Certificate otherCertificate = saveCertificate(otherOrganization, "cert-other");

    saveDocument("doc-1", "json", GraphType.GRAPH, organization, certificate);
    saveDocument("doc-2", "json", GraphType.META, organization, certificate);
    saveDocument("doc-3", "json", GraphType.META, otherOrganization, otherCertificate);

    List<Document> result = documentRepository.findByOrganization(organization);

    assertThat(result)
        .extracting(Document::getId)
        .containsExactlyInAnyOrder("doc-1", "doc-2");
  }

  @Test
  void findByOrganization_noDocuments_returnsEmptyList() {
    Organization organization = saveOrganization("org-empty");

    List<Document> result = documentRepository.findByOrganization(organization);

    assertThat(result).isEmpty();
  }

  @Test
  void findByOrganization_nonExistingOrganization_returnsEmptyList() {
    Organization organization = new Organization();
    organization.setId("org-missing");

    List<Document> result = documentRepository.findByOrganization(organization);

    assertThat(result).isEmpty();
  }

  private Organization saveOrganization(String id) {
    Organization organization = new Organization();
    organization.setId(id);
    entityManager.persist(organization);
    return organization;
  }

  private Certificate saveCertificate(Organization organization, String digest) {
    Certificate certificate = new Certificate();
    certificate.setCertDigest(digest);
    certificate.setCert("cert-body");
    certificate.setCertificateType(CertificateType.CLIENT);
    certificate.setIsRevoked(false);
    certificate.setReceived_on(LocalDateTime.now().minusDays(1));
    certificate.setOrganization(organization);
    entityManager.persist(certificate);
    return certificate;
  }

  private Document saveDocument(
      String id,
      String graphFormat,
      GraphType graphType,
      Organization organization,
      Certificate certificate) {
    Document document = buildDocument(id, graphFormat, graphType, organization, certificate);
    entityManager.persist(document);
    entityManager.flush();
    return document;
  }

  private Document buildDocument(
      String id,
      String graphFormat,
      GraphType graphType,
      Organization organization,
      Certificate certificate) {
    String identifier = buildIdentifier(id, organization);

    Document document = new Document();
    document.setId(id);
    document.setIdentifier(identifier);
    document.setGraphFormat(graphFormat);
    document.setCertificate(certificate);
    document.setOrganization(organization);
    document.setGraphType(graphType);
    document.setGraph("{}");
    document.setCreatedOn(LocalDateTime.now());
    document.setSignature("sig-" + identifier);
    return document;
  }

  private String buildIdentifier(String id, Organization organization) {
    // http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/dc8efed0-0035-4029-9065-8c46667151db
    return "http://localhost:8080/api/v1/organizations/" + organization.getId() + "/documents/" + id;
  }
}
