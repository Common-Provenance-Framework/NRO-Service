package org.commonprovenance.framework.nro.facade;

import org.commonprovenance.framework.nro.api.Document.DocumentDTO;
import org.jspecify.annotations.NonNull;

public interface DocumentFacade {
  DocumentDTO getDocument(@NonNull String organizationId, String documentId, String graphFormat);
}
