package com.raycloud.rpc.serialize.protostuff;

import com.raycloud.rpc.serialize.RpcSerialize;

/**
 * Created by styb on 2017/12/2.
 */
public class ProtostuffSerialize implements RpcSerialize{

    @Override
    public <T> byte[] encode(T obj) {
        return ProtustuffCodecUtil.serialize(obj);
    }

    @Override
    public <T> T decode(byte[] data, Class<T> cls) {
        return ProtustuffCodecUtil.deserialize(data,cls);
    }
}
