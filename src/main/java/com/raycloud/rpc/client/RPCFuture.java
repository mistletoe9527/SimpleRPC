package com.raycloud.rpc.client;

import com.raycloud.rpc.message.MessageResponse;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by styb on 2017/12/5.
 */
public class RPCFuture {

    private Lock lock=new ReentrantLock();
    private Logger logger= org.slf4j.LoggerFactory.getLogger(RPCFuture.class);

    private Condition isReturn=lock.newCondition();
    private MessageResponse messageResponse;
    public Object get(URL url){
        try{
            lock.lock();
            isReturn.await();
            messageResponse.getResult();
        }catch (InterruptedException e){
            logger.debug("超时啦---msg= {} "+e.getMessage(),e.getMessage());
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }

        return messageResponse.getResult();
    }
    public void done(){
        try{
            lock.lock();
            isReturn.signal();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }

    }
    public MessageResponse getMessageResponse() {
        return messageResponse;
    }

    public void setMessageResponse(MessageResponse messageResponse) {
        this.messageResponse = messageResponse;
    }

    public Condition getIsReturn() {
        return isReturn;
    }

    public void setIsReturn(Condition isReturn) {
        this.isReturn = isReturn;
    }
}
