package com.raycloud.rpc.container;

import com.raycloud.rpc.server.ano.RpcService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by styb on 2017/12/2.
 */
public class Container {

    public static Map<String,Object> exportMap=new HashMap<>();

    public static void main(String[] args) {
        ApplicationContext applicationContext=new ClassPathXmlApplicationContext("classpath:spring.xml");
        Map<String,Object> rpcServices=applicationContext.getBeansWithAnnotation(RpcService.class);
        if(!CollectionUtils.isEmpty(rpcServices.values())){
            for(Object bean : rpcServices.values()){
                String intefaceName=bean.getClass().getAnnotation(RpcService.class).value().getName();
                exportMap.put(intefaceName,bean);
                System.out.println(intefaceName);
            }
        }

    }

}
