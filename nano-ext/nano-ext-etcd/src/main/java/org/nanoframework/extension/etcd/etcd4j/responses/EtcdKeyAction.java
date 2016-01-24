package org.nanoframework.extension.etcd.etcd4j.responses;

/**
 * The etcd key response actions
 */
public enum EtcdKeyAction {
  set,
  get,
  create,
  update,
  delete,
  expire,
  compareAndSwap,
  compareAndDelete
}