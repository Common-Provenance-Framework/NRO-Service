package org.commonprovenance.framework.nro.data.repository;

import org.commonprovenance.framework.nro.data.enums.CertificateType;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, String> {

  List<Certificate> findByOrganizationIdAndCertificateTypeAndIsRevoked(
      String id,
      CertificateType certificateType,
      boolean isRevoked);

  Certificate findFirstByOrganizationIdAndCertificateTypeAndIsRevoked(
      String id,
      CertificateType certificateType,
      boolean isRevoked);

  Certificate findFirstByOrganizationIdAndIsRevoked(
      String id,
      boolean isRevoked);

  Optional<Certificate> findByCertDigest(String digest);
}
