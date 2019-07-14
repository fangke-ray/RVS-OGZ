package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.InternetAddress;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;

import com.osh.rvs.common.MailUtils;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.AlarmMesssageEntity;
import com.osh.rvs.entity.AlarmMesssageSendationEntity;
import com.osh.rvs.entity.BoundMaps;
import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.entity.OperatorEntity;
import com.osh.rvs.inbound.OperatorMessageInbound;
import com.osh.rvs.mapper.push.AlarmMesssageMapper;
import com.osh.rvs.mapper.push.CommonMapper;
import com.osh.rvs.mapper.push.MaterialMapper;
import com.osh.rvs.mapper.push.OperatorMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.DateUtil;

public class MaterialService {
	public static Logger _log = Logger.getLogger("MaterialService");

	public MaterialEntity getMaterial(String material_id) {
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();

		SqlSession conn = factory.openSession(TransactionIsolationLevel.READ_COMMITTED);

		MaterialEntity ret = null;

		try {
			MaterialMapper dao = conn.getMapper(MaterialMapper.class);
			ret = dao.getMaterialEntityByKey(material_id);
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		} finally {
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
		return ret;
	}

	private static String gotime = ""; 
	/**
	 * 投线超时检测
	 */
	public void checkInlineService() {
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();

		// 有向DB写操作的场合
		SqlSessionManager conn = SqlSessionManager.newInstance(factory);

		// 检测同意后(S2或S3等级2天之内，S1等级3天之内, D/M等级隔天的中午前)，未投线的维修对象
		try {
			Calendar startTime = Calendar.getInstance();
			String sGotime = DateUtil.toString(startTime.getTime(), "yyyyMMdd") + startTime.get(Calendar.AM_PM);
			if (gotime.equals(sGotime)) { // 半天里已经发过了
				return;
			}
			gotime = sGotime;

			conn.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);

			MaterialMapper dao = conn.getMapper(MaterialMapper.class);
			List<MaterialEntity> ret = dao.getMaterialInlineLater(null);
			List<MaterialEntity> retEndoeye = dao.getMaterialInlineLater(6);
			List<MaterialEntity> retPeri = dao.getMaterialInlineLater(7);

			Set<String> leaderTriggerSet = new HashSet<String>();

			if (ret.size() > 0) {
				sendInlineLateMail(ret, "00000000011", leaderTriggerSet, sGotime, conn);
			}

			if (ret.size() > 0) {
				sendInlineLateMail(retEndoeye, "00000000050", leaderTriggerSet, sGotime, conn);
			}

			if (ret.size() > 0) {
				sendInlineLateMail(retPeri, "00000000070", leaderTriggerSet, sGotime, conn);
			}

			if (conn != null && conn.isManagedSessionStarted()) {
				conn.commit();
				_log.info("Committed！");

				Map<String, MessageInbound> bMap = BoundMaps.getMessageBoundMap();

				for (String leaderId : leaderTriggerSet) {
					MessageInbound mInbound = bMap.get(leaderId); 
					if (mInbound != null && mInbound instanceof OperatorMessageInbound) 
						((OperatorMessageInbound)mInbound).newMessage();
				}
			}

		} catch (Exception e) {
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

	private void sendInlineLateMail(List<MaterialEntity> ret, String line_id, Set<String> leaderTriggerSet, 
			String sGotime, SqlSessionManager conn) throws Exception {

		AlarmMesssageMapper amDao = conn.getMapper(AlarmMesssageMapper.class);

		String mailContents = "";

		// QL
		OperatorMapper oMapper = conn.getMapper(OperatorMapper.class);
		OperatorEntity cond = new OperatorEntity();
		cond.setRole_id(RvsConsts.ROLE_LINELEADER);
		cond.setLine_id(line_id);

		List<OperatorEntity> lOp = oMapper.searchOperator(cond);

		for (MaterialEntity entity : ret) {
			String material_id = entity.getMaterial_id();
			AlarmMesssageEntity amEntity = new AlarmMesssageEntity();
			amEntity.setLevel(RvsConsts.WARNING_LEVEL_NORMAL);
			amEntity.setMaterial_id(material_id);
			amEntity.setSection_id("00000000001"); // 物料
			amEntity.setLine_id("00000000011");

			amEntity.setReason(RvsConsts.WARNING_REASON_INLINE_LATE);

			amDao.createAlarmMessage(amEntity);

			CommonMapper cDao = conn.getMapper(CommonMapper.class);
			String alarmmessage_id = cDao.getLastInsertID();

			AlarmMesssageSendationEntity amsBean = new AlarmMesssageSendationEntity();

			for (OperatorEntity op : lOp) {
				amsBean.setAlarm_messsage_id(alarmmessage_id);
				amsBean.setSendation_id(op.getOperator_id());
				amDao.createAlarmMessageSendation(amsBean);

				leaderTriggerSet.add(op.getOperator_id());
			}

			mailContents += entity.getSorc_no() + "(" + entity.getModel_name() + ")"
					+ CodeListUtils.getValue("material_level_all", "" + entity.getLevel())
					+ "等级 同意日："
					+ DateUtil.toString(entity.getAgreed_date(), DateUtil.ISO_DATE_PATTERN)
					+ RvsConsts.LINE_FEED_CODE;
		}

		Collection<InternetAddress> toIas = RvsUtils.getMailIas("inline.plan.to", conn);
		if ("00000000050".equals(line_id)) {
			toIas = RvsUtils.getMailIas("inline.plan.endoeye.to", conn);
		} else if ("00000000070".equals(line_id)) {
			toIas = RvsUtils.getMailIas("inline.plan.peripheral.to", conn);
		}
		Collection<InternetAddress> ccIas = RvsUtils.getMailIas("inline.plan.cc", conn);

		// 发信
		String subject = PathConsts.MAIL_CONFIG.getProperty("inline.plan.title") + toYMD(sGotime);
		if ("00000000070".equals(line_id)) {
			toIas = RvsUtils.getMailIas("inline.plan.peripheral.to", conn);
		} else {
			mailContents = RvsUtils.getProperty(PathConsts.MAIL_CONFIG, "inline.plan.peripheral.content", mailContents);
		}

		MailUtils.sendMail(toIas, ccIas, subject, mailContents);
	}

	private String toYMD(String sGotime) {
		if (sGotime.length() < 9) return sGotime;
		return sGotime.substring(0, 4) + "年" + sGotime.substring(4, 6) + "月" + sGotime.substring(6, 8) + "日" +
				("1".equals(sGotime.substring(8, 9)) ? "P.M." : "A.M.");
	}

	public List<MaterialEntity> getShippingTodayMaterialDetail(String date,SqlSession conn ) {
		List<MaterialEntity> list = null;
		try {
			MaterialMapper dao = conn.getMapper(MaterialMapper.class);
			list = dao.searchShipping(date);
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		} 
		return list;
	}

	public List<MaterialEntity> unRepairAdjust(SqlSession conn ){
		List<MaterialEntity> list = null;
		try {
			MaterialMapper dao = conn.getMapper(MaterialMapper.class);
			list = dao.unRepairAdjust();
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}
		return list;
		
	}

	public List<MaterialEntity> searchAdvancedRecovery(String date,SqlSession conn ){
		List<MaterialEntity> list = new ArrayList<MaterialEntity>();
		try {
			MaterialMapper dao = conn.getMapper(MaterialMapper.class);
			list = dao.searchAdvancedRecovery(date);
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}
		return list;
		
	}

	public List<MaterialEntity> acceptAndDisinfectAndSterilize(String date,SqlSession conn ) {
		List<MaterialEntity> list = null;
		try {
			MaterialMapper dao = conn.getMapper(MaterialMapper.class);
			list = dao.searchAcceptDisinfectSterilize(date);
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		} 
		return list;
	}

}
