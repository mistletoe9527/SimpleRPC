package com.raycloud.rpc.registry;

/**
 * register service
 * Created by styb on 2017/12/3.
 */
import java.util.concurrent.CountDownLatch;

import com.raycloud.rpc.client.Connects;
import com.raycloud.rpc.client.URL;
import com.raycloud.rpc.constant.Constant;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);


    public ServiceRegistry() {
    }

    public void register(URL url) {
        if (url!=null && url.getClassName() != null) {
            ZooKeeper zk = connectServer(url);
            if (zk != null) {
                AddRootNode(zk); // Add root node if not exist
                createNode(zk, url);
            }
        }
    }

    private ZooKeeper connectServer(URL url) {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(url.getRegistryURL(), 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e){
            logger.error("error msg == "+e.getMessage(), e);
        }
        return zk;
    }

    private void AddRootNode(ZooKeeper zk){
        try {
            Stat s = zk.exists(Constant.REGISTRY_PATH, false);
            if (s == null) {
                zk.create("/registry", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            logger.error("error msg ="+e.getMessage(),e);
        }
    }

    private void createNode(ZooKeeper zk, URL url) {
        try {
            Stat s=zk.exists(Constant.REGISTRY_PATH+"/"+url.getClassName(),false);
            if(s == null){
                zk.create(Constant.REGISTRY_PATH+"/"+url.getClassName(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zk.create(Constant.REGISTRY_PATH+"/"+url.getClassName()+Constant.PROVIDE_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zk.create(Constant.REGISTRY_PATH+"/"+url.getClassName()+Constant.CONSUME_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            Stat ss=zk.exists(Constant.REGISTRY_PATH+"/"+url.getClassName()+Constant.PROVIDE_PATH+"/version:"+url.getVersion()+":"+url.getServer()+":"+url.getPort(),false);
            if(ss==null)
                zk.create(Constant.REGISTRY_PATH+"/"+url.getClassName()+Constant.PROVIDE_PATH+"/version:"+url.getVersion()+":"+url.getServer()+":"+url.getPort(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

//            String path = zk.create("/registry/data", bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
//            logger.debug("create zookeeper node ({} => {})", path, data);
        } catch (Exception e){
            logger.error("error msg = "+e.getMessage(), e);
        }
    }
}
