package com.osh.rvs.mapper.push;

import java.util.List;

import com.osh.rvs.entity.ConsumableOrderEntity;

/**
 * 
* @Title ConsumableOrderMapper.java
* @Project rvspush
* @Package com.osh.rvs.mapper.push
* @ClassName: ConsumableOrderMapper 
* @Description: 订购单
* @author lxb
* @date 2015-5-14 上午10:33:21
 */
public interface ConsumableOrderMapper {
	/**
	 * 查询需要订购的消耗品
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public List<ConsumableOrderEntity> searchConsumableManage(ConsumableOrderEntity entity) throws Exception;

	/**
	 * 查询最大消耗品订购单编号
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public String getMaxOrderNo(ConsumableOrderEntity entity) throws Exception;

	/**
	 * 新建消耗品订购单
	 * @param entity
	 * @throws Exception
	 */
	public void insertConsumableOrder(ConsumableOrderEntity entity) throws Exception;
	
	/**
	 * 新建消耗品订购单明细
	 * @param entity
	 * @throws Exception
	 */
	public void insertConsumableOrderDetail(ConsumableOrderEntity entity)throws Exception;
}
