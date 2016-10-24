package websocket.handlers;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

/**
 * Created by wangziqing on 16/10/24.
 */
public class ChatHandler implements Handler{
    @Override
    public boolean handleMessage(MessageContext context) {
        return false;
    }

    @Override
    public boolean handleFault(MessageContext context) {
        return false;
    }

    @Override
    public void close(MessageContext context) {

    }
}
