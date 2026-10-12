package org.commonprovenance.framework.nro.data.repository;

import org.commonprovenance.framework.nro.data.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, String> {

  Optional<Certificate> findByCertDigest(String digest);
}
