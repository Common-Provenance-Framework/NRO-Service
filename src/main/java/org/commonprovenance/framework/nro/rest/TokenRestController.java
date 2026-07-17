package org.commonprovenance.framework.nro.rest;

import java.util.List;

import org.commonprovenance.framework.nro.api.TokenResponseDTO;
import org.commonprovenance.framework.nro.api.Token.TokenRequestDTO;
import org.commonprovenance.framework.nro.facade.TokenFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Tokens", description = "Actions for tokens")
@RestController
@RequestMapping("/api/v1")
public class TokenRestController {

  private final TokenFacade tokenFacade;

  public TokenRestController(TokenFacade tokenFacade) {
    this.tokenFacade = tokenFacade;
  }

  @GetMapping("/organizations/{orgId}/tokens/{docId}/{graphFormat}")
  public ResponseEntity<List<TokenResponseDTO>> getToken(
      @PathVariable String orgId,
      @PathVariable String docId,
      @PathVariable String graphFormat) {
    List<TokenResponseDTO> response = tokenFacade.getToken(orgId, docId, graphFormat);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/organizations/{orgId}/tokens")
  public ResponseEntity<List<TokenResponseDTO>> getAllTokens(@PathVariable String orgId) {
    return ResponseEntity.ok(tokenFacade.getAllTokens(orgId));
  }

  @PostMapping("/issueToken")
  public ResponseEntity<TokenResponseDTO> issueToken(@RequestBody @Valid TokenRequestDTO body) {
    TokenResponseDTO response = tokenFacade.issueToken(body);
    return ResponseEntity.status(201).body(response);
  }

  @PostMapping("/verifySignature")
  public ResponseEntity<Void> verifySignature(
      @RequestBody TokenRequestDTO body) {
    if (tokenFacade.verifySignature(body)) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.badRequest().build();
  }
}
