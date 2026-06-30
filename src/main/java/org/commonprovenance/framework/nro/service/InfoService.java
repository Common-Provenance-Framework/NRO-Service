package org.commonprovenance.framework.nro.service;

import org.commonprovenance.framework.nro.api.InfoResponseDTO;
import org.commonprovenance.framework.nro.config.AppProperties;
import org.springframework.stereotype.Service;

@Service
public class InfoService {
  private final AppProperties appProperties;

  public InfoService(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  public InfoResponseDTO getInfo() {
    return new InfoResponseDTO(appProperties.getId(), appProperties.getCertificate());
  }
}
