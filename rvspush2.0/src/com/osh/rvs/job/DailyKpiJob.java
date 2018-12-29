package com.osh.rvs.job;

import java.util.Calendar;
import java.util.Date;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.entity.DailyKpiDataEntity;
import com.osh.rvs.service.DailyKpiReachService;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.DateUtil;

public class DailyKpiJob implements Job {

	public static Logger _log = Logger.getLogger("DailyKpiJob");
	
	@Override
	public void execute(JobExecutionContext context)throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();
		_log.info("DaliyReportJob: " + jobKey + " executing at " + new Date());

		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		try{
			conn.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);
			
			DailyKpiReachService service = new DailyKpiReachService();
			// 当天
			Calendar curTime = Calendar.getInstance();
			curTime.set(Calendar.HOUR_OF_DAY, 0);
			curTime.set(Calendar.MINUTE, 0);
			curTime.set(Calendar.SECOND, 0);
			curTime.set(Calendar.MILLISECOND, 0);

			service.insert(conn, curTime);
			
			//触发器名称
			String triggerName = context.getTrigger().getKey().getName();
			if(curTime.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY && "periodEndTrigger".equals(triggerName)){//周四,最后时间点触发
				DailyKpiDataEntity weekKPI = new DailyKpiDataEntity();
				weekKPI.setCount_date_end(curTime.getTime());
				
				//上周五
				curTime.add(Calendar.DAY_OF_MONTH, -6);
				weekKPI.setCount_date_start(curTime.getTime());
				//一周KPI
				service.insertWeekKPI(weekKPI,conn);
			}
			
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.commit();
				_log.info("Committed！");
			}
		}catch(Exception e) {
			_log.error(e.getMessage(), e);
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.rollback();
				_log.info("Rolled back！");
			}
		} finally {
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.close();
			}
			conn = null;
		}
	}

	public static void main(String[] args) {

		PathConsts.BASE_PATH = "C:\\Work\\rvsG";
		PathConsts.PROPERTIES = "\\PROPERTIES";
		PathConsts.load();

		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		try{
			conn.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);
			
			// 作业时间
			Calendar today = Calendar.getInstance();

			today.set(Calendar.MONTH, Calendar.MARCH);
			today.set(Calendar.DATE, 14);

			today.set(Calendar.HOUR_OF_DAY, 0);
			today.set(Calendar.MINUTE, 0);
			today.set(Calendar.SECOND, 0);
			today.set(Calendar.MILLISECOND, 0);

			DailyKpiReachService service = new DailyKpiReachService();
			service.insert(conn, today);
			
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.commit();
				_log.info("Committed！");
			}
		}catch(Exception e) {
			_log.error(e.getMessage(), e);
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.rollback();
				_log.info("Rolled back！");
			}
		} finally {
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.close();
			}
			conn = null;
		}
	}

	public static SqlSessionManager getTempWritableConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return SqlSessionManager.newInstance(factory);
	}
	
	public void trigger(String startDate,String endDate,String action){
		if(!CommonStringUtil.isEmpty(startDate) && !CommonStringUtil.isEmpty(endDate)){
			// 取得数据库连接
			SqlSessionManager conn = getTempWritableConn();
			
			try{
				conn.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);
				
				DailyKpiDataEntity weekKPI = new DailyKpiDataEntity();
				weekKPI.setCount_date_start(DateUtil.toDate(startDate, "yyyyMMdd"));
				weekKPI.setCount_date_end(DateUtil.toDate(endDate, "yyyyMMdd"));
				
				// 取得数据库连接
				DailyKpiReachService service = new DailyKpiReachService();
				
				if("delete".equals(action)){
					service.deletetWeekKPI(weekKPI, conn);
				}else{
					service.insertWeekKPI(weekKPI,conn);
				}
				
				if (conn != null && conn.isManagedSessionStarted()) {
					conn.commit();
					_log.info("Committed！");
				}
			}catch(Exception e) {
				_log.error(e.getMessage(), e);
				if (conn != null && conn.isManagedSessionStarted()) {
					conn.rollback();
					_log.info("Rolled back！");
				}
			} finally {
				if (conn != null && conn.isManagedSessionStarted()) {
					conn.close();
				}
				conn = null;
			}
		}
	}

}
