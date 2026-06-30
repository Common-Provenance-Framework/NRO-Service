package org.commonprovenance.framework.nro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.nro.api.Organization.StoreCertOrganizationDTO;
import org.commonprovenance.framework.nro.config.AppProperties;
import org.commonprovenance.framework.nro.data.enums.CertificateType;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.commonprovenance.framework.nro.data.records.OrganizationAndCertificates;
import org.commonprovenance.framework.nro.data.repository.CertificateRepository;
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
  private AppProperties appProperties;

  private OrganizationService organizationService;

  @BeforeEach
  void setUp() {
    organizationService = new OrganizationService(organizationRepository, certificateRepository, appProperties);
  }

  @Test
  void getAllOrganizations_existingOrganizations_returnsActiveCertificates() {
    Organization organization = new Organization();
    organization.setOrgName("org-1");
    Certificate activeCert = new Certificate();
    activeCert.setCertDigest("digest-1");

    when(organizationRepository.findAll()).thenReturn(List.of(organization));
    when(certificateRepository.findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
        "org-1",
        CertificateType.CLIENT,
        true)).thenReturn(List.of());
    when(certificateRepository.findFirstByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
        "org-1",
        CertificateType.CLIENT,
        false)).thenReturn(activeCert);

    List<OrganizationAndCertificates> result = organizationService.getAllOrganizations();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).organization()).isSameAs(organization);
    assertThat(result.get(0).activeCertificate()).isSameAs(activeCert);
    assertThat(result.get(0).revokedCertificates()).isNull();
  }

  @Test
  void getOrganization_existingOrganization_returnsCertificates() {
    Organization organization = new Organization();
    organization.setOrgName("org-1");
    Certificate activeCert = new Certificate();
    activeCert.setCertDigest("digest-1");
    Certificate revokedCert = new Certificate();
    revokedCert.setCertDigest("digest-2");

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(certificateRepository.findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
        "org-1",
        CertificateType.CLIENT,
        true)).thenReturn(List.of(revokedCert));
    when(certificateRepository.findFirstByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
        "org-1",
        CertificateType.CLIENT,
        false)).thenReturn(activeCert);

    OrganizationAndCertificates result = organizationService.getOrganization("org-1");

    assertThat(result.organization()).isSameAs(organization);
    assertThat(result.activeCertificate()).isSameAs(activeCert);
    assertThat(result.revokedCertificates()).containsExactly(revokedCert);
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
    StoreCertOrganizationDTO body = buildStoreDto("org-body");

    assertThatThrownBy(() -> organizationService.storeCertToOrganization("org-uri", body))
        .isInstanceOf(OrganizationIdMismatchException.class)
        .hasMessageContaining("org-uri");
  }

  @Test
  void storeCertToOrganization_existingOrganization_throwsOrganizationAlreadyExistsException() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");
    Organization organization = new Organization();
    organization.setOrgName("org-1");

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
        .extracting(Certificate::getCertificateType)
        .contains(CertificateType.CLIENT, CertificateType.INTERMEDIATE);
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
    organization.setOrgName("org-1");

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
    organization.setOrgName("org-1");

    Certificate existingClient = new Certificate();
    existingClient.setCertificateType(CertificateType.CLIENT);
    existingClient.setIsRevoked(false);
    existingClient.setReceived_on(LocalDateTime.now());

    Certificate existingIntermediate = new Certificate();
    existingIntermediate.setCertificateType(CertificateType.INTERMEDIATE);
    existingIntermediate.setIsRevoked(false);
    existingIntermediate.setReceived_on(LocalDateTime.now());

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(appProperties.loadTrustedCertificates()).thenReturn(List.of("trusted"));
    when(certificateRepository.findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.CLIENT, false)).thenReturn(List.of(existingClient));
    when(certificateRepository.findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.INTERMEDIATE, false)).thenReturn(List.of(existingIntermediate));
    when(certificateRepository.findByCertDigest("digest-int-1")).thenReturn(Optional.empty());

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

    ArgumentCaptor<Certificate> certCaptor = ArgumentCaptor.forClass(Certificate.class);
    verify(certificateRepository, atLeast(3)).save(certCaptor.capture());

    List<Certificate> saved = certCaptor.getAllValues();
    assertThat(saved)
        .extracting(Certificate::getIsRevoked)
        .contains(true, false);
  }

  @Test
  void updateCertificates_existingIntermediate_unrevokesExistingCertificate() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");
    Organization organization = new Organization();
    organization.setOrgName("org-1");

    Certificate existingClient = new Certificate();
    existingClient.setCertificateType(CertificateType.CLIENT);
    existingClient.setIsRevoked(false);
    existingClient.setReceived_on(LocalDateTime.now());

    Certificate existingIntermediate = new Certificate();
    existingIntermediate.setCertDigest("digest-int-1");
    existingIntermediate.setCertificateType(CertificateType.INTERMEDIATE);
    existingIntermediate.setIsRevoked(true);
    existingIntermediate.setReceived_on(LocalDateTime.now());

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(appProperties.loadTrustedCertificates()).thenReturn(List.of("trusted"));
    when(certificateRepository.findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.CLIENT, false)).thenReturn(List.of(existingClient));
    when(certificateRepository.findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.INTERMEDIATE, false)).thenReturn(List.of());
    when(certificateRepository.findByCertDigest("digest-int-1"))
        .thenReturn(Optional.of(existingIntermediate));

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

    assertThat(existingIntermediate.getIsRevoked()).isFalse();
  }

  @Test
  void updateCertificates_alreadyRevokedCertificates_remainRevoked() {
    StoreCertOrganizationDTO body = buildStoreDto("org-1");
    Organization organization = new Organization();
    organization.setOrgName("org-1");

    Certificate revokedClient = new Certificate();
    revokedClient.setCertificateType(CertificateType.CLIENT);
    revokedClient.setIsRevoked(true);

    Certificate revokedIntermediate = new Certificate();
    revokedIntermediate.setCertificateType(CertificateType.INTERMEDIATE);
    revokedIntermediate.setIsRevoked(true);

    when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
    when(appProperties.loadTrustedCertificates()).thenReturn(List.of("trusted"));
    when(certificateRepository.findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.CLIENT, false)).thenReturn(List.of());
    when(certificateRepository.findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
        "org-1", CertificateType.INTERMEDIATE, false)).thenReturn(List.of());
    when(certificateRepository.findByCertDigest("digest-int-1")).thenReturn(Optional.empty());

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

    assertThat(revokedClient.getIsRevoked()).isTrue();
    assertThat(revokedIntermediate.getIsRevoked()).isTrue();
  }

  private StoreCertOrganizationDTO buildStoreDto(String orgId) {
    StoreCertOrganizationDTO body = TestDataFactory.storeCertRequest();
    body.setOrganizationId(orgId);
    return body;
  }
}
