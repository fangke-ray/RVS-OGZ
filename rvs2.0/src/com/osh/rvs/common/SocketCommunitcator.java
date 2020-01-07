package com.osh.rvs.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class SocketCommunitcator {

	public String clientSendMessage(String host, int port, String message) {
		try {
			// 创建Socket对象
			Socket socket = new Socket(host, port);

			// 根据输入输出流和服务端连接
			OutputStream outputStream = socket.getOutputStream();// 获取一个输出流，向服务端发送信息
			PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));// 将输出流包装成打印流
			printWriter.print(message); // 
			printWriter.flush();
			socket.shutdownOutput();// 关闭输出流

			InputStream inputStream = socket.getInputStream();// 获取一个输入流，接收服务端的信息
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);// 包装成字符流，提高效率
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);// 缓冲区
			String info = "";
			String temp = null;// 临时变量
			while ((temp = bufferedReader.readLine()) != null) {
				info += temp;
			}

			// 关闭相对应的资源
			bufferedReader.close();
			inputStream.close();
			printWriter.close();
			outputStream.close();
			socket.close();

			return info;

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return "UnknownHostException";
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException";
		}
	}
}
