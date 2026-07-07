package org.commonprovenance.framework.nro.data.model;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class Token {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "document_id")
  private Document document;

  @Lob
  private String tokenValue;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTokenValue() {
    return tokenValue;
  }

  public void setTokenValue(String tokenValue) {
    this.tokenValue = tokenValue;
  }

  public Document getDocument() {
    return document;
  }

  public void setDocument(Document document) {
    this.document = document;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Token token))
      return false;
    return Objects.equals(id, token.id)
        && Objects.equals(document, token.document)
        && Objects.equals(tokenValue, token.tokenValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, document, tokenValue);
  }

  @Override
  public String toString() {
    return "Token{" +
        "id=" + id +
        ", document=" + document +
        ", tokenValue='" + tokenValue + '\'' +
        '}';
  }
}
