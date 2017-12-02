package com.raycloud.rpc.netty;

import com.raycloud.rpc.message.MessageRequest;
import com.raycloud.rpc.message.MessageResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.UUID;

/**
 * Created by styb on 2017/12/2.
 */
public class Client {
    public static void main(String[] args) throws Exception{
        String host = "127.0.0.1";
        int port = 8888;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                    .addLast(new RpcEncoder(MessageRequest.class))
                    .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                    .addLast(new RpcDecoder(MessageResponse.class))
                    .addLast(new TestHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)
            MessageRequest messageRequest=new MessageRequest();
            messageRequest.setMessageId(UUID.randomUUID().toString());
            messageRequest.setClassName("hahahah");
            f.channel().writeAndFlush(messageRequest);

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
