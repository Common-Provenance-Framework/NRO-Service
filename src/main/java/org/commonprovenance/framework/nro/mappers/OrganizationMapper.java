package org.commonprovenance.framework.nro.mappers;

import org.commonprovenance.framework.nro.api.Organization.OrganizationDTO;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.commonprovenance.framework.nro.data.model.OrganizationCertificate;
import org.commonprovenance.framework.nro.data.records.OrganizationAndCertificates;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrganizationMapper {

  public OrganizationDTO mapToOrganizationDTO(OrganizationAndCertificates orgCertEntity, boolean includeRevoked) {
    OrganizationDTO organizationResponseDTO = new OrganizationDTO();
    organizationResponseDTO.setId(orgCertEntity.organization().getId());

    organizationResponseDTO.setClientCertificate(
        orgCertEntity.activeCertificate() != null
            && orgCertEntity.activeCertificate().getCertificate() != null
                ? orgCertEntity.activeCertificate().getCertificate().getCert()
                : null);

    if (includeRevoked && orgCertEntity.revokedCertificates() != null)
      organizationResponseDTO.setRevokedCertificates(orgCertEntity.revokedCertificates().stream()
          .map(OrganizationCertificate::getCertificate)
          .map(Certificate::getCert)
          .collect(Collectors.toList()));

    return organizationResponseDTO;
  }

  public List<OrganizationDTO> mapToList(List<OrganizationAndCertificates> orgCertEntities) {
    return orgCertEntities
        .stream()
        .map(orgCertEntity -> mapToOrganizationDTO(orgCertEntity, false))
        .toList();
  }
}
