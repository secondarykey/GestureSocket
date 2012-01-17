package jp.co.ziro.gs;

public enum SocketType {

	LEFT("left"),
	RIGHT("right"),
	UP("up"),
	DOWN("down"),
	ZOOMIN("in"),
	ZOOMOUT("out"),
	NONE(""), 
	MESSAGE("root "),
	READY(MESSAGE.message()    + "ready"),
	LOST(MESSAGE.message()     + "lost"),
	SHUTDOWN(MESSAGE.message() + "shutdown");

	private String message;
	private SocketType(String message) {
		this.message = message;
	}
	public String message() {
		return this.message;
	}

	private static float margin = 12;
	private static float movement = 230;

	/**
	 * どのGestureか判別
	 * @param modX
	 * @param modY
	 * @param modZ
	 * @return
	 */
	public static SocketType getType(float modX, float modY, float modZ) {

		SocketType type = SocketType.NONE;
		if ( Math.abs(modY) < margin ) {
			if ( modX < (movement*-1) ) {
				type = SocketType.LEFT;
			} else if ( modX > movement ) {
				type = SocketType.RIGHT;
			}
		} else if ( Math.abs(modX) < margin ) {
			if ( modY < (movement*-1) ) {
				type = SocketType.DOWN;
			} else if ( modY > movement ) {
				type = SocketType.UP;
			}
		} else {
			if ( modZ < (movement*-1) ) {
				type = SocketType.ZOOMIN;
			} else if ( modZ > movement ) {
				type = SocketType.ZOOMOUT;
			}
		}
		return type;
	}
}