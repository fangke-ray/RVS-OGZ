/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2019-12-30
 * 时间: 20:36
 *
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.Collections;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO.Ports;
using System.Text;
using System.Threading;

namespace ProcedureStepCounter.Service
{
	struct TaskContent
	{
		public int idx;
		public string PortName;
		public byte[] sBytes;
		public int needRetry;
	};

	/// <summary>
	/// Description of ComConnector.
	/// </summary>
	public class ComConnector
	{
		public ComConnector()
		{
		}

		public static Dictionary<string, SerialPort> coms 
			= new Dictionary<string, SerialPort> {

		};
		public static Dictionary<string, long> sendTokens 
			= new Dictionary<string, long>();
		private static Dictionary<string, int> respIdxes 
			= new Dictionary<string, int>();
		public static Dictionary<string, int> baudRateOfCom 
			= new Dictionary<string, int>();

		static Dictionary<string, int> ComTaskQueueKeys
			= new Dictionary<string, int>();
		static ConcurrentQueue<TaskContent> ComTaskQueue 
			= new ConcurrentQueue<TaskContent>();

		public static int[] retCounts = {};

		static Thread tth = null;

		public static void InitTaskQueue() {
			ComConnector.tth = new Thread(ScanTaskQueue);
			ComConnector.tth.Start();
		}

		public static void CloseAllComs() {
			if (ComConnector.tth != null) {
				ComConnector.tth.Abort();
			}
			ClearIntoTaskQueue();
			if (coms != null) {
				foreach (SerialPort com in coms.Values) {
					if (com != null && com.IsOpen)
					{
						try
						{
							com.Close();
							com.Dispose();
						}
						catch (Exception e)
						{
							System.Diagnostics.Debug.WriteLine("com close exception:" + e.Message);
						}
					}
				}

				coms = null;
			}
		}

		public static String GetCom(string PortName)
		{
//			if (idx >= coms.Length) {
// 				Array.Resize(ref coms, idx + 1); // Array<SerialPort>
//			}
			string[] portNames = SerialPort.GetPortNames();
			if (portNames.Length == 0)
			{
				return "本机未连接" + PortName + "端口！\0D";
			}
			SerialPort com = null;
			if (coms.ContainsKey(PortName)) {
				com = coms[PortName];
			}

			if (com != null && com.IsOpen)
			{
//				try
//				{
//					this.com.Close();
//				}
//				catch (Exception)
//				{
//				}
//				this.com = null;
			} else {
				com = new SerialPort();
				com.BaudRate = baudRateOfCom[PortName];
				com.PortName = PortName;
				com.DataBits = 8;
				com.StopBits = StopBits.One;
				com.WriteTimeout = 500;
//				com.DtrEnable = true;
//				com.RtsEnable = true;
				com.ReceivedBytesThreshold = 1;
				com.DataReceived += new SerialDataReceivedEventHandler((sender, e) => 
				           port_DataReceived(PortName, sender, e)
				      );
				com.ErrorReceived += new SerialErrorReceivedEventHandler((sender, e) => 
				           port_ErrorReceived(PortName, sender, e)
				      );

				try
				{
					com.Open();
				}
				catch (Exception ex)
				{
					if (ex.Message.IndexOf("is Denied") >= 0)
					{
						try
						{
							com.Dispose();
							com.Open();
							return null;
						}
						catch (Exception)
						{
							return "本机无权连接" + PortName + "端口！\0D";
						}
					}
				}
				coms[PortName] = com;
			}

			return null;
		}

		public static String CheckSendTokens() {
			lock(sendTokens) {
				long Now = DateTime.Now.ToFileTime();
				foreach (string PortName in sendTokens.Keys) {
					long Timeleep = (Now - sendTokens[PortName]);
					if (Timeleep > 100000000) { // 开个监控
						SerialPort com = coms[PortName];
						com.DiscardOutBuffer();
						com.DiscardInBuffer();
						com.Close();
						GetCom(PortName);
						ClearIntoTaskQueue();

						System.Diagnostics.Debug.WriteLine("Reconnectted");
						sendTokens.Remove(PortName);
						return "端口访问超时导致端口" + PortName + "重启。";
					}
				}
			}
			return null;
		}

		public static String sendToMachine(int idx, string PortName, byte[] sBytes) {
			return sendToMachine(idx, PortName, sBytes, 5);
		}
		public static String sendToMachine(int idx, string PortName, byte[] sBytes, int needRetry)
		{
			if (retCounts.Length <= idx) {
				Array.Resize(ref retCounts, idx + 1);
			}
			if (!coms.ContainsKey(PortName)) {
				// 尝试连接一次
				GetCom(PortName);
			}
			if (coms.ContainsKey(PortName))
			{
				SerialPort com = coms[PortName];
				if (!com.IsOpen) {
					String retGetCom = GetCom(PortName);
					if (retGetCom != null) {
						return retGetCom;
					}
				}
				try
				{
//					for (int i=0;;i++) {
						if (sendTokens.ContainsKey(PortName)) {
							// 还在接收中
							if (needRetry > 0) {
								AddIntoTaskQueue(idx, PortName, sBytes, needRetry--);
							}
							return null;
						}

//						System.Threading.Thread.Sleep(300);
//						if (i >= 5) {
//							return null;
//						}
//					}

					System.Diagnostics.Debug.WriteLine("Snd to " + AnyBytesToString(sBytes) +" @" + DateTime.Now.ToFileTime());
					com.Write(sBytes, 0, sBytes.Length);
					if (sBytes[0] > 0) {
						string tokenKey = PortName + ":" + sBytes[0];

						if (respIdxes.ContainsKey(tokenKey)) {
							respIdxes[tokenKey] = idx;
						} else {
							respIdxes.Add(tokenKey, idx);
						}
						if (sendTokens.ContainsKey(PortName)) {
							sendTokens[PortName] = DateTime.Now.ToFileTime();
						} else {
							sendTokens.Add(PortName, DateTime.Now.ToFileTime());
						}
					}
				}
				catch (Exception ex)
				{
					return "端口无法进行通讯！" + ex.Message;
				}
				return null;
			}

			return "端口无法进行通讯！";
		}

		private static void port_DataReceived(string PortName, object sender, SerialDataReceivedEventArgs e)
		{
			SerialPort com = coms[PortName];

			if (!com.IsOpen) return;

			List<byte> rxbyteList = new List<byte>();

			int n = com.BytesToRead;
			try {
				while (n > 0) {
					byte[] rxbyte = new byte[n];
					com.Read(rxbyte, 0, n);

					rxbyteList.AddRange(rxbyte);

					Thread.Sleep(150);

					n = com.BytesToRead;
				}
			} catch (Exception readE) {
				System.Diagnostics.Debug.WriteLine("readE." + readE.Message);
			}
			// ModBus协议来讲，第三位即代表了将要收到的数据时多少位

			byte[] rxbyteArray = rxbyteList.ToArray();
			System.Diagnostics.Debug.WriteLine("Rcv:[" + AnyBytesToString(rxbyteArray) + "]");
			if (rxbyteArray.Length > 3) {
				string tokenKey = PortName + ":" + rxbyteArray[0];
				int respIdx = -1;

				if (sendTokens.ContainsKey(PortName)) {
					sendTokens.Remove(PortName);
				}

				if (respIdxes.ContainsKey(tokenKey)) {
					respIdx = respIdxes[tokenKey];
				} else {
					return;
				}
				// 仪表地址
				// 操作指令码 0x03 读出
				if (rxbyteArray[1] == 3 && rxbyteArray[2] == 4) {
					try {
						string sNumberHigh = ByteToString(rxbyteArray[5]);
						string sNumberLow = ByteToString(rxbyteArray[6]);

						retCounts[respIdx] = Convert.ToInt32(sNumberHigh) * 100
						+ Convert.ToInt32(sNumberLow);
					} catch {
						retCounts[respIdx] = -1;
					}
				}
			}
		}

		private static void port_ErrorReceived(string PortName, object sender, SerialErrorReceivedEventArgs e)
		{
			SerialPort com = coms[PortName];
			int n = com.BytesToRead;

			System.Diagnostics.Debug.WriteLine("RcvErr:[" + "PortName" + "]" + n + " => " + e.EventType);
		}

		// disable once FunctionNeverReturns
		private static void ScanTaskQueue() {
			while(true) {
				Thread.Sleep(200);

				if (ComTaskQueue.Count > 0) {
					TaskContent next = new TaskContent();
					lock(ComTaskQueue) {
						ComTaskQueue.TryDequeue(out next);
					}
					if (next.idx != -1) {
						ComTaskQueueKeys.Remove(next.PortName + AnyBytesToString(next.sBytes));
						sendToMachine(next.idx, next.PortName, next.sBytes, next.needRetry);
					}
				}
			}
		}
		private static void AddIntoTaskQueue(int idx, string PortName, byte[] sBytes, int needRetry) {
			lock(ComTaskQueue) {
				string thisKey = PortName + AnyBytesToString(sBytes);

				if (ComTaskQueueKeys.ContainsKey(thisKey)) return;

				TaskContent neoTask = new TaskContent();
				neoTask.idx = idx;
				neoTask.PortName = PortName;
				neoTask.sBytes = sBytes;
				neoTask.needRetry = needRetry;
				System.Diagnostics.Debug.WriteLine("elegated！" + ComTaskQueue.Count + "&" + thisKey);
				ComTaskQueueKeys[thisKey] = 1;
				ComTaskQueue.Enqueue(neoTask);
			}
		}
		private static void ClearIntoTaskQueue() {
			while (ComTaskQueue.Count > 0) {
				TaskContent nill = new TaskContent();
				ComTaskQueue.TryDequeue(out nill);
			}
			ComTaskQueueKeys.Clear();
		}
		private string FillBytes(string orgString, int fillTolength)
		{
			byte[] bytes = Encoding.Default.GetBytes(orgString);
			int num = bytes.Length;
			for (int i = num; i < fillTolength; i++)
			{
				orgString += " ";
			}
			return orgString;
		}
		private string FillNumber(string orgString, int fillTolength)
		{
			int length = orgString.Length;
			for (int i = length; i < fillTolength; i++)
			{
				orgString = "0" + orgString;
			}
			return orgString;
		}
		private string RevHighLow(string orgString)
		{
			int length = orgString.Length;
			string text = "";
			for (int i = 0; i < length; i += 2)
			{
				text += orgString.Substring(i + 1, 1);
				text += orgString.Substring(i, 1);
			}
			return text;
		}
		public static byte[] GetHexValue(string str)
		{
			string[] sourceStr = str.Split(' ');
			byte[] covertHexByte = new byte[sourceStr.Length];
			try
			{
				for (int i = 0; i < sourceStr.Length; i++)
				{
					covertHexByte[i] = (byte)(int.Parse(sourceStr[i], System.Globalization.NumberStyles.HexNumber)); ;
				}
			}
			catch
			{  }

			return covertHexByte;
		}

		#region  ByteToString
		public static string ByteToString(byte input)
		{
			return input.ToString("X2");
		}
		public static string TwoBytesToString(byte[] arr, bool isReverse)
		{
			if (arr.Length == 0) return "";
			try
			{
				byte hi = arr[0], lo = arr[1];
				return Convert.ToString(isReverse ? hi + lo * 0x100 : hi * 0x100 + lo, 16).ToUpper().PadLeft(4, '0');
			}
			catch (Exception ex) { throw (ex); }
		}

		public static string TwoBytesToString(byte[] arr)
		{
			try
			{
				return TwoBytesToString(arr, true);
			}
			catch (Exception ex) { throw (ex); }
		}

		public static string AnyBytesToString(byte[] arr, bool isReverse)
		{
			if (arr.Length == 0) return "";
			try
			{
				string ret = "";
				for (int i = 0; i < arr.Length - 1; i+=2) {
					byte[] partArr = new byte[2];

					partArr[0] = arr[i]; partArr[1] = arr[i + 1];
					ret += TwoBytesToString(partArr, isReverse);
				}

				return ret;
			}
			catch (Exception ex) { throw (ex); }
		}

		public static string AnyBytesToString(byte[] arr)
		{
			try
			{
				return AnyBytesToString(arr, true);
			}
			catch (Exception ex) { throw (ex); }
		}
		#endregion

		#region  CRC16
		public static byte[] CRC16(byte[] data)
		{
			int len = data.Length;
			if (len > 0)
			{
				ushort crc = 0xFFFF;

				for (int i = 0; i < len; i++)
				{
					crc = (ushort)(crc ^ (data[i]));
					for (int j = 0; j < 8; j++)
					{
						crc = (crc & 1) != 0 ? (ushort)((crc >> 1) ^ 0xA001) : (ushort)(crc >> 1);
					}
				}
				byte hi = (byte)((crc & 0xFF00) >> 8);  //高位置
				byte lo = (byte)(crc & 0x00FF);         //低位置

				return new byte[] { hi, lo };
			}
			return new byte[] { 0, 0 };
		}
		#endregion

		#region  ToModbusCRC16
		public static string ToModbusCRC16(string s)
		{
			return ToModbusCRC16(s, true);
		}

		public static string ToModbusCRC16(string s, bool isReverse)
		{
			return TwoBytesToString(CRC16(GetHexValue(s)), isReverse);
		}

		public static string ToModbusCRC16(byte[] data)
		{
			return ToModbusCRC16(data, true);
		}

		public static string ToModbusCRC16(byte[] data, bool isReverse)
		{
			return TwoBytesToString(CRC16(data), isReverse);
		}
		#endregion
	}
}
