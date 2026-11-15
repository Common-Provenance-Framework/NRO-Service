package org.commonprovenance.framework.nro.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.commonprovenance.framework.nro.support.trustedParty.TrustedPartyClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureMockMvc
@ContextConfiguration(initializers = { PostgresContainerInitializer.class, PkiInitializer.class })
@Import({ IntegrationTestData.class, TrustedPartyClient.class })
public @interface IntegrationTest {
}
