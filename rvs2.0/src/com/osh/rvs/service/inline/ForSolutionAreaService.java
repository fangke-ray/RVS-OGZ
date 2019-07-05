package com.osh.rvs.service.inline;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.AlarmMesssageEntity;
import com.osh.rvs.bean.inline.ForSolutionAreaEntity;
import com.osh.rvs.bean.inline.PauseFeatureEntity;
import com.osh.rvs.bean.master.OperatorEntity;
import com.osh.rvs.bean.partial.MaterialPartialEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.inline.ForSolutionAreaForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.data.AlarmMesssageMapper;
import com.osh.rvs.mapper.inline.ForSolutionAreaMapper;
import com.osh.rvs.mapper.master.HolidayMapper;
import com.osh.rvs.mapper.master.OperatorMapper;
import com.osh.rvs.mapper.partial.MaterialPartialMapper;
import com.osh.rvs.service.AlarmMesssageService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.Converter;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateConverter;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.copy.IntegerConverter;

public class ForSolutionAreaService {

	public static final Integer ALRAM_POST = 1;
	public static final Integer ALRAM_RELEASE = 2;

	private Logger _log = Logger.getLogger(getClass());

	public List<ForSolutionAreaForm> getAreaList(ActionForm form, HttpServletRequest req, SqlSession conn,
			List<MsgInfo> errors, Integer resolveLevel) {
		List<ForSolutionAreaForm> ret = new ArrayList<ForSolutionAreaForm>();
		ForSolutionAreaForm mForm = (ForSolutionAreaForm) form;
		ForSolutionAreaEntity conditionEntity = new ForSolutionAreaEntity();
		BeanUtil.copyToBean(mForm, conditionEntity, CopyOptions.COPYOPTIONS_NOEMPTY);

		// TODO
		Converter<Integer> ic = IntegerConverter.getInstance();
		Converter<Date> dc = DateConverter.getInstance(DateUtil.DATE_PATTERN);
		String happen_time = req.getParameter("happen_time");
		String resolved = req.getParameter("resolved");  
		conditionEntity.setHappen_time(dc.getAsObject(happen_time));
		conditionEntity.setResolved(ic.getAsObject(resolved));

		ForSolutionAreaMapper mapper = conn.getMapper(ForSolutionAreaMapper.class);

		List<ForSolutionAreaEntity> retBeans = mapper.search(conditionEntity);

		HolidayMapper hdao = conn.getMapper(HolidayMapper.class);
		Map<String, String> delayMap = new HashMap<String, String>();

		for(ForSolutionAreaEntity retBean : retBeans) {

			ForSolutionAreaForm retForm = new ForSolutionAreaForm();
			BeanUtil.copyToForm(retBean, retForm, CopyOptions.COPYOPTIONS_NOEMPTY);

			// 过期颜色
			if (retForm.getScheduled_date() != null) {
				if (delayMap.containsKey(retForm.getScheduled_date() + retBean.getAm_pm())) {
					retForm.setRemain_days(delayMap.get(retForm.getScheduled_date() + retBean.getAm_pm()));
				} else {
					Map<String, Object> condiMap = new HashMap<String, Object>();
					condiMap.put("scheduled_date", retBean.getScheduled_date());
					condiMap.put("am_pm", retBean.getAm_pm());
					String remain_days = hdao.compareExperial(condiMap);
					delayMap.put(retForm.getScheduled_date() + retBean.getAm_pm(), remain_days);
					retForm.setRemain_days(remain_days);
				}
			}

			if (conditionEntity.getExpedition_diff() != null) {
				if(retForm.getScheduled_date()==null){
					continue;
				}
				if (conditionEntity.getExpedition_diff() == 1) {
					if (Integer.parseInt(retForm.getRemain_days()) >= 0)
						continue;
				} else if (conditionEntity.getExpedition_diff() == 0) {
					if (Integer.parseInt(retForm.getRemain_days()) < 0)
						continue;
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
					if (resolveLevel <= message.getLevel()) {
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
				if (levelup) retForm.setBreak_level("ME");
			} else {
				retForm.setBreak_message("");
			}

			ret.add(retForm);
		}

		return ret;
	}

	/**
	 * 解决中断
	 * @param form
	 * @param logindata
	 * @param conn
	 * @throws Exception
	 */
	public void solve(ActionForm form, LoginData logindata, SqlSessionManager conn) throws Exception {
		ForSolutionAreaMapper mapper = conn.getMapper(ForSolutionAreaMapper.class);
		
		ForSolutionAreaEntity entity = new ForSolutionAreaEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		entity.setResolver_id(logindata.getOperator_id());
	
		mapper.solve(entity);

		// 
	}

	public List<ForSolutionAreaEntity> checkBlock(String material_id, String position_id, String line_id, SqlSession conn) {
		ForSolutionAreaMapper mapper = conn.getMapper(ForSolutionAreaMapper.class);
		if ("00000000021".equals(position_id) || "00000000027".equals(position_id) ||
				"00000000079".equals(position_id) || "00000000094".equals(position_id) ||
				"00000000098".equals(position_id) || "00000000099".equals(position_id)) { // 零件签收

			return null; // TODO 客户要求移出
		}
		return mapper.checkOffline(material_id, position_id, line_id);
	}

	public ForSolutionAreaEntity checkBlockByAlarm(String alarm_messsage_id,
			SqlSessionManager conn) {
		AlarmMesssageMapper amMapper = conn.getMapper(AlarmMesssageMapper.class);
		AlarmMesssageEntity alarmMesssage = amMapper.getBreakAlarmMessageByKey(alarm_messsage_id);
		List<ForSolutionAreaEntity> rst = checkBlock(alarmMesssage.getMaterial_id(), alarmMesssage.getPosition_id(), null, conn);
		if (rst != null) {
			for (ForSolutionAreaEntity rsa : rst) {
				if (rsa.getReason() >= 2 && rsa.getReason() <= 4)
					return rsa;
			}
		}
		return null;
	}

	/**
	 * 建立中断，并通知相关人员
	 * @param material_id
	 * @param comment
	 * @param reason
	 * @param position_id
	 * @param conn
	 * @throws Exception
	 */
	public void create(String material_id, String comment, int reason, String position_id,
			SqlSessionManager conn, boolean addAlert) throws Exception {

		_log.info("material_id:" + material_id + " comment:" + comment);

		ForSolutionAreaMapper mapper = conn.getMapper(ForSolutionAreaMapper.class);
		ForSolutionAreaEntity entity = new ForSolutionAreaEntity();

		entity.setMaterial_id(material_id);
		entity.setReason(reason);
		entity.setPosition_id(position_id);
		entity.setComment(comment);
		mapper.create(entity);

		List<String> listOperator = new ArrayList<String>();

		listOperator.addAll(mapper.getLeadersByObject(material_id, position_id));

		listOperator.addAll(mapper.getInlineManagers());

		OperatorMapper oMapper = conn.getMapper(OperatorMapper.class);

		List<OperatorEntity> schedulers = oMapper.getOperatorWithRole(RvsConsts.ROLE_SCHEDULER);

		for (OperatorEntity scheduler : schedulers) {
			listOperator.add(scheduler.getOperator_id());
		}

		// 本人不推送
//		for (String operator : listOperator) {
//			if () {
//				break;
//			}
//		}

		if (addAlert) {

//			MaterialMapper mMapper = conn.getMapper(MaterialMapper.class);
//			MaterialEntity me = mMapper.getMaterialEntityByKey(material_id);
//
//			PositionMapper pMapper = conn.getMapper(PositionMapper.class);
//			PositionEntity pe = pMapper.getPositionByID(position_id);
//			
//			HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
//			httpclient.start();

//			try {
//				HttpGet request = new HttpGet(
//						"http://localhost:8080/rvsTouch/beep/" + me.getSection_id() + "/" + pe.getLine_id());
//				_log.info("finger:" + request.getURI());
//				httpclient.execute(request, null);
//			} catch (Exception e) {
//			} finally {
//				Thread.sleep(100);
//				httpclient.shutdown();
//			}
		}
	}

	public void updateToPushed(ForSolutionAreaEntity entity, SqlSessionManager conn) throws Exception {
		ForSolutionAreaMapper mapper = conn.getMapper(ForSolutionAreaMapper.class);
		mapper.updateToPushed(entity);

		String material_id = entity.getMaterial_id();
		String position_id = entity.getPosition_id();
		List<String> listOperator = new ArrayList<String>();

		listOperator.addAll(mapper.getLeadersByObject(material_id, position_id));

		listOperator.addAll(mapper.getInlineManagers());

		OperatorMapper oMapper = conn.getMapper(OperatorMapper.class);

		List<OperatorEntity> schedulers = oMapper.getOperatorWithRole(RvsConsts.ROLE_SCHEDULER);

		for (OperatorEntity scheduler : schedulers) {
			listOperator.add(scheduler.getOperator_id());
		}

		// 本人不推送
//		for (String operator : listOperator) {
//			if () {
//				break;
//			}
//		}

//		MaterialMapper mMapper = conn.getMapper(MaterialMapper.class);
//		MaterialEntity me = mMapper.getMaterialEntityByKey(material_id);
//
//		PositionMapper pMapper = conn.getMapper(PositionMapper.class);
//		PositionEntity pe = pMapper.getPositionByID(position_id);
//		
//		HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
//		httpclient.start();
//
//		try {
//			HttpGet request = new HttpGet(
//					"http://localhost:8080/rvsTouch/beep/" + me.getSection_id() + "/" + pe.getLine_id());
//			_log.info("finger:" + request.getURI());
//			httpclient.execute(request, null);
//		} catch (Exception e) {
//		} finally {
//			Thread.sleep(100);
//			httpclient.shutdown();
//		}
	}

	public void updateToAppend(ForSolutionAreaEntity entity, SqlSessionManager conn) throws Exception {
		ForSolutionAreaMapper mapper = conn.getMapper(ForSolutionAreaMapper.class);
		mapper.updateToAppend(entity);

//		MaterialMapper mMapper = conn.getMapper(MaterialMapper.class);
//		MaterialEntity me = mMapper.getMaterialEntityByKey(entity.getMaterial_id());
//
//		PositionMapper pMapper = conn.getMapper(PositionMapper.class);
//		PositionEntity pe = pMapper.getPositionByID(entity.getPosition_id());

		// 如果有预提->待发放状态的零件订单，则不追加订单，否则追加
		MaterialPartialMapper mpMapper = conn.getMapper(MaterialPartialMapper.class);
		List<MaterialPartialEntity> list = mpMapper.searchMaterialPartialById(entity.getMaterial_id());
		boolean hasOrder = false;
		int maxOccurTimes = 0;
		for (MaterialPartialEntity mp : list) {
			if (mp.getBo_flg() >= 7) {
				hasOrder = true;
			}
			if (mp.getOccur_times() > maxOccurTimes) maxOccurTimes = mp.getOccur_times();
		}
		if (!hasOrder) {
			MaterialPartialEntity mpEntity = new MaterialPartialEntity();
			mpEntity.setMaterial_id(entity.getMaterial_id());
			mpEntity.setOccur_times(maxOccurTimes + 1);
			mpEntity.setBo_flg(9);
			mpEntity.setOrder_date(new Date());
			mpMapper.insertMaterialPartial(mpEntity);
		}

//		HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
//		httpclient.start();
//
//		try {
//			HttpGet request = new HttpGet(
//					"http://localhost:8080/rvsTouch/beep/" + me.getSection_id() + "/" + pe.getLine_id());
//			_log.info("finger:" + request.getURI());
//			httpclient.execute(request, null);
//		} catch (Exception e) {
//		} finally {
//			Thread.sleep(100);
//			httpclient.shutdown();
//		}
	}

	public void solveBo(String material_id, Integer occur_times, String operater_id, SqlSessionManager conn) throws Exception {
		ForSolutionAreaMapper mapper = conn.getMapper(ForSolutionAreaMapper.class);

		List<ForSolutionAreaEntity> offlines = mapper.getOfflineOfMaterial(material_id);

		// 如果全发放则结束所有待处理
		for (ForSolutionAreaEntity offline : offlines) {

			Integer reason = offline.getReason();
			if (reason == 1 || (reason == 4 && occur_times > 1)) {
				offline.setResolver_id(operater_id);
				mapper.solve(offline);
			}
		}
	}

	public void solveBreak(ForSolutionAreaEntity entity, String operator_id,
			SqlSessionManager conn) throws Exception {
		ForSolutionAreaMapper mapper = conn.getMapper(ForSolutionAreaMapper.class);
		Date happen_time = entity.getHappen_time();
		if (happen_time != null && new Date().getTime() - happen_time.getTime() > 30 * 60 * 1000) {
			entity.setResolver_id(operator_id);
			mapper.solve(entity);
		} else {
			mapper.remove(entity);
		}
	}

	/** 
	 * 未修理返还了
	 * @param material_id
	 * @param user
	 * @param conn
	 * @throws Exception
	 */
	public void solveAsStop(String material_id, LoginData user,
			SqlSessionManager conn) throws Exception {
		ForSolutionAreaMapper mapper = conn.getMapper(ForSolutionAreaMapper.class);
		ForSolutionAreaEntity entity = new ForSolutionAreaEntity();
		entity.setMaterial_id(material_id);
		entity.setResolver_id(user.getOperator_id());
		mapper.solveAsStop(entity);

		// 发送解决
		List<String> listOperator = new ArrayList<String>();

		OperatorMapper oMapper = conn.getMapper(OperatorMapper.class);

		List<OperatorEntity> leaders = oMapper.getOperatorWithRole(RvsConsts.ROLE_LINELEADER);

		for (OperatorEntity leader : leaders) {
			listOperator.add(leader.getOperator_id());
		}

		listOperator.addAll(mapper.getInlineManagers());

		List<OperatorEntity> schedulers = oMapper.getOperatorWithRole(RvsConsts.ROLE_SCHEDULER);

		for (OperatorEntity scheduler : schedulers) {
			listOperator.add(scheduler.getOperator_id());
		}

		// 本人不推送
//		for (String operator : listOperator) {
//			if () {
//				break;
//			}
//		}
	}

	public String getInsertedKey(SqlSessionManager conn) {
		CommonMapper mapper = conn.getMapper(CommonMapper.class);
		return mapper.getLastInsertID();
	}

	public void doBoFsaEnter(String material_id, String comment, SqlSessionManager conn) {
		// 找到该维修对象 缺零件 中断追加零件
		ForSolutionAreaMapper mapper = conn.getMapper(ForSolutionAreaMapper.class);
		List<String> keys =  mapper.getUnsolvedForBoByMaterial(material_id);

		if (keys.size() == 0) {
			try {
				create(material_id, comment, 1, null, conn, false);
			} catch (Exception e) {
				_log.error(material_id + ": " + e.getMessage(), e);
			}
		}
	}

	public void doBoFsaLeave(String material_id, LoginData logindata, SqlSessionManager conn) {
		// 找到该维修对象 缺零件 中断追加零件
		ForSolutionAreaMapper mapper = conn.getMapper(ForSolutionAreaMapper.class);
		List<String> keys =  mapper.getUnsolvedForBoByMaterial(material_id);

		for (String key : keys) {
			ForSolutionAreaEntity entity = new ForSolutionAreaEntity();
			entity.setFor_solution_area_key(key);
			entity.setResolver_id(logindata.getOperator_id());
			
			try {
				mapper.solve(entity);
			} catch (Exception e) {
				_log.error(material_id + ": " + e.getMessage(), e);
			}
		}
	}
}
