package com.raycloud.rpc.netty;

import com.raycloud.rpc.message.MessageResponse;
import com.raycloud.rpc.serialize.RpcSerialize;
import com.raycloud.rpc.serialize.protostuff.ProtostuffSerialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by styb on 2017/12/2.
 */
public class RpcEncoder extends MessageToByteEncoder{

    private Class<?> encodeClass;
    public RpcEncoder(Class<?> encodeClass){
        this.encodeClass=encodeClass;
    }
    private RpcSerialize rpcSerialize=new ProtostuffSerialize();
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object obj, ByteBuf byteBuf) throws Exception {
        if(encodeClass.isInstance(obj)){
            byte[] data=rpcSerialize.encode(obj);
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }
    }
}
