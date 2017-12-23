package com.raycloud.rpc.client;

import java.lang.reflect.Proxy;

/**
 * Created by styb on 2017/12/22.
 */
public class PRCClient {

    public  static <T> T  create(Class<T> inteface){
        return (T)Proxy.newProxyInstance(inteface.getClassLoader(),new Class<?>[] {inteface},new RPCProxy());
    }
}
