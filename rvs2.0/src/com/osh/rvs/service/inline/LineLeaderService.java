package com.osh.rvs.service.inline;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.AlarmMesssageEntity;
import com.osh.rvs.bean.data.AlarmMesssageSendationEntity;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.inline.LineLeaderEntity;
import com.osh.rvs.bean.inline.MaterialFactEntity;
import com.osh.rvs.bean.inline.MaterialProcessAssignEntity;
import com.osh.rvs.bean.inline.PauseFeatureEntity;
import com.osh.rvs.bean.master.OperatorEntity;
import com.osh.rvs.bean.master.OperatorNamedEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.AlarmMesssageForm;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.form.inline.LineLeaderForm;
import com.osh.rvs.mapper.data.AlarmMesssageMapper;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.inline.DeposeStorageMapper;
import com.osh.rvs.mapper.inline.LineLeaderMapper;
import com.osh.rvs.mapper.inline.MaterialProcessAssignMapper;
import com.osh.rvs.mapper.inline.ProductionFeatureMapper;
import com.osh.rvs.mapper.master.OperatorMapper;
import com.osh.rvs.mapper.master.PositionMapper;
import com.osh.rvs.mapper.qf.MaterialFactMapper;
import com.osh.rvs.service.AlarmMesssageService;
import com.osh.rvs.service.CustomerService;
import com.osh.rvs.service.MaterialService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

public class LineLeaderService {
	Logger _log = Logger.getLogger(LineLeaderService.class);

	/**
	 * 取得当前课室+工程下处理中的全部维修对象信息
	 * @param section_id
	 * @param line_id
	 * @param today 
	 * @param conn
	 * @return
	 */
	public List<LineLeaderForm> getPerformanceList(String section_id, String line_id, String position_id, String today, SqlSession conn) {
		List<LineLeaderForm> ret = new ArrayList<LineLeaderForm>();

		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
		if ("".equals(position_id)) position_id = null;
		List<LineLeaderEntity> listEntities = dao.getWorkingMaterials(section_id, line_id, position_id, today);

		CopyOptions cos = new CopyOptions();
		cos.excludeEmptyString();
		cos.excludeNull();

		AlarmMesssageMapper amDao = conn.getMapper(AlarmMesssageMapper.class);

		for (LineLeaderEntity entity : listEntities) {
			// CCD玻璃已更换完成品，不要显示在NS仕挂里
			if ("302".equals(entity.getProcess_code())
					&& entity.getOperate_result() == 2) {
				continue;
			}
			// LG 玻璃已更换完成品，不要显示在NS仕挂里
			if ("303".equals(entity.getProcess_code())
					&& entity.getOperate_result() == 2) {
				continue;
			}
			LineLeaderForm retForm = new LineLeaderForm();
			BeanUtil.copyToForm(entity, retForm, cos);
			if (entity.getOperate_result() == 3) {
				String amLevel = amDao.getBreakLevelByMaterialId(entity.getMaterial_id(), entity.getPosition_id());
				retForm.setSymbol(CodeListUtils.getValue("alarm_symbol", amLevel));
			}
			ret.add(retForm);
		}

		return ret;
	}

	/**
	 * 工位仕挂一览For图表
	 * @param section_id
	 * @param line_id
	 * @param conn
	 * @param listResponse
	 * 
	 * @return 有无分线
	 */
	public void getChartContent(String section_id, String line_id, SqlSession conn, Map<String, Object> listResponse) {
		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
		List<Map<String, String>> workingOfPositions = dao.getWorkingOfPositions(section_id, line_id);
		boolean division_flg = false;

		// 数据整合
		List<Map<String, String>> newWorkingOfPositions = new ArrayList<Map<String, String>>();
		for (int i=0; i < workingOfPositions.size(); i+=2){
			Map<String, String> workingOfPositionH = workingOfPositions.get(i);
			Map<String, String> workingOfPositionL = workingOfPositions.get(i+1);
			String light_division_flg = workingOfPositionH.get("LIGHT_DIVISION_FLG");
			String process_code = workingOfPositionH.get("PROCESS_CODE");

			boolean depar = false;
			if ("1".equals(light_division_flg)) { // 分线
				depar = true;
//				if ("0".equals(workingOfPositionL.get("material_count"))
//						&& "0".equals(workingOfPositionL.get("light_fix_count"))) { // B线无仕挂
//					depar = false;
//				}
			}

			if (depar) { // 分线
				division_flg = true;
				workingOfPositionH.put("PROCESS_CODE", process_code + "A");
				workingOfPositionL.put("PROCESS_CODE", process_code + "B");
				newWorkingOfPositions.add(workingOfPositionH);
				newWorkingOfPositions.add(workingOfPositionL);
			} else { // 不分
				Float fCount = 0f;
				Float light_fix_count = 0f;
				try {
					fCount = Float.parseFloat(workingOfPositionH.get("material_count"))
							+ Float.parseFloat(workingOfPositionL.get("material_count"));
					light_fix_count = Float.parseFloat(workingOfPositionH.get("light_fix_count"))
							+ Float.parseFloat(workingOfPositionL.get("light_fix_count"));
				} catch (NumberFormatException e) {
				}
				workingOfPositionH.put("material_count", String.valueOf(fCount));
				workingOfPositionH.put("light_fix_count", String.valueOf(light_fix_count));
				newWorkingOfPositions.add(workingOfPositionH);
			}
		}

		List<String> positions = new ArrayList<String>();
		List<Integer> overlines = new ArrayList<Integer>();
		List<Object> counts = new ArrayList<Object>();
		List<Object> light_fix_counts = new ArrayList<Object>();

		for (Map<String, String> workingOfPosition : newWorkingOfPositions){
			String process_code = workingOfPosition.get("PROCESS_CODE");
			if (process_code.endsWith("A") || process_code.endsWith("B")) {
				process_code = process_code.substring(0, process_code.length() - 1);
			}
//			if ("400".equals(process_code)) {
//				positions.add("<a href=\"javaScript:positionFilter('"+workingOfPosition.get("POSITION_ID")+"')\">" + workingOfPosition.get("PROCESS_CODE") + " " + workingOfPosition.get("NAME") + "\n(x 10)" + "</a>");
//			} else {
				positions.add("<a href=\"javaScript:positionFilter('"+workingOfPosition.get("POSITION_ID")+"')\">" + workingOfPosition.get("PROCESS_CODE") + "</a>\r\n<br><a href=\"javaScript:positionFilter('"+workingOfPosition.get("POSITION_ID")+"')\">" + workingOfPosition.get("NAME") + "</a>");
//			}

			String sWaitingflow = RvsUtils.getWaitingflow(section_id, null, process_code);

			Integer iWaitingflow = null;
			if (sWaitingflow != null) {
				try {
					iWaitingflow = Integer.parseInt(sWaitingflow);
//					if (workingOfPosition.get("PROCESS_CODE").endsWith("B")) {
//						iWaitingflow = 6;
//					}
				} catch (NumberFormatException e) {
				}
			}
			overlines.add(iWaitingflow);
			overlines.add(iWaitingflow);
			overlines.add(iWaitingflow);
			overlines.add(null);

			// 大修理数据
			Float fCount = 0f;
			try {
				fCount = Float.parseFloat(workingOfPosition.get("material_count"));
//				if ("400".equals(process_code)) {
//					fCount /= 10;
//				}
			} catch (NumberFormatException e) {
			}

			// 小修理数据
			Float light_fix_count = 0f;
			try {
				light_fix_count = Float.parseFloat(workingOfPosition.get("light_fix_count"));
//				if ("400".equals(process_code)) {
//					light_fix_count /= 10;
//				}
			} catch (NumberFormatException e) {
			}
			// 合计
			Float total = fCount + light_fix_count;

			Map<String, Object> series = new HashMap<String, Object>();
			Map<String, Object> color = new HashMap<String, Object>();
			Map<String, Object> linearGradient = new HashMap<String, Object>();
			linearGradient.put("x1", "0");
			linearGradient.put("x2", "0");
			linearGradient.put("y1", "1");
			linearGradient.put("y2", "0");
			color.put("linearGradient", linearGradient);
			List<ArrayList<Object>> stops = new ArrayList<ArrayList<Object>>();
			ArrayList<Object> stop = new ArrayList<Object>();
			if (iWaitingflow != null && iWaitingflow != 0 && fCount > iWaitingflow && light_fix_count == 0){
				// 超过等待去上限的{color : '#f04e08', y : 47}
				stop = new ArrayList<Object>();
				stop.add("0.7");
				stop.add("#92D050");
				stops.add(stop);
				stop = new ArrayList<Object>();
				stop.add("0.8");
				stop.add("#FFC000");
				stops.add(stop);
				color.put("stops", stops);
				series.put("color", color);
				series.put("y", fCount);
				counts.add(series);
			} else {
				counts.add(fCount);
			}

			if (iWaitingflow != null && iWaitingflow != 0
					&& (light_fix_count > iWaitingflow || (total > iWaitingflow && light_fix_count > 0))) {
				stop = new ArrayList<Object>();
				stop.add("0.7");
				stop.add("#cc76cc");
				stops.add(stop);
				stop = new ArrayList<Object>();
				stop.add("0.8");
				stop.add("#FFC000");
				stops.add(stop);
				color.put("stops", stops);
				series.put("color", color);
				series.put("y", light_fix_count);
				light_fix_counts.add(series);
			} else {
				if (light_fix_count == 0) {
					light_fix_counts.add(null);
				} else {
					light_fix_counts.add(light_fix_count);
				}
			}
		}

		if (overlines.size() > 1)
			overlines.remove(overlines.size() - 1);

		// 分解工程取得分解库位信息
		if ("00000000012".equals(line_id)) {
			DeposeStorageMapper sdMapper = conn.getMapper(DeposeStorageMapper.class);
			int decomStorageCount = sdMapper.getDecomStorageCount();

			positions.add(1, "内镜分解库位");

			counts.add(1, decomStorageCount);

			overlines.add(4, null);
			overlines.add(5, null);
			overlines.add(6, null);
			overlines.add(7, null);
		}

		// 周边维修工程取得周边库位信息
		if ("00000000070".equals(line_id)) {
			MaterialFactMapper mfMapper = conn.getMapper(MaterialFactMapper.class);
			MaterialFactEntity entity = new MaterialFactEntity();
			entity.setLevel(5);
			List<MaterialFactEntity> mflist = mfMapper.searchMaterial(entity);
			int cntWip = 0; int cntStock = 0;
			for (MaterialFactEntity materialFact : mflist) {
				if (CommonStringUtil.isEmpty(materialFact.getWip_location())) {
					cntStock++;
				} else {
					cntWip++;
				}
			}

			positions.add(1, "等待投线在库");
			positions.add(2, "等待投线离库");

			counts.add(1, cntWip);
			counts.add(2, cntStock);

			overlines.add(4, null);
			overlines.add(5, null);
			overlines.add(6, null);
			overlines.add(7, null);
			overlines.add(4, null);
			overlines.add(5, null);
			overlines.add(6, null);
			overlines.add(7, null);
		}

		listResponse.put("categories", positions);
		listResponse.put("overlines", overlines);
		listResponse.put("counts", counts);
		listResponse.put("light_fix_counts", light_fix_counts);

		// 有分线
		if(division_flg) {
			listResponse.put("division_flg" , "1");
		}

		return;
	}

	/**
	 * 切换维修对象在本线加急
	 * @param material_id
	 * @param line_id
	 * @param conn
	 * @throws Exception 
	 */
	public void switchLeaderExpedite(String material_id, String line_id, SqlSessionManager conn) throws Exception {
		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
		if ("00000000011".equals(line_id))
			dao.switchQuotationFirst(material_id);
		else
			dao.switchLeaderExpedite(material_id, line_id);
	}

	/**
	 * 取得中断信息
	 * @param material_id
	 * @param operator_id 本人
	 * @param position_id
	 * @param conn
	 * @return
	 */
	public AlarmMesssageForm getWarning(String material_id, String operator_id, String position_id, SqlSession conn) {
		// 取得对应工位的中断信息
		AlarmMesssageMapper dao = conn.getMapper(AlarmMesssageMapper.class);
		AlarmMesssageService amService = new AlarmMesssageService();

		AlarmMesssageEntity entity = amService.getBreakAlarmMessage(material_id, position_id, conn);

		AlarmMesssageForm form = new AlarmMesssageForm();
		CopyOptions co = new CopyOptions();

		co.dateConverter("MM-dd HH:mm", "occur_time");
		co.include("alarm_messsage_id", "occur_time", "sorc_no", "model_name", "serial_no", "line_name", "process_code", "operator_name", "position_name");
		BeanUtil.copyToForm(entity, form, co);

		// 取得原因
		PauseFeatureEntity pauseEntity = dao.getBreakOperatorMessageByID(entity.getAlarm_messsage_id());
		if (pauseEntity == null) {
			pauseEntity = dao.getBreakOperatorMessage(entity.getOperator_id(), material_id, position_id);
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
		String sMyComments = "";

		List<AlarmMesssageSendationEntity> listSendation = dao.getBreakAlarmMessageSendation(entity.getAlarm_messsage_id());
		for (AlarmMesssageSendationEntity sendation : listSendation) {
			if (!CommonStringUtil.isEmpty(sendation.getComment())) {
				if (operator_id.equals(sendation.getSendation_id())) {
					sMyComments = sendation.getComment();
				} else {
					sComments += "\n" + sendation.getSendation_name() + ":" + sendation.getComment();
				}
			}
		}
		form.setComment(sComments);
		form.setMyComment(sMyComments);

		return form;
	}

	/**
	 * 线长完成零件相关工位
	 */
	public void partialResolve(String material_id, String model_name, String position_id, SqlSessionManager conn, LoginData user) throws Exception {
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);

		// 取得标准工作时间
		int use_seconds = 0;
		PositionMapper pDao = conn.getMapper(PositionMapper.class);
		PositionEntity position = pDao.getPositionByID(position_id);

		String sUse_seconds = RvsUtils.getZeroOverLine(model_name, null, user, position.getProcess_code());
		try {
			use_seconds = (int) (Double.parseDouble(sUse_seconds) * 60);
		} catch (Exception e) {
		}

		// 设定条件
		ProductionFeatureEntity entity = new ProductionFeatureEntity();
		entity.setOperator_id(user.getOperator_id());
		entity.setOperate_result(RvsConsts.OPERATE_RESULT_FINISH);
		entity.setUse_seconds(use_seconds);
		entity.setMaterial_id(material_id);
		entity.setPosition_id(position_id);
		entity.setSection_id(user.getSection_id());
		entity.setPcs_inputs("{\"EN"+position.getProcess_code()+"01\":\"1\"}");

		// 更新为完成
		dao.finishPatchProductionFeature(entity);

	}

	/**
	 * 取得受理报价工程处理中的全部维修对象信息
	 * @param section_id
	 * @param line_id
	 * @param conn
	 * @return
	 */
	public List<MaterialForm> getBeforePerformanceList(MaterialForm form, SqlSession conn) {
		List<MaterialForm> ret = new ArrayList<MaterialForm>();
		MaterialEntity bean = new MaterialEntity();
		BeanUtil.copyToBean(form, bean, CopyOptions.COPYOPTIONS_NOEMPTY);

		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
		List<MaterialEntity> listEntities = dao.getBeforePerformanceList(bean);

		AlarmMesssageMapper amDao = conn.getMapper(AlarmMesssageMapper.class);

		for (MaterialEntity entity : listEntities) {
			MaterialForm retForm = new MaterialForm();
			BeanUtil.copyToForm(entity, retForm, CopyOptions.COPYOPTIONS_NOEMPTY);
			if (entity.getOperate_result() == 3) {
				String amLevel = amDao.getBreakLevelByMaterialId(entity.getMaterial_id(), entity.getCategory_id());
				retForm.setStatus(CodeListUtils.getValue("alarm_symbol", amLevel));
			}
			ret.add(retForm);
		}

		return ret;
	}

	/**
	 * 更新维修对象信息（报价线长）
	 * @param form
	 * @param session
	 * @param conn
	 * @param errors
	 * @throws Exception 
	 */
	public void update(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
		MaterialEntity entity = new MaterialEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		if (entity.getCustomer_name() != null) {
			CustomerService cservice = new CustomerService();
			entity.setCustomer_id(cservice.getCustomerStudiedId(entity.getCustomer_name(), entity.getOcm(), conn));
		}

		Date[] workDates = RvsUtils.getTimeLimit(entity.getAgreed_date(), entity.getLevel(), null, conn, false);
		Date workDate = workDates[0];
		entity.setScheduled_date(workDate);

		dao.updateMaterial(entity);
		
		MaterialForm materialForm = (MaterialForm)form;
		
		String level = materialForm.getLevel();//等级
		String fix_type = materialForm.getFix_type();//修理方式

		boolean isLightFix = RvsUtils.isLightFix(level) && "1".equals(fix_type);
		if(!isLightFix){
			MaterialProcessAssignEntity materialProcessAssignEntity = new MaterialProcessAssignEntity();
			BeanUtil.copyToBean(form, materialProcessAssignEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
			MaterialProcessAssignMapper materialProcessAssignMapper = conn.getMapper(MaterialProcessAssignMapper.class);

			String lightFixes = materialProcessAssignMapper.getLightFixesByMaterial(entity.getMaterial_id(), null);
			if (!isEmpty(lightFixes)) {
				//删除维修对象选用小修理
				materialProcessAssignMapper.deleteMaterialLightFix(materialProcessAssignEntity.getMaterial_id());
				//删除维修对象独有修理流程
				materialProcessAssignMapper.deleteMaterialProcessAssign(materialProcessAssignEntity.getMaterial_id());

				MaterialService mService = new MaterialService();
				// 删除小修理流程说明
				mService.removeComment(entity.getMaterial_id(), "00000000001", conn);
			}
		}
		
//		// FSE 数据同步
//		try{
//			FseBridgeUtil.toUpdateMaterial(entity.getMaterial_id(), "ll_update");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * 上报中断信息
	 * @param req
	 * @param user
	 * @param conn
	 * @throws Exception
	 */
	public void hold(HttpServletRequest req, LoginData user, SqlSessionManager conn) throws Exception {
		String section_id = user.getSection_id();

		String alarm_messsage_id = req.getParameter("alarm_messsage_id");

		AlarmMesssageMapper dao = conn.getMapper(AlarmMesssageMapper.class);
		AlarmMesssageEntity alarm_messsage = new AlarmMesssageEntity();
		alarm_messsage.setAlarm_messsage_id(alarm_messsage_id);
		alarm_messsage.setLevel(2);
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

		// 取得本课室经理人员
		OperatorMapper oDao = conn.getMapper(OperatorMapper.class);
		OperatorEntity oCondition = new OperatorEntity();
		oCondition.setSection_id(section_id);
		oCondition.setRole_id(RvsConsts.ROLE_MANAGER);

		// 发送警报到经理
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

	public String getOrderPos(String line_id, SqlSession conn) {
		PositionMapper pMapper = conn.getMapper(PositionMapper.class);
		PositionEntity position = new PositionEntity();
		position.setLine_id(line_id);
		position.setSpecial_page("part_order");
		List<PositionEntity> l = pMapper.searchPosition(position);
		if (l == null || l.size() == 0) return null;
		return l.get(0).getProcess_code();
	}
}
