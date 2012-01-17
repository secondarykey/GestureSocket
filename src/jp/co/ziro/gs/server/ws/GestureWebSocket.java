package jp.co.ziro.gs.server.ws;

import java.io.IOException;


import org.eclipse.jetty.websocket.WebSocket;

public class GestureWebSocket implements WebSocket.OnTextMessage{

    private Connection connection = null;
    public GestureWebSocket() {
    }

    @Override
    public void onOpen(Connection con) {
        connection = con;
        WebSocketFactory.getInstance().join(this);
    }

    @Override
    public void onClose(int arg0, String arg1) {
    	WebSocketFactory.getInstance().leave(this);
    }

    @Override
    public void onMessage(String msg) {
    	WebSocketFactory.getInstance().onMessage(this, msg);
    }

    /**
     * メッセージ送信
     * @param msg
     * @throws IOException
     */
    public void send(String msg) throws IOException {
    	connection.sendMessage(msg);
    }
}
