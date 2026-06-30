package org.commonprovenance.framework.nro.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.commonprovenance.framework.nro.api.Organization.StoreCertOrganizationDTO;
import org.commonprovenance.framework.nro.config.AppProperties;
import org.commonprovenance.framework.nro.data.enums.CertificateType;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.commonprovenance.framework.nro.data.records.OrganizationAndCertificates;
import org.commonprovenance.framework.nro.data.records.SortedCertificates;
import org.commonprovenance.framework.nro.data.repository.CertificateRepository;
import org.commonprovenance.framework.nro.data.repository.OrganizationRepository;
import org.commonprovenance.framework.nro.exceptions.CertificateVerificationException;
import org.commonprovenance.framework.nro.exceptions.OrganizationAlreadyExistsException;
import org.commonprovenance.framework.nro.exceptions.OrganizationIdMismatchException;
import org.commonprovenance.framework.nro.exceptions.OrganizationNotFoundException;
import org.commonprovenance.framework.nro.utils.TrustedPartyUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final CertificateRepository certificateRepository;
  private final AppProperties appProperties;

  public OrganizationService(OrganizationRepository organizationRepository,
      CertificateRepository certificateRepository,
      AppProperties appProperties) {
    this.organizationRepository = organizationRepository;
    this.certificateRepository = certificateRepository;
    this.appProperties = appProperties;
  }

  @Transactional(readOnly = true)
  public List<OrganizationAndCertificates> getAllOrganizations() {
    List<Organization> organizations = organizationRepository.findAll();
    List<OrganizationAndCertificates> result = new ArrayList<>();

    for (Organization org : organizations) {
      SortedCertificates sorted = getSortedCertificates(org.getOrgName());

      result.add(new OrganizationAndCertificates(
          org,
          sorted.activeCertificate(),
          null // We don't need revoked certificates for all organizations
      ));
    }

    return result;
  }

  // This work also for getAllCertificates
  @Transactional(readOnly = true)
  public OrganizationAndCertificates getOrganization(
      @NonNull String organizationName) {
    Organization organization = organizationRepository
        .findById(organizationName)
        .orElseThrow(() -> new OrganizationNotFoundException(organizationName));

    SortedCertificates sortedCertificates = getSortedCertificates(organizationName);

    return new OrganizationAndCertificates(
        organization,
        sortedCertificates.activeCertificate(),
        sortedCertificates.revokedCertificates());
  }

  @Transactional
  public void updateCertificates(
      @NonNull String organizationName,
      StoreCertOrganizationDTO body) {
    // Checks of request body are done in Controller using Jakarta validation

    if (!Objects.equals(organizationName, body.getOrganizationId())) {
      throw new OrganizationIdMismatchException(organizationName);
    }

    organizationRepository
        .findById(organizationName)
        .orElseThrow(() -> new OrganizationNotFoundException(organizationName));

    if (!TrustedPartyUtils.verifyChainOfTrust(
        body.getClientCertificate(),
        body.getIntermediateCertificates(),
        appProperties.loadTrustedCertificates())) {
      throw new CertificateVerificationException("Could not verify the chain of trust for the provided certificates");
    }

    revokeAndUpdateCertifacates(organizationName, body.getClientCertificate(), body.getIntermediateCertificates());
  }

  @Transactional
  public void storeCertToOrganization(
      @NonNull String organizationName,
      StoreCertOrganizationDTO body) {
    // Checks of request body are done in Controller using Jakarta validation

    if (!Objects.equals(organizationName, body.getOrganizationId())) {
      throw new OrganizationIdMismatchException(organizationName);
    }

    organizationRepository.findById(organizationName)
        .ifPresent(org -> {
          throw new OrganizationAlreadyExistsException(
              "Organization with id [" + organizationName + "] already exists");
        });

    if (!TrustedPartyUtils.verifyChainOfTrust(
        body.getClientCertificate(),
        body.getIntermediateCertificates(),
        appProperties.loadTrustedCertificates())) {
      throw new CertificateVerificationException("Could not verify the chain of trust for the provided certificates");
    }

    storeOrganizationAndCerts(organizationName, body.getClientCertificate(), body.getIntermediateCertificates());
  }

  @Transactional
  protected void storeOrganizationAndCerts(String organizationName, String clientCertificate, List<String> intermediateCertificates) {
    Organization org = new Organization();
    org.setOrgName(organizationName);
    organizationRepository.save(org);

    Certificate clientCert = new Certificate();
    clientCert.setCertDigest(TrustedPartyUtils.computeCertificateDigest(clientCertificate));
    clientCert.setCert(clientCertificate);
    clientCert.setCertificateType(CertificateType.CLIENT);
    clientCert.setIsRevoked(false);
    clientCert.setReceived_on(LocalDateTime.now());
    clientCert.setOrganization(org);
    certificateRepository.save(clientCert);

    for (String intermediateCertificate : intermediateCertificates) {
      Certificate intermediateCert = new Certificate();
      intermediateCert.setCertDigest(TrustedPartyUtils.computeCertificateDigest(intermediateCertificate));
      intermediateCert.setCert(intermediateCertificate);
      intermediateCert.setCertificateType(CertificateType.INTERMEDIATE);
      intermediateCert.setIsRevoked(false);
      intermediateCert.setReceived_on(LocalDateTime.now());
      intermediateCert.setOrganization(org);
      certificateRepository.save(intermediateCert);
    }
  }

  @Transactional
  protected void revokeAndUpdateCertifacates(
      @NonNull String organizationName,
      String clientCertificate,
      List<String> intermediateCertificates) {
    revokeAllStoredCertificates(organizationName);

    Organization org = organizationRepository.findById(organizationName).orElseThrow(() -> new OrganizationNotFoundException(organizationName));
    Certificate clientCert = new Certificate();
    clientCert.setCertDigest(TrustedPartyUtils.computeCertificateDigest(clientCertificate));
    clientCert.setCert(clientCertificate);
    clientCert.setCertificateType(CertificateType.CLIENT);
    clientCert.setIsRevoked(false);
    clientCert.setReceived_on(LocalDateTime.now());
    clientCert.setOrganization(org);
    certificateRepository.save(clientCert);

    for (String intermediateCert : intermediateCertificates) {
      String digest = TrustedPartyUtils.computeCertificateDigest(intermediateCert);

      Optional<Certificate> existingCertOpt = certificateRepository.findByCertDigest(digest);

      if (existingCertOpt.isPresent()) {
        Certificate existingCert = existingCertOpt.get();
        existingCert.setIsRevoked(false);
        certificateRepository.save(existingCert);
      } else {
        // Otherwise, insert as new intermediate certificate
        Certificate intermediateCertEntity = new Certificate();
        intermediateCertEntity.setCertDigest(digest);
        intermediateCertEntity.setCert(intermediateCert);
        intermediateCertEntity.setCertificateType(CertificateType.INTERMEDIATE);
        intermediateCertEntity.setIsRevoked(false);
        intermediateCertEntity.setReceived_on(LocalDateTime.now());
        intermediateCertEntity.setOrganization(org);
        certificateRepository.save(intermediateCertEntity);
      }
    }
  }

  @Transactional
  protected void revokeAllStoredCertificates(String organizationName) {
    List<Certificate> clientCertificates = certificateRepository
        .findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(organizationName, CertificateType.CLIENT, false);

    List<Certificate> intermediateCertificates = certificateRepository
        .findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(organizationName, CertificateType.INTERMEDIATE, false);

    for (Certificate certificate : clientCertificates) {
      certificate.setIsRevoked(true);
      certificateRepository.save(certificate);
    }

    for (Certificate intermediateCertificate : intermediateCertificates) {
      intermediateCertificate.setIsRevoked(true);
      certificateRepository.save(intermediateCertificate);
    }
  }

  @Transactional(readOnly = true)
  private SortedCertificates getSortedCertificates(String organizationName) {
    List<Certificate> revokedCerts = certificateRepository
        .findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
            organizationName,
            CertificateType.CLIENT,
            true);

    Certificate activeCert = certificateRepository
        .findFirstByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
            organizationName,
            CertificateType.CLIENT,
            false);

    return new SortedCertificates(activeCert, revokedCerts);
  }
}
