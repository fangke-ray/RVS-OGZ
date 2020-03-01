/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2019-12-28
 * 时间: 20:24
 *
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Windows.Forms;

namespace ProcedureStepCounter.Service
{
	public partial class RecSocket
		{
		public delegate void ShowInForm(String startAjax);
		public delegate string GetFromForm();
		static ShowInForm showProcessMaterial = null;
		static GetFromForm showFinishCount = null;
		static MainForm mainForm = null;

		public RecSocket(MainForm mF) {
			showProcessMaterial = new ShowInForm(mF.showProcessMaterial);
			showFinishCount = new GetFromForm(mF.getCount);
			mainForm = mF;
		}
		public void ListenMethod()
		{
			try {
			Console.WriteLine("hello world!");
			//新建一个套接字Socket即创建Socket
			//Socket（AddressFamily.InterNetwork， SocketType.Stream， ProtocolType.Tcp）
			// 这一行创建了一个套接字， 它的3个参数分别代表地址族、 套接字类型和协议。
			// 地址族指明是使用IPv4还是IPv6， 含义如表6-8所示， 本例中使用的是IPv4， 即InterNetwork
			Socket listenfd =
				new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
			// Bind指定套接字的IP和端口
			// listenfd.Bind（ipEp） 将给listenfd套接字绑定IP和端口。
			// 示例程序中绑定的是本地地址“127.0.0.1”和1234号端口。
			// 127.0.0.1是回送地址， 指本地机， 一般用于测试。
			// 读者也可以设置成真实的IP地址， 然后在两台电脑上分别运行客户端和服务端程序。
			IPAddress ipAdr = IPAddress.Parse(Common.local_ip);// 10.220.126.181
			// 根据IP地址创建IPAddress对象，如IPAddress.Parse("192.168.1.1")

			IPEndPoint ipEp = new IPEndPoint(ipAdr, 50023);//用IPAddress指定的地址和端口号初始化
			listenfd.Bind(ipEp);
			//服务端通过listenfd.Listen（0） 开启监听， 等待客户端连接。
			// 参数backlog指定队列中最多可容纳等待接受的连接数， 0表示不限制
			listenfd.Listen(0);
			Console.WriteLine("[服务器]启动成功");

			while(true) {
				//Accept
				//开启监听后， 服务器调用listenfd.Accept（） 接收客户端连接。
				// 本例使用的所有Socket方法都是阻塞方法， 也就是说当没有客户端连接时，
				// 服务器程序会卡在listenfd.Accept（） 处， 而不会往下执行， 直到
				// 接收了客户端的连接。 Accept返回一个新客户端的Socket， 对于服务器来说，
				// 它有一个监听Socket（例子中的listenfd） 用来接收（Accept） 客户端的连接，
				// 对每个客户端来说还有一个专门的Socket（例子中的 connfd） 用来处理该客户端的数据
				Socket connfd = listenfd.Accept();
				Console.WriteLine("[服务器]Accept");

				IPEndPoint netPoint = connfd.RemoteEndPoint as IPEndPoint;
				IPAddress clientIp = netPoint.Address;
				int clientPort = netPoint.Port;

				// connfd.Send("IP");

				Thread recvThread = new Thread(new ParameterizedThreadStart(recv));
				recvThread.IsBackground = true;
				recvThread.Start(connfd);
			}
			} catch (Exception e) {
				MessageBox.Show("建立接收服务失败!" + e.Message,  "Error");
			}
		}
		static void recv(object socketServerParam)
		{
			Socket socketServer = socketServerParam as Socket;
			string receiveStr = "";
			while(true)
			{
				try{
					// Recv
					// 服务器通过connfd.Receive接收客户端数据， Receive也是阻塞方法， 没有收到客户端数据时，
					// 程序将卡在Receive处， 而不会往下执行。 Receive带有一个byte[]类型的参数，
					// 它将存储接收到的数据， Receive的返回值则指明接收到的数据的长度。
					// 之后使用System.Text.Encoding.UTF8.GetString（readBuff， 0， count）
					// 将byte[] 数组转换成字符串显示在屏幕上。
					byte[] readBuff = new byte[1024];
					int length = socketServer.Receive(readBuff);
					if (length == 0) {
						break;
					}
					string str = System.Text.Encoding.UTF8.GetString(readBuff, 0, length);

					Console.WriteLine("[服务器接收]" + str);
					receiveStr += str;
				} catch(Exception e) {
					socketServer.Close();
					break;
				}
			}

			if (socketServer.Connected) {
				//Send
				// 服务器通过connfd.Send发送数据， 它接受一个byte[]类型的参数指明要发送的内容。
				// Send的返回值指明发送数据的长度（例子中没有使用） 。
				// 服务器程序用System.Text.Encoding.Default.GetBytes（字符串）
				// 把字符串转换成byte[] 数组， 然后发送给客户端（且会在字符串前面加上“serv echo”）
				if (receiveStr.StartsWith("In:")) {
					showProcessMaterial(receiveStr.Substring(3));
				} else if (receiveStr.StartsWith("Out:")) {
					string countString = showFinishCount();
					byte[] bytes = System.Text.Encoding.Default.GetBytes("getCount:" + countString);
					socketServer.Send(bytes);
				}
				socketServer.Close();
			}
		}
	}
}