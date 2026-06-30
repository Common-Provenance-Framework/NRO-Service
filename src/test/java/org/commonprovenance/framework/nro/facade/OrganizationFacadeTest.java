package org.commonprovenance.framework.nro.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.commonprovenance.framework.nro.api.Organization.OrganizationDTO;
import org.commonprovenance.framework.nro.api.Organization.StoreCertOrganizationDTO;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.commonprovenance.framework.nro.data.records.OrganizationAndCertificates;
import org.commonprovenance.framework.nro.mappers.OrganizationMapper;
import org.commonprovenance.framework.nro.service.OrganizationService;
import org.commonprovenance.framework.nro.utils.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationFacadeTest {

  @Mock
  private OrganizationService organizationService;

  @Mock
  private OrganizationMapper organizationMapper;

  @InjectMocks
  private OrganizationFacadeImpl organizationFacade;

  @Test
  void getAllOrganizations_returnsMappedList() {
    Organization organization = new Organization();
    organization.setOrgName("org-1");
    OrganizationAndCertificates orgCert = new OrganizationAndCertificates(organization, null, null);
    OrganizationDTO dto = TestDataFactory.organizationDto("org-1");

    when(organizationService.getAllOrganizations()).thenReturn(List.of(orgCert));
    when(organizationMapper.mapToList(List.of(orgCert))).thenReturn(List.of(dto));

    List<OrganizationDTO> result = organizationFacade.getAllOrganizations();

    assertThat(result).containsExactly(dto);
  }

  @Test
  void getOrganization_returnsMappedOrganization() {
    Organization organization = new Organization();
    organization.setOrgName("org-1");
    OrganizationAndCertificates orgCert = new OrganizationAndCertificates(organization, null, List.of());
    OrganizationDTO dto = TestDataFactory.organizationDto("org-1");

    when(organizationService.getOrganization("org-1")).thenReturn(orgCert);
    when(organizationMapper.mapToOrganizationDTO(orgCert, false)).thenReturn(dto);

    OrganizationDTO result = organizationFacade.getOrganization("org-1");

    assertThat(result).isSameAs(dto);
    verify(organizationMapper).mapToOrganizationDTO(orgCert, false);
  }

  @Test
  void retrieveCertificates_includesRevoked() {
    Organization organization = new Organization();
    organization.setOrgName("org-1");
    OrganizationAndCertificates orgCert = new OrganizationAndCertificates(organization, null, List.of());
    OrganizationDTO dto = TestDataFactory.organizationDto("org-1");

    when(organizationService.getOrganization("org-1")).thenReturn(orgCert);
    when(organizationMapper.mapToOrganizationDTO(orgCert, true)).thenReturn(dto);

    OrganizationDTO result = organizationFacade.retrieveCertificates("org-1");

    assertThat(result).isSameAs(dto);
    verify(organizationMapper).mapToOrganizationDTO(orgCert, true);
  }

  @Test
  void updateCertificates_delegatesToService() {
    StoreCertOrganizationDTO body = TestDataFactory.storeCertRequest();

    organizationFacade.updateCertificates("org-1", body);

    verify(organizationService).updateCertificates("org-1", body);
  }

  @Test
  void registerOrganization_delegatesToService() {
    StoreCertOrganizationDTO body = TestDataFactory.storeCertRequest();

    organizationFacade.registerOrganization("org-1", body);

    verify(organizationService).storeCertToOrganization("org-1", body);
  }
}
