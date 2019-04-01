package com.osh.rvs.mapper.push;

import com.osh.rvs.entity.PostMessageEntity;

public interface PostMessageMapper {
	/**
	 * 建立推送信息
	 * 
	 * @param entity
	 * @throws Exception
	 */
	public void createPostMessage(PostMessageEntity entity);

	/**
	 * 建立推送信息接收人
	 * 
	 * @param sendation
	 * @throws Exception
	 */
	public void createPostMessageSendation(PostMessageEntity sendation);
}
