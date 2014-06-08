package com.flyingh.moguard.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

	public static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[1024 * 16];
		int len = -1;
		while ((len = is.read(buffer)) != -1) {
			os.write(buffer, 0, len);
		}
		os.close();
		is.close();
	}

}
