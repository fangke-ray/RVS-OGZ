/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2019-12-30
 * 时间: 14:05
 * 
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.ComponentModel;
using System.Drawing;
using System.Windows.Forms;

namespace ProcedureStepCounter.Entity
{
	/// <summary>
	/// Description of ComData.
	/// </summary>
	public partial class ComData
	{
		public ComData()
		{

		}

		public string item_id { get; set; }
		public string item_name { get; set; }
		public string set_times { get; set; }
		public int count { get; set; }
		public string com_port { get; set; }
		public int pbs { get; set; }
		public string connect_status { get; set; }

		// int parity_bits; // 校验位
	}
}
