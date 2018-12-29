package com.osh.rvs.inbound;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;

import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.BoundMaps;
import com.osh.rvs.entity.OperatorEntity;
import com.osh.rvs.entity.ProductionAssignEntity;
import com.osh.rvs.mapper.push.OperatorMapper;
import com.osh.rvs.mapper.push.ProductionAssignMapper;

public class OperatorMessageInbound extends MessageInbound {
	private String thisOperatorId = "";
	private String thisRoleId = null;
	private boolean alive = false;
	private JSON json = new JSON();

	private Logger log = Logger.getLogger("TriggerServlet-OperatorMessage");

	@Override
	protected void onOpen(WsOutbound outbound) {
		log.info("anything connect from....");
	}

	@Override
	protected void onClose(int status) {
		// 连接被断开时
		// 从对接池中除去
		BoundMaps.removeMessageBound(thisOperatorId);

		log.info(thisOperatorId + " closed....." + status);
	}

	@Override
	protected void onBinaryMessage(ByteBuffer data) throws IOException {
		log.info("收到前台流：" + data);
	}

	@Override
	protected void onTextMessage(CharBuffer data) throws IOException {

		// 通过这里来登记连接页面的positionID
		log.info("收到前台通信：" + data);
		String s = data.toString();
		if (s.startsWith("entach:")) {
			putme(s.replaceAll("entach:", ""));
		} else if (s.startsWith("pong:")) {
			log.info(thisOperatorId + " get a message pong.");
			alive = true;
		} else if (s.startsWith("callLight:")) {
			refreshLightWaiting();
		} else if (s.startsWith("assignOperator:")) {
			String jsonString = s.replaceAll("assignOperator:", "");
			assignLightWaiting(jsonString);
//		} else {
//			boardcast(s);
		}
	}

	private void assignLightWaiting(String jsonString) {
		@SuppressWarnings("rawtypes")
		Map requestMap = json.parse(jsonString, Map.class);
		String material_id = (String) requestMap.get("material_id");
		String position_id = (String) requestMap.get("position_id");
		String assigned_operator_id = (String) requestMap.get("operator_id");

		SqlSessionManager conn = RvsUtils.getTempWritableConn();
		try {
			conn.startManagedSession(false);
			ProductionAssignMapper mapper = conn.getMapper(ProductionAssignMapper.class);

			ProductionAssignEntity entity = new ProductionAssignEntity();
			entity.setMaterial_id(material_id);
			entity.setPosition_id(position_id);
			entity.setAssigned_operator_id(assigned_operator_id);

			// 线长指定
			mapper.leaderAssign(entity);

			conn.commit();

			// 通知被指定者
			if (BoundMaps.getMessageBoundMap().containsKey(assigned_operator_id)) {
				((OperatorMessageInbound) BoundMaps.getMessageBoundMap().get(assigned_operator_id)).refreshLightWaiting();
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			conn.rollback();
		} finally {
			if (conn != null && conn.isManagedSessionStarted()) {
				conn.close();
			}
			conn = null;
		}
	}

	private void putme(String operatorId) {
		if (operatorId.indexOf("+") >= 0) {
			String[] sp = operatorId.split("\\+");
			operatorId = sp[0];
			thisRoleId = sp[1];
		}
		if (BoundMaps.getMessageBoundMap().containsKey(operatorId)) {
			try {
				OperatorMessageInbound that = (OperatorMessageInbound) BoundMaps.getMessageBoundMap().get(operatorId);
				if (that.isAlive()) {
					// 已经登录，返回要求退出
					CharBuffer buffer = CharBuffer.wrap("{method:\"conrrupted\", id:\"" + thisOperatorId + "\"}");
					this.getWsOutbound().writeTextMessage(buffer);
					log.info(thisOperatorId + " conrrupted to " + operatorId);
					return;
				} else {
					that.getWsOutbound().close(0, null);
					BoundMaps.removeMessageBound(that.thisOperatorId);
				}
			} catch (IOException e) {
				log.error("IOE : " + e.getMessage());
			}
		}
		thisOperatorId = BoundMaps.addMessageSocket(operatorId, this);

		if ("le".equals(thisRoleId)) {
			Map<String, OperatorEntity> leaderMap = BoundMaps.getLeaderMap();
			if (!leaderMap.containsKey(operatorId)) {
				SqlSession conn = RvsUtils.getTempConn();
				OperatorMapper mapper = conn.getMapper(OperatorMapper.class);
	
				// 取得线长信息
				try {
					OperatorEntity oe = mapper.getOperatorByID(operatorId);
	
					// 线长取得所属员工信息
					OperatorEntity condition = new OperatorEntity();
					condition.setLine_id(oe.getLine_id());
					condition.setSection_id(oe.getSection_id());
					condition.setWork_count_flg(1);
					oe.setBelongs(mapper.searchOperator(condition));
	
					leaderMap.put(operatorId, oe);
				} catch (Exception e) {
					log.error("取得线长信息 : " + e.getMessage(), e);
				} finally {
					conn.close();
					conn = null;
				}
			}
		}

		try {
			// 连接成功，返回得到的工位标识
			Map<String, Object> retMap = new HashMap<String, Object>();
			retMap.put("method", "connectted");
			retMap.put("id", thisOperatorId);
			if ("le".equals(thisRoleId)) {
				retMap.put("belongs", BoundMaps.getLeaderMap().get(operatorId).getBelongs());
			}
			JSON json = new JSON();
			CharBuffer buffer = CharBuffer.wrap(json.format(retMap));
			this.getWsOutbound().writeTextMessage(buffer);
			log.info(thisOperatorId + " connectted.....");
		} catch (IOException e) {
			log.error("IOE : " + e.getMessage());
		}
	}

	public void newMessage() {
		try {
			// 该操作者需要更新信息
			CharBuffer buffer = CharBuffer.wrap("{method:\"message\", id:\"" + thisOperatorId + "\"}");
			this.getWsOutbound().writeTextMessage(buffer);
			log.info(thisOperatorId + " message updated.....");
		} catch (IOException e) {
			log.error("IOE : " + e.getMessage());
		}
	}

	private boolean isAlive() {
		try {
			alive = false;
			log.info(thisOperatorId + " send a message ping.");
			// 已经登录，返回要求退出
			CharBuffer buffer = CharBuffer.wrap("{method:\"ping\", id:\"" + thisOperatorId + "\"}");
			this.getWsOutbound().writeTextMessage(buffer);
		} catch (IOException e) {
			log.error("IOE : " + e.getMessage());
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		return alive;
	}

	public void refreshLightWaiting() {
		SqlSession conn = RvsUtils.getTempConn();
		ProductionAssignMapper mapper = conn.getMapper(ProductionAssignMapper.class);

		List<ProductionAssignEntity> list = null;
		// 取得本人相关小修理信息
		try {
			// 操作人员
			if ("op".equals(thisRoleId)) {
				list = mapper.getProductionAssignByOperator(thisOperatorId);
			}
			// 线长
			else if ("le".equals(thisRoleId)) {
				OperatorEntity entity = BoundMaps.getLeaderMap().get(thisOperatorId);
				list = mapper.getProductionAssignByLine(entity.getSection_id(), entity.getLine_id());
			}
		} catch (Exception e) {
			log.error("取得本人相关小修理信息 : " + e.getMessage(), e);
		} finally {
			conn.close();
			conn = null;
		}

		if (list == null) return;

		try {
			Map<String, Object> retMap = new HashMap<String, Object>();
			retMap.put("method", "light");
			retMap.put("list", list);
			// 该操作者需要更新信息
			CharBuffer buffer = CharBuffer.wrap(json.format(retMap));
			this.getWsOutbound().writeTextMessage(buffer);
			log.info(thisOperatorId + " light updated.....");
		} catch (IOException e) {
			log.error("IOE : " + e.getMessage());
		}
	}
}
