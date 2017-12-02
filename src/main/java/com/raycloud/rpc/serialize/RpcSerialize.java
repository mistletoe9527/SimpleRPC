package com.raycloud.rpc.serialize;

/**
 * Created by styb on 2017/12/2.
 */
public interface RpcSerialize {

    <T> byte[] encode(T obj);

    <T> T decode(byte[] data,Class<T> cls);
}
