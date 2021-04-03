package com.osh.rvs.servlet;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

@ServerEndpoint("/position")
public class PositionPanelServletEndPoint {


	// key thisPositionKey
	private static Map<String, PositionPanelServletEndPoint> clients 
		= new ConcurrentHashMap<String, PositionPanelServletEndPoint> ();
	private Session session;
	private String thisSectionId = "";
	private String thisAbsPositionId = "";
	private String thisPositionKey = "";

	private Logger log = Logger.getLogger("TriggerServlet-PositionPanel");

	@OnOpen
	public void onOpen(@PathParam("position") String position, Session session)
			throws IOException {

		this.session = session;

		log.info("已连接");
	}

	@OnClose
	public void onClose() throws IOException {
		if (!"".equals(thisPositionKey)) {
			clients.remove(thisPositionKey);
			log.info(thisPositionKey + " closed.....");
		}
	}

	@OnMessage
	public void onMessage(String s) throws IOException {
		// 通过这里来登记连接页面的positionID
		if (s.startsWith("entach")) {
			putme(s.replaceAll("entach:", ""));
		} else if (s.startsWith("refreshSelf")) {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
			refreshSelfPosition();
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

	private void putme(String positionId) {
		String[] ids = positionId.split("#");
		if (ids.length < 2) {
			return;
		}
		thisPositionKey = this.addPositionSocket(positionId, this);
		thisSectionId = ids[0];
		thisAbsPositionId = ids[1];

		// 连接成功，返回得到的工位标识
		this.session.getAsyncRemote().sendText("{\"method\":\"connectted\", \"id\":\"" + thisPositionKey + "\"}");
		log.info(thisPositionKey + " connectted.....");
	}

	/**
	 * 等待信息需要刷新
	 * @param section_id
	 * @param position_id
	 * @param operator_id 
	 */
	public void refreshWaiting(String section_id, String position_id, String operator_id) {

		if (section_id != null && !"".equals(section_id)) {
			if (!"any".equals(section_id) && !section_id.equals(this.thisSectionId)) {
				return;
			}
		}
		if (!position_id.equals(this.thisAbsPositionId)) {
			return;
		}
		this.session.getAsyncRemote().sendText("{\"method\":\"refreshWaiting\"}");
	}

	private void refreshSelfPosition() {
		for (String endPointKey : clients.keySet()) {
			if (endPointKey.equals(thisPositionKey)) {
				continue;
			}
			PositionPanelServletEndPoint that = clients.get(endPointKey);
			if (!"any".equals(that.thisSectionId) && !that.thisSectionId.equals(this.thisSectionId)) {
				continue;
			}
			if (!that.thisAbsPositionId.equals(this.thisAbsPositionId)) {
				continue;
			}
			that.session.getAsyncRemote().sendText("{\"method\":\"refreshWaiting\"}");
		}
	}

	private String addPositionSocket(String positionId,
			PositionPanelServletEndPoint entity) {
		String addedId = positionId + new Date().getTime();
		clients.put(addedId, entity);
		return addedId;
	}

	public static synchronized Map<String, PositionPanelServletEndPoint> getClients() {
		return clients;
	}
}
