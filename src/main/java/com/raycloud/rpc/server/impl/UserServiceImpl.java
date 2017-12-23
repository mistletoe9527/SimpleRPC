package com.raycloud.rpc.server.impl;

import com.raycloud.rpc.server.UserService;
import com.raycloud.rpc.server.ano.*;
import org.springframework.stereotype.Component;

/**
 * Created by styb on 2017/12/2.
 */
@RpcService(UserService.class)
@RpcRegistry("st01:2181")
@RpcVersion("1.0.0")
@RpcServer("127.0.0.1")
@RpcPort("8888")
@Component
public class UserServiceImpl implements UserService{

    @Override
    public int test(String v1, int v2) {
        return Integer.parseInt(v1)+v2;
    }

    @Override
    public int test(int a, int b) {
        return a*b;
    }
}
