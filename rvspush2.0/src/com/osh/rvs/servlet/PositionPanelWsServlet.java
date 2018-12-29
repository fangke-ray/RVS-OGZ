package com.osh.rvs.servlet;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;

import com.osh.rvs.inbound.PositionPanelInbound;

public class PositionPanelWsServlet extends WebSocketServlet {

	private static final long serialVersionUID = -6484496836432213216L;

	@Override
	protected StreamInbound createWebSocketInbound(String arg0, HttpServletRequest arg1) {
		return new PositionPanelInbound();
	}
}
