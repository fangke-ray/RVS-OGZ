package com.osh.rvs.entity;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.websocket.MessageInbound;

public class BoundMaps {

	private static Map<String, MessageInbound> positionBoundMap;
//	private static Map<String, MessageInbound> lineSocketMap;
	private static Map<String, MessageInbound> messageSocketMap;

	private static Map<String, OperatorEntity> leaderMap;

	public static synchronized Map<String, MessageInbound> getPositionBoundMap() {
		return positionBoundMap;
	}

	public static synchronized String addPositionSocket(String key, MessageInbound entity) {
		if (!positionBoundMap.containsKey(key)) {
			positionBoundMap.put(key, entity);
		} else {
			key += new Date().getTime();
			positionBoundMap.put(key, entity);
		}
		return key;
	}

	public static synchronized void removePositionBound(String key) {
		if (positionBoundMap.containsKey(key)) {
			positionBoundMap.remove(key);
		}
	}

	public static synchronized Map<String, MessageInbound> getMessageBoundMap() {
		return messageSocketMap;
	}

	public static synchronized String addMessageSocket(String key, MessageInbound entity) {
		if (!messageSocketMap.containsKey(key)) {
			messageSocketMap.put(key, entity);
		} else {
			key += new Date().getTime();
			messageSocketMap.put(key, entity);
		}
		return key;
	}

	public static synchronized void removeMessageBound(String key) {
		if (messageSocketMap.containsKey(key)) {
			messageSocketMap.remove(key);
		}
	}

	public static void init() {
		positionBoundMap = Collections.synchronizedMap(new HashMap<String, MessageInbound>());
//		lineSocketMap = Collections.synchronizedMap(new HashMap<String, MessageInbound>());
		messageSocketMap = Collections.synchronizedMap(new HashMap<String, MessageInbound>());
		leaderMap = Collections.synchronizedMap(new HashMap<String, OperatorEntity>());
	}

	public static Map<String, OperatorEntity> getLeaderMap() {
		return leaderMap;
	}
}
