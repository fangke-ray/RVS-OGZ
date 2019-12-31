/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2019-12-30
 * 时间: 20:36
 *
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.Collections.Generic;
using System.IO.Ports;
using System.Text;
using System.Threading;

namespace ProcedureStepCounter.Service
{
	/// <summary>
	/// Description of ComConnector.
	/// </summary>
	public class ComConnector
	{
		public ComConnector()
		{
		}

		public static SerialPort[] coms = {null, null};

		public static int[] retCounts = {-1, -1};

		public static void CloseAllComs() {
			if (coms != null) {
				foreach (SerialPort com in coms) {
					if (com != null && com.IsOpen)
					{
						try
						{
							com.Close();
						}
						catch (Exception)
						{
						}
					}
				}

				coms = null;
			}
		}

		public static String GetCom(int idx, string PortName)
		{
			string[] portNames = SerialPort.GetPortNames();
			if (portNames.Length == 0)
			{
				return "本机未连接" + PortName + "端口！\0D";
			}
			SerialPort com = coms[idx];
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
				com.BaudRate = 9600;
				com.PortName = PortName;
				com.DataBits = 8;
				com.StopBits = StopBits.One;
				com.DataReceived += new SerialDataReceivedEventHandler((sender, e) => port_DataReceived(idx, sender, e));

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
				coms[idx] = com;
			}

			return null;
		}

		public static String sendToMachine(int idx, byte[] sBytes)
		{
			SerialPort com = coms[idx];
			if (com != null && com.IsOpen)
			{
				try
				{
					com.Write(sBytes, 0, sBytes.Length);
				}
				catch (Exception ex)
				{
					return "端口无法进行通讯！" + ex.Message;
				}
				return null;
			}

			return "端口无法进行通讯！";
		}

		private static void port_DataReceived(int idx, object sender, SerialDataReceivedEventArgs e)
		{
			SerialPort com = coms[idx];

			List<byte> rxbyteList = new List<byte>();

			int n = com.BytesToRead;
			while (n > 0) {
				byte[] rxbyte = new byte[n];
				com.Read(rxbyte, 0, n);

				rxbyteList.AddRange(rxbyte);

				Thread.Sleep(150);

				n = com.BytesToRead;
			}
			// ModBus协议来讲，第三位即代表了将要收到的数据时多少位

			byte[] rxbyteArray = rxbyteList.ToArray();
			Console.WriteLine(rxbyteArray.Length);
			Console.ReadLine();
			if (rxbyteArray.Length > 3) {
				// 仪表地址
				// 操作指令码 0x03 读出
				// 4 ???
				if (rxbyteArray[0] == 1 && rxbyteArray[1] == 3 && rxbyteArray[2] == 4) {
					try
					{
						string sNumberHigh = ByteToString(rxbyteArray[5]);
						string sNumberLow = ByteToString(rxbyteArray[6]);

						retCounts[idx] = Convert.ToInt32(sNumberHigh) * 100
							+ Convert.ToInt32(sNumberLow);
					}
					catch (Exception ex)
					{
						retCounts[idx] = -1;
					}
				}
			}
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
		public static string ByteToString(byte input)
		{
			return input.ToString("X2");
		}
	}
}
