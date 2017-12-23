package com.raycloud.rpc.client;

import com.raycloud.rpc.message.MessageRequest;
import com.raycloud.rpc.message.MessageResponse;
import com.raycloud.rpc.netty.ClientHandler;
import com.raycloud.rpc.netty.RpcDecoder;
import com.raycloud.rpc.netty.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by styb on 2017/12/2.
 */
public class Connects {

    private Connects(){}

    public List<ClientHandler> handlers=new CopyOnWriteArrayList<>();

    public Map<String,ClientHandler> handlerMap=new ConcurrentHashMap<>();

    public volatile Map<String,List<String>> provides;

    private static Connects connects=new Connects();

    public static Connects getInstance(){
        return connects;
    }

    public void updateHandlers(Set<String> dataList){

        if(!CollectionUtils.isEmpty(dataList)) {
            for (String data : dataList) {
                openConntet(data);
            }
        }

    }
    public void openConntet(final String data){
        if(handlerMap.containsKey(data)){
            return;
        }
        final CountDownLatch countDownLatch=new CountDownLatch(1);
        String server=data.split(":")[0];
        String port=data.split(":")[1];
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
                            .addLast(new ClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(server, Integer.parseInt(port)).sync(); // (5)
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    handlers.add(channelFuture.channel().pipeline().get(ClientHandler.class));
                    handlerMap.put(data,channelFuture.channel().pipeline().get(ClientHandler.class));
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            // Wait until the connection is closed.
//            f.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
//            workerGroup.shutdownGracefully();
        }
    }


    public ClientHandler getHandler(String className)throws Exception{

        List<String> serverList=provides.get(className);
        if(CollectionUtils.isEmpty(serverList)){
            throw new Exception("no provide classname="+className);
        }else{
            //选择一个
            if(serverList.size()==1){
                String data = serverList.get(0).split(":")[2]+":"+serverList.get(0).split(":")[3];
                ClientHandler clientHandler=handlerMap.get(data);
                if(clientHandler!=null){
                    return clientHandler;
                }
            }else{
                //有待开发 选取策略
                String data = serverList.get(0).split(":")[2]+":"+serverList.get(0).split(":")[3];
                ClientHandler clientHandler=handlerMap.get(data);
                if(clientHandler!=null){
                    return clientHandler;
                }
            }
        }
        return null;
    }

    public Map<String, List<String>> getProvides() {
        return provides;
    }

    public void setProvides(Map<String, List<String>> provides) {
        this.provides = provides;
    }
}
