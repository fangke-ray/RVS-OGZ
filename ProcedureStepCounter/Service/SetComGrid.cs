/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2019-12-30
 * 时间: 14:29
 * 
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.Collections.Generic;

using System.Windows.Forms;
using ProcedureStepCounter.Entity;

namespace ProcedureStepCounter.Service
{
	/// <summary>
	/// Description of SetComGrid.
	/// </summary>
	public partial class SetComGrid
	{
		public SetComGrid()
		{
		}
		public static void InitDataGridColumnHeader(DataGridView grid) {
			grid.RowTemplate.Height = 40;

			grid.Columns[0].HeaderText = "";
			grid.Columns[0].Width=0;

			grid.Columns[1].HeaderText = "计数项目";
			grid.Columns[1].Width=320;

			grid.Columns[2].HeaderText = "标准次数";
			grid.Columns[2].Width=115;
			grid.Columns[2].DefaultCellStyle.Alignment = DataGridViewContentAlignment.MiddleCenter;

			grid.Columns[3].HeaderText = "最近计数";
			grid.Columns[3].Width=115;
			grid.Columns[3].DefaultCellStyle.Alignment = DataGridViewContentAlignment.MiddleCenter;

			grid.Columns[4].HeaderText = "端口";
			grid.Columns[4].Width=100;
			grid.Columns[4].DefaultCellStyle.Alignment = DataGridViewContentAlignment.MiddleCenter;

			grid.Columns[5].HeaderText = "波特率";
			grid.Columns[5].Width=100;
			grid.Columns[5].DefaultCellStyle.Alignment = DataGridViewContentAlignment.MiddleCenter;

			grid.Columns[6].HeaderText = "连接";
			grid.Columns[6].Width=80;
			grid.Columns[6].DefaultCellStyle.Alignment = DataGridViewContentAlignment.MiddleCenter;

			grid.Columns[7].Visible = false;
			grid.Columns[8].Visible = false;
		}

		public static void InitSetDataGridColumnHeader(DataGridView grid) {
			grid.RowTemplate.Height = 40;

			grid.Columns[0].HeaderText = "";
			grid.Columns[0].Visible = false;

			grid.Columns[1].HeaderText = "计数项目";
			grid.Columns[1].Width=170;
			grid.Columns[1].ReadOnly = true;

			grid.Columns[2].HeaderText = "端口";
			grid.Columns[2].Width=60;
			grid.Columns[2].DefaultCellStyle.Alignment = DataGridViewContentAlignment.MiddleCenter;
			grid.Columns[2].DefaultCellStyle.BackColor = System.Drawing.Color.Aqua;

			grid.Columns[3].HeaderText = "波特率";
			grid.Columns[3].Width=68;
			grid.Columns[3].DefaultCellStyle.Alignment = DataGridViewContentAlignment.MiddleCenter;
			grid.Columns[3].DefaultCellStyle.BackColor = System.Drawing.Color.Aqua;

			grid.Columns[4].HeaderText = "地址号";
			grid.Columns[4].Width=70;
			grid.Columns[4].DefaultCellStyle.Alignment = DataGridViewContentAlignment.MiddleCenter;
			grid.Columns[4].DefaultCellStyle.BackColor = System.Drawing.Color.Aqua;
		}
	}
}
