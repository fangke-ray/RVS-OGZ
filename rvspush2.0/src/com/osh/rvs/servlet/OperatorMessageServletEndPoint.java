package com.osh.rvs.servlet;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.BoundMaps;
import com.osh.rvs.entity.OperatorEntity;
import com.osh.rvs.mapper.push.OperatorMapper;

@ServerEndpoint("/operator")
public class OperatorMessageServletEndPoint {


	// 外层key thisOperatorId， 内层key thisOperatorId+date = thisId
	private static Map<String, Map<String, OperatorMessageServletEndPoint>> clients 
		= new ConcurrentHashMap<String, Map<String, OperatorMessageServletEndPoint>>();
	private Session session;
	private String thisId = "";
	private String thisOperatorId = "";
	private String thisRoleId = null;
	private boolean alive = false;

	private Logger log = Logger.getLogger("TriggerServlet-OperatorMessage");

	@OnOpen
	public void onOpen(@PathParam("username") String username, Session session)
			throws IOException {

		this.session = session;

		log.info("已连接");
	}

	@OnClose
	public void onClose() throws IOException {
		if (clients.containsKey(thisOperatorId)) {
			clients.get(thisOperatorId).remove(thisId);
			log.info(thisId + " closed.....");
		}
	}

	@OnMessage
	public void onMessage(String s) throws IOException {
		log.info("收到前台通信：" + s);
		if (s.startsWith("entach:")) {
			putme(s.replaceAll("entach:", ""));
		} else if (s.startsWith("pong:")) {
			log.info(thisId + " get a message pong.");
			alive = true;
		}
	}

	@OnError
	public void onError(Session session, Throwable error) {
		if (error instanceof SocketTimeoutException) {
			log.error("Remote Closed..." + error.getMessage());
		} else {
			log.error(error.getMessage(), error);
		}
	}

	private void putme(String operatorId) {
		if (operatorId.indexOf("+") >= 0) {
			String[] sp = operatorId.split("\\+");
			operatorId = sp[0];
			thisRoleId = sp[1];
		}
		if (clients.containsKey(operatorId)) {
			try {
				Map<String, OperatorMessageServletEndPoint> operatorMap = clients.get(operatorId);
				if (operatorMap != null && !operatorMap.isEmpty()) {
					for (String addedId : operatorMap.keySet()) {
						OperatorMessageServletEndPoint that = operatorMap.get(addedId);
						if (that.isAlive()) {
							// 已经登录，返回要求退出
							this.session.getAsyncRemote().sendText("{method:\"conrrupted\", id:\"" + thisOperatorId + "\"}");
							log.info(thisId + " conrrupted to " + that.thisId);
							return;
						} else {
							that.session.close();
							clients.get(that.thisOperatorId).remove(that.thisId);
						}
					}
				}
			} catch (IOException e) {
				log.error("IOE : " + e.getMessage());
			}
		}
		thisOperatorId = operatorId;
		thisId = addMessageSocket(operatorId, this);

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

		// 连接成功，返回得到的工位标识
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("method", "connectted");
		retMap.put("id", thisOperatorId);
		if ("le".equals(thisRoleId)) {
			retMap.put("belongs", BoundMaps.getLeaderMap().get(operatorId).getBelongs());
		}
		JSON json = new JSON();
		json.setSuppressNull(true);
		this.session.getAsyncRemote().sendText(json.format(retMap));
		log.info(thisOperatorId + " connectted.....");
	}

	public void newMessage() {
		// 该操作者需要更新信息
		this.session.getAsyncRemote().sendText("{method:\"message\", id:\"" + thisOperatorId + "\"}");
		log.info(thisOperatorId + " message updated.....");
	}

	private boolean isAlive() {
		alive = false;
		this.session.getAsyncRemote().sendText("{method:\"ping\", id:\"" + thisOperatorId + "\"}");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		return alive;
	}

	private String addMessageSocket(String operatorId,
			OperatorMessageServletEndPoint entity) {
		String addedId = operatorId + new Date().getTime();
		if (!clients.containsKey(operatorId)) {
			clients.put(operatorId, new HashMap<String, OperatorMessageServletEndPoint>());
		}
		clients.get(operatorId).put(addedId, entity);
		return addedId;
	}

	public static synchronized Map<String, Map<String, OperatorMessageServletEndPoint>> getClients() {
		return clients;
	}
}
