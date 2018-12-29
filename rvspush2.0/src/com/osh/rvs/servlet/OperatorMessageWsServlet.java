package com.osh.rvs.servlet;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.log4j.Logger;

import com.osh.rvs.inbound.OperatorMessageInbound;

public class OperatorMessageWsServlet extends WebSocketServlet {

	private Logger log = Logger.getLogger("TriggerServlet-OperatorMessage");
	private static final long serialVersionUID = -1759898886649818461L;

	@Override
	protected StreamInbound createWebSocketInbound(String arg0, HttpServletRequest arg1) {
		log.info(arg1.getRemoteAddr());
		return new OperatorMessageInbound();
	}
}
