package jp.co.ziro.gs;

public class Start {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

        GestureDisplay gesture = new GestureDisplay();
        //other thread
   		gesture.start();

        Server server = new Server();
        try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException("サーバ起動失敗",e);
		}
		server.run();
	}
}
