package org.commonprovenance.framework.nro.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.commonprovenance.framework.nro.api.TokenResponseDTO;
import org.commonprovenance.framework.nro.api.Token.TokenRequestDTO;
import org.commonprovenance.framework.nro.config.AppProperties;
import org.commonprovenance.framework.nro.data.model.Document;
import org.commonprovenance.framework.nro.data.model.Token;
import org.commonprovenance.framework.nro.mappers.TokenMapper;
import org.commonprovenance.framework.nro.service.TokenService;
import org.springframework.stereotype.Service;

@Service
public class TokenFacadeImpl implements TokenFacade {

  private final TokenService tokenService;
  private final AppProperties appProperties;
  private final TokenMapper tokenMapper;

  public TokenFacadeImpl(TokenService tokenService, AppProperties appProperties, TokenMapper tokenMapper) {
    this.tokenService = tokenService;
    this.appProperties = appProperties;
    this.tokenMapper = tokenMapper;
  }

  @Override
  public List<TokenResponseDTO> getToken(String organizationId, String documentId, String documentFormat) {
    List<Token> tokens = tokenService.getToken(
        Objects.requireNonNull(organizationId),
        documentId,
        documentFormat);

    return tokenMapper.mapToList(tokens);
  }

  @Override
  public List<TokenResponseDTO> getAllTokens(String organizationId) {
    Map<Document, List<Token>> tokensByDoc = tokenService.getAllTokens(Objects.requireNonNull(organizationId));

    List<TokenResponseDTO> result = new ArrayList<>();

    for (Map.Entry<Document, List<Token>> entry : tokensByDoc.entrySet()) {
      List<Token> tokens = entry.getValue();

      for (Token token : tokens) {
        TokenResponseDTO dto = tokenMapper.mapToDTO(token);
        result.add(dto);
      }
    }
    return result;
  }

  @Override
  public TokenResponseDTO issueToken(TokenRequestDTO body) {
    return tokenMapper.mapToDTO(tokenService.issueToken(Objects.requireNonNull(body)));
  }

  @Override
  public boolean verifySignature(TokenRequestDTO body) {
    return tokenService.verifySignature(body);
  }
}
