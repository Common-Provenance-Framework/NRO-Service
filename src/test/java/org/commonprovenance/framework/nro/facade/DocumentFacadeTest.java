package org.commonprovenance.framework.nro.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.commonprovenance.framework.nro.api.Document.DocumentDTO;
import org.commonprovenance.framework.nro.data.enums.DocumentType;
import org.commonprovenance.framework.nro.data.model.Certificate;
import org.commonprovenance.framework.nro.data.model.Document;
import org.commonprovenance.framework.nro.data.model.Organization;
import org.commonprovenance.framework.nro.mappers.DocumentMapper;
import org.commonprovenance.framework.nro.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentFacadeTest {

  @Mock
  private DocumentService documentService;

  @Mock
  private DocumentMapper documentMapper;

  @InjectMocks
  private DocumentFacadeImpl documentFacade;

  @Test
  void getDocument_existingDocument_returnsMappedDto() {
    Organization organization = new Organization();
    organization.setId("org-1");
    Certificate certificate = new Certificate();
    certificate.setCertDigest("cert-1");
    Document document = new Document();
    document.setIdentifier("doc-1");
    document.setDocFormat("json");
    document.setOrganization(organization);
    document.setCertificate(certificate);
    document.setDocumentType(DocumentType.GRAPH);
    document.setDocumentText("{}");
    document.setCreatedOn(LocalDateTime.now());
    document.setSignature("sig");

    DocumentDTO dto = new DocumentDTO();
    dto.setIdentifier("doc-1");

    when(documentService.getDocument("org-1", "doc-1", "json")).thenReturn(document);
    when(documentMapper.mapToDTO(document)).thenReturn(dto);

    DocumentDTO result = documentFacade.getDocument("org-1", "doc-1", "json");

    assertThat(result).isSameAs(dto);
  }
}
