package org.commonprovenance.framework.nro.data.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Organization {

  @Id
  @Column(length = 40)
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Organization that))
      return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Organization{" +
        "id=" + id + '}';
  }
}
