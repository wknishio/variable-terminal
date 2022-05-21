package com.github.luben.zstd;

abstract class SharedDictBase extends AutoCloseBase {
  
  protected void finalize() {
    close();
  }
}
