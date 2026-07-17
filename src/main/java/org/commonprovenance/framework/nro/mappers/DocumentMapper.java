package org.commonprovenance.framework.nro.mappers;

import org.commonprovenance.framework.nro.api.Document.DocumentDTO;
import org.commonprovenance.framework.nro.data.model.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

  public DocumentDTO mapToDTO(Document document) {
    DocumentDTO dto = new DocumentDTO();
    dto.setId(document.getId());
    dto.setIdentifier(document.getIdentifier());
    dto.setGraphFormat(document.getGraphFormat());
    dto.setCertDigest(document.getCertificate().getCertDigest());
    dto.setOrganizationId(document.getOrganization().getId());
    dto.setGraphType(document.getGraphType());
    dto.setGraph(document.getGraph());
    dto.setCreatedOn(document.getCreatedOn());
    dto.setSignature(document.getSignature());
    return dto;
  }
}
