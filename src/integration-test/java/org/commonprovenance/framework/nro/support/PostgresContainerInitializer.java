package org.commonprovenance.framework.nro.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresContainerInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final String POSTGRES_IMAGE = "postgres:18-alpine";

  private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE));

  static {
    POSTGRES.start();
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    TestPropertyValues.of(
        "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
        "spring.datasource.username=" + POSTGRES.getUsername(),
        "spring.datasource.password=" + POSTGRES.getPassword())
        .applyTo(applicationContext.getEnvironment());
  }
}
