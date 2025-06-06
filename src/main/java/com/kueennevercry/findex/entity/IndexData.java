package com.kueennevercry.findex.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "index_data")
public class IndexData {

  @Id
  @GeneratedValue
  private Long id;
}
