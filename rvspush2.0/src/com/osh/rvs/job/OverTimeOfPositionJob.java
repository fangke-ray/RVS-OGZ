package com.osh.rvs.job;
import java.util.Date;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Job;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;

public class OverTimeOfPositionJob implements Job {

	public Logger _log = Logger.getLogger("OverTimeOfPositionJob");

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();
		_log.info("OverTimeOfPositionJob says: " + jobKey + " executing at " + new Date());

		// 取得数据库连接
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		SqlSessionManager conn = SqlSessionManager.newInstance(factory);

		// 取得 working
		/// 取得本次工时
		// Integer use_seconds = ppService.getTotalTimeByRework(workingPf, conn);

	}
}
