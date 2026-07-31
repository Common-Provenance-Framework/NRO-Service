package org.commonprovenance.framework.nro.mappers;

import java.util.List;

import org.commonprovenance.framework.nro.api.TokenResponseDTO;
import org.commonprovenance.framework.nro.data.model.Token;
import org.springframework.stereotype.Component;

@Component
public class TokenMapper {

  public TokenResponseDTO mapToDTO(Token token) {
    TokenResponseDTO tokenResponseDTO = new TokenResponseDTO();
    tokenResponseDTO.setJwt(token.getJwt());
    return tokenResponseDTO;
  }

  public List<TokenResponseDTO> mapToList(List<Token> tokens) {
    return tokens
        .stream()
        .map(this::mapToDTO)
        .toList();
  }
}
