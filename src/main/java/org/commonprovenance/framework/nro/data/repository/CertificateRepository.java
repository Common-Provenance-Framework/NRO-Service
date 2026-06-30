package org.commonprovenance.framework.nro.data.repository;

import org.commonprovenance.framework.nro.data.enums.CertificateType;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, String> {

  List<Certificate> findByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
      String orgName,
      CertificateType certificateType,
      boolean isRevoked);

  Certificate findFirstByOrganizationOrgNameAndCertificateTypeAndIsRevoked(
      String orgName,
      CertificateType certificateType,
      boolean isRevoked);

  Certificate findFirstByOrganizationOrgNameAndIsRevoked(
      String orgName,
      boolean isRevoked);

  Optional<Certificate> findByCertDigest(String digest);
}
