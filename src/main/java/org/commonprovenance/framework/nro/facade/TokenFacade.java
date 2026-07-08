package org.commonprovenance.framework.nro.facade;

import java.util.List;

import org.commonprovenance.framework.nro.api.Token.TokenRequestDTO;
import org.commonprovenance.framework.nro.api.TokenResponseDTO;

public interface TokenFacade {
  List<TokenResponseDTO> getToken(String organizationId, String documentId, String documentFormat);

  List<TokenResponseDTO> getAllTokens(String organizationId);

  TokenResponseDTO issueToken(TokenRequestDTO body);

  boolean verifySignature(TokenRequestDTO body);
}
