package com.raycloud.rpc.netty;

import com.raycloud.rpc.message.MessageRequest;
import com.raycloud.rpc.serialize.RpcSerialize;
import com.raycloud.rpc.serialize.protostuff.ProtostuffSerialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by styb on 2017/12/2.
 */
public class RpcDecoder extends ByteToMessageDecoder{

    private Class<?> decodeClass;
    private RpcSerialize rpcSerialize=new ProtostuffSerialize();
    public RpcDecoder(Class<?> decodeClass){
        this.decodeClass=decodeClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(byteBuf.readableBytes()<=4){
            return;
        }
        byteBuf.markReaderIndex();
        int length=byteBuf.readInt();
        if(byteBuf.readableBytes()<length){
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data=new byte[length];
        byteBuf.readBytes(data);
        Object object=rpcSerialize.decode(data,decodeClass);
        list.add(object);
    }
}
