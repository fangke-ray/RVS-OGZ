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
	partial class MainForm
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

		private System.Windows.Forms.Timer ComRefreshTimer;
		private System.Windows.Forms.Button SettingButton;

		protected override void Dispose(bool disposing)
		{
			if (disposing && this.components != null)
			{
				this.components.Dispose();
			}
			base.Dispose(disposing);
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
			this.SettingButton = new System.Windows.Forms.Button();
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
			this.ComRefreshTimer.Interval = 1500;
			this.ComRefreshTimer.Tick += new System.EventHandler(this.refreshCounter);
			// 
			// SettingButton
			// 
			this.SettingButton.Image = ((System.Drawing.Image)(resources.GetObject("SettingButton.Image")));
			this.SettingButton.Location = new System.Drawing.Point(951, 95);
			this.SettingButton.Name = "SettingButton";
			this.SettingButton.Size = new System.Drawing.Size(29, 28);
			this.SettingButton.TabIndex = 11;
			this.SettingButton.UseVisualStyleBackColor = true;
			this.SettingButton.Click += new System.EventHandler(this.SettingButtonClick);
			// 
			// MainForm
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(11F, 25F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(1046, 523);
			this.Controls.Add(this.SettingButton);
			this.Controls.Add(this.pictureBox1);
			this.Controls.Add(this.gb_process);
			this.Controls.Add(this.gb_coms);
			this.Font = new System.Drawing.Font("微软雅黑", 14F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(134)));
			this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
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
