package com.raycloud.rpc.registry;

import com.raycloud.rpc.client.Connects;
import com.raycloud.rpc.client.URL;
import com.raycloud.rpc.constant.Constant;
import com.raycloud.rpc.netty.ClientHandler;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 服务发现
 *
 */
public class ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);

    private  CountDownLatch latch = new CountDownLatch(1);
    private List<String> dataList=new ArrayList<>();
    public volatile Map<String,List<String>> provides=new  ConcurrentHashMap<String,List<String>>();
    private ZooKeeper zookeeper;
    private Lock lock=new ReentrantLock();
    private static ServiceDiscovery serviceDiscovery=new ServiceDiscovery("st01:2181");
    public static ServiceDiscovery getInstance(){
        return serviceDiscovery;
    }
    public ServiceDiscovery(String registry) {
        zookeeper = connectServer(registry);
    }


    private ZooKeeper connectServer(String registry) {
        final CountDownLatch latch=new CountDownLatch(1);
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registry, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            logger.error("", e);
        }
        return zk;
    }

    public void watchNode(final URL url){
        try{
            List<String> nodeList = zookeeper.getChildren("/registry/"+url.getClassName()+Constant.PROVIDE_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(url);
                    }
                }
            });
            provides.put(url.getClassName(),nodeList);

            UpdateConnectedServer();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void UpdateConnectedServer(){
        Set<String> dataList=new HashSet<>();
        if(!provides.isEmpty()){
            for(Map.Entry<String,List<String>> entry:provides.entrySet()){
                for(String server:entry.getValue()){
                    dataList.add(server.split(":")[2]+":"+server.split(":")[3]);
                }

            }
        }
        Connects.getInstance().setProvides(provides);
        Connects.getInstance().updateHandlers(dataList);
    }

    public void stop(){
        if(zookeeper!=null){
            try {
                zookeeper.close();
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }
}
