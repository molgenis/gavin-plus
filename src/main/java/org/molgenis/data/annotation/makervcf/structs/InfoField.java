package org.molgenis.data.annotation.makervcf.structs;


import javax.annotation.Nullable;

public class InfoField {

  String name;
  String seperator;
  String key;

  public InfoField(String name) {
    this.name = name;
  }

  public InfoField(String name, String seperator, String key) {
    this.name = name;
    this.seperator = seperator;
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Nullable
  public String getSeperator() {
    return seperator;
  }

  public void setSeperator(String seperator) {
    this.seperator = seperator;
  }

  @Nullable
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
