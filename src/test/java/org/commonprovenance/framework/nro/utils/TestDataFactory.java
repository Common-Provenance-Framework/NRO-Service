package org.commonprovenance.framework.nro.utils;

import java.time.LocalDateTime;
import java.util.List;

import org.commonprovenance.framework.nro.api.Organization.OrganizationDTO;
import org.commonprovenance.framework.nro.api.Organization.StoreCertOrganizationDTO;
import org.commonprovenance.framework.nro.api.Token.TokenRequestDTO;
import org.commonprovenance.framework.nro.data.enums.DocumentType;

public final class TestDataFactory {

  private TestDataFactory() {
  }

  public static TokenRequestDTO tokenRequest() {
    TokenRequestDTO body = new TokenRequestDTO();
    body.setOrganizationId("org-1");
    body.setDocument("ZHVtbXktZG9j");
    body.setDocumentFormat("json");
    body.setDocumentType(DocumentType.GRAPH);
    body.setCreatedOn(LocalDateTime.of(2024, 1, 1, 12, 0).toString());
    body.setSignature("sig");
    return body;
  }

  public static StoreCertOrganizationDTO storeCertRequest() {
    StoreCertOrganizationDTO body = new StoreCertOrganizationDTO();
    body.setOrganizationId("org-1");
    body.setClientCertificate("client-cert");
    body.setIntermediateCertificates(List.of("intermediate-1"));
    return body;
  }

  public static OrganizationDTO organizationDto(String orgId) {
    OrganizationDTO dto = new OrganizationDTO();
    dto.setOrganizationId(orgId);
    return dto;
  }

}
