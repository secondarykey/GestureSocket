package jp.co.ziro.gs.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationUtil {

	private static Properties gblProperty;
	private static void load(String path) {
		InputStream stream;
		try {
			stream = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("設定ファイルが存在しません。",e);
		}
		try {
			gblProperty = new Properties();
			gblProperty.load(stream);
		} catch (IOException e) {
			throw new RuntimeException("設定ファイルの読み込みに失敗しました。",e);
		}
	}

	public static String get(String key) {
		if ( gblProperty == null ) {
			load("application.properties");
		}
		return gblProperty.getProperty(key);
	}

	public static Integer getInteger(String key) {
		return Integer.valueOf(get(key));
	}

	public static float getFloat(String key) {
		return Float.valueOf(get(key));
	}
}
