package com.osh.rvs.mapper.master;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.OperatorEntity;
import com.osh.rvs.bean.master.OperatorNamedEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.bean.master.RoleEntity;

public interface OperatorMapper {

	/** insert single */
	public int insertOperator(OperatorEntity operator) throws Exception;

	public int updateOperator(OperatorEntity operator) throws Exception;

	public int updatePassword(OperatorEntity operator) throws Exception;

	/** search all */
	public List<OperatorEntity> getAllOperator();

	public List<OperatorNamedEntity> getAllOperatorNamed();

	public OperatorEntity getOperatorByID(String operator_id);

	public OperatorEntity getOperatorNamedByID(String operator_id);

	/**
	 * search
	 * 
	 * @param privacy_id
	 */
	public List<OperatorNamedEntity> searchOperator(OperatorEntity operator);

	public void deleteOperator(OperatorEntity updateBean) throws Exception;

	/** 得到某用户的全部临时角色 */
	public List<String> getRolesOfOperator(String operator_id);

	public List<RoleEntity> getRolesOfOperatorNamed(String operator_id);

	/**
	 * 插入某用户的临时角色
	 * 
	 * @param expiration
	 */
	public void insertRoleOfOperator(@Param("operator_id") String operator_id,
			@Param("role_id") String role_id,
			@Param("expiration") String expiration) throws Exception;

	/** 更新某用户的临时角色 */
	public void updateRoleOfOperator(@Param("operator_id") String operator_id,
			@Param("role_id") String role_id,
			@Param("expiration") String expiration) throws Exception;

	/** 删除某用户的临时角色 */
	public void deleteRoleOfOperator(@Param("operator_id") String operator_id)
			throws Exception;

	/** 得到某用户的全部技能 */
	public List<String> getPositionsOfOperator(String operator_id);

	public List<PositionEntity> getPositionsOfOperatorNamed(String operator_id);

	/** 插入某用户的技能 */
	public void insertPositionOfOperator(
			@Param("operator_id") String operator_id,
			@Param("position_id") String position_id) throws Exception;

	/** 删除某用户的技能 */
	public void deletePositionOfOperator(
			@Param("operator_id") String operator_id) throws Exception;

	public LoginData searchLoginOperator(OperatorEntity conditionBean);

	/** 取得警报的接收者 **/
	public List<OperatorNamedEntity> getResolver();

	public Date getPwdDate(String operator_id);

	/**取得有权限的担当人 TODO getOperatorWithPrivacy **/
	public List<OperatorNamedEntity> getOperatorWithPrivacy(@Param("privacy_id") Integer privacy_id,
			@Param("department") Integer department);

	public List<OperatorEntity> getOperatorWithRole(String role_id);
	
	public List<OperatorEntity> getOperatorWithPosition(String position_id);
	
	//查询所有治具点检者
	public List<OperatorNamedEntity> searchToolsOperator(OperatorEntity operator);
}
