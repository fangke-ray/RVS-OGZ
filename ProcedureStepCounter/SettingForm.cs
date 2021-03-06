﻿/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2020-02-17
 * 时间: 9:03
 *
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Text.RegularExpressions;
using System.Windows.Forms;
using ProcedureStepCounter.Entity;
using ProcedureStepCounter.Service;

namespace ProcedureStepCounter
{
	/// <summary>
	/// Description of SettingForm.
	/// </summary>
	public partial class SettingForm : Form
	{
		static int changeBas = 0; // 基础数据是否被更改
		static bool checkedStatus = false; // Radio修改从CheckChange开始

		public SettingForm()
		{
			InitializeComponent();

			changeBas = 0;
			ComSetGrid.DataSource = GetFromComdata();
			this.txt_rvs_server_ip.Text = Common.rvs_server_ip;
			this.txt_rvs_server_name.Text = Common.rvs_server_name;

			this.txt_local_ip.Text = Common.local_ip;
			this.txt_process_code.Text = Common.process_code;

			checkedStatus = false;
			switch (Common.line_part) {
				case "A" : this.rad_px_a.Checked = true; checkedStatus=true; break;
				case "B" : this.rad_px_b.Checked = true; checkedStatus=true; break;
			}

			SetComGrid.InitSetDataGridColumnHeader(ComSetGrid);
			changeBas = -1;
		}

		private List<ComSettingData> GetFromComdata()
		{
			List<ComSettingData> ret = new List<ComSettingData>();
			foreach (ComData en in Common.comdata) {
				ComSettingData csData = new ComSettingData();
				csData.item_id = en.item_id;
				csData.item_name = en.item_name;
				csData.com_port = en.com_port;
				csData.pbs = en.pbs;
				csData.address_code = en.address_code;

				ret.Add(csData);
			}
			return ret;
		}

		void SettingBeginEdit(object sender, DataGridViewCellCancelEventArgs e)
		{
			if (e != null && e.RowIndex != -1)
			{
				DataGridView dgv = sender as DataGridView;
				DataGridViewCell selectedCell = dgv.CurrentCell;
				selectedCell.Style.BackColor = Color.White;
			}
		}
		void SettingValdation(object sender, DataGridViewCellValidatingEventArgs e)
		{
			if (e != null && e.RowIndex != -1)
			{
				DataGridView dgv = sender as DataGridView;
				DataGridViewCell selectedCell = dgv.CurrentCell;
				if (e.ColumnIndex < 2) {
					return;
				}

				string vVal = e.FormattedValue.ToString();
				bool isCancel = valid(e.ColumnIndex, vVal);
				if (e.ColumnIndex == 3) e.Cancel = isCancel;

				if (isCancel) {
					selectedCell.Style.BackColor = Color.Orange;
				} else {
					selectedCell.Style.BackColor = Color.Aqua;
				}
			}
		}
		void SettingValidated(object sender, DataGridViewCellEventArgs e)
		{
			if (e == null) return;
			DataGridView dgv = sender as DataGridView;
			DataGridViewCell selectedCell = dgv.Rows[e.RowIndex].Cells[e.ColumnIndex];
			if (e.RowIndex != -1 && e.ColumnIndex == 2 && selectedCell.Value != null)
			{
				selectedCell.Value = selectedCell.Value.ToString().ToUpper();
			}
		}

//		void SettingEe(object sender, DataGridViewCellEventArgs e)
//		{
//			DataGridView dgv = sender as DataGridView;
//			DataGridViewCell selectedCell = dgv.Rows[e.RowIndex].Cells[e.ColumnIndex];
//			MessageBox.Show(selectedCell.Value.ToString(), "Info");
//		}
		bool CheckAllComSetGrid()
		{
			this.lbl_rvs_server_ip.Focus();
			bool isPass = true;
			foreach(DataGridViewRow row in ComSetGrid.Rows) {
				DataGridViewCell cell = row.Cells[2];
				bool isCancle = valid(2, cell.Value == null ? "" : cell.Value.ToString());
				if (isCancle) cell.Style.BackColor = Color.Orange;
				isPass &= !isCancle;
				cell = row.Cells[3];
				isCancle = valid(3, cell.Value == null ? "" : cell.Value.ToString());
				if (isCancle) cell.Style.BackColor = Color.Orange;
				isPass &= !isCancle;
				cell = row.Cells[4];
				isCancle = valid(4, cell.Value == null ? "" : cell.Value.ToString());
				if (isCancle) cell.Style.BackColor = Color.Orange;
				isPass &= !isCancle;
			}
			return isPass;
		}

		private bool valid(int ColumnIndex, string vVal) {
			bool isCancel = false;
			switch(ColumnIndex) {
				case 2 : {
					if (vVal.Equals("")) {
						isCancel = true;
					} else {
						if (vVal.StartsWith("c") || vVal.StartsWith("C")) {
							vVal = vVal.ToUpper();
						}
						isCancel = !Regex.IsMatch(vVal, @"^COM\d{1,2}$");
					}
					break;
				}
				case 3 : {
					if (vVal.Equals("")) {
						isCancel = true;
					} else {
						isCancel = !Regex.IsMatch(vVal, @"^\d{4,5}$");
					}
					break;
				}
				case 4 : {
					if (vVal.Equals("")) {
						isCancel = true;
					} else {
						isCancel = !Regex.IsMatch(vVal, @"^\d{2}$");
					}
					break;
				}
			}
			return isCancel;
		}
		void SettingShown(object sender, EventArgs e)
		{
			if (!CheckAllComSetGrid() || Owner == null) {
				WarnLabel.Visible = true;
			} else {
				WarnLabel.Visible = false;
			}
			if (Owner == null) {
				WarnLabel.Text = "请参照 RVS 服务器及本机网络，在此设置后重启。";
			} else {
				WarnLabel.Text = "请参照仪表端及连接端口，在此设置后使用。";
			}
		}
		void Check_And_Update(object sender, FormClosingEventArgs e)
		{
			if (!CheckAllComSetGrid()) {
				WarnLabel.Visible = true;
				e.Cancel = true;
			} else {
				WarnLabel.Visible = false;
				e.Cancel = false;

				string filePath = System.IO.Path.Combine(System.AppDomain.CurrentDomain.BaseDirectory, "ProcedureStepCounter.ini");

				List<ComSettingData> DataSourceList = ComSetGrid.DataSource as List<ComSettingData>;
				for (int i = 0; i < DataSourceList.Count; i++) {
					ComSettingData csData = DataSourceList[i];
					ComData en = Common.comdata[i];
					string avaCounterId = csData.item_id;
					en.com_port = csData.com_port;
					en.pbs = csData.pbs;
					en.address_code = csData.address_code;

					ComConnector.baudRateOfCom[csData.com_port] = csData.pbs;

					IniAccessory.Write("item." + avaCounterId, "com_port", csData.com_port, filePath);
					IniAccessory.Write("item." + avaCounterId, "pbs", "" + csData.pbs, filePath);
					IniAccessory.Write("item." + avaCounterId, "address_code", csData.address_code, filePath);
				}

				Common.process_code = txt_process_code.Text;
				IniAccessory.Write("position", "process_code", txt_process_code.Text, filePath);

				string line_part = "";
				if (rad_px_a.Checked) {
					line_part = "A";
				} else if (rad_px_b.Checked) {
					line_part = "B";
				}
				Common.line_part = line_part;
				IniAccessory.Write("position", "line_part", line_part, filePath);

				if (changeBas == 1) {
					IniAccessory.Write("net", "rvs_server_ip", txt_rvs_server_ip.Text, filePath);
					IniAccessory.Write("net", "rvs_server_name", txt_rvs_server_name.Text, filePath);
					IniAccessory.Write("net", "local_ip", txt_local_ip.Text, filePath);
					MessageBox.Show("配置已更新，请重新启动。", "Info");
					if (Owner != null) {
						changeBas = 0;
						Owner.Close();
					}
					Environment.Exit(0);
				}
			}
		}
		void BaseTextChanged(object sender, EventArgs e)
		{
			if (changeBas == 0) return;
			TextBox lbl = (TextBox) sender;
			lbl.BackColor = Color.LightBlue;
			if (changeBas == -1) {
				changeBas = 1;
			}

		}
		void Rad_px_Click(object sender, EventArgs e)
		{
			RadioButton rb = (RadioButton) sender;
			if (checkedStatus)
			{
				rb.Checked = false;
				//Checked=false触发了CheckedChanged，所以这里应该把状态重置为true
				checkedStatus = true;
			}
			checkedStatus = !checkedStatus;
		}
		void Rad_px_CheckedChanged(object sender, EventArgs e)
		{
			RadioButton rb = (RadioButton) sender;
			if (!rb.Checked)
			{
				checkedStatus = false;
			}
			if (changeBas == 0) return;
			rb.BackColor = Color.LightBlue;
			if (changeBas == -1) {
				changeBas = 1;
			}
		}
	}
}
