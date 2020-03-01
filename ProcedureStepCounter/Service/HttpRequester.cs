/*
 * 由SharpDevelop创建。
 * 用户： Ray.G
 * 日期: 2020-02-16
 * 时间: 15:42
 *
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.IO;
using System.Net;
using System.Net.Cache;
using System.Text;

namespace ProcedureStepCounter.Service
{
	/// <summary>
	/// Description of HttpRequester.
	/// </summary>
	public class HttpRequester
	{
		public HttpRequester()
		{
		}

		public static string Login() {
			string param = "client_ip=" + Common.local_ip +
				"&process_code=" + Common.process_code +
				"&line_part=" + Common.line_part;
			HttpWebRequest req =
				(HttpWebRequest)WebRequest.Create(
					string.Format("http://{0}/{1}/login.do?method=doMeter&{2}",
								Common.rvs_server_ip, Common.rvs_server_name, param));
			HttpRequestCachePolicy noCachePolicy =
				new HttpRequestCachePolicy(HttpRequestCacheLevel.NoCacheNoStore);
			req.CachePolicy = noCachePolicy;
			req.ContentType = "text/html;charset=UTF-8";
			req.Method = "GET";
			req.Timeout = 2000;

			try {
				using (System.Net.WebResponse wr = req.GetResponse())
				{
					HttpWebResponse res = (HttpWebResponse) wr;
					if (res.StatusDescription.Equals("OK")) {
						//在这里对接收到的页面内容进行处理
						var responseContent = "";
						using (Stream resStream = res.GetResponseStream())
						{
							using (StreamReader reader = new StreamReader(resStream, Encoding.UTF8))
							{
								responseContent = reader.ReadToEnd().ToString();
							}
						}
						res.Close();
						req.Abort();
						return responseContent;
					}
					return res.StatusDescription;
				}
			} catch (System.Net.WebException e) {
				return e.Message;
			}
		}
	}
}
