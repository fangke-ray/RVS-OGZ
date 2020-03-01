/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2020-02-17
 * 时间: 10:34
 * 
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;

namespace ProcedureStepCounter.Entity
{
	/// <summary>
	/// Description of ComSettingData.
	/// </summary>
	public class ComSettingData
	{
		public ComSettingData()
		{
		}

		public string item_id { get; set; }
		public string item_name { get; set; }
		public string com_port { get; set; }
		public int pbs { get; set; }
		public string address_code { get; set; }

	}
}
