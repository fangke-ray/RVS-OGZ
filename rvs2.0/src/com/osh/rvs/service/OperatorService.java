package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.OperatorEntity;
import com.osh.rvs.bean.master.OperatorNamedEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.bean.master.RoleEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.master.OperatorForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.master.OperatorMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.CryptTool;
import framework.huiqing.common.util.message.ApplicationMessage;

public class OperatorService {

	/**
	 * 检索记录列表
	 * 
	 * @param form
	 *            提交表单
	 * @param conn
	 *            数据库连接
	 * @param privacy_id
	 *            拥有权限
	 * @param errors
	 *            错误内容列表
	 * @return List<OperatorForm> 查询结果表单
	 */
	public List<OperatorForm> search(ActionForm form, SqlSession conn, List<MsgInfo> errors) {

		// 表单复制到数据对象
		OperatorEntity conditionBean = new OperatorEntity();
		BeanUtil.copyToBean(form, conditionBean, null);

		// 从数据库中查询记录
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		List<OperatorNamedEntity> lResultBean = dao.searchOperator(conditionBean);

		// 建立页面返回表单
		List<OperatorForm> lResultForm = new ArrayList<OperatorForm>();

		// 数据对象复制到表单
		BeanUtil.copyToFormList(lResultBean, lResultForm, null, OperatorForm.class);

		return lResultForm;
	}

	/**
	 * 按照主键检索单条记录用于编辑
	 * 
	 * @param form
	 *            提交表单
	 * @param conn
	 *            数据库连接
	 * @param errors
	 *            错误内容列表
	 * @return OperatorForm 查询结果表单
	 */
	public OperatorForm getShowedit(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		// 表单复制到数据对象
		OperatorEntity coditionBean = new OperatorEntity();
		BeanUtil.copyToBean(form, coditionBean, null);
		String operator_id = coditionBean.getOperator_id();

		// 从数据库中查询记录
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		OperatorEntity resultBean = dao.getOperatorByID(operator_id);

		if (resultBean == null) {
			// 检索不到的情况下
			MsgInfo error = new MsgInfo();
			error.setComponentid("operator_id");
			error.setErrcode("dbaccess.recordNotExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", "用户"));
			errors.add(error);
			return null;
		} else {
			// 建立页面返回表单
			OperatorForm resultForm = new OperatorForm();

			// 数据对象复制到表单
			BeanUtil.copyToForm(resultBean, resultForm, null);

			// 取得可选工位
			List<String> pResultBeans = dao.getPositionsOfOperator(operator_id);
			resultForm.setAbilities(pResultBeans);

			// 取得兼任权限
			List<String> rResultBeans = dao.getRolesOfOperator(operator_id);
			resultForm.setTemp_role(rResultBeans);

			return resultForm;
		}
	}

	/**
	 * 按照主键检索单条记录
	 * 
	 * @param form
	 *            提交表单
	 * @param conn
	 *            数据库连接
	 * @param errors
	 *            错误内容列表
	 * @return OperatorForm 查询结果表单
	 */
	public OperatorForm getDetail(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		// 表单复制到数据对象
		OperatorEntity coditionBean = new OperatorEntity();
		BeanUtil.copyToBean(form, coditionBean, null);
		String operator_id = coditionBean.getOperator_id();

		// 从数据库中查询记录
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		OperatorEntity resultBean = dao.getOperatorNamedByID(operator_id);

		if (resultBean == null) {
			// 检索不到的情况下
			MsgInfo error = new MsgInfo();
			error.setComponentid("operator_id");
			error.setErrcode("dbaccess.recordNotExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", "用户"));
			errors.add(error);
			return null;
		} else {
			// 建立页面返回表单
			OperatorForm resultForm = new OperatorForm();

			// 数据对象复制到表单
			BeanUtil.copyToForm(resultBean, resultForm, null);

			List<String> pResultBeans = dao.getPositionsOfOperator(operator_id);

			resultForm.setAbilities(pResultBeans);

			return resultForm;
		}
	}

	/**
	 * 标准检查以外的合法性检查
	 * 
	 * @param operatorForm
	 *            表单
	 * @param errors
	 *            错误内容列表
	 */
	public void customValidate(OperatorForm operatorForm, SqlSession conn, List<MsgInfo> errors) {
		// 用户角色是操作工的情况下,必须至少选择一项技能(工位) TODO 按权限
		if ("00000000006".equals(operatorForm.getRole_id()) && CommonStringUtil.isEmpty(operatorForm.getPosition_id())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("position_id");
			error.setErrcode("validator.required");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "主要工位"));
			errors.add(error);
		}
		// 用户角色是线长的情况下,必须选择课室和工程
		if ("00000000005".equals(operatorForm.getRole_id()) && CommonStringUtil.isEmpty(operatorForm.getSection_id())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("section_id");
			error.setErrcode("validator.required");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "课室"));
			errors.add(error);
		}
		if ("00000000005".equals(operatorForm.getRole_id()) && CommonStringUtil.isEmpty(operatorForm.getLine_id())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("line_id");
			error.setErrcode("validator.required");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "工程"));
			errors.add(error);
		}
		// 工号不可重复
		if (errors.size() == 0 && CommonStringUtil.isEmpty(operatorForm.getId())) {
			OperatorMapper dao = conn.getMapper(OperatorMapper.class);
			// 表单复制到数据对象
			OperatorEntity conditionBean = new OperatorEntity();
			BeanUtil.copyToBean(operatorForm, conditionBean, (new CopyOptions()).include("job_no"));
			List<OperatorNamedEntity> resultBean = dao.searchOperator(conditionBean);
			if (resultBean != null && resultBean.size() > 0) {
				MsgInfo error = new MsgInfo();
				error.setComponentid("job_no");
				error.setErrcode("dbaccess.columnNotUnique");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", "工号",
						conditionBean.getJob_no(), "用户"));
				errors.add(error);
			}
		}

	}

	/**
	 * 执行插入
	 * 
	 * @param form
	 *            提交表单
	 * @param session
	 *            当前用户会话
	 * @param conn
	 *            数据库连接
	 * @param errors
	 *            错误内容列表
	 * @throws Exception
	 */
	public void insert(OperatorForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors)
			throws Exception {
		// 新建自动生成密码
		form.setPwd("0011a$Df");

		// 表单复制到数据对象
		OperatorEntity insertBean = new OperatorEntity();
		BeanUtil.copyToBean(form, insertBean, CopyOptions.COPYOPTIONS_NOEMPTY);
		insertBean.setPwd(CryptTool.encrypttoStr(insertBean.getPwd() + insertBean.getJob_no().toUpperCase()));

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		insertBean.setUpdated_by(user.getOperator_id());

		// 新建记录插入到数据库中
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		dao.insertOperator(insertBean);

		// 取得刚才插入的主键
		CommonMapper commonMapper = conn.getMapper(CommonMapper.class);
		insertBean.setOperator_id(commonMapper.getLastInsertID());

		// 拥有权限关系插入到数据库中
		List<String> insertAbilities = form.getAbilities();
		for (String sPosition_id : insertAbilities) {
			dao.insertPositionOfOperator(insertBean.getOperator_id(), sPosition_id);
		}

		// 兼任角色关系插入到数据库中
		List<String> temproles = form.getTemp_role();
		for (String role_id : temproles) {
			dao.insertRoleOfOperator(insertBean.getOperator_id(), role_id, "9999/12/31");
		}
	}

	/**
	 * 执行更新
	 * 
	 * @param form
	 *            提交表单
	 * @param session
	 *            当前用户会话
	 * @param conn
	 *            数据库连接
	 * @param errors
	 *            错误内容列表
	 * @throws Exception
	 */
	public void update(OperatorForm operatorForm, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors)
			throws Exception {
		// 表单复制到数据对象
		OperatorEntity updateBean = new OperatorEntity();
		BeanUtil.copyToBean(operatorForm, updateBean, CopyOptions.COPYOPTIONS_NOEMPTY);

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		updateBean.setUpdated_by(user.getOperator_id());

		// 更新数据库中记录
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		dao.updateOperator(updateBean);

		// 删除原有权限关系
		dao.deletePositionOfOperator(updateBean.getOperator_id());

		// 拥有权限关系插入到数据库中
		List<String> updateAbilities = operatorForm.getAbilities();
		for (String sPosition_id : updateAbilities) {
			dao.insertPositionOfOperator(updateBean.getOperator_id(), sPosition_id);
		}

		// 删除原有兼任角色关系
		dao.deleteRoleOfOperator(updateBean.getOperator_id());

		// 兼任角色关系插入到数据库中
		List<String> temproles = operatorForm.getTemp_role();
		for (String role_id : temproles) {
			dao.insertRoleOfOperator(updateBean.getOperator_id(), role_id, "9999/12/31");
		}
	}

	/**
	 * 执行逻辑删除
	 * 
	 * @param form
	 *            提交表单
	 * @param session
	 *            当前用户会话
	 * @param conn
	 *            数据库连接
	 * @param errors
	 *            错误内容列表
	 * @throws Exception
	 */
	public void delete(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors)
			throws Exception {
		// 表单复制到数据对象
		OperatorEntity deleteBean = new OperatorEntity();
		BeanUtil.copyToBean(form, deleteBean, null);

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		deleteBean.setUpdated_by(user.getOperator_id());

		// 在数据库中逻辑删除记录
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		dao.deleteOperator(deleteBean);
	}

	public void generatepasswd(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors)
			throws Exception {
		// 表单复制到数据对象
		OperatorEntity updateBean = new OperatorEntity();
		BeanUtil.copyToBean(form, updateBean, CopyOptions.COPYOPTIONS_NOEMPTY);
		updateBean.setPwd(CryptTool.encrypttoStr(updateBean.getPwd() + updateBean.getJob_no().toUpperCase()));

		// 在数据库中更新密码
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		dao.updatePassword(updateBean);
	}

	public Map<String, String> getUserRoles(String operator_id, SqlSession conn) {
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		List<RoleEntity> rResultBeans = dao.getRolesOfOperatorNamed(operator_id);

		Map<String, String> ret = new TreeMap<String, String>();

		for (RoleEntity rResultBean : rResultBeans) {
			ret.put(rResultBean.getRole_id(), rResultBean.getName());
		}
		return ret;
	}

	// 取得全部工位
	public List<PositionEntity> getUserPositions(String operator_id, SqlSession conn) {
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		List<PositionEntity> pResultBeans = dao.getPositionsOfOperatorNamed(operator_id);

		return pResultBeans;
	}

	public String getResolverReferChooser(SqlSession conn) {

		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		List<OperatorNamedEntity> allOperator = dao.getResolver();

		List<String[]> lst = getSetReferChooser(allOperator, false);

		String pReferChooser = CodeListUtils.getReferChooser(lst);

		return pReferChooser;
	}

	// 取得品保担当人
	public String getOptions(SqlSession conn) {
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		String position_id = RvsConsts.POSITION_QA;
		List<OperatorEntity> list = dao.getOperatorWithPosition(position_id);
		Map<String, String> map = new TreeMap<String, String>();

		for (OperatorEntity bean : list) {
			map.put(bean.getOperator_id(), bean.getName());
		}

		return CodeListUtils.getSelectOptions(map, null, "", false);
	}
	
	//取得所有的操作人员
	public String getAllOperatorName(int department, SqlSession conn){

		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		OperatorEntity condition = new OperatorEntity();
		condition.setDepartment(department);
		List<OperatorNamedEntity> allOperator = dao.searchOperator(condition);

		List<String[]> lst = getSetReferChooser(allOperator, true);

		String pReferChooser = CodeListUtils.getReferChooser(lst);

		return pReferChooser;
	}

	//取得所有的操作人员
	public String getAllOperatorName(SqlSession conn){

		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		List<OperatorNamedEntity> allOperator = dao.searchOperator(null);

		List<String[]> lst = getSetReferChooser(allOperator, true);

		String pReferChooser = CodeListUtils.getReferChooser(lst);

		return pReferChooser;
	}

	// 取得所有治具点检人员
	public String getAllJigOperatorName(SqlSession conn) {

		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		List<OperatorNamedEntity> allJigOperator = dao.searchToolsOperator(null);

		List<String[]> lst = getSetReferChooser(allJigOperator, true);

		String pReferChooser = CodeListUtils.getReferChooser(lst);

		return pReferChooser;
	}

	/**
	 * 集中处理人员的选择参照
	 * @param conn
	 * @return
	 */
	public static List<String[]> getSetReferChooser(List<OperatorNamedEntity> result, boolean withLine) {
		List<String[]> lst = new ArrayList<String[]>();

		// 重名处理 TODO

		int arraySize = withLine ? 4 : 3;
		for (OperatorNamedEntity operator : result) {
			String[] p = new String[arraySize];
			p[0] = operator.getOperator_id();
			p[1] = operator.getName() + (operator.isDelete_flg() ? "(停用)" : "");
			p[2] = operator.getRole_name();
			if (withLine) {
				if(CommonStringUtil.isEmpty(operator.getLine_name())){
					p[3] = operator.getJob_no();
				}else{
					p[3] = operator.getLine_name();
				}
			}

			lst.add(p);
		}

		return lst;
	}

	private static Map<String, List<Long>> BruteForceRecord = new HashMap<String, List<Long>>();
	private static Map<String, String> IpRecorder = new HashMap<String, String>();
	public static boolean checkBruteForce(String clientIp) {
		if (!BruteForceRecord.containsKey(clientIp))
			return false;

		synchronized (BruteForceRecord) {
			Long now = (new Date()).getTime();

			List<Long> list = BruteForceRecord.get(clientIp);
			for (int i = list.size() - 1; i >= 0; i--) {
				Long l = list.get(i);
				if (now - l > 3600000l) { // 3600000 1小时
					list.remove(i);
				}
			}
			return (list.size() >= 5);
		}
	}

	public static void recordBruteForce(String clientIp) {
		synchronized (BruteForceRecord) {
			if (!BruteForceRecord.containsKey(clientIp)) {
				BruteForceRecord.put(clientIp, new ArrayList<Long>());
			}

			BruteForceRecord.get(clientIp).add((new Date()).getTime());
		}
	}

	public static void setIpRecorder(String clientIp, String job_no) {
		synchronized (IpRecorder) {
			IpRecorder.put(clientIp, job_no);
		}
	}

	public static Map<String, String> getBruteForceRecordList() {
		Map<String, String> retDict = new TreeMap<String, String>();
		synchronized (BruteForceRecord) {
			for (String clientIp : BruteForceRecord.keySet()) {
				List<Long> list = BruteForceRecord.get(clientIp);
				if (list.size() >= 5) {
					String jobNo = IpRecorder.get(clientIp);
					if (jobNo == null) jobNo = "(无记录)"; 
					retDict.put(clientIp, jobNo);
				}
			}
		}
		return retDict;
	}

	public static void clearBruteForceRecord(String clientIp) {
		synchronized (BruteForceRecord) {
			BruteForceRecord.remove(clientIp);
		}
	}
}
