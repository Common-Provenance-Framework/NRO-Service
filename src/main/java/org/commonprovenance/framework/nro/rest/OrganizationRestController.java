package org.commonprovenance.framework.nro.rest;

import java.util.List;

import org.commonprovenance.framework.nro.api.Organization.OrganizationDTO;
import org.commonprovenance.framework.nro.api.Organization.StoreCertOrganizationDTO;
import org.commonprovenance.framework.nro.facade.OrganizationFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Organizations", description = "")
@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationRestController {

  private final OrganizationFacade organizationFacade;

  public OrganizationRestController(OrganizationFacade organizationFacade) {
    this.organizationFacade = organizationFacade;
  }

  @GetMapping
  public ResponseEntity<List<OrganizationDTO>> getAllOrganizations() {
    List<OrganizationDTO> organizations = organizationFacade.getAllOrganizations();
    return ResponseEntity.ok(organizations);
  }

  @GetMapping("/{organizationId}")
  public ResponseEntity<OrganizationDTO> getOrganization(@PathVariable @NonNull String organizationId) {
    OrganizationDTO response = organizationFacade.getOrganization(organizationId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{organizationId}")
  public ResponseEntity<Void> registerOrganization(@PathVariable @NonNull String organizationId,
      @RequestBody @Valid StoreCertOrganizationDTO body) {
    organizationFacade.registerOrganization(organizationId, body);
    return ResponseEntity.status(201).build();
  }

  @GetMapping("/{organizationId}/certs")
  public ResponseEntity<OrganizationDTO> retrieveCertificates(@PathVariable @NonNull String organizationId) {
    OrganizationDTO orgWithAllCerts = organizationFacade.retrieveCertificates(organizationId);
    return ResponseEntity.ok(orgWithAllCerts);
  }

  @PutMapping("/{organizationId}/certs")
  public ResponseEntity<?> updateCertificates(@PathVariable @NonNull String organizationId,
      @RequestBody @Valid StoreCertOrganizationDTO body) {
    organizationFacade.updateCertificates(organizationId, body);
    return ResponseEntity.status(201).build();
  }
}
