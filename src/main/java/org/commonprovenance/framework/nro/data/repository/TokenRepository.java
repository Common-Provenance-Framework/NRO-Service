package org.commonprovenance.framework.nro.data.repository;

import java.util.List;

import org.commonprovenance.framework.nro.data.model.Document;
import org.commonprovenance.framework.nro.data.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {

  List<Token> findByDocument(Document document);
}
