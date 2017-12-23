package com.raycloud.rpc.netty;

import com.raycloud.rpc.client.RPCFuture;
import com.raycloud.rpc.message.MessageRequest;
import com.raycloud.rpc.message.MessageResponse;
import io.netty.channel.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by styb on 2017/12/2.
 */
public class ClientHandler extends SimpleChannelInboundHandler<MessageResponse> {

    private Map<String,RPCFuture> responseMap=new HashMap<String,RPCFuture>();

    public Channel getChannel() {
        return channel;
    }

    private volatile Channel channel;


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageResponse messageResponse) throws Exception {

        if(responseMap.get(messageResponse.getMessageId())!=null){
            RPCFuture rpcFuture=responseMap.get(messageResponse.getMessageId());
            responseMap.remove(messageResponse.getMessageId());
            rpcFuture.setMessageResponse(messageResponse);
            rpcFuture.done();
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel=ctx.channel();
    }

    public RPCFuture sendRequest(MessageRequest messageRequest) {
        final CountDownLatch latch = new CountDownLatch(1);
        RPCFuture rpcFuture = new RPCFuture();
        responseMap.put(messageRequest.getMessageId(), rpcFuture);
        channel.writeAndFlush(messageRequest);

        channel.writeAndFlush(messageRequest).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return rpcFuture;
    }
}
