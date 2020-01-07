package com.osh.rvs.service.master;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.master.ProcedureStepCountEntity;
import com.osh.rvs.common.SocketCommunitcator;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.mapper.master.ProcedureStepCountMapper;

public class ProcedureStepCountService {

	private static final Integer RELATION_TYPE_MODEL = 1;
	private static final String IP_ADDR = "10.220.126.181"; // 10.220.126.181 // 127.0.0.1

	public String startProcedureStepCount(MaterialForm mform,
			SqlSession conn) {

		SocketCommunitcator scUtil = new SocketCommunitcator();
		Map<String, String> map = new HashMap<String, String>();
		map.put("omr_notifi_no", mform.getSorc_no());
		map.put("model_name", mform.getModel_name());
		map.put("serial_no", mform.getSerial_no());

		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		ProcedureStepCountEntity entity = new ProcedureStepCountEntity();
		entity.setProcedure_step_count_id("00000000001"); // TODO
		entity.setRelation_type(RELATION_TYPE_MODEL); // TODO
		entity.setRelation_id(mform.getModel_id());
		List<ProcedureStepCountEntity> list = mapper.searchProcedureStepOfModel(entity);

		if (list.size() == 0) {
			return "NotSetException";
		}

		map.put("set_times", "" + list.get(0).getStep_times());
		return scUtil.clientSendMessage(IP_ADDR, 50023, "In:" + JSON.encode(map));
	}

	public String finishProcedureStepCount() {
		SocketCommunitcator scUtil = new SocketCommunitcator();
		
		return scUtil.clientSendMessage(IP_ADDR, 50023, "Out:");
	}
}
