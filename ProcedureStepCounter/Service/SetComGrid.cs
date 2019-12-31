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

		}
		public static List<ComData> InitGrid() {
			List<ComData> data = new List<ComData>();
			ComData testData = new ComData();
			testData.item_id = "1";
			testData.item_name = "KE-45胶水涂布次数2";
			testData.set_times = "-";
			testData.count = 0;
			testData.com_port = "COM4";
			testData.pbs = 9600;
			testData.connect_status = "断开";

			data.Add(testData);

			return data;
		}
	}
}
