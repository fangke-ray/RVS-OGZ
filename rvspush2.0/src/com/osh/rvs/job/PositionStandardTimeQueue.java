package com.osh.rvs.job;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;

import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.entity.AlarmClock;
import com.osh.rvs.entity.AlarmMesssageEntity;
import com.osh.rvs.entity.AlarmMesssageSendationEntity;
import com.osh.rvs.entity.BoundMaps;
import com.osh.rvs.entity.OperatorEntity;
import com.osh.rvs.inbound.OperatorMessageInbound;
import com.osh.rvs.mapper.push.AlarmMesssageMapper;
import com.osh.rvs.mapper.push.CommonMapper;
import com.osh.rvs.mapper.push.OperatorMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.copy.DateUtil;

public class PositionStandardTimeQueue implements Runnable {

	public static PositionStandardTimeQueue instance = new PositionStandardTimeQueue();

	private static Map<Long, AlarmClock> alarmClockQueue = new TreeMap<Long, AlarmClock>();

	private static Map<String, Long> materialPositionIndexes = new HashMap<String, Long>();

	public static synchronized void startAlarmClockQueue(String material_id, String position_id, String line_id, String operator_id, Integer standard_minute, Integer cost_minute) {
		String key = material_id + "_" + position_id;

		if (materialPositionIndexes.containsKey(key)) return;

		// long remainTime = new Double((standard_minute * 1.1 - cost_minute) * 60000l).longValue();
		long remainTime = new Double((standard_minute - cost_minute) * 60000l).longValue() + 29999l;
		if (remainTime < 0) return;

		long ringTime = new Date().getTime() + remainTime;

		AlarmClock alarmClock = new AlarmClock(ringTime, material_id, position_id, line_id, operator_id);

		while (alarmClockQueue.containsKey(ringTime)) {
			ringTime++;
		}

		alarmClockQueue.put(ringTime, alarmClock);
		materialPositionIndexes.put(key, ringTime);
	}

	public static synchronized void stopAlarmClockQueue(String material_id, String position_id) {
		String mpKey = material_id + "_" + position_id;
		if (materialPositionIndexes.containsKey(mpKey)) {
			Long acKey = materialPositionIndexes.get(mpKey);
			materialPositionIndexes.remove(mpKey);
			alarmClockQueue.remove(acKey);
		}
	}

	@Override
	public void run() {
		try {
			while(true) {
				long nowTimeStamp = new Date().getTime();
				synchronized(this) {
					Set<Long> removeKeys = new HashSet<Long>();
					Set<String> leaderTriggerSet = new HashSet<String>();

					SqlSessionManager connManager = null;

					for (Long acKey : alarmClockQueue.keySet()) {
						if (acKey < nowTimeStamp) {
							if (connManager == null) {
								connManager = getTempWritableConn();
								connManager.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);
							}
							removeKeys.add(acKey);
							try {
								createMessage(alarmClockQueue.get(acKey), connManager, leaderTriggerSet);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}

					if (connManager != null && connManager.isManagedSessionStarted()) {
						connManager.commit();
						connManager.close();

						Map<String, MessageInbound> bMap = BoundMaps.getMessageBoundMap();

						for (String leaderId : leaderTriggerSet) {
							MessageInbound mInbound = bMap.get(leaderId); 
							if (mInbound != null && mInbound instanceof OperatorMessageInbound) 
								((OperatorMessageInbound)mInbound).newMessage();
						}
					}
					connManager = null;

					for (Long removeKey : removeKeys) {
						materialPositionIndexes.remove(alarmClockQueue.get(removeKey).getMp_key());
						alarmClockQueue.remove(removeKey);
					}
				}
				Thread.sleep(30000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void createMessage(AlarmClock alarmClock, SqlSessionManager conn, Set<String> leaderTriggerSet) throws Exception {

		AlarmMesssageMapper amMapper = conn.getMapper(AlarmMesssageMapper.class);
		CommonMapper cmnMapper = conn.getMapper(CommonMapper.class);
		OperatorMapper oMapper = conn.getMapper(OperatorMapper.class);

		AlarmMesssageEntity amBean = alarmClock.getRing_message();
		amBean.setOccur_time(new Date());
		amMapper.createAlarmMessage(amBean);

		String alarmMesssageId = cmnMapper.getLastInsertID();
		// 查询收件人-线长
		OperatorEntity cond = new OperatorEntity();
		cond.setRole_id(RvsConsts.ROLE_LINELEADER);
		cond.setLine_id(alarmClock.getLine_id());

		List<OperatorEntity> lOp = oMapper.searchOperator(cond);

		for (OperatorEntity op : lOp) {
			AlarmMesssageSendationEntity amsBean = new AlarmMesssageSendationEntity();
			amsBean.setAlarm_messsage_id(alarmMesssageId);
			amsBean.setSendation_id(op.getOperator_id());
			amMapper.createAlarmMessageSendation(amsBean);

			leaderTriggerSet.add(op.getOperator_id());
		}
	}

	public static String getQueue() {
		StringBuffer retString = new StringBuffer();
		for (Long ac : alarmClockQueue.keySet()) {
			retString.append(DateUtil.toString(new Date(ac), DateUtil.ISO_TIME_PATTERN)).append(" ");
			retString.append(alarmClockQueue.get(ac).getRing_message().getPosition_id()).append("\n");
		}
		return retString.toString();
	}

	private static SqlSessionManager getTempWritableConn() {
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return SqlSessionManager.newInstance(factory);
	}
}
