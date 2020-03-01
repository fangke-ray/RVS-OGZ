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
	public class MainForm : Form
	{
		private IContainer components = null;

		private System.Windows.Forms.GroupBox gb_coms;
		private System.Windows.Forms.GroupBox gb_process;

		private System.Windows.Forms.Label lbl_modelName;
		private System.Windows.Forms.Label lbl_processCode;
		private System.Windows.Forms.Label lbl_serialNo;
		private System.Windows.Forms.Label lbl_notifiNo;
		private System.Windows.Forms.Label cpt_processCode;
		private System.Windows.Forms.Label cpt_modelName;
		private System.Windows.Forms.Label cpt_serialNo;
		private System.Windows.Forms.Label cpt_notifiNo;
		private System.Windows.Forms.DataGridView ComDataGrid;
		private System.Windows.Forms.Timer ComTimer;

		private const string GET_AL1 = "01 03 00 01 00 02 95 CB";
		private const string RESET_AL1 = "01 06 00 00 00 03 C9 CB";
		private System.Windows.Forms.PictureBox pictureBox1;

		RecSocket recSocket;
		private System.Windows.Forms.Timer ComRefreshTimer;

		public MainForm()
		{
			this.InitializeComponent();
			this.ReadInitializeFile();
			this.lbl_processCode.Text = Common.process_code + " " + Common.line_part;

			ComTimer.Interval = 500;
			ComTimer.Start();

			ComRefreshTimer.Start();

			string resContent = HttpRequester.Login();
			this.checkResContent(resContent);

			recSocket = new RecSocket(this);
			Thread socketThread =
				new Thread(new ThreadStart(recSocket.ListenMethod));
			socketThread.Name = "SocketThread";
			socketThread.IsBackground = true;
			socketThread.Start();
		}

		//
		private void MainFormLoad(object sender, EventArgs e)
		{

		}
			//
		private void MainFormClosing(object sender, EventArgs e)
		{
			ComConnector.CloseAllComs();
		}

		private void GetComClick(int idx, string PortName)
		{
			String flagMessage = ComConnector.GetCom(idx, PortName);

			DataGridViewCell statusCell = ComDataGrid.Rows[idx].Cells[6];

			if (statusCell != null) {
				if (flagMessage == null)
				{
					statusCell.Style.ForeColor = Color.Green;
					statusCell.Value = "连通";
					return;
				}

				statusCell.Style.ForeColor = Color.DarkRed;
				statusCell.Value = "断开";
				MessageBox.Show(flagMessage, "Error");
			}
		}

		private bool GetAL1(int idx)
		{
			String retError = ComConnector.sendToMachine(idx,
					ComConnector.GetHexValue(GET_AL1));
			if (retError != null) {
				DataGridViewCell statusCell = ComDataGrid.Rows[idx].Cells[6];

				MessageBox.Show(retError, "Error");
				statusCell.Style.ForeColor = Color.DarkRed;
				statusCell.Value = "断开";

				return false;
			}
			ComTimer.Enabled = true;
			return true;
		}

		private bool ResetAL1(int idx)
		{
			String retError = ComConnector.sendToMachine(idx,
					ComConnector.GetHexValue(RESET_AL1));
			if (retError != null) {
				DataGridViewCell statusCell = ComDataGrid.Rows[idx].Cells[6];

				MessageBox.Show(retError, "Error");
				statusCell.Style.ForeColor = Color.DarkRed;
				statusCell.Value = "断开";

				return false;
			}
			ComDataGrid.Rows[idx].Cells[3].Value = 0;
			return true;
		}

		protected override void Dispose(bool disposing)
		{
			if (disposing && this.components != null)
			{
				this.components.Dispose();
			}
			base.Dispose(disposing);
		}

		void ComDataGridCellClick(object sender, DataGridViewCellEventArgs e)
		{
			if (e != null && e.RowIndex != -1)
			{
				DataGridView dgv = sender as DataGridView;
				DataGridViewCell selectedCell = dgv.CurrentCell;
				string portName = dgv.CurrentRow.Cells[4].Value as string;
				switch (selectedCell.ColumnIndex) {
					case 6 : {
						GetComClick(selectedCell.RowIndex, portName);
						break;
					}
					case 3 : {
						GetAL1(selectedCell.RowIndex);
						break;
					}
					case 2 : {
						ResetAL1(selectedCell.RowIndex);
						break;
					}
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

					foreach (string set_times_of_id in materialData.set_times_map.Keys) {
						for (int i = 0; i < ComDataGrid.Rows.Count; i++) {
							DataGridViewRow row = ComDataGrid.Rows[i];
							if (set_times_of_id.Equals(row.Cells[0].Value)) {
								row.Cells[2].Value = materialData.set_times_map[set_times_of_id];
								row.Cells[3].Value = "0";
								ResetAL1(i);
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
					row.Cells[2].Value = "-";
				}
			};
			Invoke(action);

			Dictionary<string, string> noeMap = new Dictionary<string, string>();
			foreach (DataGridViewRow row in ComDataGrid.Rows) {
				noeMap.Add(row.Cells[0].Value.ToString(), row.Cells[3].Value.ToString());
			}
			JavaScriptSerializer jss = new JavaScriptSerializer();
			return jss.Serialize(noeMap);
		}

		private void readCounter(object sender, EventArgs e)
		{
			for (int i=0 ; i < ComConnector.retCounts.Length; i++) {
				if (i >= ComDataGrid.Rows.Count) break;
				DataGridViewCell CountCell = ComDataGrid.Rows[i].Cells[3];
				if (ComConnector.retCounts[i] >= 0) {
					CountCell.Value = ComConnector.retCounts[i];
				}
			}
			ComTimer.Enabled = false;
		}

		private void refreshCounter(object sender, EventArgs e)
		{
			if (!"-".Equals(lbl_notifiNo.Text)
					&& "连通".Equals(ComDataGrid.Rows[0].Cells[6].Value)) {
				GetAL1(0);
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
					MessageBox.Show("请完成配置文件 ProcedureStepCounter.ini 中的设定后，再使用本工具。", "Error");
					Environment.Exit(0);
				}
			} else {
				MessageBox.Show("请完成配置文件 ProcedureStepCounter.ini 中的设定后，再使用本工具。", "Error");

				IniAccessory.Write("net", "rvs_server_ip", "10.220.130.100", filePath);
				IniAccessory.Write("net", "rvs_server_name", "rvs", filePath);
				IniAccessory.Write("net", "local_ip", "10.220.126.181", filePath);

				IniAccessory.Write("position", "process_code", "431", filePath);
				IniAccessory.Write("position", "line_part", "A", filePath);

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
																			"ProcedureStepCounter.ini 中的服务器IP设定后，再使用本工具。",
																			Common.rvs_server_ip, resContent), "Error");
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
																				"请RVS系统管理者确认配置后，再使用本工具。",
																				Common.rvs_server_ip, recv.error), "Error");
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

						transData.connect_status = "断开";

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

		private void InitializeComponent()
		{
			this.components = new System.ComponentModel.Container();
			System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(MainForm));
			this.gb_coms = new System.Windows.Forms.GroupBox();
			this.ComDataGrid = new System.Windows.Forms.DataGridView();
			this.gb_process = new System.Windows.Forms.GroupBox();
			this.cpt_serialNo = new System.Windows.Forms.Label();
			this.lbl_modelName = new System.Windows.Forms.Label();
			this.lbl_processCode = new System.Windows.Forms.Label();
			this.lbl_serialNo = new System.Windows.Forms.Label();
			this.lbl_notifiNo = new System.Windows.Forms.Label();
			this.cpt_processCode = new System.Windows.Forms.Label();
			this.cpt_modelName = new System.Windows.Forms.Label();
			this.cpt_notifiNo = new System.Windows.Forms.Label();
			this.ComTimer = new System.Windows.Forms.Timer(this.components);
			this.pictureBox1 = new System.Windows.Forms.PictureBox();
			this.ComRefreshTimer = new System.Windows.Forms.Timer(this.components);
			this.gb_coms.SuspendLayout();
			((System.ComponentModel.ISupportInitialize)(this.ComDataGrid)).BeginInit();
			this.gb_process.SuspendLayout();
			((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).BeginInit();
			this.SuspendLayout();
			//
			// gb_coms
			//
			this.gb_coms.Controls.Add(this.ComDataGrid);
			this.gb_coms.Location = new System.Drawing.Point(65, 288);
			this.gb_coms.Margin = new System.Windows.Forms.Padding(5, 1, 5, 1);
			this.gb_coms.Name = "gb_coms";
			this.gb_coms.Padding = new System.Windows.Forms.Padding(5, 1, 5, 1);
			this.gb_coms.Size = new System.Drawing.Size(915, 197);
			this.gb_coms.TabIndex = 5;
			this.gb_coms.TabStop = false;
			this.gb_coms.Text = "计数结果";
			//
			// ComDataGrid
			//
			this.ComDataGrid.AllowUserToAddRows = false;
			this.ComDataGrid.AllowUserToDeleteRows = false;
			this.ComDataGrid.AllowUserToResizeColumns = false;
			this.ComDataGrid.AllowUserToResizeRows = false;
			this.ComDataGrid.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
			this.ComDataGrid.Location = new System.Drawing.Point(17, 29);
			this.ComDataGrid.Name = "ComDataGrid";
			this.ComDataGrid.ReadOnly = true;
			this.ComDataGrid.RowTemplate.Height = 23;
			this.ComDataGrid.Size = new System.Drawing.Size(879, 150);
			this.ComDataGrid.TabIndex = 0;
			this.ComDataGrid.CellClick += new System.Windows.Forms.DataGridViewCellEventHandler(this.ComDataGridCellClick);
			//
			// gb_process
			//
			this.gb_process.Controls.Add(this.cpt_serialNo);
			this.gb_process.Controls.Add(this.lbl_modelName);
			this.gb_process.Controls.Add(this.lbl_processCode);
			this.gb_process.Controls.Add(this.lbl_serialNo);
			this.gb_process.Controls.Add(this.lbl_notifiNo);
			this.gb_process.Controls.Add(this.cpt_processCode);
			this.gb_process.Controls.Add(this.cpt_modelName);
			this.gb_process.Controls.Add(this.cpt_notifiNo);
			this.gb_process.Location = new System.Drawing.Point(65, 127);
			this.gb_process.Margin = new System.Windows.Forms.Padding(5, 1, 5, 1);
			this.gb_process.Name = "gb_process";
			this.gb_process.Padding = new System.Windows.Forms.Padding(5, 1, 5, 1);
			this.gb_process.Size = new System.Drawing.Size(915, 137);
			this.gb_process.TabIndex = 9;
			this.gb_process.TabStop = false;
			this.gb_process.Text = "作业信息";
			//
			// cpt_serialNo
			//
			this.cpt_serialNo.Location = new System.Drawing.Point(486, 41);
			this.cpt_serialNo.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
			this.cpt_serialNo.Name = "cpt_serialNo";
			this.cpt_serialNo.Size = new System.Drawing.Size(112, 27);
			this.cpt_serialNo.TabIndex = 6;
			this.cpt_serialNo.Text = "机身号";
			//
			// lbl_modelName
			//
			this.lbl_modelName.BackColor = System.Drawing.SystemColors.Info;
			this.lbl_modelName.ForeColor = System.Drawing.Color.Black;
			this.lbl_modelName.Location = new System.Drawing.Point(174, 88);
			this.lbl_modelName.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
			this.lbl_modelName.Name = "lbl_modelName";
			this.lbl_modelName.Size = new System.Drawing.Size(227, 27);
			this.lbl_modelName.TabIndex = 8;
			this.lbl_modelName.Text = "-";
			//
			// lbl_processCode
			//
			this.lbl_processCode.BackColor = System.Drawing.SystemColors.Info;
			this.lbl_processCode.ForeColor = System.Drawing.Color.Black;
			this.lbl_processCode.Location = new System.Drawing.Point(607, 88);
			this.lbl_processCode.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
			this.lbl_processCode.Name = "lbl_processCode";
			this.lbl_processCode.Size = new System.Drawing.Size(220, 27);
			this.lbl_processCode.TabIndex = 8;
			this.lbl_processCode.Text = "";
			//
			// lbl_serialNo
			//
			this.lbl_serialNo.BackColor = System.Drawing.SystemColors.Info;
			this.lbl_serialNo.ForeColor = System.Drawing.Color.Black;
			this.lbl_serialNo.Location = new System.Drawing.Point(607, 41);
			this.lbl_serialNo.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
			this.lbl_serialNo.Name = "lbl_serialNo";
			this.lbl_serialNo.Size = new System.Drawing.Size(220, 27);
			this.lbl_serialNo.TabIndex = 8;
			this.lbl_serialNo.Text = "-";
			//
			// lbl_notifiNo
			//
			this.lbl_notifiNo.BackColor = System.Drawing.SystemColors.Info;
			this.lbl_notifiNo.ForeColor = System.Drawing.Color.Black;
			this.lbl_notifiNo.Location = new System.Drawing.Point(174, 41);
			this.lbl_notifiNo.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
			this.lbl_notifiNo.Name = "lbl_notifiNo";
			this.lbl_notifiNo.Size = new System.Drawing.Size(227, 27);
			this.lbl_notifiNo.TabIndex = 8;
			this.lbl_notifiNo.Text = "-";
			//
			// cpt_processCode
			//
			this.cpt_processCode.Location = new System.Drawing.Point(486, 88);
			this.cpt_processCode.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
			this.cpt_processCode.Name = "cpt_processCode";
			this.cpt_processCode.Size = new System.Drawing.Size(112, 27);
			this.cpt_processCode.TabIndex = 6;
			this.cpt_processCode.Text = "作业岗位";
			//
			// cpt_modelName
			//
			this.cpt_modelName.Location = new System.Drawing.Point(53, 88);
			this.cpt_modelName.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
			this.cpt_modelName.Name = "cpt_modelName";
			this.cpt_modelName.Size = new System.Drawing.Size(112, 27);
			this.cpt_modelName.TabIndex = 6;
			this.cpt_modelName.Text = "机型名";
			//
			// cpt_notifiNo
			//
			this.cpt_notifiNo.Location = new System.Drawing.Point(53, 41);
			this.cpt_notifiNo.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
			this.cpt_notifiNo.Name = "cpt_notifiNo";
			this.cpt_notifiNo.Size = new System.Drawing.Size(112, 27);
			this.cpt_notifiNo.TabIndex = 6;
			this.cpt_notifiNo.Text = "维修单号";
			//
			// ComTimer
			//
			this.ComTimer.Tick += new System.EventHandler(this.readCounter);
			//
			// pictureBox1
			//
			this.pictureBox1.Image = ((System.Drawing.Image)(resources.GetObject("pictureBox1.Image")));
			this.pictureBox1.Location = new System.Drawing.Point(65, 25);
			this.pictureBox1.Name = "pictureBox1";
			this.pictureBox1.Size = new System.Drawing.Size(292, 74);
			this.pictureBox1.SizeMode = System.Windows.Forms.PictureBoxSizeMode.Zoom;
			this.pictureBox1.TabIndex = 10;
			this.pictureBox1.TabStop = false;
			//
			// ComRefreshTimer
			//
			this.ComRefreshTimer.Interval = 2000;
			this.ComRefreshTimer.Tick += new System.EventHandler(this.refreshCounter);
			//
			// MainForm
			//
			this.AutoScaleDimensions = new System.Drawing.SizeF(11F, 25F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(1046, 523);
			this.Controls.Add(this.pictureBox1);
			this.Controls.Add(this.gb_process);
			this.Controls.Add(this.gb_coms);
			this.Font = new System.Drawing.Font("微软雅黑", 14F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(134)));
			this.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.Name = "MainForm";
			this.Text = "工作步骤计次工具";
			this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.MainFormClosing);
			this.Load += new System.EventHandler(this.MainFormLoad);
			this.gb_coms.ResumeLayout(false);
			((System.ComponentModel.ISupportInitialize)(this.ComDataGrid)).EndInit();
			this.gb_process.ResumeLayout(false);
			((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).EndInit();
			this.ResumeLayout(false);

		}
	}
}
