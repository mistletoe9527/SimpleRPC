package com.raycloud.rpc.container;

import com.raycloud.rpc.client.PRCClient;
import com.raycloud.rpc.client.URL;
import com.raycloud.rpc.message.MessageRequest;
import com.raycloud.rpc.message.MessageResponse;
import com.raycloud.rpc.netty.ClientHandler;
import com.raycloud.rpc.netty.RpcDecoder;
import com.raycloud.rpc.netty.RpcEncoder;
import com.raycloud.rpc.netty.ServerHandler;
import com.raycloud.rpc.registry.ServiceDiscovery;
import com.raycloud.rpc.registry.ServiceRegistry;
import com.raycloud.rpc.server.UserService;
import com.raycloud.rpc.server.ano.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by styb on 2017/12/2.
 */
@Component
public class Container implements ApplicationContextAware,InitializingBean{

    public static Map<String,Object> exportMap=new HashMap<>();


    public static void registry(ServiceRegistry serviceRegistry,Object bean){
        String intefaceName=bean.getClass().getAnnotation(RpcService.class).value().getName();
        exportMap.put(intefaceName,bean);
        RpcPort port =bean.getClass().getAnnotation(RpcPort.class);
        RpcRegistry registry =bean.getClass().getAnnotation(RpcRegistry.class);
        RpcServer server =bean.getClass().getAnnotation(RpcServer.class);
        RpcVersion version =bean.getClass().getAnnotation(RpcVersion.class);
        URL url=new URL();
        url.setPort(port==null?"8888":port.value());
        url.setRegistryURL(registry==null?"st01:2181":registry.value());
        url.setServer(server==null?"127.0.0.1":server.value());
        url.setVersion(version==null?"1.0.0":version.value());
        url.setClassName(intefaceName);
        serviceRegistry.register(url);
        //服务发现
        URL u=new URL();
        u.setRegistryURL("st01:2181");
        u.setClassName(intefaceName);
        ServiceDiscovery.getInstance().watchNode(u);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        Map<String,Object> rpcServices=applicationContext.getBeansWithAnnotation(RpcService.class);
//        if(!CollectionUtils.isEmpty(rpcServices.values())){
//            //选取注册中心暴露服务
//            ServiceRegistry serviceRegistry=new ServiceRegistry();
//            for(Object bean : rpcServices.values()){
//                //将服务信息注册到 注册中心
//                registry(serviceRegistry,bean);
//            }
//        }
    }

    public static void main(String[] args) throws Exception{
        ApplicationContext applicationContext=new ClassPathXmlApplicationContext("classpath:spring.xml");
        Map<String,Object> rpcServices=applicationContext.getBeansWithAnnotation(RpcService.class);
        if(!CollectionUtils.isEmpty(rpcServices.values())){
            //服务启动
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel channel) throws Exception {
                                channel.pipeline()
                                        .addLast(new LengthFieldBasedFrameDecoder(65536,0,4,0,0))
                                        .addLast(new RpcDecoder(MessageRequest.class))
                                        .addLast(new RpcEncoder(MessageResponse.class))
                                        .addLast(new ServerHandler(exportMap));
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture future = bootstrap.bind("127.0.0.1", 8888).sync();
                //启动完成
                //选取注册中心暴露服务
                ServiceRegistry serviceRegistry=new ServiceRegistry();
                for(Object bean : rpcServices.values()){
                    //将服务信息注册到 注册中心
                    registry(serviceRegistry,bean);
                }

                test();

                future.channel().closeFuture().sync();
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        EventLoopGroup bossGroup = new NioEventLoopGroup();
//        EventLoopGroup workerGroup = new NioEventLoopGroup();
//        try {
//            ServerBootstrap bootstrap = new ServerBootstrap();
//            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
//                    .childHandler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        public void initChannel(SocketChannel channel) throws Exception {
//                            channel.pipeline()
//                                    .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
//                                    .addLast(new RpcDecoder(MessageRequest.class))
//                                    .addLast(new RpcEncoder(MessageResponse.class))
//                                    .addLast(new ClientHandler());
//                        }
//                    })
//                    .option(ChannelOption.SO_BACKLOG, 128)
//                    .childOption(ChannelOption.SO_KEEPALIVE, true);
//
//            ChannelFuture future = bootstrap.bind("127.0.0.1", 8888).sync();
//            future.channel().closeFuture().sync();
//        } finally {
//            workerGroup.shutdownGracefully();
//            bossGroup.shutdownGracefully();
//        }
    }

    public static void test(){
        UserService userService= PRCClient.create(UserService.class);
        System.out.println(userService.test(1, 5));
    }
}
