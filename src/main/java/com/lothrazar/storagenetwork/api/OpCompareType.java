package com.lothrazar.storagenetwork.api;
public enum OpCompareType {

  GREATER, LESS, EQUAL;

  public static OpCompareType get(int i) {
    return OpCompareType.values()[i];
  }

  public OpCompareType toggle() {
    switch (this) {
      case LESS:
        return EQUAL;
      case EQUAL:
        return GREATER;
      case GREATER:
        return LESS;
    }
    return OpCompareType.EQUAL;
  }

  public String symbol() {
    switch (this) {
      case LESS:
        return "<";
      case EQUAL:
        return "=";
      case GREATER:
      default:
        return ">";
    }
  }

  public String word() {
    switch (this) {
      case LESS:
        return "less";
      case EQUAL:
        return "eq";
      case GREATER:
      default:
        return "more";
    }
  }
}
