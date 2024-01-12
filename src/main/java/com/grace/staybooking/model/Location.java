package com.grace.staybooking.model;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Document(indexName = "loc")
public class Location implements Serializable {
  private static final long serialVersionUID = 1L;

  //stay_id
  @Id
  @Field(type = FieldType.Long)
  private Long id;

  //lat, lon
  @GeoPointField
  private GeoPoint geoPoint;

  public Location(Long id, GeoPoint geoPoint) {
    this.id = id;
    this.geoPoint = geoPoint;
  }

  public Long getId() {
    return id;
  }

  public GeoPoint getGeoPoint() {
    return geoPoint;
  }
}