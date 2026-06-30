package org.commonprovenance.framework.nro.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.commonprovenance.framework.nro.api.InfoResponseDTO;
import org.commonprovenance.framework.nro.service.InfoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InfoFacadeTest {

  @Mock
  private InfoService infoService;

  @InjectMocks
  private InfoFacadeImpl infoFacade;

  @Test
  void getInfo_returnsServiceResponse() {
    InfoResponseDTO dto = new InfoResponseDTO("tp-1", "cert");
    when(infoService.getInfo()).thenReturn(dto);

    InfoResponseDTO result = infoFacade.getInfo();

    assertThat(result).isSameAs(dto);
  }
}
