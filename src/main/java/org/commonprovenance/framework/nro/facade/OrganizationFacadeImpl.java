package org.commonprovenance.framework.nro.facade;

import java.util.List;

import org.commonprovenance.framework.nro.api.Organization.OrganizationDTO;
import org.commonprovenance.framework.nro.api.Organization.StoreCertOrganizationDTO;
import org.commonprovenance.framework.nro.mappers.OrganizationMapper;
import org.commonprovenance.framework.nro.service.OrganizationService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class OrganizationFacadeImpl implements OrganizationFacade {

  private final OrganizationService organizationService;
  private final OrganizationMapper organizationMapper;

  public OrganizationFacadeImpl(OrganizationService organizationService, OrganizationMapper organizationMapper) {
    this.organizationService = organizationService;
    this.organizationMapper = organizationMapper;
  }

  @Override
  public List<OrganizationDTO> getAllOrganizations() {
    return organizationMapper.mapToList(organizationService.getAllOrganizations());
  }

  @Override
  public OrganizationDTO getOrganization(@NonNull String organizationId) {
    return organizationMapper.mapToOrganizationDTO(organizationService.getOrganization(organizationId), false);
  }

  @Override
  public void updateCertificates(
      @NonNull String organizationId,
      StoreCertOrganizationDTO body) {
    organizationService.updateCertificates(organizationId, body);
  }

  @Override
  public OrganizationDTO retrieveCertificates(@NonNull String organizationId) {
    return organizationMapper.mapToOrganizationDTO(organizationService.getOrganization(organizationId), true);
  }

  @Override
  public void registerOrganization(
      @NonNull String organizationId,
      StoreCertOrganizationDTO body) {
    organizationService.storeCertToOrganization(organizationId, body);
  }

}
