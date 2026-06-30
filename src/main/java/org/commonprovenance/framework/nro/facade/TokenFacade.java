package org.commonprovenance.framework.nro.facade;

import java.util.List;

import org.commonprovenance.framework.nro.api.Token.TokenDTO;
import org.commonprovenance.framework.nro.api.Token.TokenRequestDTO;

public interface TokenFacade {
  List<TokenDTO> getToken(String organizationId, String documentId, String documentFormat);
  List<TokenDTO> getAllTokens(String organizationId);
  List<TokenDTO> issueToken(TokenRequestDTO body);
  boolean verifySignature(TokenRequestDTO body);
}
