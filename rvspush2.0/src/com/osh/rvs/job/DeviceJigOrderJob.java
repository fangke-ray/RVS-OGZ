package com.osh.rvs.job;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.osh.rvs.common.MailUtils;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.BoundMaps;
import com.osh.rvs.entity.DeviceJigOrderEntity;
import com.osh.rvs.entity.OperatorEntity;
import com.osh.rvs.entity.PostMessageEntity;
import com.osh.rvs.inbound.OperatorMessageInbound;
import com.osh.rvs.mapper.push.CommonMapper;
import com.osh.rvs.mapper.push.DeviceJigOrderMapper;
import com.osh.rvs.mapper.push.DevicesTypeMapper;
import com.osh.rvs.mapper.push.OperatorMapper;
import com.osh.rvs.mapper.push.PostMessageMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.copy.DateUtil;

/**
 * 设备工具订购申请
 * 
 * @author liuxb
 * 
 */
public class DeviceJigOrderJob implements Job {
	private static Logger log = Logger.getLogger("DeviceJigOrderJob");

	/** 设备 **/
	private final String OBJECT_TYPE_DEVICE = "1";
	/** 专用工具 **/
	private final String OBJECT_TYPE_SPECIAL_TOOLS = "2";
	/** 一般工具 **/
	private final String OBJECT_TYPE_GENERAL_TOOLS = "3";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();
		log.info("DeviceJigOrderJob: " + jobKey + " executing at " + new Date());

		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();

		// 当天
		Calendar today = Calendar.getInstance();

		boolean isHoliday = RvsUtils.isHoliday(today.getTime(), conn);

		// 非工作日
		if (isHoliday) {
			return;
		}

		try {
			conn.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);

			make(conn);

			if (conn != null && conn.isManagedSessionStarted()) {
				conn.commit();
				log.info("Committed！");
			}
		} catch (Exception e) {
			log.error("DeviceJigOrderJob" + e.getMessage());
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.rollback();
				log.info("Rolled back！");
			}
		} finally {
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.close();
			}
			conn = null;
		}
	}

	private void make(SqlSessionManager conn) {
		OperatorMapper operatorMapper = conn.getMapper(OperatorMapper.class);
		DeviceJigOrderMapper deviceJigOrderMapper = conn.getMapper(DeviceJigOrderMapper.class);
		PostMessageMapper postMessageMapper = conn.getMapper(PostMessageMapper.class);
		CommonMapper commonMapper = conn.getMapper(CommonMapper.class);

		OperatorEntity operatorEntity = new OperatorEntity();
		operatorEntity.setRole_id(RvsConsts.ROLE_DEVICEMANAGER);
		// 设备管理员
		List<OperatorEntity> dtManagerList = operatorMapper.searchOperator(operatorEntity);

		// 25号
		int nearDay = 25;

		/** 没有询价的申请 以及 询价日期较旧的申请 **/
		// 询价发送日期
		Calendar sendDate = Calendar.getInstance();
		sendDate.set(Calendar.HOUR_OF_DAY, 0);
		sendDate.set(Calendar.MINUTE, 0);
		sendDate.set(Calendar.SECOND, 0);
		sendDate.set(Calendar.MILLISECOND, 0);

		// 几号
		int day = sendDate.get(Calendar.DAY_OF_MONTH);
		if (day > nearDay) {
			// 超过25号，取本月25号
			sendDate.set(Calendar.DAY_OF_MONTH, nearDay);
		} else if (day < nearDay) {
			// 在25号之前，取上个月25号
			sendDate.add(Calendar.MONTH, -1);
			sendDate.set(Calendar.DAY_OF_MONTH, nearDay);
		}

		// 没有询价的申请数
		int unInvoiceNum = deviceJigOrderMapper.countUnInvoice();
		// 询价日期较旧的申请数
		int oldInvoiceNum = deviceJigOrderMapper.countOldInvoice(sendDate.getTime());

		if (unInvoiceNum > 0 || oldInvoiceNum > 0) {
			String content = "目前有：";
			if (unInvoiceNum > 0) {
				content += unInvoiceNum + "件尚未询价的设备工具订购申请品；";
			}
			if (oldInvoiceNum > 0) {
				content += oldInvoiceNum + "件询价日期较旧的设备工具订购申请品；";
			}
			content += "请处理。";

			PostMessageEntity postMessageEntity = new PostMessageEntity();
			postMessageEntity.setLevel(1);
			postMessageEntity.setOccur_time(Calendar.getInstance().getTime());
			postMessageEntity.setReason(31);
			postMessageEntity.setSender_id("0");
			postMessageEntity.setContent(content);

			// 建立推送信息
			postMessageMapper.createPostMessage(postMessageEntity);
			String postMessageId = commonMapper.getLastInsertID();

			for (OperatorEntity op : dtManagerList) {
				postMessageEntity.setPost_message_id(postMessageId);
				postMessageEntity.setReceiver_id(op.getOperator_id());
				// 建立推送信息
				postMessageMapper.createPostMessageSendation(postMessageEntity);
			}
		}

		/** 临近纳期的报价 **/
		Map<String, List<DeviceJigOrderEntity>> scheduledMap = new LinkedHashMap<String, List<DeviceJigOrderEntity>>();

		List<DeviceJigOrderEntity> list = deviceJigOrderMapper.searchNearScheduledQuotation();
		for (DeviceJigOrderEntity deviceJigOrderEntity : list) {
			String applicatorID = deviceJigOrderEntity.getApplicator_id();
			if (scheduledMap.containsKey(applicatorID)) {
				scheduledMap.get(applicatorID).add(deviceJigOrderEntity);
			} else {
				List<DeviceJigOrderEntity> subList = new LinkedList<DeviceJigOrderEntity>();
				subList.add(deviceJigOrderEntity);
				scheduledMap.put(applicatorID, subList);
			}
		}

		for (String applicatorID : scheduledMap.keySet()) {
			List<DeviceJigOrderEntity> subList = scheduledMap.get(applicatorID);

			String content = "有：";
			for (DeviceJigOrderEntity entity : subList) {
				Integer quantity = entity.getQuantity();
				String monthDay = DateUtil.toString(entity.getScheduled_date(), "MM月dd日");

				content += quantity + "件申请订购的设备或工具预计于" + monthDay + "到货，";
			}
			content += "请保持关注。";

			PostMessageEntity postMessageEntity = new PostMessageEntity();
			postMessageEntity.setLevel(1);
			postMessageEntity.setOccur_time(Calendar.getInstance().getTime());
			postMessageEntity.setReason(32);
			postMessageEntity.setSender_id("0");
			postMessageEntity.setContent(content);

			// 建立推送信息
			postMessageMapper.createPostMessage(postMessageEntity);
			String postMessageId = commonMapper.getLastInsertID();

			postMessageEntity.setPost_message_id(postMessageId);
			postMessageEntity.setReceiver_id(applicatorID);
			// 建立推送信息
			postMessageMapper.createPostMessageSendation(postMessageEntity);
		}

		/** 超过纳期还没有收货 **/
		list = deviceJigOrderMapper.searchOverScheduledAndUnRecept();
		scheduledMap.clear();
		for (DeviceJigOrderEntity deviceJigOrderEntity : list) {
			String applicatorID = deviceJigOrderEntity.getApplicator_id();
			if (scheduledMap.containsKey(applicatorID)) {
				scheduledMap.get(applicatorID).add(deviceJigOrderEntity);
			} else {
				List<DeviceJigOrderEntity> subList = new LinkedList<DeviceJigOrderEntity>();
				subList.add(deviceJigOrderEntity);
				scheduledMap.put(applicatorID, subList);
			}
		}

		for (String applicatorID : scheduledMap.keySet()) {
			List<DeviceJigOrderEntity> subList = scheduledMap.get(applicatorID);

			String content = "有：";
			for (DeviceJigOrderEntity entity : subList) {
				Integer quantity = entity.getQuantity();
				String monthDay = DateUtil.toString(entity.getScheduled_date(), "MM月dd日");

				content += quantity + "件申请订购的设备或工具预计于" + monthDay + "到货，";
			}
			content += "目前尚未收货。请对应处理。";

			PostMessageEntity postMessageEntity = new PostMessageEntity();
			postMessageEntity.setLevel(2);
			postMessageEntity.setOccur_time(Calendar.getInstance().getTime());
			postMessageEntity.setReason(32);
			postMessageEntity.setSender_id("0");
			postMessageEntity.setContent(content);

			// 建立推送信息
			postMessageMapper.createPostMessage(postMessageEntity);
			String postMessageId = commonMapper.getLastInsertID();

			for (OperatorEntity op : dtManagerList) {
				postMessageEntity.setPost_message_id(postMessageId);
				postMessageEntity.setReceiver_id(op.getOperator_id());
				// 建立推送信息接收人
				postMessageMapper.createPostMessageSendation(postMessageEntity);
			}
		}

		/** 收货后还没有验收 **/
		list = deviceJigOrderMapper.searchUnInlineRecept();
		for (DeviceJigOrderEntity entity : list) {
			String content = "有您申请订购的" + entity.getQuantity() + "件设备或者工具已经收货登录到系统，请确认实物后，进行验收。";

			PostMessageEntity postMessageEntity = new PostMessageEntity();
			postMessageEntity.setLevel(1);
			postMessageEntity.setOccur_time(Calendar.getInstance().getTime());
			postMessageEntity.setReason(33);
			postMessageEntity.setSender_id("0");
			postMessageEntity.setContent(content);

			// 建立推送信息
			postMessageMapper.createPostMessage(postMessageEntity);
			String postMessageId = commonMapper.getLastInsertID();

			postMessageEntity.setPost_message_id(postMessageId);
			postMessageEntity.setReceiver_id(entity.getApplicator_id());
			// 建立推送信息接收人
			postMessageMapper.createPostMessageSendation(postMessageEntity);
		}
	}

	public static void main(String[] args) {
		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();
		try {
			conn.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);

			DeviceJigOrderJob job = new DeviceJigOrderJob();
			job.make(conn);

			if (conn != null && conn.isManagedSessionStarted()) {
				conn.commit();
				log.info("Committed！");
			}
		} catch (Exception e) {
			log.error("DeviceJigOrderJob" + e.getMessage());
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.rollback();
				log.info("Rolled back！");
			}
		} finally {
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.close();
				log.info("Close Connnection！");
			}
			conn = null;
		}
	}

	/**
	 * 设备工具订购申请编辑
	 * 
	 * @param orderNO 订单号
	 * @param operatorID 更新人
	 */
	public void deviceJigOrderEdit(String orderNO, String operatorID) {
		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();
		try {
			conn.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);

			CommonMapper commonMapper = conn.getMapper(CommonMapper.class);
			OperatorMapper operatorMapper = conn.getMapper(OperatorMapper.class);
			PostMessageMapper postMessageMapper = conn.getMapper(PostMessageMapper.class);

			// 更新人名称
			String operatorName = operatorMapper.getOperatorByID(operatorID).getName();

			OperatorEntity operatorEntity = new OperatorEntity();
			operatorEntity.setRole_id(RvsConsts.ROLE_MANAGER);
			operatorEntity.setSection_id("1");
			// 经理
			List<OperatorEntity> receiverList = operatorMapper.searchOperator(operatorEntity);

			// 推送信息
			String content = operatorName + "更新了设备工具订购单" + orderNO + "，请知晓。";
			PostMessageEntity postMessageEntity = new PostMessageEntity();
			postMessageEntity.setLevel(2);
			postMessageEntity.setOccur_time(Calendar.getInstance().getTime());
			postMessageEntity.setReason(31);
			postMessageEntity.setSender_id(operatorID);
			postMessageEntity.setContent(content);

			// 建立推送信息
			postMessageMapper.createPostMessage(postMessageEntity);
			String postMessageId = commonMapper.getLastInsertID();

			for (OperatorEntity op : receiverList) {
				postMessageEntity.setPost_message_id(postMessageId);
				postMessageEntity.setReceiver_id(op.getOperator_id());
				// 建立推送信息接收人
				postMessageMapper.createPostMessageSendation(postMessageEntity);
			}

			Collection<InternetAddress> toIas = RvsUtils.getMailIas("device_jig_order_applicate.to", conn);
			Collection<InternetAddress> ccIas = RvsUtils.getMailIas("device_jig_order_applicate.cc", conn);

			// 发信
			String subject = PathConsts.MAIL_CONFIG.getProperty("device_jig_order_applicate.title");
			String mailContents = content +"\r\n请到RVS设备工具订购画面去确认。";
			MailUtils.sendMail(toIas, ccIas, subject, mailContents);
			
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.commit();
				
				Map<String, MessageInbound> bMap = BoundMaps.getMessageBoundMap();
				
				//通知经理
				for (OperatorEntity op : receiverList) {
					MessageInbound mInbound = bMap.get(op.getOperator_id()); 
					if (mInbound != null && mInbound instanceof OperatorMessageInbound) 
						((OperatorMessageInbound)mInbound).newMessage();
				}
				
				log.info("Committed！");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.rollback();
				log.info("Rolled back！");
			}
		} finally {
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.close();
				log.info("Close Connnection！");
			}
			conn = null;
		}
	}

	/**
	 * 到货验收
	 */
	public void inlineRecept(Map<String, String> param) {
		// 取得数据库连接
		SqlSessionManager conn = getTempWritableConn();
		try {
			conn.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);

			CommonMapper commonMapper = conn.getMapper(CommonMapper.class);
			DevicesTypeMapper devicesTypeMapper = conn.getMapper(DevicesTypeMapper.class);
			PostMessageMapper postMessageMapper = conn.getMapper(PostMessageMapper.class);

			// 操作者
			String operatorID = param.get("operator_id");
			// 设备工具ID
			String deviceTypeId = param.get("device_type_id");
			// 对象类别
			String objectType = param.get("object_type");
			// 型号
			String modelName = param.get("model_name");
			// 管理编号
			String manageCode = param.get("manage_code");
			// 申请人
			String applicatorId = param.get("applicator_id");
			
			if(operatorID.equals(applicatorId)){
				return;			
			}

			// 设备 /一般工具
			String deviceTypName = "";
			if (OBJECT_TYPE_DEVICE.equals(objectType) || OBJECT_TYPE_GENERAL_TOOLS.equals(objectType)) {
				deviceTypName = devicesTypeMapper.getDeviceTypeByID(deviceTypeId).getName();
			}

			// 推送信息
			String content = "";
			if (OBJECT_TYPE_DEVICE.equals(objectType)) {
				content = "您申请订购的设备" + deviceTypName + "(" + modelName + ")已经收货登录到系统，管理编号" + manageCode + "，请确认实物后，进行验收。";
			} else if (OBJECT_TYPE_SPECIAL_TOOLS.equals(objectType)) {
				content = "您申请订购的专用工具" + "(" + modelName + ")已经收货登录到系统，计" + manageCode + "件，请确认实物后，进行验收。";
			} else if (OBJECT_TYPE_GENERAL_TOOLS.equals(objectType)) {
				content = "您申请订购的一般工具" + deviceTypName + "(" + modelName + ")已经收货登录到系统，管理编号" + manageCode + "，请确认实物后，进行验收。";
			}

			PostMessageEntity postMessageEntity = new PostMessageEntity();
			postMessageEntity.setLevel(2);
			postMessageEntity.setOccur_time(Calendar.getInstance().getTime());
			postMessageEntity.setReason(33);
			postMessageEntity.setSender_id(operatorID);
			postMessageEntity.setContent(content);

			// 建立推送信息
			postMessageMapper.createPostMessage(postMessageEntity);
			String postMessageId = commonMapper.getLastInsertID();
			
			postMessageEntity.setPost_message_id(postMessageId);
			postMessageEntity.setReceiver_id(applicatorId);
			// 建立推送信息接收人
			postMessageMapper.createPostMessageSendation(postMessageEntity);
			
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.commit();
				
				Map<String, MessageInbound> bMap = BoundMaps.getMessageBoundMap();
				MessageInbound mInbound = bMap.get(applicatorId); 
				if (mInbound != null && mInbound instanceof OperatorMessageInbound) 
					((OperatorMessageInbound)mInbound).newMessage();
				
				log.info("Committed！");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.rollback();
				log.info("Rolled back！");
			}
		} finally {
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.close();
			}
			conn = null;
		}
	}

	public static SqlSessionManager getTempWritableConn() {
		log.info("new Connnection");
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return SqlSessionManager.newInstance(factory);
	}

}
