package com.osh.rvs.service.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.master.ProcedureStepCountEntity;
import com.osh.rvs.common.SocketCommunitcator;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.mapper.master.ProcedureStepCountMapper;
import com.osh.rvs.service.AlarmMesssageService;
import com.osh.rvs.service.MaterialService;

import framework.huiqing.bean.message.MsgInfo;

public class ProcedureStepCountService {

	private static final Integer RELATION_TYPE_MODEL = 1;
	private static String IP_ADDR = "10.220.126.181"; // 10.220.126.181 // 127.0.0.1

	private Logger _log = Logger.getLogger(getClass());

	public void clientLogin(String client_ip, String process_code,
			String line_part, Map<String, Object> callbackResponse,
			SqlSessionManager conn) {
		IP_ADDR = client_ip;

		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		ProcedureStepCountEntity condi = new ProcedureStepCountEntity();
		condi.setProcess_code(process_code);
		int px = 0;
		if ("B".equals(line_part)) px = 1;
		condi.setPx(px);
		List<ProcedureStepCountEntity> list = mapper.searchProcedureStepCount(condi);

		if (list.size() == 0) {
			callbackResponse.put("error", "当前设定的岗位没有计数规则设置。");
		} else {
			List<ProcedureStepCountEntity> avaCounterList = new ArrayList<ProcedureStepCountEntity>();
			for (ProcedureStepCountEntity entity : list) {
				if (!client_ip.equals(entity.getClient_address())) {
					// TODO
				}

				ProcedureStepCountEntity avaCounter = new ProcedureStepCountEntity();
				avaCounter.setProcedure_step_count_id(entity.getProcedure_step_count_id());
				avaCounter.setName(entity.getName());
				avaCounterList.add(avaCounter);
			}
			callbackResponse.put("avaCounterList", avaCounterList);
		}
	}

	public String startProcedureStepCount(MaterialForm mform,
			String position_id, SqlSession conn) {

		SocketCommunitcator scUtil = new SocketCommunitcator();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("omr_notifi_no", mform.getSorc_no());
		map.put("model_name", mform.getModel_name());
		map.put("serial_no", mform.getSerial_no());

		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		ProcedureStepCountEntity entity = new ProcedureStepCountEntity();
//		entity.setProcedure_step_count_id("00000000001"); // TODO
		entity.setRelation_type(RELATION_TYPE_MODEL); // TODO
		entity.setRelation_id(mform.getModel_id());
		entity.setPosition_id(position_id);
		List<ProcedureStepCountEntity> list = mapper.searchProcedureStepOfModel(entity);

		if (list.size() == 0) {
			return "NotSetException";
		}

		Map<String, String> setTimesMap = new HashMap<String, String>();
		for (ProcedureStepCountEntity pscEntity : list) {
			setTimesMap.put(pscEntity.getProcedure_step_count_id(), "" + pscEntity.getStep_times());
			if (pscEntity.getProcedure_step_count_id().equals("00000000001")) {
				map.put("set_times", "" + list.get(0).getStep_times()); // old version
			}
		}
		
		map.put("set_times_map", setTimesMap);
		return scUtil.clientSendMessage(IP_ADDR, 50023, "In:" + JSON.encode(map));
	}

	public String finishProcedureStepCount() {
		SocketCommunitcator scUtil = new SocketCommunitcator();
		
		return scUtil.clientSendMessage(IP_ADDR, 50023, "Out:");
	}

	public void test1(Map<String, Object> listResponse) throws InterruptedException {

		SocketCommunitcator scUtil = new SocketCommunitcator();
		Map<String, String> map = new HashMap<String, String>();

		map.put("omr_notifi_no", "TEST1_单号");
		map.put("model_name", "TEST1_型号");
		map.put("serial_no", "TEST1_序列");
		map.put("set_times", "7");
		String recv = scUtil.clientSendMessage("10.220.126.181", 50023, "In:" + JSON.encode(map));

		Thread.sleep(10000);

		recv = scUtil.clientSendMessage("10.220.126.181", 50023, "Out:" + JSON.encode(map));
		_log.info(recv);
		listResponse.put("recv", recv);
	}

	public void test2(Map<String, Object> listResponse, SqlSession conn) throws InterruptedException {

		SocketCommunitcator scUtil = new SocketCommunitcator();
		Map<String, Object> map = new HashMap<String, Object>();

		MaterialService mService = new MaterialService();
		MaterialEntity mEntity = mService.loadSimpleMaterialDetailEntity(conn, "00000143411");

		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		ProcedureStepCountEntity entity = new ProcedureStepCountEntity();
//		entity.setProcedure_step_count_id("00000000001"); // TODO
		entity.setRelation_type(RELATION_TYPE_MODEL); // TODO
		entity.setRelation_id(mEntity.getModel_id());
		entity.setPosition_id("00000000036");
		List<ProcedureStepCountEntity> list = mapper.searchProcedureStepOfModel(entity);

		Map<String, String> setTimesMap = new HashMap<String, String>();
		Map<String, String> nameMap = new HashMap<String, String>();
		for (ProcedureStepCountEntity pscEntity : list) {
			setTimesMap.put(pscEntity.getProcedure_step_count_id(), "" + pscEntity.getStep_times());
			nameMap.put(pscEntity.getProcedure_step_count_id(), "" + pscEntity.getName());
			if (pscEntity.getProcedure_step_count_id().equals("00000000001")) {
				map.put("set_times", "" + list.get(0).getStep_times()); // old version
			}
		}

		map.put("omr_notifi_no", "10161844");
		map.put("model_name", "GIF-XP260");
		map.put("serial_no", "TEST2_序列");

		map.put("set_times", "7");
		map.put("set_times_map", setTimesMap);

		String recv = scUtil.clientSendMessage(IP_ADDR, 50023, "In:" + JSON.encode(map));

		Thread.sleep(20000);

		recv = scUtil.clientSendMessage(IP_ADDR, 50023, "Out:" + JSON.encode(map));

		_log.info(recv);
		String rec = recv.substring("getCount:".length());
		Map<String, String> decodedRec = JSON.decode(rec, Map.class);
		for (String key : decodedRec.keySet()) {
			if (setTimesMap.containsKey(key)) {
				int setTimes = 0, actualTimes = 0;
				try {
					setTimes = Integer.parseInt(setTimesMap.get(key)); 
					actualTimes = Integer.parseInt(decodedRec.get(key));

					if (actualTimes < setTimes) {
						MsgInfo info = new MsgInfo();
						info.setErrmsg("当前维修对象作业[" + nameMap.get(key) + "]应当进行 " + setTimes + " 次，实际记录 " + actualTimes + "次。请操作达到计数。");
						_log.info(info.getErrmsg());
					} else if (actualTimes < setTimes) {
						String message = "当前维修对象作业[" + nameMap.get(key) + "]应当进行 " + setTimes + " 次，实际记录 " + actualTimes + "次。";
						listResponse.put("procedure_step_count_message", message);
						AlarmMesssageService amService = new AlarmMesssageService();
						mEntity = mService.loadSimpleMaterialDetailEntity(conn, "00000143411");
//						LoginData user = new LoginData();
//						amService.createPscAlarmMessage("00000143411", mEntity.getSorc_no(), message, user, conn);
						_log.info(message);
					}

				} catch (NumberFormatException e) {
				}
			}
		}

		listResponse.put("recv", recv);
	}


	public void confirmFinish(Map<String, String> decodedRec,
			String material_id, LoginData user,
			Map<String, Object> listResponse, List<MsgInfo> infoes,
			SqlSessionManager conn) throws Exception {
		Map<String, ProcedureStepCountEntity> setTimesMap = new HashMap<String, ProcedureStepCountEntity>();

		MaterialService mService = new MaterialService();
		MaterialEntity mEntity = mService.loadSimpleMaterialDetailEntity(conn, material_id);

		ProcedureStepCountMapper mapper = conn.getMapper(ProcedureStepCountMapper.class);
		ProcedureStepCountEntity entity = new ProcedureStepCountEntity();
		entity.setRelation_type(RELATION_TYPE_MODEL); // TODO
		entity.setRelation_id(mEntity.getModel_id());
		entity.setPosition_id("00000000036");
		List<ProcedureStepCountEntity> list = mapper.searchProcedureStepOfModel(entity);

		for (ProcedureStepCountEntity pscEntity : list) {
			setTimesMap.put(pscEntity.getProcedure_step_count_id(), pscEntity);
		}

		String alarmMessageContents = "";

		for (String key : decodedRec.keySet()) {
			if (setTimesMap.containsKey(key)) {
				int setTimes = 0, actualTimes = 0;
				try {
					setTimes = setTimesMap.get(key).getStep_times(); 
					actualTimes = Integer.parseInt(decodedRec.get(key));

					if (actualTimes < setTimes) {
						MsgInfo info = new MsgInfo();
						info.setErrmsg("当前维修对象作业[" + setTimesMap.get(key).getName() + "]应当进行 " + setTimes + " 次，实际记录 " + actualTimes + " 次。请操作达到计数。");
						infoes.add(info);
						_log.info(info.getErrmsg());
					} else if (actualTimes < setTimes) {
						alarmMessageContents += "当前维修对象作业[" + setTimesMap.get(key).getName() + "]应当进行 " + setTimes + " 次，实际已经记录 " + actualTimes + "了次。";
					}

				} catch (NumberFormatException e) {
				}
			}
		}

		if (alarmMessageContents.length() > 0) {
			listResponse.put("procedure_step_count_message", alarmMessageContents);
			AlarmMesssageService amService = new AlarmMesssageService();
			amService.createPscAlarmMessage(material_id, mEntity.getSorc_no(), alarmMessageContents, user, conn);
			_log.info(alarmMessageContents);
		}

	}
}
