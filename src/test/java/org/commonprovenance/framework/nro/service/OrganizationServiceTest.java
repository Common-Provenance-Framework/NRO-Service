package org.commonprovenance.framework.nro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.commonprovenance.framework.nro.api.Organization.StoreCertOrganizationDTO;
import org.commonprovenance.framework.nro.config.AppProperties;
import org.commonprovenance.framework.nro.data.enums.CertificateType;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.commonprovenance.framework.nro.data.model.OrganizationCertificate;
import org.commonprovenance.framework.nro.data.records.OrganizationAndCertificates;
import org.commonprovenance.framework.nro.data.repository.CertificateRepository;
import org.commonprovenance.framework.nro.data.repository.OrganizationCertificateRepository;
import org.commonprovenance.framework.nro.data.repository.OrganizationRepository;
import org.commonprovenance.framework.nro.exceptions.CertificateVerificationException;
import org.commonprovenance.framework.nro.exceptions.OrganizationAlreadyExistsException;
import org.commonprovenance.framework.nro.exceptions.OrganizationIdMismatchException;
import org.commonprovenance.framework.nro.exceptions.OrganizationNotFoundException;
import org.commonprovenance.framework.nro.utils.TestDataFactory;
import org.commonprovenance.framework.nro.utils.TrustedPartyUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private CertificateRepository certificateRepository;

  @Mock
  private OrganizationCertificateRepository organizationCertificateRepository;

  @Mock
  private AppProperties appProperties;

  private OrganizationService organizationService;

  @BeforeEach
  void setUp() {
    organizationService = new OrganizationService(
        organizationRepository,
        certificateRepository,
        organizationCertificateRepository,
        appProperties);
  }

  @Test
  void getAllOrganizations_existingOrganizations_returnOnlyActiveCertificates() {
    Organization organization = new Organization();
    organization.setId("org-1");
    Certificate certificate_1 = new Certificate();
    certificate_1.setCertDigest("shared-1");
    Certificate certificate_2 = new Certificate();
    certificate_2.setCertDigest("shared-2");

    OrganizationCertificate active = association(organization, certificate_1, false);
    OrganizationCertificate revoked = association(organization, certificate_2, true);

    when(organizationRepository.findAll()).thenReturn(List.of(active.getOrganization()));
    when(organizationCertificateRepository.findByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1",
        CertificateType.CLIENT,
        true)).thenReturn(List.of(revoked));
    when(organizationCertificateRepository.findFirstByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1",
        CertificateType.CLIENT,
        false)).thenReturn(Optional.of(active));

    List<OrganizationAndCertificates> result = organizationService.getAllOrganizations();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).organization()).isSameAs(organization);
    assertThat(result.get(0).activeCertificate().getCertificate()).isSameAs(active.getCertificate());
    assertThat(result.get(0).revokedCertificates()).isNull();
  }

  @Test
  void getOrganization_existingOrganization_returnsCertificates() {
    Organization organization = new Organization();
    organization.setId("org-1");
    Certificate certificate_1 = new Certificate();
    certificate_1.setCertDigest("shared-1");
    Certificate certificate_2 = new Certificate();
    certificate_2.setCertDigest("shared-2");

    OrganizationCertificate active = association(organization, certificate_1, false);
    OrganizationCertificate revoked = association(organization, certificate_2, true);

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(organizationCertificateRepository.findByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1",
        CertificateType.CLIENT,
        true)).thenReturn(List.of(revoked));
    when(organizationCertificateRepository.findFirstByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1",
        CertificateType.CLIENT,
        false)).thenReturn(Optional.of(active));

    OrganizationAndCertificates result = organizationService.getOrganization("org-1");

    assertThat(result.organization()).isSameAs(organization);
    assertThat(result.activeCertificate()).isSameAs(active);
    assertThat(result.revokedCertificates()).singleElement().isSameAs(revoked);
  }

  @Test
  void getOrganization_missingOrganization_throwsOrganizationNotFoundException() {
    when(organizationRepository.findById("missing-org")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> organizationService.getOrganization("missing-org"))
        .isInstanceOf(OrganizationNotFoundException.class)
        .hasMessageContaining("missing-org");
  }

  @Test
  void storeCertToOrganization_idMismatch_throwsOrganizationIdMismatchException() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");

    assertThatThrownBy(() -> organizationService.storeCertToOrganization("org-2", body))
        .isInstanceOf(OrganizationIdMismatchException.class)
        .hasMessageContaining("org-2");
  }

  @Test
  void storeCertToOrganization_existingOrganization_throwsOrganizationAlreadyExistsException() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");
    Organization organization = new Organization();
    organization.setId("org-1");

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));

    assertThatThrownBy(() -> organizationService.storeCertToOrganization("org-1", body))
        .isInstanceOf(OrganizationAlreadyExistsException.class)
        .hasMessageContaining("org-1");
  }

  @Test
  void storeCertToOrganization_chainFail_throwsCertificateVerificationException() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");

    when(organizationRepository.findById("org-1")).thenReturn(Optional.empty());
    when(appProperties.loadTrustedCertificates()).thenReturn(List.of("trusted"));

    try (MockedStatic<TrustedPartyUtils> utils = mockStatic(TrustedPartyUtils.class)) {
      utils.when(() -> TrustedPartyUtils.verifyChainOfTrust(
          body.getClientCertificate(),
          body.getIntermediateCertificates(),
          List.of("trusted"))).thenReturn(false);

      assertThatThrownBy(() -> organizationService.storeCertToOrganization("org-1", body))
          .isInstanceOf(CertificateVerificationException.class);
    }
  }

  @Test
  void storeCertToOrganization_validRequest_savesOrganizationAndCertificates() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");

    when(organizationRepository.findById("org-1")).thenReturn(Optional.empty());
    when(appProperties.loadTrustedCertificates()).thenReturn(List.of("trusted"));

    try (MockedStatic<TrustedPartyUtils> utils = mockStatic(TrustedPartyUtils.class)) {
      utils.when(() -> TrustedPartyUtils.verifyChainOfTrust(
          body.getClientCertificate(),
          body.getIntermediateCertificates(),
          List.of("trusted"))).thenReturn(true);
      utils.when(() -> TrustedPartyUtils.computeCertificateDigest("client-cert"))
          .thenReturn("digest-client");
      utils.when(() -> TrustedPartyUtils.computeCertificateDigest("intermediate-1"))
          .thenReturn("digest-int-1");

      organizationService.storeCertToOrganization("org-1", body);
    }

    verify(organizationRepository).save(any(Organization.class));

    ArgumentCaptor<Certificate> certCaptor = ArgumentCaptor.forClass(Certificate.class);
    verify(certificateRepository, atLeast(2)).save(certCaptor.capture());

    List<Certificate> saved = certCaptor.getAllValues();
    assertThat(saved)
        .extracting(Certificate::getCertDigest)
        .contains("digest-client", "digest-int-1");
  }

  @Test
  void updateCertificates_idMismatch_throwsOrganizationIdMismatchException() {
    StoreCertOrganizationDTO body = buildStoreDto("org-body");

    assertThatThrownBy(() -> organizationService.updateCertificates("org-uri", body))
        .isInstanceOf(OrganizationIdMismatchException.class)
        .hasMessageContaining("org-uri");
  }

  @Test
  void updateCertificates_missingOrganization_throwsOrganizationNotFoundException() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");
    when(organizationRepository.findById("org-1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> organizationService.updateCertificates("org-1", body))
        .isInstanceOf(OrganizationNotFoundException.class)
        .hasMessageContaining("org-1");
  }

  @Test
  void updateCertificates_chainFail_throwsCertificateVerificationException() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");
    Organization organization = new Organization();
    organization.setId("org-1");

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(appProperties.loadTrustedCertificates()).thenReturn(List.of("trusted"));

    try (MockedStatic<TrustedPartyUtils> utils = mockStatic(TrustedPartyUtils.class)) {
      utils.when(() -> TrustedPartyUtils.verifyChainOfTrust(
          body.getClientCertificate(),
          body.getIntermediateCertificates(),
          List.of("trusted"))).thenReturn(false);

      assertThatThrownBy(() -> organizationService.updateCertificates("org-1", body))
          .isInstanceOf(CertificateVerificationException.class);
    }
  }

  @Test
  void updateCertificates_validRequest_revokesAndStoresCertificates() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");
    Organization organization = new Organization();
    organization.setId("org-1");

    Certificate existingClient = new Certificate();
    existingClient.setCertDigest("digest-client-ex");
    OrganizationCertificate existingClientAssociation = association(organization, existingClient, false);
    existingClientAssociation.setReceivedOn(LocalDateTime.now());

    Certificate existingIntermediate = new Certificate();
    existingIntermediate.setCertDigest("digest-int-ex");

    OrganizationCertificate existingInterAssociation = association(organization, existingIntermediate, true);
    existingInterAssociation.setCertificateType(CertificateType.INTERMEDIATE);
    existingInterAssociation.setReceivedOn(LocalDateTime.now());

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(appProperties.loadTrustedCertificates()).thenReturn(List.of("trusted"));
    when(organizationCertificateRepository.findByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.CLIENT, false)).thenReturn(List.of(existingClientAssociation));
    when(organizationCertificateRepository.findByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.INTERMEDIATE, false)).thenReturn(List.of(existingInterAssociation));
    when(organizationCertificateRepository.save(any(OrganizationCertificate.class)))
        .thenAnswer(invocation -> ((OrganizationCertificate) invocation.getArgument(0)));
    when(certificateRepository.save(any(Certificate.class)))
        .thenAnswer(invocation -> ((Certificate) invocation.getArgument(0)));

    when(certificateRepository.findByCertDigest(anyString()))
        .then(invocation -> {
          switch (invocation.getArgument(0).toString()) {
            case "digest-client-ex":
              return Optional.of(existingClient);
            case "digest-int-ex":
              return Optional.of(existingIntermediate);
            default:
              return Optional.empty();
          }
        });
    when(organizationCertificateRepository.findByOrganizationIdAndCertificate_CertDigest(anyString(), anyString()))
        .then(invocation -> {
          if (!invocation.getArgument(0).toString().equals("org-1"))
            return Optional.empty();

          switch (invocation.getArgument(1).toString()) {
            case "digest-client-ex":
              return Optional.of(existingClientAssociation);
            case "digest-int-ex":
              return Optional.of(existingInterAssociation);
            default:
              return Optional.empty();
          }
        });

    try (MockedStatic<TrustedPartyUtils> utils = mockStatic(TrustedPartyUtils.class)) {
      utils.when(() -> TrustedPartyUtils.verifyChainOfTrust(
          body.getClientCertificate(),
          body.getIntermediateCertificates(),
          List.of("trusted"))).thenReturn(true);
      utils.when(() -> TrustedPartyUtils.computeCertificateDigest("client-cert"))
          .thenReturn("digest-client");
      utils.when(() -> TrustedPartyUtils.computeCertificateDigest("intermediate-1"))
          .thenReturn("intermediate-1");

      organizationService.updateCertificates("org-1", body);
    }

    ArgumentCaptor<OrganizationCertificate> orgCertCaptor = ArgumentCaptor.forClass(OrganizationCertificate.class);
    // twice when revoke two existing associations
    // twice when create two new associations
    verify(organizationCertificateRepository, atLeast(4)).save(orgCertCaptor.capture());
    List<OrganizationCertificate> savedAssociations = orgCertCaptor.getAllValues();
    assertThat(savedAssociations)
        .extracting(OrganizationCertificate::getCertificate)
        .extracting(Certificate::getCertDigest)
        .contains("digest-client-ex", "digest-int-ex", "digest-client", "intermediate-1");

    ArgumentCaptor<Certificate> certCaptor = ArgumentCaptor.forClass(Certificate.class);
    // twice when create two new certificates
    verify(certificateRepository, atLeast(2)).save(certCaptor.capture());
    List<Certificate> saved = certCaptor.getAllValues();
    assertThat(saved)
        .extracting(Certificate::getCertDigest)
        .contains("digest-client", "intermediate-1");
  }

  @Test
  void updateCertificates_existingIntermediate_unrevokesExistingCertificate() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");
    Organization organization = new Organization();
    organization.setId("org-1");

    Certificate existingClient = new Certificate();
    existingClient.setCertDigest("digest-client-ex");
    OrganizationCertificate existingClientAssociation = association(organization, existingClient, false);
    existingClientAssociation.setReceivedOn(LocalDateTime.now());

    Certificate existingIntermediate = new Certificate();
    existingIntermediate.setCertDigest("digest-int-ex");

    OrganizationCertificate existingInterAssociation = association(organization, existingIntermediate, true);
    existingInterAssociation.setCertificateType(CertificateType.INTERMEDIATE);
    existingInterAssociation.setIsRevoked(true);
    existingInterAssociation.setReceivedOn(LocalDateTime.now());

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(appProperties.loadTrustedCertificates()).thenReturn(List.of("trusted"));
    when(organizationCertificateRepository.findByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.CLIENT, false)).thenReturn(List.of(existingClientAssociation));
    when(organizationCertificateRepository.findByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.INTERMEDIATE, false)).thenReturn(List.of());
    when(certificateRepository.findByCertDigest(anyString()))
        .then(invocation -> {
          switch (invocation.getArgument(0).toString()) {
            case "digest-client-ex":
              return Optional.of(existingClient);
            case "digest-int-ex":
              return Optional.of(existingIntermediate);
            default:
              return Optional.empty();
          }
        });

    when(organizationCertificateRepository.findByOrganizationIdAndCertificate_CertDigest(anyString(), anyString()))
        .then(invocation -> {
          if (!invocation.getArgument(0).toString().equals("org-1"))
            return Optional.empty();

          switch (invocation.getArgument(1).toString()) {
            case "digest-client-ex":
              return Optional.of(existingClientAssociation);
            case "digest-int-ex":
              return Optional.of(existingInterAssociation);
            default:
              return Optional.empty();
          }
        });

    when(organizationCertificateRepository.save(any(OrganizationCertificate.class)))
        .thenAnswer(invocation -> ((OrganizationCertificate) invocation.getArgument(0)));
    when(certificateRepository.save(any(Certificate.class)))
        .thenAnswer(invocation -> ((Certificate) invocation.getArgument(0)));

    try (MockedStatic<TrustedPartyUtils> utils = mockStatic(TrustedPartyUtils.class)) {
      utils.when(() -> TrustedPartyUtils.verifyChainOfTrust(
          body.getClientCertificate(),
          body.getIntermediateCertificates(),
          List.of("trusted"))).thenReturn(true);
      utils.when(() -> TrustedPartyUtils.computeCertificateDigest("client-cert"))
          .thenReturn("digest-client");
      utils.when(() -> TrustedPartyUtils.computeCertificateDigest("intermediate-1"))
          .thenReturn("digest-int-ex");

      organizationService.updateCertificates("org-1", body);
    }
    assertThat(existingInterAssociation.getIsRevoked()).isFalse();
  }

  @Test
  void updateCertificates_alreadyRevokedCertificates_remainRevoked() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");
    Organization organization = new Organization();
    organization.setId("org-1");

    Certificate existingClient = new Certificate();
    existingClient.setCertDigest("digest-client-ex");
    OrganizationCertificate existingClientAssociation = association(organization, existingClient, false);
    existingClientAssociation.setIsRevoked(true);
    existingClientAssociation.setReceivedOn(LocalDateTime.now());

    Certificate existingIntermediate = new Certificate();
    existingIntermediate.setCertDigest("digest-int-ex");

    OrganizationCertificate existingInterAssociation = association(organization, existingIntermediate, true);
    existingInterAssociation.setCertificateType(CertificateType.INTERMEDIATE);
    existingInterAssociation.setIsRevoked(true);
    existingInterAssociation.setReceivedOn(LocalDateTime.now());

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(appProperties.loadTrustedCertificates()).thenReturn(List.of("trusted"));

    when(organizationCertificateRepository.findByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.CLIENT, false)).thenReturn(List.of());
    when(organizationCertificateRepository.findByOrganizationIdAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.INTERMEDIATE, false)).thenReturn(List.of());
    when(certificateRepository.findByCertDigest("digest-int-1")).thenReturn(Optional.empty());
    when(certificateRepository.findByCertDigest("digest-client")).thenReturn(Optional.empty());

    try (MockedStatic<TrustedPartyUtils> utils = mockStatic(TrustedPartyUtils.class)) {
      utils.when(() -> TrustedPartyUtils.verifyChainOfTrust(
          body.getClientCertificate(),
          body.getIntermediateCertificates(),
          List.of("trusted"))).thenReturn(true);
      utils.when(() -> TrustedPartyUtils.computeCertificateDigest("client-cert"))
          .thenReturn("digest-client");
      utils.when(() -> TrustedPartyUtils.computeCertificateDigest("intermediate-1"))
          .thenReturn("digest-int-1");

      organizationService.updateCertificates("org-1", body);
    }

    assertThat(existingClientAssociation.getIsRevoked()).isTrue();
    assertThat(existingInterAssociation.getIsRevoked()).isTrue();
  }

  @Test
  void storeCertToOrganization_sameIntermediateForTwoOrganizations_reusesCertificate() {
    StoreCertOrganizationDTO body = TestDataFactory.storeCertRequest();
    when(organizationRepository.findById("org-1")).thenReturn(Optional.empty());
    when(organizationRepository.findById("org-2")).thenReturn(Optional.empty());
    when(appProperties.loadTrustedCertificates()).thenReturn(List.of("trusted"));
    Map<String, Certificate> certificates = new HashMap<>();
    when(certificateRepository.findByCertDigest(any(String.class)))
        .thenAnswer(invocation -> Optional.ofNullable(certificates.get(invocation.getArgument(0))));
    when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> {
      Certificate certificate = invocation.getArgument(0);
      certificates.put(certificate.getCertDigest(), certificate);
      return certificate;
    });
    when(organizationCertificateRepository.save(any(OrganizationCertificate.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    try (MockedStatic<TrustedPartyUtils> utils = mockStatic(TrustedPartyUtils.class)) {
      utils.when(() -> TrustedPartyUtils.verifyChainOfTrust(
          body.getClientCertificate(), body.getIntermediateCertificates(), List.of("trusted")))
          .thenReturn(true);
      utils.when(() -> TrustedPartyUtils.computeCertificateDigest("client-cert"))
          .thenReturn("client-digest");
      utils.when(() -> TrustedPartyUtils.computeCertificateDigest("intermediate-1"))
          .thenReturn("shared-digest");

      organizationService.storeCertToOrganization("org-1", body);

      body.setId("org-2");
      organizationService.storeCertToOrganization("org-2", body);
    }

    ArgumentCaptor<OrganizationCertificate> associationCaptor = ArgumentCaptor.forClass(OrganizationCertificate.class);
    verify(organizationCertificateRepository, atLeast(4)).save(associationCaptor.capture());
    assertThat(associationCaptor.getAllValues()).hasSize(4);

    assertThat(
        associationCaptor.getAllValues().stream()
            .map(OrganizationCertificate::getCertificate)
            .map(Certificate::getCertDigest)
            .distinct())
        .hasSize(2);

    ArgumentCaptor<Certificate> certificateCaptor = ArgumentCaptor.forClass(Certificate.class);
    verify(certificateRepository, atLeast(2)).save(certificateCaptor.capture());
    assertThat(certificateCaptor.getAllValues()).extracting(Certificate::getCertDigest)
        .containsExactly("client-digest", "shared-digest");
  }

  @Test
  void revokeAllStoredCertificates_onlyChangesSelectedOrganization() {
    OrganizationCertificate first = association("org-a", "shared", false);
    OrganizationCertificate second = association("org-b", "shared", false);
    when(organizationCertificateRepository
        .findByOrganizationIdAndCertificateTypeAndIsRevoked("org-a", CertificateType.CLIENT, false))
        .thenReturn(List.of(first));
    when(organizationCertificateRepository
        .findByOrganizationIdAndCertificateTypeAndIsRevoked("org-a", CertificateType.INTERMEDIATE, false))
        .thenReturn(List.of());

    organizationService.revokeAllStoredCertificates("org-a");

    assertThat(first.getIsRevoked()).isTrue();
    assertThat(second.getIsRevoked()).isFalse();
  }

  @Test
  void storeCertToOrganization_existingOrganization_rejectsRequest() {
    StoreCertOrganizationDTO body = TestDataFactory.storeCertRequest();
    Organization organization = new Organization();
    organization.setId("org-1");
    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));

    assertThatThrownBy(() -> organizationService.storeCertToOrganization("org-1", body))
        .isInstanceOf(OrganizationAlreadyExistsException.class);
  }

  @Test
  void storeCertToOrganization_idMismatch_rejectsRequest() {
    StoreCertOrganizationDTO body = TestDataFactory.storeCertRequest();

    assertThatThrownBy(() -> organizationService.storeCertToOrganization("other-org", body))
        .isInstanceOf(OrganizationIdMismatchException.class);
  }

  private Certificate existingCertificate(String digest) {
    Certificate certificate = new Certificate();
    certificate.setCertDigest(digest);
    certificate.setCert("cert-body");
    return certificate;
  }

  private OrganizationCertificate association(String organizationId, String digest, boolean revoked) {
    Organization organization = new Organization();
    organization.setId(organizationId);
    OrganizationCertificate association = new OrganizationCertificate();
    association.setOrganization(organization);
    association.setCertificate(existingCertificate(digest));
    association.setCertificateType(CertificateType.CLIENT);
    association.setIsRevoked(revoked);
    return association;
  }

  private OrganizationCertificate association(Organization organization, Certificate cert, boolean revoked) {

    OrganizationCertificate association = new OrganizationCertificate();
    association.setOrganization(organization);
    association.setCertificate(cert);
    association.setCertificateType(CertificateType.CLIENT);
    association.setIsRevoked(revoked);
    return association;
  }

  private StoreCertOrganizationDTO buildStoreDto(String orgId) {
    StoreCertOrganizationDTO body = TestDataFactory.storeCertRequest();
    body.setId(orgId);
    return body;
  }
}
