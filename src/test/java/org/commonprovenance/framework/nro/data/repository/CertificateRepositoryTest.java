package org.commonprovenance.framework.nro.data.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.commonprovenance.framework.nro.data.model.Certificate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class CertificateRepositoryTest {

  @Autowired
  private CertificateRepository certificateRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  void save_validCertificate_persistsAndLoads() {
    Certificate certificate = new Certificate();
    certificate.setCertDigest("cert-save");
    certificate.setCert("cert-body");

    certificateRepository.save(certificate);

    Optional<Certificate> reloaded = certificateRepository.findByCertDigest("cert-save");

    assertThat(reloaded).isPresent();
    assertThat(reloaded.get().getCert()).isEqualTo("cert-body");
    assertThat(reloaded.get().getCertDigest()).isEqualTo("cert-save");
  }

  @Test
  void findByCertDigest_existingDigest_returnsCertificate() {
    saveCertificate("cert-digest");

    Optional<Certificate> result = certificateRepository.findByCertDigest("cert-digest");

    assertThat(result).isPresent();
    assertThat(result.get().getCert()).isEqualTo("cert-body");
    assertThat(result.get().getCertDigest()).isEqualTo("cert-digest");
  }

  @Test
  void findByCertDigest_nonExistingDigest_returnsEmpty() {
    Optional<Certificate> result = certificateRepository.findByCertDigest("cert-missing");

    assertThat(result).isEmpty();
  }

  private Certificate saveCertificate(String digest) {
    Certificate certificate = new Certificate();
    certificate.setCertDigest(digest);
    certificate.setCert("cert-body");
    entityManager.persist(certificate);
    entityManager.flush();
    return certificate;
  }

}
