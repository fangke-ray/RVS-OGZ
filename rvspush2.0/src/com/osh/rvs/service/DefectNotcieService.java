package com.osh.rvs.service;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;

import com.osh.rvs.common.MailUtils;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.AlarmMesssageEntity;
import com.osh.rvs.entity.OperatorEntity;
import com.osh.rvs.entity.PostMessageEntity;
import com.osh.rvs.mapper.push.AlarmMesssageMapper;
import com.osh.rvs.mapper.push.CommonMapper;
import com.osh.rvs.mapper.push.MaterialMapper;
import com.osh.rvs.mapper.push.OperatorMapper;
import com.osh.rvs.mapper.push.PostMessageMapper;
import com.osh.rvs.servlet.OperatorMessageServletEndPoint;

public class DefectNotcieService {

	Logger _log = Logger.getLogger("DefectNotcieService");

	/** 不良提出 */
	private final static int STEP_POINTOUT = 0;
	/** 原因分析 */
	private final static int STEP_ANALYSIS = 1;
	/** 对策立案 */
	private final static int STEP_CASED = 2;
	/** 对策待实现 */
	private final static int STEP_REALIZING = 3;
	/** 对策待确认 */
	private final static int STEP_CONFIRM = 4;

	/** 步骤通知 */
	private final static int STEP_NOTICE_TO_ROOT = 6;

	/** 最终检查不良 */
	public final static int DEFECTIVE_TYPE_QA = 1;

	public void post(String alarm_message_id, String post_operator_id,
			String step, String confirm_step) {
		_log.info("post start.." + alarm_message_id + ">>" + post_operator_id);
		SqlSessionManager conn = RvsUtils.getTempWritableConn();
		try {
			conn.startManagedSession(false);

			// 推送理由
			int iStep = Integer.parseInt(step);
			Integer reason = 70 + iStep;
			if (iStep == STEP_NOTICE_TO_ROOT) {
				if (isEmpty(confirm_step)) return;
				switch(confirm_step) {
				case STEP_POINTOUT + "" : reason = 75; break;
				case STEP_ANALYSIS + "" : reason = 76; break;
				case STEP_CASED + "" : reason = 77; break;
				}
				if (!"77".equals(reason)) {
					// 检查是否发送过推送
					if (checkSent(alarm_message_id, "" + reason, conn)) {
						return;
					}
				}
			}

			if (isEmpty(confirm_step)) {
				// 检查是否发送过推送
				if (checkSent(alarm_message_id, "" + reason, conn)) {
					return;
				}
			}

			// 取得发送者信息
			OperatorMapper oMapper = conn.getMapper(OperatorMapper.class);
			OperatorEntity oCondition = new OperatorEntity();
			oCondition.setDepartment(RvsConsts.DEPART_REPAIR);
			List<OperatorEntity> listOperators = null;

			OperatorEntity sender = oMapper.getOperatorByID(post_operator_id);
			String sender_name = "";
			if (sender != null) sender_name = sender.getName();

			// 取得不良信息
			AlarmMesssageMapper amMapper = conn.getMapper(AlarmMesssageMapper.class);
			AlarmMesssageEntity defectiveAnalysis = amMapper.getDefectiveAnalysisByAlarmMessageId(alarm_message_id);
			// 维修品信息
			String omrNotifiNo = defectiveAnalysis.getSorc_no();
			// 备注
			String defectivePhenomenon = defectiveAnalysis.getDefective_phenomenon();
			if (!isEmpty(defectivePhenomenon)) {
				defectivePhenomenon = "（暂无信息）";
			} else {
				if (defectivePhenomenon.substring(0, 1).equals("{")) {
					
				}
			}

			// 发送者信息
			List<String> reciverIdList = new ArrayList<String>();
			List<InternetAddress> reciverMailList = new ArrayList<InternetAddress>();

			String message = null;
			// 取得信息
			// 取得接收者
			switch(reason) {
			case 70 : // STEP_POINTOUT 
				if (isEmpty(confirm_step)) {
					message = sender_name + "提出了针对维修品" + omrNotifiNo + "的不良现象，请予以确认。";
					// 通知上级经理
					oCondition.setRole_id(RvsConsts.ROLE_MANAGER);
					listOperators = oMapper.searchOperator(oCondition);
					compileResultList(reciverIdList, reciverMailList, listOperators, post_operator_id);
					// 通知技术人员
					oCondition.setRole_id(RvsConsts.ROLE_OPERATOR);
					oCondition.setSection_id("00000000011"); // “技术科”
					listOperators = oMapper.searchOperator(oCondition);
					compileResultList(reciverIdList, reciverMailList, listOperators, post_operator_id);
					if (DEFECTIVE_TYPE_QA == defectiveAnalysis.getDefective_type()) {
						// 取得维修品完成工程
						MaterialMapper mMapper = conn.getMapper(MaterialMapper.class);
						String finish_line_id = mMapper.getMaterialFinishLine(defectiveAnalysis.getMaterial_id());
						// 通知完成工程的线长
						oCondition.setRole_id(RvsConsts.ROLE_LINELEADER);
						oCondition.setSection_id(null);
						oCondition.setLine_id(finish_line_id);
						listOperators = oMapper.searchOperator(oCondition);
						compileResultList(reciverIdList, reciverMailList, listOperators, post_operator_id);
					}
				} else {
					message = "维修品" + omrNotifiNo + "的不良报告已经确认，请展开分析与对策。";
					// 通知技术人员
					oCondition.setRole_id(RvsConsts.ROLE_OPERATOR);
					oCondition.setSection_id("00000000011"); // “技术科”
					listOperators = oMapper.searchOperator(oCondition);
					compileResultList(reciverIdList, reciverMailList, listOperators, post_operator_id);
				}

				break;
			case 71 : // STEP_ANALYSIS 
				message = sender_name + "提出了针对维修品" + omrNotifiNo + "的不良原因分析，请予以确认。";
				// 通知技术经理
				oCondition.setRole_id(RvsConsts.ROLE_LINELEADER);
				oCondition.setSection_id("00000000011"); // “技术科”
				listOperators = oMapper.searchOperator(oCondition);
				compileResultList(reciverIdList, reciverMailList, listOperators, post_operator_id);

				break;
			case 72 : // STEP_CASED 
				message = sender_name + "提出了针对维修品" + omrNotifiNo + "的不良对策，请予以确认。";
				// 通知技术经理
				oCondition.setRole_id(RvsConsts.ROLE_LINELEADER);
				oCondition.setSection_id("00000000011"); // “技术科”
				listOperators = oMapper.searchOperator(oCondition);
				compileResultList(reciverIdList, reciverMailList, listOperators, post_operator_id);

				break;
			case 73 : // STEP_REALIZING 
				message = sender_name + "完成了针对维修品" + omrNotifiNo + "的不良对策实施，请予以确认。";
				// 通知上级经理
				oCondition.setRole_id(RvsConsts.ROLE_MANAGER);
				listOperators = oMapper.searchOperator(oCondition);
				compileResultList(reciverIdList, reciverMailList, listOperators, post_operator_id);
				break;
			case 74 : // STEP_CONFIRM 
				message = sender_name + "完成了针对维修品" + omrNotifiNo + "的不良对策实施确认，请予以确认。";
				// 通知上级经理
				oCondition.setRole_id(RvsConsts.ROLE_MANAGER);
				listOperators = oMapper.searchOperator(oCondition);
				compileResultList(reciverIdList, reciverMailList, listOperators, post_operator_id);
				break;
			case 75 : // STEP_NOTICE_TO_ROOT / STEP_POINTOUT 
				message = sender_name + "提出了针对维修品" + omrNotifiNo + "的不良现象，包含了零件追加需求，请予以确认。";
				// 通知现品人员
				oCondition.setRole_id(RvsConsts.ROLE_FACTINLINE);
				listOperators = oMapper.searchOperator(oCondition);
				compileResultList(reciverIdList, reciverMailList, listOperators, post_operator_id);
				break;
			case 76 : // STEP_NOTICE_TO_ROOT / STEP_ANALYSIS 
				message = sender_name + "提出了针对维修品" + omrNotifiNo + "的零件相关分析，请参考。";
				// 通知技术人员
				oCondition.setRole_id(RvsConsts.ROLE_OPERATOR);
				oCondition.setSection_id("00000000011"); // “技术科”
				listOperators = oMapper.searchOperator(oCondition);
				compileResultList(reciverIdList, reciverMailList, listOperators, post_operator_id);
				break;
			case 77 : // STEP_NOTICE_TO_ROOT / STEP_CASED 
				message = sender_name + "确认了针对维修品" + omrNotifiNo + "的不良对策方案，请开展实施。";
				if (DEFECTIVE_TYPE_QA == defectiveAnalysis.getDefective_type()) {
					// 取得维修品完成工程
					MaterialMapper mMapper = conn.getMapper(MaterialMapper.class);
					String finish_line_id = mMapper.getMaterialFinishLine(defectiveAnalysis.getMaterial_id());
					// 通知完成工程的线长
					oCondition.setRole_id(RvsConsts.ROLE_LINELEADER);
					oCondition.setSection_id(null);
					oCondition.setLine_id(finish_line_id);
					listOperators = oMapper.searchOperator(oCondition);
					compileResultList(reciverIdList, reciverMailList, listOperators, post_operator_id);
				} else {
					// 通知提出者
					if (defectiveAnalysis.getOperator_id() != null) {
						reciverIdList.add(defectiveAnalysis.getOperator_id());
						OperatorEntity sponor = oMapper.getOperatorByID(defectiveAnalysis.getOperator_id());
						if (sponor.getEmail() != null) {
							reciverMailList.add(new InternetAddress(sponor.getEmail(), sponor.getName()));
						}
					}
				}
				break;
			}

			_log.info("post message.." + message);

			// 记录发送
			if (reciverIdList.size() > 0) {
				PostMessageMapper mapper = conn.getMapper(PostMessageMapper.class);
				PostMessageEntity pmEntity = new PostMessageEntity();
				pmEntity.setRoot_post_message_id(alarm_message_id);
				pmEntity.setLevel(1);
				if (isEmpty(confirm_step)) {
					pmEntity.setLevel(2);
				}
				pmEntity.setSender_id(post_operator_id);
				pmEntity.setContent(message);
				pmEntity.setReason(reason);
				mapper.createPostMessage(pmEntity);

				CommonMapper commMapper = conn.getMapper(CommonMapper.class);
				String postMessageId = commMapper.getLastInsertID();
				pmEntity.setPost_message_id(postMessageId);

				for (String reciverId : reciverIdList) {
					pmEntity.setReceiver_id(reciverId);
					mapper.createPostMessageSendation(pmEntity);
				}

				conn.commit();

				Map<String, Map<String, OperatorMessageServletEndPoint>> mesClients = OperatorMessageServletEndPoint.getClients();
				//通知接收人员
				for (String reciverId : reciverIdList) {
					Map<String, OperatorMessageServletEndPoint> mInbound = mesClients.get(reciverId); 
					if (mInbound != null) {
						for (String endpointKey : mInbound.keySet()) {
							mInbound.get(endpointKey).newMessage();
						}
					}
				}
			}

			// 邮件推送
			if (reciverMailList.size() > 0) {
				message += "\r\n" + defectivePhenomenon;
				MailUtils.sendMail(reciverMailList, null, "不良对策进度提示", message);
			}

		} catch (Exception e) {
			_log.error(e.getMessage(), e);
			conn.rollback();
		} finally {
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.close();
			}
			conn = null;
		}
	}

	/**
	 * 
	 * @param reciverIdList
	 * @param reciverMailList
	 * @param listOperators 查询结果
	 * @param post_operator_id 信息发送者
	 */
	private void compileResultList(List<String> reciverIdList,
			List<InternetAddress> reciverMailList, List<OperatorEntity> listOperators,
			String post_operator_id) {
		for (OperatorEntity op : listOperators) {
			// 触发者自己收不到
			if (!post_operator_id.equals(op.getOperator_id())) {
				if (!reciverIdList.contains(op.getOperator_id())) {
					reciverIdList.add(op.getOperator_id());
					if (!isEmpty(op.getEmail())) {
						if (!reciverMailList.contains(op.getEmail())) {
							try {
								reciverMailList.add(new InternetAddress(op.getEmail(), op.getName()));
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 检查是否发送过此推送
	 * 
	 * @param alarm_message_id
	 * @param reason
	 * @param conn
	 * @return
	 */
	private boolean checkSent(String alarm_message_id, String reason,
			SqlSessionManager conn) {
		PostMessageMapper mapper = conn.getMapper(PostMessageMapper.class);
		int cnt = mapper.checkSentByRootReason(alarm_message_id, reason);
		return cnt > 0;
	}

}
