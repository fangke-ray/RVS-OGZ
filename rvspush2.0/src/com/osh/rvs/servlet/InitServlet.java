package com.osh.rvs.servlet;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.entity.BoundMaps;
import com.osh.rvs.job.DailyKpiJob;
import com.osh.rvs.job.DailyKpiSheetsJob;
import com.osh.rvs.job.DailyWorkSheetsJob;
import com.osh.rvs.job.DayWorkTotalToMonthJob;
import com.osh.rvs.job.ForecastOverTimeOfMaterialJob;
import com.osh.rvs.job.InfectWarningJob;
import com.osh.rvs.job.InlinePlanJob;
import com.osh.rvs.job.OverTimeOfMaterialJob;
import com.osh.rvs.job.PartialWarehouseJob;
import com.osh.rvs.job.PositionStandardTimeQueue;
import com.osh.rvs.job.RemainTimeJob;
import com.osh.rvs.job.SchedulePostponeJob;

public class InitServlet extends HttpServlet {

	private static final long serialVersionUID = -3163557381361759907L;

	private Scheduler scheduler = null;

	public Logger logger = Logger.getLogger("Init");

	@Override
	public void init(ServletConfig config) throws ServletException {
		// 设定WebSocket连接对象集
		BoundMaps.init();

		loadScheduler();

		new Thread(PositionStandardTimeQueue.instance).start();

		super.init(config);
	}

	@Override
	public void destroy() {
		if (scheduler != null) closeScheduler();
		logger.info("over----------------");

		super.destroy();
	}

	private void loadScheduler() {
		try {
			// Grab the Scheduler instance from the Factory
			scheduler = StdSchedulerFactory.getDefaultScheduler();

			// and start it off
			scheduler.start();

			// 纳期提醒时间
			int otmHour = 17;
			int otmMinute = 00;
			String expedition_jobtime = PathConsts.MAIL_CONFIG.getProperty("expedition.jobtime");
			logger.info(PathConsts.BASE_PATH + PathConsts.PROPERTIES + "\\mail.properties" + " : expedition_jobtime=" + expedition_jobtime);
			if (expedition_jobtime != null && expedition_jobtime.matches("\\d\\d:\\d\\d")) {
				String[] sp = expedition_jobtime.split(":");
				if (sp.length == 2) {
					otmHour = Integer.parseInt(sp[0], 10);
					otmMinute = Integer.parseInt(sp[1], 10);
				}
			}
			logger.info("o" + otmHour + ":" + otmMinute);

			// 纳期提醒事务
			JobDetail job = newJob(OverTimeOfMaterialJob.class).withIdentity("overTimeOfMaterialJob", "rvspush").build();

			CronTrigger trigger = newTrigger().withIdentity("overTimeOfMaterialTrigger", "rvspush")
					.withSchedule(dailyAtHourAndMinute(otmHour, otmMinute))
//					.forJob("overTimeOfMaterialJob", "rvspush")
					.build();

			scheduler.scheduleJob(job, trigger);

			// 纳期提醒（次日）事务
			expedition_jobtime = PathConsts.MAIL_CONFIG.getProperty("fore.expedition.jobtime");
			logger.info(PathConsts.BASE_PATH + PathConsts.PROPERTIES + "\\mail.properties" + " : fore_expedition_jobtime=" + expedition_jobtime);
			if (expedition_jobtime != null && expedition_jobtime.matches("\\d\\d:\\d\\d")) {
				String[] sp = expedition_jobtime.split(":");
				if (sp.length == 2) {
					otmHour = Integer.parseInt(sp[0], 10);
					otmMinute = Integer.parseInt(sp[1], 10);
				}
			}

			job = newJob(ForecastOverTimeOfMaterialJob.class).withIdentity("foreOverTimeOfMaterialTrigger", "rvspush").build();

			trigger = newTrigger().withIdentity("foreOverTimeOfMaterialTrigger", "rvspush")
					.withSchedule(dailyAtHourAndMinute(otmHour, otmMinute))
					.build();

			scheduler.scheduleJob(job, trigger);
			
			// 作业日报生成事务
			job = newJob(DailyWorkSheetsJob.class).withIdentity("dailyWorkSheetsJob", "rvspush").build();

			trigger = newTrigger().withIdentity("dailyWorkSheetsTrigger", "rvspush")
					.withSchedule(dailyAtHourAndMinute(23, 05)) // 23, 05
					.build();

			scheduler.scheduleJob(job, trigger);

			// 计划自动推延事务
			job = newJob(SchedulePostponeJob.class).withIdentity("schedulePostponeJob", "rvspush").build();

			trigger = newTrigger().withIdentity("schedulePostponeTrigger", "rvspush")
					.withSchedule(dailyAtHourAndMinute(23, 45))
					.build();

			scheduler.scheduleJob(job, trigger);

			// KPI日报生成事务
			job = newJob(DailyKpiSheetsJob.class).withIdentity("dailyKpiSheetsJob2", "rvspush").build();

			otmHour = 10; otmMinute = 27;
			expedition_jobtime = PathConsts.MAIL_CONFIG.getProperty("daily.kpi.jobtime");
			logger.info(PathConsts.BASE_PATH + PathConsts.PROPERTIES + "\\mail.properties" + " : daily.kpi.jobtime=" + expedition_jobtime);
			if (expedition_jobtime != null && expedition_jobtime.matches("\\d\\d:\\d\\d")) {
				String[] sp = expedition_jobtime.split(":");
				if (sp.length == 2) {
					otmHour = Integer.parseInt(sp[0], 10);
					otmMinute = Integer.parseInt(sp[1], 10);
				}
			}

			trigger = newTrigger().withIdentity("dailyKpiSheetsTriggerReport2", "rvspush")
					.withSchedule(dailyAtHourAndMinute(otmHour, otmMinute)) // 10, 27
					.build();

			scheduler.scheduleJob(job, trigger);
			
//			job = newJob(OverTimeOfPositionJob.class)
//				    .withIdentity("overTimeOfPositionJob", "rvspush")
//				    .build();
//
//			trigger = newTrigger()
//			    .withIdentity("overTimeOfPositionTrigger", "rvspush")
//			    .withSchedule(cronSchedule("0 * * * * ?"))
//			    .build();
//
//			scheduler.scheduleJob(job, trigger);

//			// 周报任务脚本
//			job = newJob(WeekStatisticsJob.class)
//			    .withIdentity("weekStatisticsJob", "rvspush")
//			    .build();
//
//			// 每周四晚上
//			trigger = newTrigger()
//			    .withIdentity("weekStatisticsTrigger", "rvspush")
//			    .withSchedule(cronSchedule("0 0 22 ? * 5")) // 22
//			    .build();

//			scheduler.scheduleJob(job, trigger);

			// 工作月报任务脚本
			job = newJob(DayWorkTotalToMonthJob.class)
				    .withIdentity("dayWorkTotalToMonthJob", "rvspush")
				    .build();

			// 每月最后一日晚上
			trigger = newTrigger()
			    .withIdentity("dayWorkTotalToMonthTrigger", "rvspush")
			    .withSchedule(cronSchedule("0 15 22 L * ?")) // "0 15 22 L * ?"
			    .build();

			scheduler.scheduleJob(job, trigger);

			// 点检通知脚本
			job = newJob(InfectWarningJob.class)
			    .withIdentity("infectWarningJob", "rvspush")
			    .build();

			// 每天早上6点
			trigger = newTrigger().withIdentity("infectWarningTrigger", "rvspush")
					.withSchedule(dailyAtHourAndMinute(6, 01)) // 6,1
					.build();

			scheduler.scheduleJob(job, trigger);

//			// 点检归档脚本
//			job = newJob(InfectFilingJob.class)
//			    .withIdentity("infectFilingJob", "rvspush")
//			    .build();
//
//			// 每月第一个周一
//			trigger = newTrigger().withIdentity("infectFilingTrigger", "rvspush")
//				    .withSchedule(cronSchedule("0 20 19 ? * 2#1")) // first Monday
//					.build();

//			scheduler.scheduleJob(job, trigger);

			// 作业剩余时间脚本
			job = newJob(RemainTimeJob.class)
			    .withIdentity("remainTimeJob", "rvspush")
			    .build();

			// 每天工作时间每隔20分钟
			trigger = newTrigger()
			    .withIdentity("remainTimeTrigger", "rvspush")
			    .withSchedule(cronSchedule("0 0/20 9-17 * * ?")) // 
			    .build();

			scheduler.scheduleJob(job, trigger);

			//KPI
			JobDetail dailyKpiJob = JobBuilder.newJob(DailyKpiJob.class).storeDurably().withIdentity("DailyKpiJob", "rvspush").build();
			CronTrigger trigger4 = TriggerBuilder.newTrigger().withIdentity("periodOneTrigger", "rvspush").forJob(dailyKpiJob).withSchedule(dailyAtHourAndMinute(10, 45)).build();
			CronTrigger trigger5 = TriggerBuilder.newTrigger().withIdentity("periodTwoTrigger", "rvspush").forJob(dailyKpiJob).withSchedule(dailyAtHourAndMinute(12, 0)).build();
			CronTrigger trigger6 = TriggerBuilder.newTrigger().withIdentity("periodThreeTrigger", "rvspush").forJob(dailyKpiJob).withSchedule(dailyAtHourAndMinute(14, 45)).build();
			CronTrigger trigger7 = TriggerBuilder.newTrigger().withIdentity("periodFourTrigger", "rvspush").forJob(dailyKpiJob).withSchedule(dailyAtHourAndMinute(15, 45)).build();
			CronTrigger trigger8 = TriggerBuilder.newTrigger().withIdentity("periodFiveTrigger", "rvspush").forJob(dailyKpiJob).withSchedule(dailyAtHourAndMinute(17, 10)).build();
			CronTrigger trigger9 = TriggerBuilder.newTrigger().withIdentity("periodEndTrigger", "rvspush").forJob(dailyKpiJob).withSchedule(dailyAtHourAndMinute(20, 15)).build();

			scheduler.addJob(dailyKpiJob,true);
			scheduler.scheduleJob(trigger4);
			scheduler.scheduleJob(trigger5);
			scheduler.scheduleJob(trigger6);
			scheduler.scheduleJob(trigger7);
			scheduler.scheduleJob(trigger8);
			scheduler.scheduleJob(trigger9);
			
			// 投线计划订单
			JobDetail inlinePlanJob = JobBuilder.newJob(InlinePlanJob.class).storeDurably().withIdentity("InlinePlanJob", "rvspush").build();
			scheduler.addJob(inlinePlanJob, true);

			int inlineJobStep = 1;
			while (true){
				String inlinePlanPrepairTime = PathConsts.SCHEDULE_SETTINGS.getProperty("inline.plan.prepair.time." + inlineJobStep);
				if (inlinePlanPrepairTime == null) break;

				String[] sp = inlinePlanPrepairTime.split(":");
				if (sp.length == 2) {
					otmHour = Integer.parseInt(sp[0], 10);
					otmMinute = Integer.parseInt(sp[1], 10);
				}

				CronTrigger triggerIpj = TriggerBuilder.newTrigger()
						.withIdentity("triggerIpj" + inlineJobStep, "rvspush")
						.forJob(inlinePlanJob)
						.withSchedule(dailyAtHourAndMinute(otmHour, otmMinute))
						.build();

				scheduler.scheduleJob(triggerIpj);

				inlineJobStep ++;
			}

			// 零件出入库工时
			job = newJob(PartialWarehouseJob.class).withIdentity("partialWarehouseJob", "rvspush").build();

			trigger = newTrigger().withIdentity("partialWarehouseTrigger", "rvspush")
					.withSchedule(dailyAtHourAndMinute(23, 05)) // 23, 05
					.build();

			scheduler.scheduleJob(job, trigger);

		} catch (NumberFormatException nfe) {
			logger.error("Scheduler Load Fail :" + nfe.getMessage() , nfe);
		} catch (SchedulerException se) {
			logger.error("Scheduler Load Fail :" + se.getMessage() , se);
		}
	}

	private void closeScheduler() {
		try {
			if (scheduler != null && scheduler.isStarted())
			scheduler.shutdown();

		} catch (SchedulerException se) {
			logger.error("Scheduler Close Fail :" + se.getMessage() , se);
		}
	}

}