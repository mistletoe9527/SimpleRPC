package com.raycloud.rpc.message;

/**
 * Created by styb on 2017/12/2.
 */
public class MessageResponse {

    private String messageId;

    private String error;

    private Object result;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
