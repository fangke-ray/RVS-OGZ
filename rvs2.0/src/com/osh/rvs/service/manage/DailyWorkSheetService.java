package com.osh.rvs.service.manage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

public class DailyWorkSheetService {
	Logger _log = Logger.getLogger(DailyWorkSheetService.class);

	public List<Map<String, Object>> searchFileName(String filepath) {

		List<Map<String, Object>> fileList = new ArrayList<Map<String, Object>>();

		File file = new File(filepath);
		if (file.exists()) {
			File[] fs = file.listFiles();
			// 遍历文件
			for (int i = 0; i < fs.length; i++) {
				Map<String, Object> fileMap = new HashMap<String, Object>();				
				if (!fs[i].isDirectory()) {
					
					String filename = fs[i].getName();
					// 文件名字
					fileMap.put("fileName", filename);
					// 文件生成时间
					fileMap.put("fileDayTime",filename.replaceAll(".*(\\d{4}\\-\\d{2}\\-\\d{2}).*", "$1"));
					File readfile = new File(filepath + "\\confirm\\" + filename);
					if (readfile.exists()) {
						fileMap.put("confirmfilename", filename);
					} else {
						fileMap.put("confirmfilename", "");
					}
					fileList.add(fileMap);
				}

			}

		}

		return fileList;
	}

	public void sendRespond(String jobNo, String fileName) throws IOReactorException, InterruptedException {
		String postFileName = fileName.replaceAll("包装检查表", "BZJCB");

		// 签当前人员章到确认者位置
		HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
		httpclient.start();
		try {
			String inUrl = "http://localhost:8080/rvspush/trigger/daily_report_respond/" + postFileName + "/" + jobNo + "/package";
			HttpGet request = new HttpGet(inUrl);
			_log.info("finger:" + request.getURI());
			httpclient.execute(request, null);
		} catch (Exception e) {
		} finally {
			Thread.sleep(100);
			httpclient.shutdown();
		}

	}
}