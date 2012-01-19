package jp.co.ziro.gs;

import jp.co.ziro.gs.util.ApplicationUtil;

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

	private static float margin   = ApplicationUtil.getFloat("gesture.margin");
	private static float movement = ApplicationUtil.getFloat("gesture.movement");

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
			if ( Math.abs(modY) < (margin*6) && 
				 Math.abs(modX) < (margin*6) ) {
				if ( modZ < (movement*-0.7) ) {
					type = SocketType.ZOOMIN;
				} else if ( modZ > (movement*0.7) ) {
					type = SocketType.ZOOMOUT;
				}
			}
		}
		return type;
	}
}
