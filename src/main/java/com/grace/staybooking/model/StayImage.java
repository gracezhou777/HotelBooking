package com.grace.staybooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "stay_image")
public class StayImage implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private String url;//主键

  @ManyToOne //多个图片对应一个stay
  @JoinColumn(name = "stay_id")
  @JsonIgnore//返回的时候只需要返回url，不需要返回stay
  private Stay stay;

  public StayImage() {}

  public StayImage(String url, Stay stay) {
    this.url = url;
    this.stay = stay;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Stay getStay() {
    return stay;
  }

  public void setStay(Stay stay) {
    this.stay = stay;
  }
}

