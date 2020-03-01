/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2020-02-17
 * 时间: 0:19
 * 
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.Collections;

namespace ProcedureStepCounter.Entity
{
	/// <summary>
	/// Description of ResFromServer.
	/// </summary>
	public class ResFromServer
	{
		public ResFromServer()
		{
		}
		public String error { get; set; }
		public ArrayList avaCounterList { get; set; }
	}
}
