/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2019-12-26
 * 时间: 16:49
 *
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Threading;
using System.Windows.Forms;
using ProcedureStepCounter.Entity;
using ProcedureStepCounter.Service;
using System.Web.Script.Serialization;

namespace ProcedureStepCounter
{
	public partial class MainForm : Form
	{
		private const string NONE = "00 00 00 00 00 00 00 00";
		// private const string GET_AL1 = "01 03 00 01 00 02 95 CB";
		// private const string RESET_AL1 = "01 06 00 00 00 03 C9 CB";
		private const string GET_AL1 = "03 00 01 00 02"; // 功能码 = 3; 起始BIT位 = 01; 读数据长度 = 02
		private const string RESET_AL1 = "06 00 00 00 02"; // 功能码 = 6; 写入地址 = 00; 写入数据 = 02
		private System.Windows.Forms.PictureBox pictureBox1;

		RecSocket recSocket;

		private const int IDX_SET_TIMES_ID = 0;
		private const int IDX_STANDARD_COUNT = 2;
		private const int IDX_CURRENT_COUNT = 3;
		private const int IDX_PORT_NAME = 4;
		private const int IDX_CONSTATUS_COUNT = 6;
		private const int IDX_ADDRESS_CODE = 7;
		private const int IDX_COUNTED_COUNT = 8;

		public int ComErrorCount = 0;

		public MainForm()
		{
			this.InitializeComponent();
			this.ReadInitializeFile();
			this.lbl_processCode.Text = Common.process_code + " " + Common.line_part;

			ComTimer.Interval = 500;
			ComTimer.Start();

			string resContent = HttpRequester.Login();
			this.checkResContent(resContent);

			recSocket = new RecSocket(this);
			Thread socketThread =
				new Thread(new ThreadStart(recSocket.ListenMethod));
			socketThread.Name = "SocketThread";
			socketThread.IsBackground = true;
			socketThread.Start();

			ComRefreshTimer.Start();
			ComConnector.InitTaskQueue();
		}

		//
		private void MainFormLoad(object sender, EventArgs e)
		{
			for (int i = 0; i < ComDataGrid.Rows.Count; i++) {
				DataGridViewCell statusCell = ComDataGrid.Rows[i].Cells[IDX_CONSTATUS_COUNT];
				if ("连通".Equals(statusCell.Value)) {
					statusCell.Style.ForeColor = Color.Green;
				} else {
					statusCell.Style.ForeColor = Color.DarkRed;
				}
			}
		}
		//
		private void MainFormClosing(object sender, EventArgs e)
		{
			ComConnector.CloseAllComs();
		}

		private void GetComClick(int idx, string PortName)
		{
			String flagMessage = ComConnector.GetCom(PortName);

			DataGridViewCell statusCell = ComDataGrid.Rows[idx].Cells[IDX_CONSTATUS_COUNT];

			if (statusCell != null) {
				if (flagMessage == null)
				{
					statusCell.Style.ForeColor = Color.Green;
					String retError = ComConnector.sendToMachine(idx, PortName, ComConnector.GetHexValue(NONE));
					if (retError != null) {

						MessageBox.Show(retError, "Error");
						statusCell.Style.ForeColor = Color.DarkRed;
						statusCell.Value = "断开";
					} else {
						statusCell.Value = "连通";
					}
					return;
				}

				statusCell.Style.ForeColor = Color.DarkRed;
				statusCell.Value = "断开";
				MessageBox.Show(flagMessage, "Error");
			}
		}

		private bool GetAL1(int idx)
		{
			String PortName = ComDataGrid.Rows[idx].Cells[IDX_PORT_NAME].Value.ToString();
			DataGridViewCell addressCell = ComDataGrid.Rows[idx].Cells[IDX_ADDRESS_CODE];
			String FunctionCode = addressCell.Value + " " + GET_AL1;
			String CrcCode = ComConnector.ToModbusCRC16(FunctionCode);
			String retError = ComConnector.sendToMachine(idx, PortName,
								ComConnector.GetHexValue(FunctionCode + " " + CrcCode.Substring(0,2) + " " + CrcCode.Substring(2,2)));
			if (retError != null) {
				DataGridViewCell statusCell = ComDataGrid.Rows[idx].Cells[IDX_CONSTATUS_COUNT];

				statusCell.Style.ForeColor = Color.DarkRed;
				statusCell.Value = "断开";
				ComErrorCount ++;
				if (ComErrorCount > 8) {
					MessageBox.Show("COM 连接次数过多，将关闭程序", "Error");
					Environment.Exit(0);
				}

				MessageBox.Show(retError, "Error");

				return false;
			}

//			ComErrorCount = 0;
			ComTimer.Enabled = true;

			return true;
		}

		private bool ResetAL1(int idx)
		{
			String PortName = ComDataGrid.Rows[idx].Cells[IDX_PORT_NAME].Value.ToString();
			DataGridViewCell addressCell = ComDataGrid.Rows[idx].Cells[IDX_ADDRESS_CODE];
			String FunctionCode = addressCell.Value + " " + RESET_AL1;
			String CrcCode = ComConnector.ToModbusCRC16(FunctionCode);
			String retError = ComConnector.sendToMachine(idx, PortName,
								ComConnector.GetHexValue(FunctionCode + " " + CrcCode.Substring(0,2) + " " + CrcCode.Substring(2,2)));
			if (retError != null) {
				DataGridViewCell statusCell = ComDataGrid.Rows[idx].Cells[IDX_CONSTATUS_COUNT];

				MessageBox.Show(retError, "Error");
				statusCell.Style.ForeColor = Color.DarkRed;
				statusCell.Value = "断开";
				ComErrorCount ++;
				if (ComErrorCount > 8) {
					MessageBox.Show("COM 连接次数过多，将关闭程序", "Error");
					Environment.Exit(0);
				}

				return false;
			}

			ComErrorCount = 0;
			ComDataGrid.Rows[idx].Cells[IDX_CURRENT_COUNT].Value = 0;
			return true;
		}

		/*
		 * 通信单元格-点击
		 */
		void ComDataGridCellClick(object sender, DataGridViewCellEventArgs e)
		{
			if (e != null && e.RowIndex != -1)
			{
				DataGridView dgv = sender as DataGridView;
				DataGridViewCell selectedCell = dgv.CurrentCell;
				string portName = dgv.CurrentRow.Cells[IDX_PORT_NAME].Value as string;
				switch (selectedCell.ColumnIndex) {
					case IDX_CONSTATUS_COUNT : {
						GetComClick(selectedCell.RowIndex, portName);
						break;
					}
					case IDX_CURRENT_COUNT : {
						if ("-".Equals(lbl_notifiNo.Text)) {
							// 未针对计数时的测试功能
							GetAL1(selectedCell.RowIndex);
						}
						break;
					}
//					case IDX_STANDARD_COUNT : {
//						ResetAL1(selectedCell.RowIndex);
//						break;
//					}
				}
			}
		}

		public void showProcessMaterial(string startAjax)
		{
			if (startAjax != null) {
				JavaScriptSerializer js = new JavaScriptSerializer();//实例化一个能够序列化数据的类
				MaterialData materialData = js.Deserialize<MaterialData>(startAjax); //将json数据转化为对象类型并赋值给list
				Action action = () =>
				{
					lbl_notifiNo.Text = materialData.omr_notifi_no;
					lbl_serialNo.Text = materialData.serial_no;
					lbl_modelName.Text = materialData.model_name;
					lbl_processCode.Text = Common.process_code + " " + Common.line_part;

					if (materialData.counted_times_map != null) {
						foreach (string set_times_of_id in materialData.counted_times_map.Keys) {
							System.Diagnostics.Debug.WriteLine("counted_times_map key is " + set_times_of_id);
							for (int i = 0; i < ComDataGrid.Rows.Count; i++) {
								DataGridViewRow row = ComDataGrid.Rows[i];
								if (set_times_of_id.Equals(row.Cells[IDX_SET_TIMES_ID].Value)) {
									System.Diagnostics.Debug.WriteLine("counted_times_map value is " + materialData.counted_times_map[set_times_of_id]);
									row.Cells[IDX_COUNTED_COUNT].Value = materialData.counted_times_map[set_times_of_id];
									break;
								}
							}
						}
					}

					foreach (string set_times_of_id in materialData.set_times_map.Keys) {
						for (int i = 0; i < ComDataGrid.Rows.Count; i++) {
							DataGridViewRow row = ComDataGrid.Rows[i];
							if (set_times_of_id.Equals(row.Cells[IDX_SET_TIMES_ID].Value)) {
								if (!row.Cells[IDX_STANDARD_COUNT].Value.ToString().Equals("-")) {
									break;
								}
								row.Cells[IDX_STANDARD_COUNT].Value = materialData.set_times_map[set_times_of_id];
								row.Cells[IDX_CURRENT_COUNT].Value = row.Cells[IDX_COUNTED_COUNT].Value;
								ResetAL1(i);
								if (i < ComConnector.retCounts.Length) ComConnector.retCounts[i] = 0;
								break;
							}
						}
					}
				};
			Invoke(action);
			}
		}

		public string getCount()
		{
			Action action = () =>
			{
				lbl_notifiNo.Text = "-";
				lbl_serialNo.Text = "-";
				lbl_modelName.Text = "-";
				lbl_processCode.Text = Common.process_code + " " + Common.line_part;
				foreach (DataGridViewRow row in ComDataGrid.Rows) {
					row.Cells[IDX_STANDARD_COUNT].Value = "-";
					row.Cells[IDX_COUNTED_COUNT].Value = "0";
				}
			};
			Invoke(action);

			Dictionary<string, string> noeMap = new Dictionary<string, string>();
			foreach (DataGridViewRow row in ComDataGrid.Rows) {
				noeMap.Add(row.Cells[IDX_SET_TIMES_ID].Value.ToString(), row.Cells[IDX_CURRENT_COUNT].Value.ToString());
			}
			JavaScriptSerializer jss = new JavaScriptSerializer();
			return jss.Serialize(noeMap);
		}

		/**
		 * 向仪表发送后查询计数指令后，更新列表的延期事件
		 */
		private void readCounter(object sender, EventArgs e)
		{
			for (int i=0 ; i < ComConnector.retCounts.Length; i++) {
				if (i >= ComDataGrid.Rows.Count) break;
				DataGridViewCell CountCell = ComDataGrid.Rows[i].Cells[IDX_CURRENT_COUNT];
				DataGridViewCell CountedCell = ComDataGrid.Rows[i].Cells[IDX_COUNTED_COUNT];
				int countedValue = 0;
				if (ComConnector.retCounts[i] >= 0) {
					countedValue = ComConnector.retCounts[i] + Convert.ToInt16(CountedCell.Value);
					CountCell.Value = countedValue;
				}
				DataGridViewCell StandardCell = ComDataGrid.Rows[i].Cells[IDX_STANDARD_COUNT];
				if (!StandardCell.Value.ToString().Equals("-")) {
					int standard = Convert.ToInt16(StandardCell.Value);
					if (countedValue < standard) {
						CountCell.Style.BackColor = Color.Yellow;
					} else {
						CountCell.Style.BackColor = Color.White;
					}
				} else {
					CountCell.Style.BackColor = Color.White;
				}
			}

			string retCheck = ComConnector.CheckSendTokens();
			if (retCheck != null) {
				ComErrorCount ++;
				if (ComErrorCount > 8) {
					MessageBox.Show("COM 连接次数过多，将关闭程序", "Error");
					Environment.Exit(0);
				}

				MessageBox.Show(retCheck, "Error");

				return;
			}

			ComTimer.Enabled = false;
		}

		/**
		 * 定时获取计数的时钟
		 */
		private void refreshCounter(object sender, EventArgs e)
		{
//			GetAL1(0); // TODO tes
//				for(int i = 0; i < Common.comdata.Count;i++) {
//					if ("连通".Equals(ComDataGrid.Rows[i].Cells[IDX_CONSTATUS_COUNT].Value)) {
//						GetAL1(i);
//					}
//				}

			 if (!"-".Equals(lbl_notifiNo.Text)) {
				for(int i = 0; i < Common.comdata.Count;i++) {
					if ("连通".Equals(ComDataGrid.Rows[i].Cells[IDX_CONSTATUS_COUNT].Value)) {
						GetAL1(i);
					}
				}
			 }
		}

		private void ReadInitializeFile()
		{
			string filePath = System.IO.Path.Combine(System.AppDomain.CurrentDomain.BaseDirectory, "ProcedureStepCounter.ini");
			if (System.IO.File.Exists(filePath))
			{
				Common.rvs_server_ip = IniAccessory.Read("net", "rvs_server_ip", "", filePath);
				Common.rvs_server_name = IniAccessory.Read("net", "rvs_server_name", "", filePath);
				Common.local_ip = IniAccessory.Read("net", "local_ip", "", filePath);
				Common.process_code = IniAccessory.Read("position", "process_code", "", filePath);
				Common.line_part = IniAccessory.Read("position", "line_part", "", filePath);
				if (Common.rvs_server_ip.Equals("") ||
						Common.local_ip.Equals("") ||
						Common.process_code.Equals("")
					) {

					SettingForm fSetting = new SettingForm();
					fSetting.ShowDialog(null);

					MessageBox.Show("请完成基本配置设定后，再使用本工具。", "Error");
					Environment.Exit(0);
				}
			} else {
				MessageBox.Show("请完成配置文件 ProcedureStepCounter.ini 中的设定后，再使用本工具。", "Error");

				IniAccessory.Write("net", "rvs_server_ip", "10.220.130.100", filePath);
				IniAccessory.Write("net", "rvs_server_name", "rvs", filePath);
				IniAccessory.Write("net", "local_ip", "10.220.126.181", filePath);

				IniAccessory.Write("position", "process_code", "431", filePath);
				IniAccessory.Write("position", "line_part", "A", filePath);

				SettingForm fSetting = new SettingForm();
				fSetting.ShowDialog(null);

				Environment.Exit(0);
			}
		}

		private void checkResContent(string resContent)
		{
			if (resContent == null || resContent.Equals("")) {
				resContent = "无法通信";
			}

			bool isIniSet = true;

			if (!resContent.StartsWith("{")) {
				MessageBox.Show(string.Format("在连接服务器[{0}]时发生错误：{1}。\r\n" +
																			"请确认RVS是否能正常使用；本机的网络连接是否正常；\r\n" +
																			"修改服务器IP设定后，再使用本工具。",
																			Common.rvs_server_ip, resContent), "Error");
				SettingForm fSetting = new SettingForm();
				fSetting.ShowDialog(null);
				Environment.Exit(0);
			} else {
				// MessageBox.Show("json:" + resContent, "JSON");
				JavaScriptSerializer jss = new JavaScriptSerializer();

				ResFromServer recv = jss.Deserialize<ResFromServer>(resContent);
				if (recv == null) {
					MessageBox.Show(string.Format("从服务器[{0}]时取得的配置错误：无法取得配置。\r\n" +
																				"请RVS系统管理者确认配置后，再使用本工具。",
																				Common.rvs_server_ip), "Error");
					Environment.Exit(0);
				} else if (recv.error != null) {
					MessageBox.Show(string.Format("从服务器[{0}]时取得的配置错误：{1}。\r\n" +
																				"请RVS系统管理者确认配置，或者确认本机配置后，再使用本工具。",
																				Common.rvs_server_ip, recv.error), "Error");
					SettingForm fSetting = new SettingForm();
					fSetting.ShowDialog(null);
					Environment.Exit(0);
				} else {
					string filePath = System.IO.Path.Combine(System.AppDomain.CurrentDomain.BaseDirectory, "ProcedureStepCounter.ini");

					foreach (var o in recv.avaCounterList) {

						Dictionary<string, object> avaCounter = o as Dictionary<string, object>;
						string avaCounterId = avaCounter["procedure_step_count_id"] as string;

						ComData transData = new ComData();
						transData.item_id = avaCounterId;
						transData.item_name = avaCounter["name"] as string;

						transData.set_times = "-";
						transData.count = 0;

						transData.pbs = 9600;
						string iniComPort =
							IniAccessory.Read("item." + avaCounterId, "com_port", "", filePath);
						if ("".Equals(iniComPort)) {
							isIniSet = false;
						} else {
							transData.com_port = iniComPort;
						}

						string iniPbs =
							IniAccessory.Read("item." + avaCounterId, "pbs", "9600", filePath);
						transData.pbs = int.Parse(iniPbs);

						string iniAddressCode =
							IniAccessory.Read("item." + avaCounterId, "address_code", "01", filePath);
						transData.address_code = iniAddressCode;

						transData.connect_status = "断开";

						// 记录COM的bps
						if (isIniSet && !ComConnector.baudRateOfCom.ContainsKey(transData.com_port)) {
							ComConnector.baudRateOfCom[transData.com_port] = transData.pbs;
						}

						Common.comdata.Add(transData);
					}
				}
			}

			ComDataGrid.DataSource = Common.comdata;
			SetComGrid.InitDataGridColumnHeader(ComDataGrid);
			if (isIniSet) {
				for(var idx = 0; idx < Common.comdata.Count; idx++) {
					GetComClick(idx, Common.comdata[idx].com_port);
				}
			} else {
				SettingForm fSetting = new SettingForm();
				fSetting.ShowDialog(this);
			}
		}

		/**
		 * 设置按钮-点击
		 */
		void SettingButtonClick(object sender, EventArgs e)
		{
			SettingForm fSetting = new SettingForm();
			fSetting.ShowDialog(this);

			for(var idx = 0; idx < Common.comdata.Count; idx++) {
				GetComClick(idx, Common.comdata[idx].com_port);
			}
		}
	}
}
