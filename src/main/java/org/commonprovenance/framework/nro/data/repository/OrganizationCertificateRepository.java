package org.commonprovenance.framework.nro.data.repository;

import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.nro.data.enums.CertificateType;
import org.commonprovenance.framework.nro.data.model.OrganizationCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationCertificateRepository extends JpaRepository<OrganizationCertificate, Long> {

  List<OrganizationCertificate> findByOrganizationIdAndCertificateTypeAndIsRevoked(
      String organizationId,
      CertificateType certificateType,
      boolean isRevoked);

  Optional<OrganizationCertificate> findFirstByOrganizationIdAndCertificateTypeAndIsRevoked(
      String organizationId,
      CertificateType certificateType,
      boolean isRevoked);

  Optional<OrganizationCertificate> findFirstByOrganizationIdAndIsRevoked(
      String organizationId,
      boolean isRevoked);

  Optional<OrganizationCertificate> findByOrganizationIdAndCertificate_CertDigest(
      String organizationId,
      String certDigest);
}
