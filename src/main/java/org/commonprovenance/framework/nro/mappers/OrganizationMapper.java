package org.commonprovenance.framework.nro.mappers;

import org.commonprovenance.framework.nro.api.Organization.OrganizationDTO;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.commonprovenance.framework.nro.data.records.OrganizationAndCertificates;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrganizationMapper {

  public OrganizationDTO mapToOrganizationDTO(OrganizationAndCertificates orgCertEntity, boolean includeRevoked) {
    OrganizationDTO organizationResponseDTO = new OrganizationDTO();
    organizationResponseDTO.setOrganizationId(orgCertEntity.organization().getOrgName());
    Certificate activeCertificate = orgCertEntity.activeCertificate();
    organizationResponseDTO.setClientCertificate(activeCertificate != null ? activeCertificate.getCert() : null);

    if (includeRevoked) {
      List<String> revokedCertificates = new ArrayList<>();
      if (orgCertEntity.revokedCertificates() != null) {
        for (Certificate cert : orgCertEntity.revokedCertificates()) {
          revokedCertificates.add(cert.getCert());
        }
      }
      organizationResponseDTO.setRevokedCertificates(revokedCertificates);
    }

    return organizationResponseDTO;
  }

  public List<OrganizationDTO> mapToList(List<OrganizationAndCertificates> orgCertEntities) {
    return orgCertEntities
        .stream()
        .map(orgCertEntity -> mapToOrganizationDTO(orgCertEntity, false))
        .toList();
  }
}
