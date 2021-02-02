package com.osh.rvs.job;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.entity.MaterialRemainTimeEntity;
import com.osh.rvs.entity.PositionEntity;
import com.osh.rvs.mapper.push.HolidayMapper;
import com.osh.rvs.mapper.push.MaterialMapper;
import com.osh.rvs.mapper.push.ProductionFeatureMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.copy.DateUtil;

public class RemainTimeJob implements Job {


	public static Logger _log = Logger.getLogger("RemainTimeJob");

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();

		// 作业时间
		Calendar monthStart = Calendar.getInstance();

		_log.info("RemainTimeJob: " + jobKey + " executing at " + monthStart);
//
//		monthStart.set(Calendar.DATE, 1);
//		monthStart.set(Calendar.HOUR_OF_DAY, 0);
//		monthStart.set(Calendar.MINUTE, 0);
//		monthStart.set(Calendar.SECOND, 0);
//		monthStart.set(Calendar.MILLISECOND, 0);

		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		countRemainTime(monthStart, conn);
	}


	/**
	 * 统计维修剩余时间
	 * @param today
	 * @param conn
	 */
	private void countRemainTime(Calendar now, SqlSessionManager conn) {
		try {
			conn.startManagedSession(false);

			MaterialMapper mMapper = conn.getMapper(MaterialMapper.class);
			ProductionFeatureMapper pfMapper = conn.getMapper(ProductionFeatureMapper.class);
			HolidayMapper hMapper = conn.getMapper(HolidayMapper.class);
			// 删除已经不在维修中的记录
			mMapper.deleteRemainTime();
			
			// 查询全部维修中的记录
			List<MaterialEntity> inlines = mMapper.getInlineMaterials();

			// 计算剩余时间并且更新
			for (MaterialEntity entity : inlines) {
				try {
				List<PositionEntity> positions = pfMapper.getNonfinishedPositions(entity.getMaterial_id(), null);
				int remainD = 0,remainN = 0,remainC = 0;
				boolean passNs = ("1".equals(entity.getPat_id()) || 1 == entity.getLevel());
				for (PositionEntity position : positions) {
					// 未做各工时
					try {
						if (passNs && "00000000013".equals(position.getLine_id())) continue;
						int overline = Integer.parseInt(RvsUtils.getLevelOverLine(entity.getModel_name(),
								entity.getCategory_name(), "" + entity.getLevel(), position.getProcess_code()));
						if ("00000000012".equals(position.getLine_id()) || "00000000060".equals(position.getLine_id())) {
							remainD += overline;						
						} else if ("00000000013".equals(position.getLine_id())) {
							remainN += overline;						
						} else if ("00000000012".equals(position.getLine_id()) || "00000000050".equals(position.getLine_id())
								|| "00000000061".equals(position.getLine_id()) || "00000000070".equals(position.getLine_id())
								|| "00000000054".equals(position.getLine_id())) {
							remainC += overline;						
						}

					} catch (Exception e) {
						// log
					}
				}
				// 剩余
				MaterialRemainTimeEntity mrtEntity = new MaterialRemainTimeEntity();
				mrtEntity.setMaterial_id(entity.getMaterial_id());
				if (remainD > remainN) {
					mrtEntity.setRemain_before_com_minutes(remainD);
				} else {
					mrtEntity.setRemain_before_com_minutes(remainN);
				}
				mrtEntity.setRemain_minutes(remainC);
				Map<String, Object> cond = new HashMap<String, Object>();

				// 无订购则无预计日
				if (entity.getSymbol1() == null || entity.getSymbol1() == 9) {
				} else if (entity.getSymbol1() > 0) {
					// 有BO则按入库预定日开始算
					if (entity.getArrival_plan_date() == null 
							|| "9999/12/31".equals(DateUtil.toString(entity.getArrival_plan_date(), DateUtil.DATE_PATTERN))) {
						// 无入库预定日
					} else {
						cond.put("start", new Date(entity.getArrival_plan_date().getTime() + 10*60*60*1000 )); // 10点
						cond.put("interval",
								mrtEntity.getRemain_before_com_minutes() + mrtEntity.getRemain_minutes());
						_log.info("arr+" + cond.get("start") + "+" + cond.get("interval"));
						Date cD = hMapper.addMinutes(cond);
						mrtEntity.setExpected_finish_time(cD);
					}
				} else {
					// 无BO则现在开始算
					cond.put("start", now.getTime());
					cond.put("interval",
							mrtEntity.getRemain_before_com_minutes() + mrtEntity.getRemain_minutes());
					_log.info("now+" + cond.get("start") + "+" + cond.get("interval"));
					Date cD = hMapper.addMinutes(cond);
					mrtEntity.setExpected_finish_time(cD);
				}

				mMapper.insertRemainTime(mrtEntity);
				} catch(Exception e) {
					_log.error(entity.getMaterial_id() + "|" + entity.getArrival_plan_date() + "|" + e.getMessage(), e);
				}
			}

			conn.commit();

//			// 触发FSE同步
//			try {
//				URL url = new URL("http://localhost:8080/fseBridge/trigger/upd_rmn/"+now.getTimeInMillis()+"/push");
//				url.getQuery();
//				URLConnection urlconn = url.openConnection();
//				urlconn.setReadTimeout(1); // 不等返回
//				urlconn.connect();
//				urlconn.getContentType(); // 这个就能触发
//			} catch (Exception e) {
//				_log.error("Failed", e);
//			}
		} catch(Exception e) {
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

	/**
	 * 测试进口
	 * @param args
	 * @throws JobExecutionException
	 */
	public static void main(String[] args) throws JobExecutionException {
		// 作业时间
		Calendar today = Calendar.getInstance();

//		today.set(Calendar.DATE, 11);
//		today.set(Calendar.HOUR_OF_DAY, 17);
//		today.set(Calendar.MINUTE, 10);

		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		PathConsts.BASE_PATH = "D:\\rvs";
		PathConsts.REPORT_TEMPLATE = "\\ReportTemplates";
				;
		PathConsts.REPORT = "\\Reports";
		PathConsts.PROPERTIES = "\\PROPERTIES";
		PathConsts.load();

		RemainTimeJob job = new RemainTimeJob();
		job.countRemainTime(today, conn);
	}

	private static SqlSessionManager getTempWritableConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return SqlSessionManager.newInstance(factory);
	}
}
