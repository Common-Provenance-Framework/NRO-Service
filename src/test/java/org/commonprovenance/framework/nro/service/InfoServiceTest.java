package org.commonprovenance.framework.nro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.commonprovenance.framework.nro.api.InfoResponseDTO;
import org.commonprovenance.framework.nro.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InfoServiceTest {

  @Mock
  private AppProperties appProperties;

  @InjectMocks
  private InfoService infoService;

  @Test
  void getInfo_returnsConfiguredInfo() {
    when(appProperties.getId()).thenReturn("tp-1");
    when(appProperties.getCertificate()).thenReturn("cert-data");

    InfoResponseDTO result = infoService.getInfo();

    assertThat(result.getId()).isEqualTo("tp-1");
    assertThat(result.getCertificate()).isEqualTo("cert-data");
  }
}
