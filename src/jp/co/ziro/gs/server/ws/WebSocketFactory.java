package jp.co.ziro.gs.server.ws;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.WebSocket;

public class WebSocketFactory {

	private static WebSocketFactory instance = null;

    private int idcounter = 0;
    private Map<GestureWebSocket, Integer> users = new ConcurrentHashMap<GestureWebSocket, Integer>();

    public static WebSocketFactory getInstance() {
    	if ( instance == null ) {
    		instance = new WebSocketFactory();
    	}
        return instance;
    }

    private WebSocketFactory() {
    }

    public WebSocket createInstance() {
        return new GestureWebSocket();
    }

    // WebSocketコネクションを登録する
    // ついでに適当なIDを割り当てる
    public void join(GestureWebSocket ws) {
        if (users.containsKey(ws))
            return;
        idcounter++;
        users.put(ws, idcounter);
        System.out.println("login " + idcounter);
    }

    // 登録されていたコネクションを取り除く
    public void leave(GestureWebSocket ws) {
        int id = users.get(ws);
        users.remove(ws);
        System.out.println("logoff " + id);
    }

    // WebSocketからメッセージが届いたら付加情報を付けて全コネクションに流す
    public void onMessage(GestureWebSocket ws, String msg) {
        int id = users.get(ws);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        msg = id + ": " + msg + " " + fmt.format(new Date());
        sendAll(msg);
    }

    // 抱えてる全コネクションに流す
    public synchronized static void sendMessage(String msg) {
    	System.out.println(msg);
    	getInstance().sendAll(msg);
    }

    // 抱えてる全コネクションに流す
    private void sendAll(String msg) {
        for (GestureWebSocket socket : users.keySet()) {
            try {
                socket.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
