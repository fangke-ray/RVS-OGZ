package com.osh.rvs.inbound;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.log4j.Logger;

import com.osh.rvs.entity.BoundMaps;

public class PositionPanelInbound extends MessageInbound {
	private String thisSectionId = "";
	private String thisAbsPositionId = "";
	private String thisPositionId = "";

	Logger log = Logger.getLogger("TriggerServlet");

	@Override
	protected void onOpen(WsOutbound outbound) {
		log.info("anything connect....");
	}

	@Override
	protected void onClose(int status) {
		// 连接被断开时
		if (!"".equals(thisPositionId)) {
			// 从对接池中除去
			BoundMaps.removePositionBound(thisPositionId);
		}
		log.info(thisPositionId + " closed.....");
	}

	@Override
	protected void onBinaryMessage(ByteBuffer arg0) throws IOException {
	}

	@Override
	protected void onTextMessage(CharBuffer data) throws IOException {

		// 通过这里来登记连接页面的positionID
		log.info("收到前台通信：" + data);
		String s = data.toString();
		if (s.startsWith("entach")) {
			putme(s.replaceAll("entach:", ""));
		}
	}

	private void putme(String positionId) {
		String[] ids = positionId.split("#");
		if (ids.length < 2) {
			return;
		}
		thisPositionId = BoundMaps.addPositionSocket(positionId, this);
		thisSectionId = ids[0];
		thisAbsPositionId = ids[1];
		try {
			// 连接成功，返回得到的工位标识
			CharBuffer buffer = CharBuffer.wrap("{\"method\":\"connectted\", \"id\":\"" + thisPositionId + "\"}");
			this.getWsOutbound().writeTextMessage(buffer);
			log.info(thisPositionId + " connectted.....");
		} catch (IOException e) {
			log.error("IOE : " + e.getMessage());
		}
	}

	/**
	 * 等待信息需要刷新
	 * @param section_id
	 * @param position_id
	 */
	public void refreshWaiting(String section_id, String position_id) {

		try {
			if (section_id != null && !"".equals(section_id)) {
				if (!"any".equals(section_id) && !section_id.equals(this.thisSectionId)) {
					return;
				}
			}
			if (!position_id.equals(this.thisAbsPositionId)) {
				return;
			}
			this.getWsOutbound().writeTextMessage(CharBuffer.wrap("{\"method\":\"refreshWaiting\"}"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
