package jp.co.ziro.gs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jp.co.ziro.gs.server.ws.WSGesutureServlet;
import jp.co.ziro.gs.server.ws.WebSocketFactory;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Server extends org.eclipse.jetty.server.Server {

	public Server() {

		super(80);

        // Jetty の初期化
        setStopAtShutdown(true);
        setGracefulShutdown(1000);
        ServletContextHandler root = new ServletContextHandler(this, "/", ServletContextHandler.SESSIONS);

        // デフォルトのWebインタフェースの設定
        root.setResourceBase("./war");
        root.addServlet(DefaultServlet.class, "/*");

        // WebSocket 受け付ける Servlet を登録
        ServletHolder wsh = new ServletHolder(new WSGesutureServlet());
        root.addServlet(wsh, "/ws/*");
	}

	/**
	 * コンソールの処理
	 */
	public void run() {

        try {
        	
        	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        	// コマンド入力待ち
        	// その他はサーバ側からのメッセージ送信扱い
        	while (true) {

            	System.out.print("GS>");
            	String line;
				try {
					line = reader.readLine();
				} catch (IOException e) {
					throw new RuntimeException("データ読み取り失敗",e);
				}
	
            	if (line == null || "".equals(line.trim())) {
                	continue;
            	} else if ("q".equals(line.trim())) {
                	WebSocketFactory.sendMessage(SocketType.SHUTDOWN.message());
                	break;
            	} else {
                	WebSocketFactory.sendMessage(SocketType.MESSAGE .message()+ line.trim());
            	}
        	}
        } finally {
            try {
    			stop();
    			join();
    			System.exit(0);
    		} catch (Exception e) {
    			throw new RuntimeException("サーバのクローズ失敗",e);
    		}
        }
	}
}
