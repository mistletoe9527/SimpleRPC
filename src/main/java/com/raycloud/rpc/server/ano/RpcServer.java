package com.raycloud.rpc.server.ano;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PRC annocation for service
 *
 * Created by styb on 2017/12/2.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcServer {
    String value();
}
