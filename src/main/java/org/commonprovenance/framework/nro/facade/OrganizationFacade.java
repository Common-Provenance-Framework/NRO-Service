package org.commonprovenance.framework.nro.facade;

import java.util.List;

import org.commonprovenance.framework.nro.api.Organization.OrganizationDTO;
import org.commonprovenance.framework.nro.api.Organization.StoreCertOrganizationDTO;
import org.springframework.lang.NonNull;

public interface OrganizationFacade {
  List<OrganizationDTO> getAllOrganizations();

  OrganizationDTO getOrganization(@NonNull String organizationId);

  void registerOrganization(
      @NonNull String organizationId,
      StoreCertOrganizationDTO body);

  OrganizationDTO retrieveCertificates(@NonNull String organizationId);

  void updateCertificates(
      @NonNull String organizationId,
      StoreCertOrganizationDTO body);
}
