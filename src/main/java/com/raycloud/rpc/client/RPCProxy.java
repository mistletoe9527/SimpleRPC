package com.raycloud.rpc.client;

import com.raycloud.rpc.message.MessageRequest;
import com.raycloud.rpc.netty.ClientHandler;
import com.raycloud.rpc.registry.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by styb on 2017/12/3.
 */
public class RPCProxy implements InvocationHandler{


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        MessageRequest messageRequest=new MessageRequest();
        messageRequest.setClassName(method.getDeclaringClass().getName());
        messageRequest.setMethodName(method.getName());
        messageRequest.setMessageId(UUID.randomUUID().toString());
        messageRequest.setTypeParameters(method.getParameterTypes());
        messageRequest.setParameters(args);
//        Thread.sleep(10000);com.raycloud.rpc.server.UserService
        System.out.println(method.getDeclaringClass().getName());
        ClientHandler clientHandler=Connects.getInstance().getHandler(method.getDeclaringClass().getName());
        RPCFuture rpcFuture=clientHandler.sendRequest(messageRequest);
        URL url=new URL();
        url.setTimeout(1000);
        return rpcFuture.get(url);

    }




}
