package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;

import com.osh.rvs.common.MailUtils;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.AlarmMesssageEntity;
import com.osh.rvs.entity.AlarmMesssageSendationEntity;
import com.osh.rvs.entity.PositionEntity;
import com.osh.rvs.mapper.push.AlarmMesssageMapper;
import com.osh.rvs.mapper.push.CommonMapper;
import com.osh.rvs.mapper.push.PositionMapper;
import com.osh.rvs.servlet.OperatorMessageServletEndPoint;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;

public class TriggerPositionService {

	public static Logger _log = Logger.getLogger("PositionJob");

	public void checkOverLine(String position_id, String section_id, String material_id, List<String> oList) {

		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();

		// 有向DB写操作的场合
		SqlSessionManager conn = SqlSessionManager.newInstance(factory);

		try {
			conn.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);

			PositionMapper dao = conn.getMapper(PositionMapper.class);
			PositionEntity pBean = dao.getPositionByID(position_id);
			if (pBean == null) {
				_log.error("Invalid Position");
				return;
			}

			// 取得工位上限
			String waitingflow = RvsUtils.getWaitingflow(section_id, pBean.getProcess_code());
			if (waitingflow == null || waitingflow == "0") {
				// 无限制
				_log.error("无限制");
				return;
			} else if ("400".equals(pBean.getProcess_code())) {
				waitingflow += "0"; // 当前400工位上限设定以10件为单位
			}

			int iwaitingflow = Integer.parseInt(waitingflow);

			// 无平行工位
//			if (!"00000000001".equals(section_id) || pBean.getLight_division_flg() == 0) {
				int ifactwaitingflow = dao.getPositionHeap(section_id, position_id, null);
				if (ifactwaitingflow > iwaitingflow) {
					String content = overAlert(section_id, position_id, "", pBean, ifactwaitingflow, iwaitingflow, conn);
					if (content != null) {
						sendMail(section_id, position_id, pBean, content, oList, conn);
					}
				}
//			} else if (pBean.getLight_division_flg() == 1) {
//				// 只有总组自动切线
//				boolean isCom = "00000000014".equals(pBean.getLine_id());
//
//				String content = null;
//				// A线先判断
//				// List<String> materialIds = ;
//				int ifactwaitingflow = dao.getPositionHeap(section_id, position_id, "A");
//				if (ifactwaitingflow + 1 > iwaitingflow) {
//					content = overAlert(section_id, position_id, "A", pBean, ifactwaitingflow, iwaitingflow, conn);
//
//					if (isCom && content!= null && material_id != null) {
//						// 超量的话, 踢出不紧急的等待维修品
//						MaterialMapper mapper = conn.getMapper(MaterialMapper.class);
//						MaterialEntity mBean = mapper.getMaterialDetail(material_id);
//						mapper.sendToBLine(material_id);
//						content += "\n维修对象" + mBean.getSorc_no() + "(" + mBean.getModel_name() + ")" + "送入 B 线。";
//					}
//					
//				}
//				// B线判断
//				ifactwaitingflow = dao.getPositionHeap(section_id, position_id, "B");
//				int ibwaitingflow = 6; // (iwaitingflow + 1) / 2;
//				if (ifactwaitingflow + 1 > ibwaitingflow) {
//					String contentB = overAlert(section_id, position_id, "B", pBean, ifactwaitingflow, iwaitingflow, conn);
//					if (content!= null) {
//						content += "\n" + contentB;
//					} else {
//						content = contentB;
//					}
//				}
//
//				if (content != null) {
//					sendMail(section_id, position_id, pBean, content, conn);
//				}
//			}
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

	private String overAlert(String section_id, String position_id, String px, PositionEntity pBean, int ifactwaitingflow, 
			int iwaitingflow, SqlSessionManager conn) throws Exception {

		// 检查是否已存在改工位的未处理超量警报
		AlarmMesssageMapper amDao = conn.getMapper(AlarmMesssageMapper.class);
		int count = 0;
		if (!"B".equals(px)) 
			count = amDao.countOverflowUnresolvedAlarmMessage(section_id, position_id);
		if (count == 0) {
			String mailContent = "修理生产G 的" + pBean.getProcess_code() + " " 
					+ pBean.getName() + "工位仕挂量为：" + ifactwaitingflow + " 台，已经超过设定的上限：" + iwaitingflow + "请知晓并且处理。";
			return mailContent;
		}
		return null;
	}

	private void sendMail(String section_id, String position_id, PositionEntity pBean, 
			String mailContent, List<String> oList, SqlSessionManager conn) throws Exception {

		AlarmMesssageMapper amDao = conn.getMapper(AlarmMesssageMapper.class);
		String lineId = pBean.getLine_id();

		AlarmMesssageEntity amBean = new AlarmMesssageEntity();
		amBean.setLevel(RvsConsts.WARNING_LEVEL_ERROR);
		amBean.setReason(RvsConsts.WARNING_REASON_WAITING_OVERFLOW);
		amBean.setLine_id(lineId);
		amBean.setSection_id(section_id);
		amBean.setPosition_id(position_id);

		amDao.createAlarmMessage(amBean);

		CommonMapper cDao = conn.getMapper(CommonMapper.class);
		String alarmmessage_id = cDao.getLastInsertID();

		List<String> senderIds = new ArrayList<String> ();
		Collection<InternetAddress> toIas = RvsUtils.getMailIas("over.alert.to", conn, lineId, senderIds);
		Collection<InternetAddress> ccIas = RvsUtils.getMailIas("over.alert.cc", conn, lineId, senderIds);

		Map<String, Map<String, OperatorMessageServletEndPoint>> mesClients = OperatorMessageServletEndPoint.getClients();

		for (String operatorId : senderIds) {
			AlarmMesssageSendationEntity amsBean = new AlarmMesssageSendationEntity();
			amsBean.setAlarm_messsage_id(alarmmessage_id);
			amsBean.setSendation_id(operatorId);
			amDao.createAlarmMessageSendation(amsBean);
			oList.add(operatorId);
		}

		// TODO 课shi
		String subject = "工位仕挂量超量";
		MailUtils.sendMail(toIas, ccIas, subject, mailContent);
	}
}
