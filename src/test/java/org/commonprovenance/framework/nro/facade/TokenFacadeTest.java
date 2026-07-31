package org.commonprovenance.framework.nro.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.commonprovenance.framework.nro.api.TokenResponseDTO;
import org.commonprovenance.framework.nro.api.Token.TokenRequestDTO;
import org.commonprovenance.framework.nro.config.AppProperties;
import org.commonprovenance.framework.nro.data.model.Document;
import org.commonprovenance.framework.nro.data.model.Token;
import org.commonprovenance.framework.nro.mappers.TokenMapper;
import org.commonprovenance.framework.nro.service.TokenService;
import org.commonprovenance.framework.nro.utils.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenFacadeTest {

  @Mock
  private TokenService tokenService;

  @Mock
  private AppProperties appProperties;

  @Mock
  private TokenMapper tokenMapper;

  @InjectMocks
  private TokenFacadeImpl tokenFacade;

  @Test
  void getToken_returnsMappedList() {
    Token token = new Token();
    TokenResponseDTO dto = new TokenResponseDTO();

    when(tokenService.getToken("org-1", "doc-1", "json")).thenReturn(List.of(token));
    when(tokenMapper.mapToList(List.of(token))).thenReturn(List.of(dto));

    List<TokenResponseDTO> result = tokenFacade.getToken("org-1", "doc-1", "json");

    assertThat(result).containsExactly(dto);
  }

  @Test
  void getAllTokens_returnsFlattenedList() {
    Document document = new Document();
    Token tokenA = new Token();
    tokenA.setJwt("jwt_A");
    Token tokenB = new Token();
    tokenB.setJwt

    ("jwt_B");
    TokenResponseDTO dtoA = new TokenResponseDTO();
    TokenResponseDTO dtoB = new TokenResponseDTO();

    when(tokenService.getAllTokens("org-1"))
        .thenReturn(Map.of(document, List.of(tokenA, tokenB)));
    when(tokenMapper.mapToDTO(tokenA)).thenReturn(dtoA);
    when(tokenMapper.mapToDTO(tokenB)).thenReturn(dtoB);

    List<TokenResponseDTO> result = tokenFacade.getAllTokens("org-1");

    assertThat(result).containsExactlyInAnyOrder(dtoA, dtoB);
  }

  @Test
  void issueToken_returnsMappedList() {
    TokenRequestDTO body = TestDataFactory.tokenRequest();
    Token token = new Token();
    TokenResponseDTO dto = new TokenResponseDTO();

    when(tokenService.issueToken(body)).thenReturn(token);
    when(tokenMapper.mapToDTO(token)).thenReturn(dto);

    TokenResponseDTO result = tokenFacade.issueToken(body);

    assertThat(result).isEqualTo(dto);
  }

  @Test
  void verifySignature_delegatesToService() {
    TokenRequestDTO body = TestDataFactory.tokenRequest();
    when(tokenService.verifySignature(body)).thenReturn(true);

    boolean result = tokenFacade.verifySignature(body);

    assertThat(result).isTrue();
    verify(tokenService).verifySignature(body);
  }
}
