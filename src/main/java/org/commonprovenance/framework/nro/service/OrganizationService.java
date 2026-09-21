package org.commonprovenance.framework.nro.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.commonprovenance.framework.nro.api.Organization.StoreCertOrganizationDTO;
import org.commonprovenance.framework.nro.config.AppProperties;
import org.commonprovenance.framework.nro.data.enums.CertificateType;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.commonprovenance.framework.nro.data.model.OrganizationCertificate;
import org.commonprovenance.framework.nro.data.records.OrganizationAndCertificates;
import org.commonprovenance.framework.nro.data.records.SortedCertificates;
import org.commonprovenance.framework.nro.data.repository.CertificateRepository;
import org.commonprovenance.framework.nro.data.repository.OrganizationCertificateRepository;
import org.commonprovenance.framework.nro.data.repository.OrganizationRepository;
import org.commonprovenance.framework.nro.exceptions.CertificateVerificationException;
import org.commonprovenance.framework.nro.exceptions.OrganizationAlreadyExistsException;
import org.commonprovenance.framework.nro.exceptions.OrganizationIdMismatchException;
import org.commonprovenance.framework.nro.exceptions.OrganizationNotFoundException;
import org.commonprovenance.framework.nro.utils.TrustedPartyUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final CertificateRepository certificateRepository;
  private final OrganizationCertificateRepository organizationCertificateRepository;
  private final AppProperties appProperties;

  public OrganizationService(
      OrganizationRepository organizationRepository,
      CertificateRepository certificateRepository,
      OrganizationCertificateRepository organizationCertificateRepository,
      AppProperties appProperties) {
    this.organizationRepository = organizationRepository;
    this.certificateRepository = certificateRepository;
    this.organizationCertificateRepository = organizationCertificateRepository;
    this.appProperties = appProperties;
  }

  @Transactional(readOnly = true)
  public List<OrganizationAndCertificates> getAllOrganizations() {
    List<Organization> organizations = organizationRepository.findAll();
    List<OrganizationAndCertificates> result = new ArrayList<>();

    for (Organization org : organizations) {
      SortedCertificates sorted = getSortedCertificates(org.getId());

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
      @NonNull String id) {
    Organization organization = organizationRepository
        .findById(id)
        .orElseThrow(() -> new OrganizationNotFoundException(id));

    SortedCertificates sortedCertificates = getSortedCertificates(id);

    return new OrganizationAndCertificates(
        organization,
        sortedCertificates.activeCertificate(),
        sortedCertificates.revokedCertificates());
  }

  @Transactional
  public void updateCertificates(
      @NonNull String id,
      StoreCertOrganizationDTO body) {
    // Checks of request body are done in Controller using Jakarta validation

    if (!Objects.equals(id, body.getId())) {
      throw new OrganizationIdMismatchException(id);
    }

    organizationRepository
        .findById(id)
        .orElseThrow(() -> new OrganizationNotFoundException(id));

    if (!TrustedPartyUtils.verifyChainOfTrust(
        body.getClientCertificate(),
        body.getIntermediateCertificates(),
        appProperties.loadTrustedCertificates())) {
      throw new CertificateVerificationException("Could not verify the chain of trust for the provided certificates");
    }

    revokeAndUpdateCertifacates(id, body.getClientCertificate(), body.getIntermediateCertificates());
  }

  @Transactional
  public void storeCertToOrganization(
      @NonNull String id,
      StoreCertOrganizationDTO body) {
    // Checks of request body are done in Controller using Jakarta validation

    if (!Objects.equals(id, body.getId())) {
      throw new OrganizationIdMismatchException(id);
    }

    organizationRepository.findById(id)
        .ifPresent(org -> {
          throw new OrganizationAlreadyExistsException(
              "Organization with id [" + id + "] already exists");
        });

    if (!TrustedPartyUtils.verifyChainOfTrust(
        body.getClientCertificate(),
        body.getIntermediateCertificates(),
        appProperties.loadTrustedCertificates())) {
      throw new CertificateVerificationException("Could not verify the chain of trust for the provided certificates");
    }

    storeOrganizationAndCerts(id, body.getClientCertificate(), body.getIntermediateCertificates());
  }

  @Transactional
  protected void storeOrganizationAndCerts(
      String id,
      String clientCertificate,
      List<String> intermediateCertificates) {
    Organization org = new Organization();
    org.setId(id);
    organizationRepository.save(org);

    saveAssociation(org, clientCertificate, CertificateType.CLIENT);

    intermediateCertificates.forEach(saveAssociation(org, CertificateType.INTERMEDIATE));
  }

  @Transactional
  protected void revokeAndUpdateCertifacates(
      String id,
      String clientCertificate,
      List<String> intermediateCertificates) {
    revokeAllStoredCertificates(id);

    Organization org = organizationRepository.findById(id).orElseThrow(() -> new OrganizationNotFoundException(id));
    saveAssociation(org, clientCertificate, CertificateType.CLIENT);

    intermediateCertificates.forEach(saveAssociation(org, CertificateType.INTERMEDIATE));
  }

  @Transactional
  protected void revokeAllStoredCertificates(String id) {
    Stream.concat(
        organizationCertificateRepository
            .findByOrganizationIdAndCertificateTypeAndIsRevoked(id, CertificateType.CLIENT, false)
            .stream(),
        organizationCertificateRepository
            .findByOrganizationIdAndCertificateTypeAndIsRevoked(id, CertificateType.INTERMEDIATE, false)
            .stream())
        .forEach(certificate -> {
          certificate.setIsRevoked(true);
          organizationCertificateRepository.save(certificate);
        });
  }

  @Transactional(readOnly = true)
  private SortedCertificates getSortedCertificates(String id) {
    List<OrganizationCertificate> revokedCerts = organizationCertificateRepository
        .findByOrganizationIdAndCertificateTypeAndIsRevoked(
            id,
            CertificateType.CLIENT,
            true);

    OrganizationCertificate activeCert = organizationCertificateRepository
        .findFirstByOrganizationIdAndCertificateTypeAndIsRevoked(
            id,
            CertificateType.CLIENT,
            false)
        .orElse(null);

    return new SortedCertificates(activeCert, revokedCerts);
  }

  private Consumer<String> saveAssociation(Organization organization, CertificateType certificateType) {
    return (String certificateData) -> this.saveAssociation(organization, certificateData, certificateType);
  }

  private void saveAssociation(Organization organization, String certificateData, CertificateType certificateType) {
    String digest = TrustedPartyUtils.computeCertificateDigest(certificateData);
    Certificate certificate = certificateRepository.findByCertDigest(digest)
        .orElseGet(() -> {
          Certificate newCertificate = new Certificate();
          newCertificate.setCertDigest(digest);
          newCertificate.setCert(certificateData);
          return certificateRepository.save(newCertificate);
        });

    OrganizationCertificate association = organizationCertificateRepository
        .findByOrganizationIdAndCertificate_CertDigest(organization.getId(), digest)
        .map(organizationCertificate -> {
          organizationCertificate.setIsRevoked(false);
          return organizationCertificate;
        })
        .orElseGet(() -> new OrganizationCertificate(organization, certificate, certificateType));

    organizationCertificateRepository.save(association);
  }
}
