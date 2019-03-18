package com.osh.rvs.job;
import static framework.huiqing.common.util.CommonStringUtil.nullToAlter;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.MailUtils;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.mapper.push.MaterialMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.DateUtil;

public class ForecastOverTimeOfMaterialJob implements Job {

	public static Logger _log = Logger.getLogger("ForecastOverTimeOfMaterialJob");

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();
		_log.info("OverTimeOfMaterialJob: " + jobKey + " executing at " + new Date());

		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		try {
			conn.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);

			if (RvsUtils.isHoliday(new Date(), conn)) {
				// 休日不发送
				_log.info("Enjoy Holiday！");
				return;
			}

			// 得到当前未产出一览
			MaterialMapper mDao = conn.getMapper(MaterialMapper.class);

			// 8天纳期前日检测
			checkForBeforeExpeditionDay(conn, mDao);

			// 零件到货后4天纳期前日检测
			checkForBeforeExpeditionByPartialDay(conn, mDao);

			if (conn != null && conn.isManagedSessionStarted()) {
				conn.commit();
				_log.info("Committed！");
			}

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

	private void checkForBeforeExpeditionByPartialDay(SqlSessionManager conn, MaterialMapper mDao) throws UnsupportedEncodingException {
		List<MaterialEntity> comTodayDelayMaterials = mDao.getCloserDelayByPartialMaterials();

		_log.info("零件到货后临近超期(Com Over) Count: " + comTodayDelayMaterials.size());
		
		String materails4Mail = "";
		String materails4HtmlMail = 
				"<table><tr><th class='td-title-c'>No.</th><th class='td-title-c'>SORC No.</th><th class='td-title-c'>型号</th><th class='td-title-c'>机身号</th><th class='td-title-c'>等级</th>" +
				"<th class='td-title-c'>同意日</th><th class='td-title-c'>入库预定日</th><th class='td-title-c'>课室</th><th class='td-title-c'>" +
				"预定纳期</th>" +
				"<th class='td-title-c'>出货安排</th><th class='td-title-c'>备注</th>";

		int i = 0;
		for (MaterialEntity comTodayDelayMaterial : comTodayDelayMaterials) { // 对每个超期的维修对象
			i ++;

			String sorc_no = comTodayDelayMaterial.getSorc_no();
			Date scheduled_date = comTodayDelayMaterial.getScheduled_date();

			// 查询收件人-线长

			// 邮件内容
			String sline = sorc_no + "##spare##"
							+ comTodayDelayMaterial.getModel_name() + "##spare##"
							+ comTodayDelayMaterial.getSerial_no() + "##spare##"
							+ CodeListUtils.getSelectOptions("material_level_all", "" + comTodayDelayMaterial.getLevel())  + "##spare##"
							+ DateUtil.toString(comTodayDelayMaterial.getAgreed_date(), "MM-dd") + "##spare##"
							+ RvsUtils.arrivalPlanDate2String(comTodayDelayMaterial.getArrival_plan_date(), "MM-dd") + "##spare##"
							+ comTodayDelayMaterial.getSection_name() + "##spare##"
							+ DateUtil.toString(scheduled_date, "MM-dd") + "##spare##"
							+ nullToAlter(DateUtil.toString(comTodayDelayMaterial.getScheduled_assign_date(), "MM-dd"), " - ") + "##spare##"
							+ nullToAlter(comTodayDelayMaterial.getScheduled_manager_comment(), "　");
			materails4Mail += sline.replaceAll("##spare##", "\t") + "\n" ;
			materails4HtmlMail += "<tr><td>" + i + "</td><td>" + sline.replaceAll("##spare##", "</td><td>") + "</td></tr>";
		}

		// 邮件
		if (materails4Mail.length() > 0) {
			materails4Mail = "以下维修对象：\n\n" +
					"SORC No.\t型号\t机身号\t等级\t同意日\t入库预定日\t课室\t" +
					RvsConsts.TIME_LIMIT + "天纳期\t出货安排\t备注" 
					+ materails4Mail + "\n下一工作日为零件到达后第４日，请参考。";

			String subject = "下一工作日为零件到达后4天的维修对象";

			materails4HtmlMail += "</table><p>以下为下一工作日为零件到达后第４日的维修对象，请参考。</p>";

			Collection<InternetAddress> toIas = RvsUtils.getMailIas("fore.expedition.partial.to", conn);
			Collection<InternetAddress> ccIas = RvsUtils.getMailIas("fore.expedition.partial.cc", conn);

			_log.info("------- Part ---- Content -------");
			_log.info(materails4HtmlMail);
			_log.info("---------------------------------");
			MailUtils.sendHtmlMail(toIas, ccIas, subject, materails4HtmlMail, materails4Mail);
		}
	}

	private void checkForBeforeExpeditionDay(SqlSessionManager conn, MaterialMapper mDao) throws UnsupportedEncodingException {
		List<MaterialEntity> comTodayDelayMaterials = mDao.getCloserDelayMaterials(); // 总组 -> 完成前工程

		_log.info("修理完成临近(Com Over) Count: " + comTodayDelayMaterials.size());

		String materails4Mail = "";
		String materails4HtmlMail = "<style>table{width: 80%;} td{border: 1px solid #000;font-size: 14px;} .td-title-c {border: 1px solid #000;color: #FFF;background-color: #1F497D;text-align: center;} tr td:last-child{max-width:180px;}</style>" +
				"<p>以下维修对象：</p>" +
				"<table><tr><th class='td-title-c'>No.</th><th class='td-title-c'>SORC No.</th><th class='td-title-c'>型号</th><th class='td-title-c'>机身号</th><th class='td-title-c'>等级</th>" +
				"<th class='td-title-c'>同意日</th><th class='td-title-c'>入库预定日</th><th class='td-title-c'>课室</th><th class='td-title-c'>" +
				RvsConsts.TIME_LIMIT + "天纳期</th>" +
				"<th class='td-title-c'>出货安排</th><th class='td-title-c'>备注</th>";

		int i = 0;
		for (MaterialEntity comTodayDelayMaterial : comTodayDelayMaterials) { // 对每个超期的维修对象
			i ++;

			String sorc_no = comTodayDelayMaterial.getSorc_no();
			Date scheduled_date = comTodayDelayMaterial.getScheduled_date();

			// 查询收件人-线长

			// 邮件内容
			String sline = sorc_no + "##spare##"
							+ comTodayDelayMaterial.getModel_name() + "##spare##"
							+ comTodayDelayMaterial.getSerial_no() + "##spare##"
							+ CodeListUtils.getSelectOptions("material_level_all", "" + comTodayDelayMaterial.getLevel())  + "##spare##"
							+ DateUtil.toString(comTodayDelayMaterial.getAgreed_date(), "MM-dd") + "##spare##"
							+ RvsUtils.arrivalPlanDate2String(comTodayDelayMaterial.getArrival_plan_date(), "MM-dd") + "##spare##"
							+ comTodayDelayMaterial.getSection_name() + "##spare##"
							+ DateUtil.toString(scheduled_date, "MM-dd") + "##spare##"
							+ nullToAlter(DateUtil.toString(comTodayDelayMaterial.getScheduled_assign_date(), "MM-dd"), " - ") + "##spare##"
							+ nullToAlter(comTodayDelayMaterial.getScheduled_manager_comment(), "　");
			materails4Mail += sline.replaceAll("##spare##", "\t") + "\n" ;
			materails4HtmlMail += "<tr><td>" + i + "</td><td>" + sline.replaceAll("##spare##", "</td><td>") + "</td></tr>";
		}

		// 邮件
		if (materails4Mail.length() > 0) {
			String base = "" + PathConsts.MAIL_CONFIG.getProperty("fore.expedition.content");
			materails4Mail = base.replaceAll("{timeLimit}", "" + RvsConsts.TIME_LIMIT)
					.replaceAll("{content}", materails4Mail);

			String subject = PathConsts.MAIL_CONFIG.getProperty("fore.expedition.title");

			materails4HtmlMail += "</table><p>将于下一工作日到达产出安排纳期，请参考。</p>";
			Collection<InternetAddress> toIas = RvsUtils.getMailIas("fore.expedition.to", conn);
			Collection<InternetAddress> ccIas = RvsUtils.getMailIas("fore.expedition.cc", conn);

			MailUtils.sendHtmlMail(toIas, ccIas, subject, materails4HtmlMail, materails4Mail);
		}
	}

	public static SqlSession getTempReadonlyConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
	}

	public static SqlSessionManager getTempWritableConn() {
		_log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return SqlSessionManager.newInstance(factory);
	}

}
