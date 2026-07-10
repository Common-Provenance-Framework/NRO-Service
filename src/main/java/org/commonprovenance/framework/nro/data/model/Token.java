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

  private String type;
  @Lob
  private String tokenValue;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Document getDocument() {
    return document;
  }

  public void setDocument(Document document) {
    this.document = document;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTokenValue() {
    return tokenValue;
  }

  public void setTokenValue(String tokenValue) {
    this.tokenValue = tokenValue;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Token token))
      return false;
    return Objects.equals(id, token.id)
        && Objects.equals(document, token.document)
        && Objects.equals(type, token.type)
        && Objects.equals(tokenValue, token.tokenValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, document, type, tokenValue);
  }

  @Override
  public String toString() {
    return "Token{" +
        "id=" + id +
        ", document=" + document +
        ", type=" + type +
        ", tokenValue='" + tokenValue + '\'' +
        '}';
  }
}
