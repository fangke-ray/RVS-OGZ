package com.osh.rvs.job;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.ReverseResolution;
import com.osh.rvs.service.PackageFilingService;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;

public class PackageFilingJob implements Job {

	public static Logger _log = Logger.getLogger("PackageFilingJob");

	private static JSON json = new JSON();
	static {
		json.setSuppressNull(true);
	}

	private static Map<String, String> modelMap = new HashMap<String, String>();

	private static void resetModelMap(SqlSession conn) {
		File packagePath = new File(PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\package");
		if (packagePath.exists() && packagePath.isDirectory()) {
			for (File template : packagePath.listFiles()) {
				String fileName = template.getName();
				int extPoint = fileName.lastIndexOf(".");
				if (extPoint > 0 && "xls".equals(fileName.substring(extPoint + 1))) {
					String modelName = fileName.substring(0, extPoint);
					String modelId = ReverseResolution.getModelByName(modelName, conn);
					if (modelId != null) {
						modelMap.put(modelName, modelId);
					}
				}
			}
		}
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();

		// 作业时间
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);

		_log.info("PackageFilingJob: " + jobKey + " executing at " + today);

		// 取得数据库连接
		SqlSession conn = getTempConn();

		_log.info("resetModelMap");
		resetModelMap(conn);

		PackageFilingService service = new PackageFilingService();

		service.packDailyFile(today, modelMap, conn);

		conn.close();
		conn = null;

	}

	public static SqlSession getTempConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
	}

//	2019-11-11 13:55:51
//	2019-11-12 08:28:43
//	2019-11-13 08:25:18
//	2019-11-14 08:25:51
//	2019-11-18 11:32:50
//	2019-11-19 08:34:58
//	2019-11-22 16:18:31
//	2019-11-23 08:37:19
//	2019-11-25 08:26:01
//	2019-11-26 08:25:17
//	2019-11-29 10:21:05
	public static void main(String[] args) throws JobExecutionException {
		// 作业时间
		Calendar today = Calendar.getInstance();

		today.set(Calendar.YEAR, 2019);
		today.set(Calendar.MONTH, Calendar.NOVEMBER);
		today.set(Calendar.DATE, 29);

		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		PathConsts.BASE_PATH = "E:\\rvsG";
		PathConsts.REPORT_TEMPLATE = "\\ReportTemplates";
		PathConsts.REPORT = "\\Reports";
		PathConsts.IMAGES = "\\Images";

		// 取得数据库连接
		SqlSession conn = getTempConn();

		resetModelMap(conn);

		PackageFilingService service = new PackageFilingService();
		service.packDailyFile(today, modelMap, conn);

		conn.close();
		conn = null;

	}
}
