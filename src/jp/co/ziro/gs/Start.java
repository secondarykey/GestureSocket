package jp.co.ziro.gs;


public class Start {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

        Server server = new Server();
        Gesture gesture = new Gesture();

        //other thread
   		gesture.start();

        try {
			server.start();
			server.run();
		} catch (Exception e) {
			throw new RuntimeException("サーバ起動失敗",e);
		}
	}
}
