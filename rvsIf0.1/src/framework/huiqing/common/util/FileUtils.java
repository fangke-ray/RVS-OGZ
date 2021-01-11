package framework.huiqing.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

	/**
	 * 复制单个文件
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static void copyFile(String oldPath, String newPath) {
		copyFile(oldPath, newPath, false);
	}
	public static void copyFile(String oldPath, String newPath, boolean mk) {
		InputStream inStream = null;
		OutputStream fs = null;

		try {
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // 文件存在时
				inStream = new BufferedInputStream(new FileInputStream(oldPath)); // 读入原文件
				if (mk) {
					int idx = newPath.lastIndexOf("/");
					if (idx < 0) idx = newPath.lastIndexOf("\\");
					if (idx >= 0) {
						String newFolder = newPath.substring(0,idx);
						File newFolderFile = new File(newFolder);
						if (!newFolderFile.exists()) {
							newFolderFile.mkdirs();
						}
					}
					File newfile = new File(newPath);
					if (!newfile.exists()) {
						newfile.createNewFile();
					}
				}
				fs =  new BufferedOutputStream(new FileOutputStream(newPath));
				byte[] buffer = new byte[2048];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
					fs.flush();
				}
				inStream.close();
				fs.close();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();

		} finally {
			inStream = null;
			fs = null;
		}
	}

}
