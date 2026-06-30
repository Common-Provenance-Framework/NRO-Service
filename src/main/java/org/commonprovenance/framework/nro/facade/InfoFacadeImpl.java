package org.commonprovenance.framework.nro.facade;

import org.commonprovenance.framework.nro.api.InfoResponseDTO;
import org.commonprovenance.framework.nro.service.InfoService;
import org.springframework.stereotype.Service;

@Service
public class InfoFacadeImpl implements InfoFacade {
  private final InfoService infoService;

  public InfoFacadeImpl(InfoService infoService) {
    this.infoService = infoService;
  }

  @Override
  public InfoResponseDTO getInfo() {
    return infoService.getInfo();
  }
}
