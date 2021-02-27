/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2020-01-06
 * 时间: 17:21
 * 
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Windows.Forms;

namespace ProcedureStepCounter.Entity
{
	/// <summary>
	/// Description of UserControl1.
	/// </summary>
	public partial class MaterialData
	{
		public MaterialData()
		{
		}

		public string omr_notifi_no { get; set; }
		public string model_name { get; set; }
		public string serial_no { get; set; }
		public Dictionary<string, string> set_times_map { get; set; }
		public Dictionary<string, string> counted_times_map { get; set; }
	}
}
