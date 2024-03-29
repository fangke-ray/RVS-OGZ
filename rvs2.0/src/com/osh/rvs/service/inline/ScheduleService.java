package com.osh.rvs.service.inline;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.AlarmMesssageEntity;
import com.osh.rvs.bean.data.AlarmMesssageSendationEntity;
import com.osh.rvs.bean.inline.DailyKpiDataEntity;
import com.osh.rvs.bean.inline.PauseFeatureEntity;
import com.osh.rvs.bean.inline.ScheduleEntity;
import com.osh.rvs.bean.inline.ScheduleHistoryEntity;
import com.osh.rvs.bean.master.OperatorEntity;
import com.osh.rvs.bean.master.OperatorNamedEntity;
import com.osh.rvs.bean.master.SectionEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.ReverseResolution;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.AlarmMesssageForm;
import com.osh.rvs.form.inline.ScheduleForm;
import com.osh.rvs.mapper.data.AlarmMesssageMapper;
import com.osh.rvs.mapper.inline.DailyKpiMapper;
import com.osh.rvs.mapper.inline.RepairPlanMapper;
import com.osh.rvs.mapper.inline.ScheduleHistoryMapper;
import com.osh.rvs.mapper.inline.ScheduleMapper;
import com.osh.rvs.mapper.master.HolidayMapper;
import com.osh.rvs.mapper.master.OperatorMapper;
import com.osh.rvs.service.AlarmMesssageService;
import com.osh.rvs.service.PositionService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.Converter;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateConverter;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.copy.IntegerConverter;

public class ScheduleService {

	/**
	 * 在线维修对象检索一览
	 * @param form
	 * @param conn
	 * @param errors
	 * @return
	 */
	public List<ScheduleForm> getMaterialList(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		ScheduleMapper dao = conn.getMapper(ScheduleMapper.class);
		HolidayMapper hdao = conn.getMapper(HolidayMapper.class);
		ScheduleEntity conditionBean = new ScheduleEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		List<String> ids = dao.searchMaterialIdsByCondition(conditionBean);
		List<ScheduleForm> retForms = new ArrayList<ScheduleForm>();
		Map<String, String> delayMap = new HashMap<String, String>();

		if (ids != null && ids.size() > 0) {
			List<ScheduleEntity> entities = dao.searchMaterialByIds(ids);

			// 按今天为零件到达后4天日期的推算
			Date now = new Date();
			String sToday = DateUtil.toString(now, DateUtil.DATE_PATTERN);
			String sPartialArrivalLine = DateUtil.toString(RvsUtils.switchWorkDate(now, -4, conn), DateUtil.DATE_PATTERN);

			for (ScheduleEntity entity : entities) {
				ScheduleForm retForm = new ScheduleForm();
				BeanUtil.copyToForm(entity, retForm, CopyOptions.COPYOPTIONS_NOEMPTY);

				if ("00000000001".equals(entity.getSection_id())) {
					// 平行分线
					Set<String> dividePositions = PositionService.getDividePositions(conn);
					entity.setPosition_id(ReverseResolution.getPositionByProcessCode(entity.getProcessing_position(), null, conn));
					entity.setPosition_id2(ReverseResolution.getPositionByProcessCode(entity.getNs_processing_position(), null, conn));
					entity.getAgreed_date(); //
					if (dividePositions.contains(entity.getPosition_id()) 
							|| (dividePositions.contains(entity.getPosition_id2()) && entity.getNs_finish_date() == null)) {
						if (entity.getQuotation_first() == 1)
							retForm.setPx("B");
						else
							retForm.setPx("A");
					}
				}

				// 如果有警告信息则读取警告信息
				if ("1".equals(retForm.getBreak_message())) {
					//retForm
					String material_id = retForm.getMaterial_id();
					AlarmMesssageService service = new AlarmMesssageService();
					List<AlarmMesssageEntity> messages = service.getUnredAlarmMessagesByMaterial(material_id, conn);

					List<String> spareBreakMessages = new ArrayList<String>();
					AlarmMesssageMapper amDao = conn.getMapper(AlarmMesssageMapper.class);
					boolean levelup = false;
					for (AlarmMesssageEntity message : messages) {
						// 要求当前等级处理
						if (3 == message.getLevel()) {
							levelup = true;
						}

						// 取得原因
						PauseFeatureEntity pauseEntity = amDao.getBreakOperatorMessageByID(message.getAlarm_messsage_id());
						if (pauseEntity != null) {
							// 取得暂停信息里的记录
							Integer iReason = pauseEntity.getReason();
							// 不良理由
							String sReason = null;
							if (iReason != null && iReason < 10) {
								sReason = CodeListUtils.getValue("break_reason", "0" + iReason);
								if ("其他".equals(sReason)) {
									sReason = pauseEntity.getComments();
								}
							} else {
								sReason = PathConsts.POSITION_SETTINGS.getProperty("break."+ pauseEntity.getProcess_code() +"." + iReason);
							}
							spareBreakMessages.add(pauseEntity.getProcess_code() + ":" + sReason + " ");
						}
					}

					retForm.setBreak_message(CommonStringUtil.joinBy("\n", spareBreakMessages.toArray(new String[spareBreakMessages.size()])));
					if (levelup) retForm.setBreak_message_level("ME");
				} else {
					retForm.setBreak_message("");
				}

				// 如果已经经过总组接收 , 取得标准工时倒计时
//				if (pfdao.checkPositionDid(entity.getMaterial_id(), "00000000032", "2")) {
//					List<String> process_codes = padao.getNonfinishedPositions(entity.getMaterial_id());
//					int remain = 0;
//					for (String process_code : process_codes) {
//						// 未做各工时
//						try {
//							int overline = Integer.parseInt(RvsUtils.getLevelOverLine(entity.getModel_name(),
//									entity.getCategory_name(), retForm.getLevel(), null, process_code));
//							remain += overline;
//						} catch (Exception e) {
//							// log
//						}
//					}
//					// 剩余
//					retForm.setCountdown(new BigDecimal(remain).divide(new BigDecimal(60), 2, BigDecimal.ROUND_HALF_UP).toString() + "小时");
//				}

				if (entity.getRemain_minutes() != null) {
					int remain_minutes = entity.getRemain_minutes();
					if (remain_minutes > 420) {
						int mhour = remain_minutes % 420 / 60;
						if (mhour == 0) {
							retForm.setCountdown((remain_minutes / 420) + "天");
						} else {
							retForm.setCountdown((remain_minutes / 420) + "天" + mhour + "小时");
						}
					} else {
						retForm.setCountdown(new BigDecimal(remain_minutes).divide(new BigDecimal(60), 2, BigDecimal.ROUND_HALF_UP).toString() + "小时");
					}
				}

				// 如果零件到货日或者纳期已经超过，标注
				String sCom_plan_date = retForm.getCom_plan_date();
				String sArrival_plan_date = retForm.getArrival_plan_date();
				if (!isEmpty(sCom_plan_date) && sCom_plan_date.compareTo(sToday) < 0 ) {
					retForm.setIs_late("纳期");
				} else if (!isEmpty(sArrival_plan_date) && sArrival_plan_date.compareTo(sPartialArrivalLine) < 0) {
					retForm.setIs_late("零件");
				}

				// 过期颜色
				if (retForm.getScheduled_date_end() != null) {
					if (delayMap.containsKey(retForm.getScheduled_date_end()
							+ retForm.getAm_pm())) {
						retForm.setRemain_days(delayMap.get(retForm
								.getScheduled_date_end() + retForm.getAm_pm()));
					} else {
						Map<String, Object> condiMap = new HashMap<String, Object>();
						condiMap.put("scheduled_date", entity.getScheduled_date_end());
						condiMap.put("am_pm", entity.getAm_pm());
						String remain_days = hdao.compareExperial(condiMap);
						delayMap.put(retForm.getScheduled_date_end() + retForm.getAm_pm(), remain_days);
						retForm.setRemain_days(remain_days);
					}
				}

				retForms.add(retForm);
			}
		}
		return retForms;
	}

	public List<ScheduleForm> getScheduleList(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		ScheduleMapper dao = conn.getMapper(ScheduleMapper.class);
		HolidayMapper hdao = conn.getMapper(HolidayMapper.class);
		ScheduleEntity conditionBean = new ScheduleEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		Map<String, String> delayMap = new HashMap<String, String>();

		List<ScheduleEntity> entities = dao.searchScheduleByCondition(conditionBean);
		List<ScheduleForm> retForms = new ArrayList<ScheduleForm>();
		for (ScheduleEntity entity : entities) {
			ScheduleForm retForm = new ScheduleForm();
			BeanUtil.copyToForm(entity, retForm, CopyOptions.COPYOPTIONS_NOEMPTY);
			
			// 过期颜色
			if (retForm.getScheduled_date_end() != null) {
				if (delayMap.containsKey(retForm.getScheduled_date_end()
						+ retForm.getAm_pm())) {
					retForm.setRemain_days(delayMap.get(retForm
							.getScheduled_date_end() + retForm.getAm_pm()));
				} else {
					Map<String, Object> condiMap = new HashMap<String, Object>();
					condiMap.put("scheduled_date", entity.getScheduled_date_end());
					condiMap.put("am_pm", entity.getAm_pm());
					String remain_days = hdao.compareExperial(condiMap);
					delayMap.put(retForm.getScheduled_date_end() + retForm.getAm_pm(), remain_days);
					retForm.setRemain_days(remain_days);
				}
			}

			if (entity.getRemain_minutes() != null) {
				int remain_minutes = entity.getRemain_minutes();
				if (remain_minutes > 420) {
					int mhour = remain_minutes % 420 / 60;
					if (mhour == 0) {
						retForm.setCountdown((remain_minutes / 420) + "天");
					} else {
						retForm.setCountdown((remain_minutes / 420) + "天" + mhour + "小时");
					}
				} else {
					retForm.setCountdown(new BigDecimal(remain_minutes).divide(new BigDecimal(60), 2, BigDecimal.ROUND_HALF_UP).toString() + "小时");
				}
			}

			retForms.add(retForm);
		}
		return retForms;
	}

	public void updateSchedule(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors)
			throws Exception {
		ScheduleMapper dao = conn.getMapper(ScheduleMapper.class);
		
		ScheduleForm sForm = (ScheduleForm)form;
		String ids = sForm.getIds();
		String lineId = sForm.getLineIds();//单一值
		String[] material_ids = ids.split(",");
		String scheduled_assign_date = sForm.getScheduled_assign_date();

		ScheduleEntity model = new ScheduleEntity();
		BeanUtil.copyToBean(form, model, null);

		// 历史记录修改
		boolean todayfix = scheduled_assign_date.equals(DateUtil.toString(new Date(), DateUtil.DATE_PATTERN));
		ScheduleHistoryMapper shMapper = conn.getMapper(ScheduleHistoryMapper.class);
		ScheduleHistoryEntity shEntity = new ScheduleHistoryEntity();
		shEntity.setScheduled_date(DateUtil.toDate(scheduled_assign_date, DateUtil.DATE_PATTERN));

		for (String material_id : material_ids) {
			// MaterialProcessEntity old = mpMapper.loadMaterialProcessOfLine(material_id, "00000000014");

			model.setMaterial_id(material_id);
			model.setLine_id(lineId);
			dao.updateSchedule(model);
			// 历史记录修改
			if (todayfix) {
				shEntity.setMaterial_id(material_id);
				if (shMapper.getByKey(shEntity) != null) {
					shMapper.appendTodayAsUpdate(shEntity);
				} else {
					ScheduleHistoryEntity retNew = shMapper.getOtherInfo(material_id);
					shEntity.setIn_schedule_means(3);
					shEntity.setRemove_flg(0);
					shEntity.setScheduled_expedited(retNew.getScheduled_expedited());
					shEntity.setArrival_plan_date(retNew.getArrival_plan_date());
					shMapper.append(shEntity);
				}
			}
		}
	}

	public void updateSchedulePeriod(HttpServletRequest req, SqlSessionManager conn, List<MsgInfo> errors)
			throws Exception {
		String material_id = req.getParameter("material_id");
		ScheduleHistoryMapper shMapper = conn.getMapper(ScheduleHistoryMapper.class);

		ScheduleHistoryEntity entity = new ScheduleHistoryEntity();
		Converter<Date> dc = DateConverter.getInstance(DateUtil.DATE_PATTERN);
		Converter<Integer> ic = IntegerConverter.getInstance();

		entity.setMaterial_id(material_id);
		entity.setScheduled_date(dc.getAsObject(req.getParameter("scheduled_date")));
		entity.setPlan_day_period(ic.getAsObject(req.getParameter("plan_day_period")));
		shMapper.updatePeriod(entity);

	}

	public void deleteSchedule(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors)
			throws Exception {
		ScheduleMapper dao = conn.getMapper(ScheduleMapper.class);

		ScheduleForm sForm = (ScheduleForm)form;
		String ids = sForm.getIds();
		String lineIds = sForm.getLineIds();
		String scheduled_assign_date = sForm.getScheduled_assign_date();
		
		String[] material_ids = ids.split(",");
		String[] line_ids = lineIds.split(",");

		ScheduleEntity model = new ScheduleEntity();

		// 历史记录修改
		boolean todayfix = scheduled_assign_date.equals(DateUtil.toString(new Date(), DateUtil.DATE_PATTERN));
		ScheduleHistoryMapper shMapper = conn.getMapper(ScheduleHistoryMapper.class);
		ScheduleHistoryEntity shEntity = new ScheduleHistoryEntity();
		shEntity.setScheduled_date(DateUtil.toDate(scheduled_assign_date, DateUtil.DATE_PATTERN));

		for (int i=0; i < material_ids.length; i++) {
			model.setLine_id(line_ids[i]);
			model.setMaterial_id(material_ids[i]);
			dao.deleteSchedule(model);
			// 历史记录修改
			if (todayfix) {
				shEntity.setMaterial_id(material_ids[i]);
				shMapper.removeToday(shEntity);
			}
		}
	}
	
	public void updateToPuse(String material_id, String move_reason, String position_id, SqlSessionManager conn) throws Exception {
		ForSolutionAreaService fsaService = new ForSolutionAreaService();
		fsaService.create(material_id, move_reason, 8, position_id, conn, true);

		// ScheduleMapper dao = conn.getMapper(ScheduleMapper.class);
		// dao.updateToPuse(id);
	}

	public AlarmMesssageForm getWarning(String material_id, SqlSession conn) {
		// 取得对应工位的中断信息
		AlarmMesssageMapper dao = conn.getMapper(AlarmMesssageMapper.class);
		AlarmMesssageEntity entity = dao.getBreakPushedAlarmMessage(material_id);
		AlarmMesssageForm form = new AlarmMesssageForm();
		CopyOptions co = new CopyOptions();

		co.dateConverter("MM-dd HH:mm", "occur_time");
		co.include("alarm_messsage_id", "occur_time", "sorc_no", "model_name", "serial_no", "line_name", "process_code", "operator_name", "position_id");
		BeanUtil.copyToForm(entity, form, co);
		// 取得原因
		PauseFeatureEntity pauseEntity = dao.getBreakOperatorMessageByID(entity.getAlarm_messsage_id());
		if (pauseEntity == null) {
			pauseEntity = dao.getBreakOperatorMessage(entity.getOperator_id(), material_id, entity.getPosition_id());
		}

		// 取得暂停信息里的记录
		Integer iReason = pauseEntity.getReason();
		// 不良理由
		String sReason = null;
		if (iReason != null && iReason < 10) {
			sReason = CodeListUtils.getValue("break_reason", "0" + iReason);
		} else {
			sReason = PathConsts.POSITION_SETTINGS.getProperty("break."+ pauseEntity.getProcess_code() +"." + iReason);
		}
		form.setReason(sReason);

		// 备注信息
		String sComments = entity.getOperator_name()+ ":" + pauseEntity.getComments();

		List<AlarmMesssageSendationEntity> listSendation = dao.getBreakAlarmMessageSendation(entity.getAlarm_messsage_id());
		for (AlarmMesssageSendationEntity sendation : listSendation) {
			if (!CommonStringUtil.isEmpty(sendation.getComment())) {
				sComments += "\n" + sendation.getSendation_name() + ":" + sendation.getComment();
			}
		}
		form.setComment(sComments);

		return form;
	}

	public void hold(HttpServletRequest req, LoginData user, SqlSessionManager conn) throws Exception {
		String alarm_messsage_id = req.getParameter("alarm_messsage_id");

		AlarmMesssageMapper dao = conn.getMapper(AlarmMesssageMapper.class);
		AlarmMesssageEntity alarm_messsage = new AlarmMesssageEntity();
		alarm_messsage.setAlarm_messsage_id(alarm_messsage_id);
		alarm_messsage.setLevel(3);
		// 警报等级升级
		dao.updateLevel(alarm_messsage);

		AlarmMesssageSendationEntity sendation = new AlarmMesssageSendationEntity();
		sendation.setAlarm_messsage_id(alarm_messsage_id);
		sendation.setSendation_id(user.getOperator_id());
		sendation.setComment(req.getParameter("comment"));
		sendation.setRed_flg(0);
		sendation.setResolve_time(new Date());

		// 留下本人备注
		AlarmMesssageService amService = new AlarmMesssageService();
		amService.replaceAlarmMessageSendation(sendation, conn);

		// 取得计划人员
		OperatorMapper oDao = conn.getMapper(OperatorMapper.class);
		OperatorEntity oCondition = new OperatorEntity();
		oCondition.setRole_id(RvsConsts.ROLE_SCHEDULER);

		// 发送警报到计划人员
		List<OperatorNamedEntity> managers = oDao.searchOperator(oCondition);
		for (OperatorNamedEntity manager : managers) {
			sendation = new AlarmMesssageSendationEntity();
			sendation.setAlarm_messsage_id(alarm_messsage_id);
			sendation.setSendation_id(manager.getOperator_id());
			int me = dao.countAlarmMessageSendation(sendation);

			if (me == 0) {
				// 如果不存在则Insert
				dao.createAlarmMessageSendation(sendation);
			}
		}
	}

	/**
	 * 取得机种别仕挂量
	 * @param conn
	 * @return
	 */
	public String getSikakeTable(SqlSession conn) {
		String retHtml = "";
		int tdCount = 1;
		ScheduleMapper dao = conn.getMapper(ScheduleMapper.class);
		List<Map<String, Object>> workingOfCategories = dao.getWorkingOfCategories();
		BigDecimal heap_total = new BigDecimal(0);

		for (Map<String, Object> workingOfCategory : workingOfCategories) {
			retHtml += "<td class=\"ui-state-default td-title\">" + workingOfCategory.get("name") + "</td>";
			BigDecimal heap = new BigDecimal((Long) workingOfCategory.get("heap"));
			retHtml += "<td class=\"td-content\">"+ heap.divide(new BigDecimal(RvsConsts.TIME_LIMIT), 0, BigDecimal.ROUND_CEILING) 
					+ "台/日 "+ heap + "台</td>";
			heap_total = heap_total.add(heap);
			tdCount++;
			if (tdCount % 3 == 0) retHtml += "</tr><tr>";
		}

		retHtml = "<tr><td class=\"ui-state-default td-title\">总计</td><td class=\"td-content\">" 
				+ heap_total.divide(new BigDecimal(RvsConsts.TIME_LIMIT), 0, BigDecimal.ROUND_CEILING) + "台/日 "+ heap_total 
				+ "台</td>" + retHtml + "</tr>";
		return retHtml.replaceAll("<tr></tr>", "");
	}

	public List<ScheduleForm> getReportScheduleList(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		ScheduleMapper dao = conn.getMapper(ScheduleMapper.class);
		ScheduleEntity conditionBean = new ScheduleEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		
		List<ScheduleEntity> entities = dao.searchReportScheduleByCondition(conditionBean);
		List<ScheduleForm> retForms = new ArrayList<ScheduleForm>();
		BeanUtil.copyToFormList(entities, retForms, null, ScheduleForm.class);
		return retForms;
	}

	/**
	 * 获得本月KPI数据
	 * @param callbackResponse
	 * @param conn
	 */
	public void getDayKpiOfWeek(Map<String, Object> callbackResponse,
			SqlSession conn) {
		DailyKpiMapper dkMapper = conn.getMapper(DailyKpiMapper.class);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(cal.getTimeInMillis());

		// 取得当天位置
		int dow =cal.get(Calendar.DAY_OF_WEEK);
		if (dow == Calendar.SUNDAY) {
			callbackResponse.put("now_column", 7);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			cal.add(Calendar.WEEK_OF_MONTH, -1);
		} else {
			callbackResponse.put("now_column", dow);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		}

		// 取得本周初始日期
		String weekstart = DateUtil.toString(cal.getTime(), DateUtil.DATE_PATTERN);
		callbackResponse.put("weekstart", weekstart);

		// 取得时期,生产计划
		String period = RvsUtils.getFYBussinessHalfYearString(cal);
		callbackResponse.put("period", period);

		String pr = RvsUtils.getBussinessHalfStartDate(today);

		// 取得时期,生产计划
		getPeriodPlans(pr, callbackResponse, conn);

		List<DailyKpiDataEntity> dkdList = new ArrayList<DailyKpiDataEntity>();
		for (int i=0;i<7;i++) {
			DailyKpiDataEntity dayData = dkMapper.getByDate(cal.getTime());
			if (dayData == null) {
				dayData = new DailyKpiDataEntity();
			}
			Date count_date = cal.getTime();
			if (i != 6) {
				if (count_date.after(today.getTime())) break;
//				if (count_date.equals(today.getTime())) { // 今日动态数据
//					Map<String, Object> condition = new HashMap<String, Object>();
//					dayData.setDirect_quotation_lt_rate(dkMapper.getDirectQuotationLtRate(count_date));
//					dayData.setFinal_inspect_pass_rate(dkMapper.getFinalInspectPassRate(count_date));
//					dayData.setIntime_complete_rate(dkMapper.getIntimeCompleteRate(count_date));
//					dayData.setTotal_plan_processed_rate(dkMapper.getPlanProcessedRate(condition));
//					condition.put("section_id", "00000000001");
//					dayData.setSection1_plan_processed_rate(dkMapper.getPlanProcessedRate(condition));
//					condition.put("section_id", "00000000003");
//					dayData.setSection2_plan_processed_rate(dkMapper.getPlanProcessedRate(condition));
//					dayData.setQuotation_lt_rate(dkMapper.getQuotationLtRate(count_date));
//				}

				dkdList.add(dayData);
			} // TODO else
//			dayData.setHalf_period_complete(dkMapper.getOutCount(periodStart, count_date));
//			dayData.setMonth_complete(dkMapper.getOutCount(monthStart, count_date));
			if (i==0) {
				callbackResponse.put("comment", dayData.getComment());
			}
			cal.add(Calendar.DATE, 1);
		}

		callbackResponse.put("dkdList", dkdList);
	}

	/**
	 * 取得半期计划产出数
	 * @param pr
	 * @param callbackResponse
	 * @param conn
	 */
	private void getPeriodPlans(String pr, 
			Map<String, Object> callbackResponse, SqlSession conn) {

		RepairPlanMapper rpMapper = conn.getMapper(RepairPlanMapper.class);

		Date dHpStart = DateUtil.toDate(pr, DateUtil.DATE_PATTERN);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dHpStart);

		int shippingPlanOfHp = 0;

		for (int i = 0; i < 6; i++, cal.add(Calendar.MONTH, 1)) {
			String planYear = "" + cal.get(Calendar.YEAR);
			String planMonth = "" + (cal.get(Calendar.MONTH) + 1);

			// callbackResponse.put("planMonth", planMonth);

			Integer shippingPlanOfMonths = rpMapper.getShippingPlan(planYear, planMonth);

			if (shippingPlanOfMonths != null) {
				shippingPlanOfHp += shippingPlanOfMonths;
			}
		}

		callbackResponse.put("shippingPlanOfHp", shippingPlanOfHp);
	}

	public String getScheduleSections(List<SectionEntity> sectionInline) {
		StringBuffer sb = new StringBuffer();
		for (SectionEntity section : sectionInline) {
			sb.append("<input type=\"radio\" name=\"schedule_section\" class=\"ui-button ui-corner-up\" id=\"section_")
					.append(section.getSection_id())
					.append("_button\" value=\"")
					.append(section.getSection_id())
					.append("\" role=\"button\"><label for=\"section_")
					.append(section.getSection_id()).append("_button\">")
					.append(section.getName()).append("</label>");
		}

		return sb.toString();
	}
}
