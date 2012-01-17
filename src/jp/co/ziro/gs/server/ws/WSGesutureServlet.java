package jp.co.ziro.gs.server.ws;

import javax.servlet.http.HttpServletRequest;


import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

public class WSGesutureServlet extends WebSocketServlet {

    private static final long serialVersionUID = 8925349860339039372L;
    
    public WSGesutureServlet() {
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
        return WebSocketFactory.getInstance().createInstance();
    }
}
