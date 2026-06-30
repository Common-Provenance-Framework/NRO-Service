package org.commonprovenance.framework.nro.rest;

import org.commonprovenance.framework.nro.api.InfoResponseDTO;
import org.commonprovenance.framework.nro.facade.InfoFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Info", description = "")
@RestController
@RequestMapping("/api/v1/info")
public class InfoRestController {
  private final InfoFacade infoFacade;

  public InfoRestController(InfoFacade infoFacade) {
    this.infoFacade = infoFacade;
  }

  @GetMapping()
  public ResponseEntity<InfoResponseDTO> getInfo() {
    return ResponseEntity.ok(infoFacade.getInfo());
  }
}
