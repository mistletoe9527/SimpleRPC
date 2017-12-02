package com.raycloud.rpc.netty;

import com.raycloud.rpc.message.MessageRequest;
import com.sun.tools.javadoc.Messager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by styb on 2017/12/2.
 */
public class TestHandler extends SimpleChannelInboundHandler<MessageRequest> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("have connetion -----");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageRequest messageRequest) throws Exception {
        System.out.println(messageRequest.getMessageId());
        System.out.println(messageRequest.getClassName());
    }
}
