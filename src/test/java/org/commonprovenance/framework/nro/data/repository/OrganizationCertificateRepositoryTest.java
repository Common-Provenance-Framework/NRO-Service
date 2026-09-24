package org.commonprovenance.framework.nro.data.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.commonprovenance.framework.nro.data.enums.CertificateType;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.commonprovenance.framework.nro.data.model.OrganizationCertificate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class OrganizationCertificateRepositoryTest {

  @Autowired
  private OrganizationCertificateRepository organizationCertificateRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  void findAssociation_return_listof_associations() {
    Certificate certificate = saveCertificate("digest-cert");
    Organization organization = saveOrganization("org");

    saveAssociation(organization, certificate, CertificateType.INTERMEDIATE, false);

    assertThat(organizationCertificateRepository
        .findByOrganizationIdAndCertificateTypeAndIsRevoked("org", CertificateType.INTERMEDIATE, false))
        .hasAtLeastOneElementOfType(OrganizationCertificate.class)
        .singleElement()
        .satisfies(association -> {
          assertThat(association.getCertificate().getCertDigest()).isEqualTo("digest-cert");
          assertThat(association.getOrganization().getId()).isEqualTo("org");
        });
  }

  @Test
  void findAssociation_return_empty_list() {
    Certificate certificate = saveCertificate("digest-cert");
    Organization organization = saveOrganization("org");

    saveAssociation(organization, certificate, CertificateType.INTERMEDIATE, false);

    assertThat(organizationCertificateRepository
        .findByOrganizationIdAndCertificateTypeAndIsRevoked("org", CertificateType.INTERMEDIATE, true))
        .isEmpty();
  }

  @Test
  void sharedCertificate_canBeAssociatedWithTwoOrganizations() {
    Certificate certificate = saveCertificate("shared-cert");
    Organization first = saveOrganization("org-first");
    Organization second = saveOrganization("org-second");

    saveAssociation(first, certificate, CertificateType.INTERMEDIATE, false);
    saveAssociation(second, certificate, CertificateType.INTERMEDIATE, false);

    assertThat(organizationCertificateRepository
        .findByOrganizationIdAndCertificateTypeAndIsRevoked("org-first", CertificateType.INTERMEDIATE, false))
        .hasAtLeastOneElementOfType(OrganizationCertificate.class)
        .singleElement()
        .satisfies(orgCert -> {
          assertThat(orgCert.getCertificate().getCert()).isEqualTo(certificate.getCert());
          assertThat(orgCert.getOrganization().getId()).isEqualTo(first.getId());
        });

    assertThat(organizationCertificateRepository
        .findByOrganizationIdAndCertificateTypeAndIsRevoked("org-second", CertificateType.INTERMEDIATE, false))
        .hasAtLeastOneElementOfType(OrganizationCertificate.class)
        .singleElement()
        .satisfies(orgCert -> {
          assertThat(orgCert.getCertificate().getCert()).isEqualTo(certificate.getCert());
          assertThat(orgCert.getOrganization().getId()).isEqualTo(second.getId());
        });
  }

  @Test
  void findFirstAssociationWithType_return_optionalOfAssociation() {
    Certificate certificate = saveCertificate("digest-cert");
    Organization organization = saveOrganization("org");

    saveAssociation(organization, certificate, CertificateType.INTERMEDIATE, false);

    assertThat(organizationCertificateRepository
        .findFirstByOrganizationIdAndCertificateTypeAndIsRevoked("org", CertificateType.INTERMEDIATE, false))
        .isPresent()
        .get()
        .satisfies(association -> {
          assertThat(association.getCertificate().getCertDigest()).isEqualTo("digest-cert");
          assertThat(association.getOrganization().getId()).isEqualTo("org");
        });
  }

  @Test
  void findAssociationWithType_return_optionalEmpty() {
    Certificate certificate = saveCertificate("digest-cert");
    Organization organization = saveOrganization("org");

    saveAssociation(organization, certificate, CertificateType.INTERMEDIATE, false);

    assertThat(organizationCertificateRepository
        .findByOrganizationIdAndCertificateTypeAndIsRevoked("org", CertificateType.INTERMEDIATE, true))
        .isEmpty();
  }

  @Test
  void organizationCertificate_queriesAreScopedToOrganizationAndTypeAndState() {
    Certificate active = saveCertificate("active-cert");
    Certificate revoked = saveCertificate("revoked-cert");
    Organization organization = saveOrganization("org-scoped");

    saveAssociation(organization, active, CertificateType.CLIENT, false);
    saveAssociation(organization, revoked, CertificateType.CLIENT, true);

    assertThat(organizationCertificateRepository
        .findFirstByOrganizationIdAndCertificateTypeAndIsRevoked("org-scoped", CertificateType.CLIENT, false)
        .map(OrganizationCertificate::getCertificate)
        .map(Certificate::getCertDigest))
        .isPresent()
        .get()
        .isEqualTo("active-cert");

    assertThat(organizationCertificateRepository
        .findByOrganizationIdAndCertificateTypeAndIsRevoked("org-scoped", CertificateType.CLIENT, true))
        .extracting(OrganizationCertificate::getCertificate)
        .extracting(Certificate::getCertDigest)
        .containsExactly("revoked-cert");
  }

  @Test
  void findFirstAssociationWithoutType_return_optionalOfAssociation() {
    Certificate certificate = saveCertificate("digest-cert");
    Organization organization = saveOrganization("org");

    saveAssociation(organization, certificate, CertificateType.INTERMEDIATE, false);

    assertThat(organizationCertificateRepository
        .findFirstByOrganizationIdAndIsRevoked("org", false))
        .isPresent()
        .get()
        .satisfies(association -> {
          assertThat(association.getCertificate().getCertDigest()).isEqualTo("digest-cert");
          assertThat(association.getOrganization().getId()).isEqualTo("org");
        });
  }

  @Test
  void findAssociationWithoutType_return_optionalEmpty() {
    Certificate certificate = saveCertificate("digest-cert");
    Organization organization = saveOrganization("org");

    saveAssociation(organization, certificate, CertificateType.INTERMEDIATE, false);

    assertThat(organizationCertificateRepository
        .findFirstByOrganizationIdAndIsRevoked("org", true))
        .isEmpty();
  }

  @Test
  void organizationCertificate_queriesAreScopedToOrganizationAndState() {
    Certificate active = saveCertificate("active-cert");
    Certificate revoked_fst = saveCertificate("revoked_fst-cert");
    Certificate revoked_snd = saveCertificate("revoked_snd-cert");
    Organization organization = saveOrganization("org-scoped");

    saveAssociation(organization, active, CertificateType.CLIENT, false);
    saveAssociation(organization, revoked_fst, CertificateType.CLIENT, true);
    saveAssociation(organization, revoked_snd, CertificateType.CLIENT, true);

    assertThat(organizationCertificateRepository
        .findFirstByOrganizationIdAndIsRevoked("org-scoped", false)
        .map(OrganizationCertificate::getCertificate)
        .map(Certificate::getCertDigest))
        .isPresent()
        .get()
        .isEqualTo("active-cert");

    assertThat(organizationCertificateRepository
        .findFirstByOrganizationIdAndIsRevoked("org-scoped", true)
        .map(OrganizationCertificate::getCertificate)
        .map(Certificate::getCertDigest))
        .isPresent()
        .get()
        .isEqualTo("revoked_fst-cert");
  }

  @Test
  void organizationCertificate_queriesAreScopedToDigest() {
    Certificate active = saveCertificate("active-cert");
    Certificate revoked = saveCertificate("revoked-cert");
    Organization organization = saveOrganization("org-scoped");

    saveAssociation(organization, active, CertificateType.CLIENT, false);
    saveAssociation(organization, revoked, CertificateType.CLIENT, true);

    assertThat(organizationCertificateRepository
        .findByOrganizationIdAndCertificate_CertDigest("org-scoped", "active-cert")
        .map(OrganizationCertificate::getCertificate)
        .map(Certificate::getCertDigest))
        .isPresent()
        .get()
        .isEqualTo("active-cert");
  }

  private Certificate saveCertificate(String digest) {
    Certificate certificate = new Certificate();
    certificate.setCertDigest(digest);
    certificate.setCert("cert-body");
    entityManager.persist(certificate);
    entityManager.flush();
    return certificate;
  }

  private Organization saveOrganization(String id) {
    Organization organization = new Organization();
    organization.setId(id);
    entityManager.persist(organization);
    entityManager.flush();
    return organization;
  }

  private OrganizationCertificate saveAssociation(
      Organization organization,
      Certificate certificate,
      CertificateType type,
      boolean revoked) {
    OrganizationCertificate association = new OrganizationCertificate();
    association.setOrganization(organization);
    association.setCertificate(certificate);
    association.setCertificateType(type);
    association.setIsRevoked(revoked);
    association.setReceivedOn(LocalDateTime.now().minusDays(1));
    entityManager.persist(association);
    entityManager.flush();
    return association;
  }
}
