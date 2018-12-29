package com.osh.rvs.servlet;

import static framework.huiqing.common.util.CommonStringUtil.fillChar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;

import com.osh.rvs.common.MailUtils;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.BoundMaps;
import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.inbound.OperatorMessageInbound;
import com.osh.rvs.inbound.PositionPanelInbound;
import com.osh.rvs.job.DailyKpiJob;
import com.osh.rvs.job.DailyWorkSheetsJob;
import com.osh.rvs.job.PositionStandardTimeQueue;
import com.osh.rvs.mapper.push.PositionMapper;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.ProductionFeatureService;
import com.osh.rvs.service.TriggerPositionService;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;

public class TriggerServlet extends HttpServlet {

	private static final long serialVersionUID = -3163557381361759907L;

	Logger log = Logger.getLogger("TriggerServlet");

	/** 进入等待区 */
	private static final String METHOD_IN = "in";
	/** 工位启动作业 */
	private static final String METHOD_WORK = "work";
	/** 工位完成作业 */
	private static final String METHOD_FINISH = "finish";
	/** 工位暂停 */
	private static final String METHOD_PAUSE = "pause";
	/** 中断再开(成为暂停) */
	private static final String METHOD_RESUME = "resume";
	/** 计划中断 */
	private static final String METHOD_BREAK = "break";
	/** 品保返回 */
	private static final String METHOD_FORBID = "forbid";
	/** 检测投线过期 */
	private static final String METHOD_LATEINLINE = "lateinline";
	/** 工位临时报表 */
	private static final String METHOD_POSITION_REPORT = "preport";
	/** 点检中断通知到线长 */
	private static final String METHOD_BREAK_TO_TEC = "breakToTec";
	/** 分配通箱库位 */
	private static final String METHOD_ASSIGN_TC_SPACE = "assign_tc_space";

	private static final String POSITION_601 = "00000000051";
	/** 推送信息 */
	private static final String METHOD_POST_MESSAGE = "postMessage";
	
	/** 消耗品订购 **/
	private static final String METHOD_CONSUMABLE_ORDER = "consumableOrder";
	
	/**一周KPI**/
	private static final String METHOD_WEEKLY_KPI = "weeklykpi";

	/** 更新配置文件 **/
	private static final String METHOD_UPDATE_PROPERTIES = "prop";

	/** 开始工位标准工时警报计时 **/
	private static final String METHOD_START_ALARM_CLOCK_QUEUE = "start_alarm_clock_queue";
	/** 取消工位标准工时警报计时 **/
	private static final String METHOD_STOP_ALARM_CLOCK_QUEUE = "stop_alarm_clock_queue";

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse arg1) throws ServletException, IOException {
		String uri = req.getRequestURI();
		uri = uri.replaceFirst(req.getContextPath(), "");
		uri = uri.replaceFirst(req.getServletPath(), "");
		String addr = req.getRemoteAddr();
		log.info("Get finger on :" + uri + " from " + addr);

		// 只有本机可以访问
		if (!"0:0:0:0:0:0:0:1".equals(addr) && !"127.0.0.1".equals(addr)) {
			log.warn("推送只限服务器本机触发");
			return;
		}
		String[] parameters = uri.split("\\/");
		if (parameters.length > 3) {
			String method = parameters[1];
			String target = parameters[2];
			String object = parameters[3];

			String action = "";
			if(parameters.length == 5){
				action = parameters[4];
			}
			
			if (METHOD_IN.equals(method)) {
				// 
				in(parameters);
			} else if (METHOD_WORK.equals(method)) {
				if (target.startsWith(POSITION_601)) {
					String[] ids = target.split("-");
					ProductionFeatureService pfservice = new ProductionFeatureService();
					pfservice.makeQaOverTime(ids[0], ids[1], object);
				}
			} else if (METHOD_FINISH.equals(method)) {
				finish(parameters);
			} else if (METHOD_PAUSE.equals(method)) {
				
			} else if (METHOD_RESUME.equals(method)) {
				
			} else if (METHOD_BREAK.equals(method)) {
				breakPosition(target, object);
			} else if (METHOD_FORBID.equals(method)) {
				//终检返品 
				forbid(target);
			} else if (METHOD_LATEINLINE.equals(method)) {
				// 投线延迟
				checklateinline();
			} else if (METHOD_POSITION_REPORT.equals(method)) {
				// 生成临时工作记录表
				positionReport(target);
			} else if (METHOD_BREAK_TO_TEC.equals(method)) {
				// 点检中断通知设备管理员
				if (parameters.length > 4) {
					sendBreakMail2Dt(target, object, parameters[4]);
				}
			} else if (METHOD_POST_MESSAGE.equals(method)) {
				postMessage(parameters);
			} else if (METHOD_WEEKLY_KPI.equals(method)){
				DailyKpiJob DailyKpiJob = new DailyKpiJob();
				DailyKpiJob.trigger(target, object, action);
			} else if (METHOD_UPDATE_PROPERTIES.equals(method)){
				PathConsts.load();
				RvsUtils.initAll();
			} else if (METHOD_START_ALARM_CLOCK_QUEUE.equals(method)){
				startAlarmClockQueue(parameters);
			} else if (METHOD_STOP_ALARM_CLOCK_QUEUE.equals(method)){
				PositionStandardTimeQueue.stopAlarmClockQueue(target, object);
			}
		}
	}

	private void startAlarmClockQueue(String[] parameters) {
		if (parameters.length < 8) {
			return;
		}
		
		String materialId = parameters[2];
		String positionId = parameters[3];
		String lineId = parameters[4];
		String operatorId = parameters[5];
		String sStandardMinute = parameters[6];
		String sCostMinute = parameters[7];

		Integer iStandardMinute = null;
		Integer iCostMinute = null;
		try {
			iStandardMinute = Integer.parseInt(sStandardMinute);
			iCostMinute = Integer.parseInt(sCostMinute);
		} catch (Exception e) {
			return;
		}

		if ("571".equals(positionId)) return;
		PositionStandardTimeQueue.startAlarmClockQueue(materialId, positionId, lineId, operatorId, iStandardMinute, iCostMinute);
	}

	/**
	 * 信息刷新
	 * @param operator_ids
	 */
	private void postMessage(String... operator_ids) {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
		}
		Map<String, MessageInbound> bMap = BoundMaps.getMessageBoundMap();
		for (String operator_id : operator_ids) {
			MessageInbound mInbound = bMap.get(operator_id); 
			if (mInbound != null && mInbound instanceof OperatorMessageInbound) 
				((OperatorMessageInbound)mInbound).newMessage();
		}
	}

	private void sendBreakMail2Dt(String message, String section_id, String position_id) throws UnsupportedEncodingException {
		// 推送设备管理员发生不合格邮件
		String position = "XXXX";

		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();

		SqlSession conn = factory.openSession(TransactionIsolationLevel.READ_COMMITTED);

		try {
			PositionMapper pMapper = conn.getMapper(PositionMapper.class);
			position = pMapper.getPositionWithSectionByID(section_id, position_id);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}

		// 在"+ position +"发生点检不合格，请确认。
		String subject = "\u5728"+ position +"\u53d1\u751f\u70b9\u68c0\u4e0d\u5408\u683c\uff0c\u8bf7\u786e\u8ba4\u3002";

		// 发生的点检品管理标号为\n
		String mailContent = "\u53d1\u751f\u7684\u70b9\u68c0\u54c1\u7ba1\u7406\u6807\u53f7\u4e3a\n" + message.replaceAll("_n_", "\n");
		Collection<InternetAddress> toIas = new ArrayList<InternetAddress>();
		toIas.add(new InternetAddress("xiaochun_hu@olympus.com.cn", "胡晓春"));
		toIas.add(new InternetAddress("chen_qiu@olympus.com.cn", "邱晨"));

		Collection<InternetAddress> ccIas = new ArrayList<InternetAddress>();

		MailUtils.sendMail(toIas, ccIas, subject, mailContent);
	}

	private void breakPosition(String material_id, String position_id) {
		// TODO Auto-generated method stub
		
	}

	private void positionReport(String position_work) {
		DailyWorkSheetsJob job = DailyWorkSheetsJob.getInstance();
		job.tempMake(position_work);
	}

	private void in(String... parameters) throws IOException {
		String position_id = fillChar(parameters[2], '0', 11, true);
		String section_id = "";
		String material_id = null;
//		boolean isLight = false;
//		List<String> light_assigned_operator_ids = null;

		if (parameters.length > 3)
			section_id = parameters[3];
		if (parameters.length > 4)
			material_id = parameters[4];
		if (parameters.length > 5) {
//			if ("1".equals(parameters[5]))
//				isLight = true;
		}

		// 小修理工位等待处理
//		if (isLight) {
//			SqlSessionManager conn = RvsUtils.getTempWritableConn();
//			try {
//				conn.startManagedSession(false);
//				// 找到所有有操作权限的工作人员
//				OperatorMapper oMapper = conn.getMapper(OperatorMapper.class);
//				List<OperatorEntity> operators = oMapper.getOperatorByPositionForLight(section_id, position_id);
//				
//				// 选出适合的工作人员
//				light_assigned_operator_ids = new ArrayList<String>();
//				String light_assigned_operator_id = null;
//				boolean selectFinished = false;
//				boolean selectFree = false;
//				Date lastDate = new Date(Long.MAX_VALUE);
//				for (OperatorEntity operator : operators) {
//					light_assigned_operator_ids.add(operator.getOperator_id());
//					// 如果专门人员有空闲则指派
//					if (operator.getFix_response() == 1 && operator.getAction_time() == null) {
//						light_assigned_operator_id = operator.getOperator_id();
//						selectFinished = true;
//					} else
//
//					// 如果其他人员有空闲则指派
//					if (!selectFinished && operator.getAction_time() == null) {
//						light_assigned_operator_id = operator.getOperator_id();
//						selectFree = true;
//					} else
//
//					// 如果没有人空闲，选最早开始作业的人员
//					if (!selectFinished && !selectFree) {
//						if (operator.getAction_time().before(lastDate)) {
//							lastDate = operator.getAction_time();
//							light_assigned_operator_id = operator.getOperator_id();
//						}
//					}
//				}
//
//				// 建立小修理等待记录
//				ProductionAssignMapper paMapper = conn.getMapper(ProductionAssignMapper.class);
//				ProductionAssignEntity inst = new ProductionAssignEntity();
//				inst.setAssigned_operator_id(light_assigned_operator_id);
//				inst.setMaterial_id(material_id);
//				inst.setPosition_id(position_id);
//				paMapper.create(inst);
//
//				// 通知线长
//				List<OperatorEntity> leaders = oMapper.getLeadersByPosition(section_id, position_id);
//				for (OperatorEntity leader : leaders) {
//					light_assigned_operator_ids.add(leader.getOperator_id());
//				}
//
//				conn.commit();
//			} catch (Exception e) {
//				log.error(e.getMessage(), e);
//				conn.rollback();
//				light_assigned_operator_ids = null;
//			} finally {
//				if (conn != null && conn.isManagedSessionStarted()) {
//					conn.close();
//				}
//				conn = null;
//			}
//		}

		// 通知使用该工位的页面
		Map<String, MessageInbound> map = BoundMaps.getPositionBoundMap();
		synchronized(map) {
			for (String positionKey : map.keySet()) {
				MessageInbound inbound = map.get(positionKey);
				if (inbound == null) {
					log.warn("对" + positionKey + "的连接不存在了");
				} else {
					((PositionPanelInbound) inbound).refreshWaiting(section_id, position_id);
				}
			}
		}

//		if (light_assigned_operator_ids != null) {
//			map = BoundMaps.getMessageBoundMap();
//			synchronized(map) {
//				for (String operatorKey : map.keySet()) {
//					if (light_assigned_operator_ids.contains(operatorKey)) {
//						MessageInbound inbound = map.get(operatorKey);
//						if (inbound == null) {
//							log.warn("对" + operatorKey + "的连接不存在了");
//						} else {
//							((OperatorMessageInbound) inbound).refreshLightWaiting();
//						}
//					}
//				}
//			}
//		}

		// 仕挂量检查
		TriggerPositionService service = new TriggerPositionService();

		service.checkOverLine(position_id, section_id, material_id);
	}

	private void finish(String... parameters) throws IOException {
		String position_id = parameters[2];
		String section_id = "";
		if (parameters.length > 3)
			section_id = parameters[3];

		// 通知使用该工位的页面
		Map<String, MessageInbound> map = BoundMaps.getPositionBoundMap();
		synchronized(map) {
			for (String positionKey : map.keySet()) {
				MessageInbound inbound = map.get(positionKey);
				if (inbound == null) {
					log.warn("对" + positionKey + "的连接不存在了");
				} else {
					((PositionPanelInbound) inbound).refreshWaiting(section_id, position_id);
				}
			}
		}
	}

	private void forbid(String material_id) throws IOException {

		// 推送终检返品邮件
		String subject = PathConsts.MAIL_CONFIG.getProperty("forbid.qa.title");

		MaterialService service = new MaterialService();

		MaterialEntity bean = service.getMaterial(material_id);
		String mailContent = RvsUtils.getProperty(PathConsts.MAIL_CONFIG, "forbid.qa.content", bean.getSorc_no());

		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();

		SqlSession conn = factory.openSession(TransactionIsolationLevel.READ_COMMITTED);

		try {
			Collection<InternetAddress> toIas = RvsUtils.getMailIas("forbid.qa.to", conn, null, null);
			Collection<InternetAddress> ccIas = RvsUtils.getMailIas("forbid.qa.cc", conn, null, null);

			MailUtils.sendMail(toIas, ccIas, subject, mailContent);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
	}

	private void checklateinline() {
		// 检测
		MaterialService service = new MaterialService();
		service.checkInlineService();

//		String position_id = "00000000053"; // TODO
//		String section_id = "";
//
//		// 通知使用该工位的页面
//		Map<String, MessageInbound> map = BoundMaps.getPositionBoundMap();
//		synchronized(map) {
//			for (String positionKey : map.keySet()) {
//				MessageInbound inbound = map.get(positionKey);
//				if (inbound == null) {
//					log.warn("对" + positionKey + "的连接不存在了");
//				} else {
//					((PositionPanelInbound) inbound).refreshWaiting(section_id, position_id);
//				}
//			}
//		}
	}

}