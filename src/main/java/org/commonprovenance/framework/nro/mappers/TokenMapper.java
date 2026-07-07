package org.commonprovenance.framework.nro.mappers;

import org.commonprovenance.framework.nro.api.TokenResponseDTO;
import org.commonprovenance.framework.nro.config.AppProperties;
import org.commonprovenance.framework.nro.data.model.Token;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TokenMapper {

  public TokenResponseDTO mapToDTO(Token token, AppProperties appProperties) {
    TokenResponseDTO tokenResponseDTO = new TokenResponseDTO();
    tokenResponseDTO.setTokenId(token.getId() == null ? null : token.getId().toString());
    tokenResponseDTO.setOrganizationId(token.getDocument().getOrganization().getOrgName());
    tokenResponseDTO.setDocumentId(token.getDocument().getIdentifier());
    tokenResponseDTO.setDocumentFormat(token.getDocument().getDocFormat());
    tokenResponseDTO.setIssuedAt(token.getCreatedOn() == null ? null : token.getCreatedOn().toString());
    tokenResponseDTO.setTokenValue(token.getTokenValue());
    return tokenResponseDTO;
  }

  public List<TokenResponseDTO> mapToList(List<Token> tokens, AppProperties appProperties) {
    return tokens
        .stream()
        .map(token -> mapToDTO(token, appProperties))
        .toList();
  }
}
