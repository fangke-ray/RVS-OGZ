package com.osh.rvs.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.mapper.push.MaterialMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;

public class InlinePlanJob implements Job {

	public static Logger _log = Logger.getLogger("InlinePlanJob");

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();

		// 作业时间
		Calendar today = Calendar.getInstance();

		_log.info("InlinePlanJob: " + jobKey + " executing at " + today);
		JobDataMap jdMap = context.getMergedJobDataMap();

		String upperlimit = jdMap.getString("upperlimit");

		if (upperlimit == null) return;

		String upperlimitFabric = jdMap.getString("upperlimitFabric");
		int iUpperlimit = 0, iUpperlimitFabric = 0;

		try {
			iUpperlimit = Integer.parseInt(upperlimit, 10);
			if (upperlimitFabric != null)
				iUpperlimitFabric = Integer.parseInt(upperlimitFabric, 10);
		} catch (NumberFormatException ne) {
		}

		if (iUpperlimit == 0) return;

		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		try {
			conn.startManagedSession(false);
			makeInlinePlan(iUpperlimit, iUpperlimitFabric, conn);

			conn.commit();
		} catch (Exception e) {
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

	private void makeInlinePlan(int iUpperlimit, int iUpperlimitFabric,
			SqlSessionManager conn) {
		MaterialMapper mapper = conn.getMapper(MaterialMapper.class);

		List<MaterialEntity> allList = mapper.getInlinePlan();
		List<MaterialEntity> resultList = new ArrayList<MaterialEntity>();

		for (MaterialEntity result : allList) {
			if (iUpperlimit == 0)
				break;
			if ("纤维镜".equals(result.getCategory_name())) {
				if (iUpperlimitFabric == 0) continue;
				resultList.add(result);
				iUpperlimitFabric--;
			} else {
				resultList.add(result);
			}
			iUpperlimit--;
		}

		mapper.deleteInlinePlan();

		for (MaterialEntity result : resultList) {
			// 设定流程课室
			Integer kind = result.getKind(); 
			Integer level = result.getLevel(); 
			Integer fix_type = result.getFix_type(); 
			if (fix_type == 2) {
				result.setSection_id(null);
				result.setPat_id(null);
			} else if (level == 9 || level == 91|| level == 92|| level == 93) {
				if (result.getSection_id() == null) {
					result.setSection_id("00000000001");
				}
				result.setPat_id(null);
			} else if (result.getSection_id() == null) {
				if (kind == 06 || kind == 07) {
					result.setSection_id("00000000012");
				} else {
					result.setSection_id("00000000001");
				}
			}
			mapper.createInlinePlan(result);
		}
	}

	public static SqlSessionManager getTempWritableConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return SqlSessionManager.newInstance(factory);
	}
}
