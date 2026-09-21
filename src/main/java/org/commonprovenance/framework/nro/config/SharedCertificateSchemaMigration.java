package org.commonprovenance.framework.nro.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test & !componenttest")
public class SharedCertificateSchemaMigration implements ApplicationRunner {

  private final JdbcTemplate jdbcTemplate;

  public SharedCertificateSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!hasLegacyCertificateColumns()) {
      return;
    }

    backfillOrganizationCertificates();
    dropLegacyCertificateColumns();
  }

  private void backfillOrganizationCertificates() {
    jdbcTemplate.execute("""
        INSERT INTO organization_certificate
            (organization_id, cert_digest, certificate_type, is_revoked, received_on)
        SELECT organization, cert_digest, certificate_type, is_revoked, received_on
        FROM certificate
        WHERE organization IS NOT NULL
        ON CONFLICT (organization_id, cert_digest) DO NOTHING
        """);
  }

  private void dropLegacyCertificateColumns() {
    jdbcTemplate.execute("""
        ALTER TABLE certificate
          DROP COLUMN organization,
          DROP COLUMN certificate_type,
          DROP COLUMN is_revoked,
          DROP COLUMN received_on
        """);
  }

  private boolean hasLegacyCertificateColumns() {
    Integer columnCount = jdbcTemplate.queryForObject("""
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'certificate'
          AND column_name IN ('organization', 'certificate_type', 'is_revoked', 'received_on')
        """, Integer.class);
    return columnCount != null && columnCount == 4;
  }
}
