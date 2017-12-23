package com.raycloud.rpc.netty;

import com.raycloud.rpc.client.RPCFuture;
import com.raycloud.rpc.message.MessageRequest;
import com.raycloud.rpc.message.MessageResponse;
import io.netty.channel.*;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * Created by styb on 2017/12/2.
 */
public class ServerHandler extends SimpleChannelInboundHandler<MessageRequest> {

    private Map<String,Object> handlerMap=new HashMap<String,Object>();

    public ServerHandler(Map<String,Object> handlerMap){
        this.handlerMap=handlerMap;
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageRequest messageRequest) throws Exception {

        MessageResponse response = new MessageResponse();
        response.setMessageId(messageRequest.getMessageId());
        try {
            Object result = handle(messageRequest);
            response.setResult(result);
        } catch (Throwable t) {
            response.setError(t.toString());
        }
        ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
            }
        });
    }

    private Object handle(MessageRequest request) throws Throwable {
        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getTypeParameters();
        Object[] parameters = request.getParameters();


        // JDK reflect
        /*Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);*/

        // Cglib reflect
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

}
