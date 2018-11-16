package com.osh.rvs.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;

import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.inline.PauseFeatureEntity;
import com.osh.rvs.bean.inline.SoloProductionFeatureEntity;
import com.osh.rvs.mapper.inline.PauseFeatureMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.message.ApplicationMessage;

public class PauseFeatureService {

	Logger logger = Logger.getLogger("PauseFeatureService");

	public void createPauseFeature(ProductionFeatureEntity productionFeature, String reason, String comments, String alarm_messsage_id,
			SqlSessionManager conn) throws Exception {
		createPauseFeature(productionFeature, reason, comments, alarm_messsage_id, conn, null);
	}
	public void createPauseFeature(ProductionFeatureEntity productionFeature, String reason, String comments, String alarm_messsage_id,
			SqlSessionManager conn, String snout_serial_no) throws Exception {
				PauseFeatureEntity entity = new PauseFeatureEntity();

		// 担当人 ID
		entity.setOperator_id(productionFeature.getOperator_id());
		// 暂停原因
		try {
			entity.setReason(Integer.parseInt(reason.trim()));
		} catch (NumberFormatException ne) {
			logger.error("暂停理由代码不是数字：" + ne.getMessage());
			entity.setReason(40); // 其他
		}
		// 维修对象ID
		entity.setMaterial_id(productionFeature.getMaterial_id());
		// 课室ID
		entity.setSection_id(productionFeature.getSection_id());
		// 工位ID
		entity.setPosition_id(productionFeature.getPosition_id());
		// 注释
		if (comments != null && comments.length() > 100) comments = comments.substring(0, 100);
		entity.setComments(comments);
		// 警报信息ID
		entity.setAlarm_messsage_id(alarm_messsage_id);
		// 序列号（先端头用）
		entity.setSnout_serial_no(snout_serial_no);

		PauseFeatureMapper dao = conn.getMapper(PauseFeatureMapper.class);

		dao.makePauseFeature(entity);
	}

	public void createPauseFeature(SoloProductionFeatureEntity productionFeature, String reason, String comments, String section_id,
			String alarm_messsage_id, SqlSessionManager conn) throws Exception {
		PauseFeatureEntity entity = new PauseFeatureEntity();

		// 担当人 ID
		entity.setOperator_id(productionFeature.getOperator_id());
		// 暂停原因
		try {
			entity.setReason(Integer.parseInt(reason.trim()));
		} catch (NumberFormatException ne) {
			logger.error("暂停理由代码不是数字：" + ne.getMessage());
			entity.setReason(40); // 其他
		}
		// 维修对象ID
		entity.setSnout_serial_no(productionFeature.getSerial_no());
		// 课室ID
		entity.setSection_id(section_id);
		// 工位ID
		entity.setPosition_id(productionFeature.getPosition_id());
		// 注释
		entity.setComments(comments);
		// 警报信息ID
		entity.setAlarm_messsage_id(alarm_messsage_id);

		PauseFeatureMapper dao = conn.getMapper(PauseFeatureMapper.class);

		dao.makePauseFeature(entity);
	}

	public void finishPauseFeature(String material_id, String section_id, String position_id, String finish_operator_id,
			SqlSessionManager conn) {
		finishPauseFeature(material_id, section_id, position_id, finish_operator_id, null, conn);
	}
	public void finishPauseFeature(String material_id, String section_id, String position_id, String finish_operator_id,
			String snout_serial_no, SqlSessionManager conn) {
		PauseFeatureMapper dao = conn.getMapper(PauseFeatureMapper.class);

		// 其他普通的暂停结束掉/个人用
		if (dao.checkOperatorPauseFeature(finish_operator_id) != null) {
			dao.stopOperatorPauseFeature(finish_operator_id);
		}

		if (material_id != null) {
			// 工序必要的暂停结束掉/维修对象用
			if (dao.checkPauseFeature(material_id, section_id, position_id) != null) {
				dao.stopPauseFeature(material_id, section_id, position_id, finish_operator_id);
			}
		}
		if (snout_serial_no != null) {
			// 工序必要的暂停结束掉/先端预制用
			dao.stopPauseFeatureSnout(snout_serial_no, section_id, position_id, finish_operator_id);
		}
	}

	public static String getPauseReasonSelectOptions() {
		Map<String, String> optGroups = new HashMap<String, String>();

		Map<String, String> pause_reasons = CodeListUtils.getList("pause_reason");

		for (String pause_code : pause_reasons.keySet()) {
			String pause_reason = pause_reasons.get(pause_code);
			String kind = pause_reason.substring(0, 1);
			if (!optGroups.containsKey(kind)) {
				optGroups.put(kind, "<optgroup label=\"" + declare(kind) + "\">");
			}
			optGroups.put(kind, optGroups.get(kind) + "<option value=\"" + pause_code + "\">" + pause_reason + "</option>");
		}

		String retString = "";
		for (String kind : optGroups.keySet()) {
			if ("其".equals(kind)) {
				retString = optGroups.get(kind) + "</optgroup>" + retString;
			} else {
				retString += optGroups.get(kind) + "</optgroup>";
			}
		}

		return retString;
	}

	/**
	 * 暂停信息分类
	 * @param kind
	 * @return
	 */
	private static String declare(String kind) {
		if ("其".equals(kind)) {
			return "(无分类)";
		} else if ("D".equals(kind)) {
			return "DT 直接作业";
		} else if ("M".equals(kind)) {
			return "M 管理时间";
		} else if ("H".equals(kind)) {
			return "H 休息离线";
		}
		return null;
	}

	public String checkPauseForm(String comments, List<MsgInfo> errors) {
		if (comments != null && comments.length() > 100) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("comments");
			error.setErrcode("validator.invalidParam.invalidMaxLengthValue");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage
					("validator.invalidParam.invalidMaxLengthValue", "备注", "100"));
			errors.add(error);
		}
		return comments;
	}
}
