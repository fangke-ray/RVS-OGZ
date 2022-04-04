package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.common.ReverseResolution;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.master.PositionForm;
import com.osh.rvs.mapper.master.PositionMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.message.ApplicationMessage;

public class PositionService {

	private static Set<String> dividePositions = null;
	private static Map<String, PositionEntity> positionEntityCache = new HashMap<String, PositionEntity>();
	private static Map<String, String> specialPagePositions = null;

	public static void clearCaches() {
		dividePositions = null;
		specialPagePositions = null;
		positionEntityCache.clear();
		ReverseResolution.positionRever.clear();
		S1PASSES = null;
		noBreakPositions = null;
	}

	/** S1等级时越过的工位 */
	private static String[] S1PASSES = null;
	private static Set<String> noBreakPositions = null;

	static {
		resetS1Passes();
	}

	private static void resetS1Passes(){
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		SqlSession conn = factory.openSession(TransactionIsolationLevel.READ_COMMITTED);

		try {
			PositionMapper dao = conn.getMapper(PositionMapper.class);

			List<PositionEntity> inlineKindPositions = dao.getInlineKindPositions();
			List<String> position_ids = new ArrayList<String>();
			for (PositionEntity inlineKindPosition : inlineKindPositions) {
				if ("s1_pass".equals(inlineKindPosition.getKind())) {
					position_ids.add(inlineKindPosition.getPosition_id());
				}
			}
			S1PASSES = position_ids.toArray(new String[position_ids.size()]);
		} catch (Exception e) {
			// logger.error("错误的s1pass配置：" + e.getMessage());
			S1PASSES = new String[0];
		} finally {
			conn.close();
			conn = null;
		}
	}
	public static String[] getS1PASSES() {
		if (S1PASSES == null)
			resetS1Passes();
		return S1PASSES;
	}

	/**
	 * 检索记录列表
	 * @param form 提交表单
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @return List<PositionForm> 查询结果表单
	 */
	public List<PositionForm> search(ActionForm form, SqlSession conn, List<MsgInfo> errors) {

		// 表单复制到数据对象
		PositionEntity conditionBean = new PositionEntity();
		BeanUtil.copyToBean(form, conditionBean, null);

		// 从数据库中查询记录
		PositionMapper dao = conn.getMapper(PositionMapper.class);
		List<PositionEntity> lResultBean = dao.searchPosition(conditionBean);

		// 建立页面返回表单
		List<PositionForm> lResultForm = new ArrayList<PositionForm>();

		// 数据对象复制到表单
		BeanUtil.copyToFormList(lResultBean, lResultForm, null, PositionForm.class);

		return lResultForm;
	}

	/**
	 * 按照主键检索单条记录
	 * @param form 提交表单
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @return PositionForm 查询结果表单
	 */
	public PositionForm getDetail(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		// 表单复制到数据对象
		PositionEntity coditionBean = new PositionEntity();
		BeanUtil.copyToBean(form, coditionBean, null);
		String position_id = coditionBean.getPosition_id();

		// 从数据库中查询记录
		PositionMapper dao = conn.getMapper(PositionMapper.class);
		PositionEntity resultBean = dao.getPositionByID(position_id);

		if (resultBean == null) {
			// 检索不到的情况下
			MsgInfo error = new MsgInfo();
			error.setComponentid("position_id");
			error.setErrcode("dbaccess.recordNotExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", "工位"));
			errors.add(error);
			return null;
		} else {
			// 建立页面返回表单
			PositionForm resultForm = new PositionForm();

			// 数据对象复制到表单
			BeanUtil.copyToForm(resultBean, resultForm, null);

			return resultForm;
		}
	}

	/**
	 * 标准检查以外的合法性检查
	 * @param positionForm 表单
	 * @param errors 错误内容列表
	 */
	public void customValidate(PositionForm positionForm, SqlSession conn, List<MsgInfo> errors) {
		// 工位ID不重复
		PositionMapper dao = conn.getMapper(PositionMapper.class);
		// 表单复制到数据对象
		PositionEntity conditionBean = new PositionEntity();
		BeanUtil.copyToBean(positionForm, conditionBean, (new CopyOptions()).include("id", "process_code", "delete_flg"));

		if (conditionBean.getProcess_code() != null) {
			if (!conditionBean.getProcess_code().matches("[0-9][0-9A-Z][0-9A-Z]")) {
				MsgInfo error = new MsgInfo();
				error.setComponentid("process_code");
				error.setErrcode("validator.invalidParam.invalidCode");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidCode", "进度代码"));
				errors.add(error);
			} else {
				List<PositionEntity> resultBean = dao.searchPosition(conditionBean);
				if (resultBean != null && resultBean.size() > 0) {
					if (resultBean.size() == 1 && !resultBean.get(0).getPosition_id().equals(conditionBean.getPosition_id())) {
						if (conditionBean.getDelete_flg() != null && conditionBean.getDelete_flg() == 2) {
							// 确定要建立临时工位
						} else {
							// 有一个同名时，确定是否建立临时工位
							MsgInfo error = new MsgInfo();
							error.setComponentid("process_code");
							error.setErrcode("info.master.position.columnNotUniqueSetTemp");
							error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.master.position.columnNotUniqueSetTemp",
									conditionBean.getProcess_code()));
							errors.add(error);
						}
					} else if (resultBean.size() > 1) {
						MsgInfo error = new MsgInfo();
						error.setComponentid("process_code");
						error.setErrcode("dbaccess.columnNotUnique");
						error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", "进度代码",
								conditionBean.getProcess_code(), "工位"));
						errors.add(error);
					}
				}
			}
		}
	}

	/**
	 * 执行插入
	 * @param form 提交表单
	 * @param session 当前用户会话
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @throws Exception
	 */
	public void insert(PositionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		// 表单复制到数据对象
		PositionEntity insertBean = new PositionEntity();
		BeanUtil.copyToBean(form, insertBean, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		if(insertBean.getLight_division_flg()==null || insertBean.getLight_division_flg()==2){//独立小修理工位标记 空,否
			insertBean.setLight_division_flg(0);
		}

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		insertBean.setUpdated_by(user.getOperator_id());

		// 新建记录插入到数据库中
		PositionMapper dao = conn.getMapper(PositionMapper.class);
		dao.insertPosition(insertBean);

		clearCaches();
	}

	/**
	 * 执行更新
	 * @param form 提交表单
	 * @param session 当前用户会话
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @throws Exception
	 */
	public void update(PositionForm positionForm, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		// 表单复制到数据对象
		PositionEntity updateBean = new PositionEntity();
		BeanUtil.copyToBean(positionForm, updateBean, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		if(updateBean.getLight_division_flg()==2){//独立小修理工位标记 否
			updateBean.setLight_division_flg(0);
		}

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		updateBean.setUpdated_by(user.getOperator_id());

		// 更新数据库中记录
		PositionMapper dao = conn.getMapper(PositionMapper.class);
		dao.updatePosition(updateBean);

		clearCaches();
	}

	/**
	 * 执行逻辑删除
	 * @param form 提交表单
	 * @param session 当前用户会话
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @throws Exception
	 */
	public void delete(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		// 表单复制到数据对象
		PositionEntity deleteBean = new PositionEntity();
		BeanUtil.copyToBean(form, deleteBean, null);

		// 取的删除前信息
		PositionMapper dao = conn.getMapper(PositionMapper.class);
		deleteBean = dao.getPositionByID(deleteBean.getPosition_id());

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		deleteBean.setUpdated_by(user.getOperator_id());

		// 在数据库中逻辑删除记录
		dao.deletePosition(deleteBean);

		PositionEntity condBean = new PositionEntity();
		condBean.setProcess_code(deleteBean.getProcess_code());
		List<PositionEntity> resultBeans = dao.searchPosition(condBean);
		if (resultBeans.size() > 0) {
			dao.setPositionRevision(deleteBean.getProcess_code());
		}

		clearCaches();
	}

	/**
	 * 取得全工位
	 * @param conn
	 * @return
	 */
	public List<PositionEntity> getAllPosition(SqlSession conn) {
		
		PositionMapper dao = conn.getMapper(PositionMapper.class);
		List<PositionEntity> lResultBean = dao.getAllPosition();
		
		return lResultBean;
	}
	
	/**
	 * 取得工位参照选择项
	 * @param conn
	 * @return
	 */
	public String getOptions(SqlSession conn) {
		return getOptions(null, false,  conn);
	}
	public String getOptions(Integer department, SqlSession conn) {
		return getOptions(department, false,  conn);
	}
	public String getOptions(Integer department, boolean showTemp, SqlSession conn) {
		List<String[]> lst = new ArrayList<String[]>();
		
		List<PositionEntity> allPosition = this.getAllPosition(conn);
		
		for (PositionEntity position: allPosition) {
			if (RvsConsts.DEPART_MANUFACT.equals(department)) {
				if (position.getProcess_code().charAt(0) != '0') continue;
			}
			if (!showTemp && position.getDelete_flg() != 0) {
				continue;
			}
			String[] p = new String[3];
			p[0] = position.getPosition_id();
			p[1] = position.getName();
			p[2] = position.getProcess_code();
			lst.add(p);
		}
		
		String pReferChooser = CodeListUtils.getReferChooser(lst);
		
		return pReferChooser;
	}

	/**
	 * 根据主键查找工位信息
	 * @param position_id
	 * @param conn
	 * @return
	 */
	public PositionEntity getPositionEntityByKey(String position_id, SqlSession conn) {
		// 从数据库中查询记录
		if (positionEntityCache.containsKey(position_id)) {
			return positionEntityCache.get(position_id);
		}
		PositionMapper dao = conn.getMapper(PositionMapper.class);
		PositionEntity resultBean = dao.getPositionByID(position_id);
		positionEntityCache.put(position_id, resultBean);
		return resultBean;
	}

	/**
	 * 取得工位
	 * @param conn
	 * @return
	 */
	public List<PositionEntity> getPositionByInlineFlg(SqlSession conn) {
		PositionMapper dao = conn.getMapper(PositionMapper.class);
		List<PositionEntity> lResultBean = dao.getPositionByInlineFlg();

		return lResultBean;
	}

	/**
	 * 取得分平行线工位
	 * @param conn
	 * @return
	 */
	public static Set<String> getDividePositions(SqlSession conn) {
		if (dividePositions == null) {
			dividePositions = new HashSet<String>();
			PositionMapper mapper = conn.getMapper(PositionMapper.class);
			List<String> l = mapper.getDividePositions();
			for (String pos_id : l) {
				dividePositions.add(pos_id);
			}
		}
		return dividePositions;
	}

	/**
	 * 取得工位特殊页面
	 * @param conn
	 * @return
	 */
	public static String getPositionSpecialPage(String position_id, SqlSession conn) {
		if (specialPagePositions == null) {
			specialPagePositions = new HashMap<String, String>();
			PositionMapper mapper = conn.getMapper(PositionMapper.class);
			List<PositionEntity> l = mapper.getSpecialPagePositions();
			for (PositionEntity pos : l) {
				specialPagePositions.put(pos.getPosition_id(), pos.getSpecial_page());
			}
		}
		return specialPagePositions.get(position_id);
	}

	public static List<String> getPositionsBySpecialPage(String special_page, SqlSession conn) {
		if (special_page == null) return null;

		List<String> ret = new ArrayList<String>();
		if (specialPagePositions == null) {
			specialPagePositions = new HashMap<String, String>();
			PositionMapper mapper = conn.getMapper(PositionMapper.class);
			List<PositionEntity> l = mapper.getSpecialPagePositions();
			for (PositionEntity pos : l) {
				specialPagePositions.put(pos.getPosition_id(), pos.getSpecial_page());
			}
		}
		for (String position_id : specialPagePositions.keySet()) {
			if (special_page.equals(specialPagePositions.get(position_id))) {
				ret.add(position_id);
			}
		}
		if (ret.size() == 0) {
			return null;
		}
		return ret;
	}

	public static String getPositionOptionsBySpecialPage(String special_page, SqlSession conn) {
		if (special_page == null) return null;

		List<String> ret = getPositionsBySpecialPage(special_page, conn);
		PositionService thisService = new PositionService();
		String retString = "";
		for (String position_id : ret) {
			PositionEntity entity = thisService.getPositionEntityByKey(position_id, conn);
			retString += "<option value=\"" + position_id + "\">" + entity.getProcess_code() + " " + entity.getName() + "</option>";
		}
		return retString;
	}

	public boolean checkPositionKind(String kind,
			List<PositionEntity> positions) {
		// TODO setKind
		for (PositionEntity position : positions) {
			if ("service_repair_referee".equals(kind) 
					&& "601".equals(position.getProcess_code())) {
				return true;
			}
			if ("quotation".equals(kind) 
					&& "151".equals(position.getProcess_code())) {
				return true;
			}
		}
		return false;
	}
	public static Set<String> getNoBreakPositions() {
		if (noBreakPositions == null) {
			noBreakPositions = new HashSet<String>();
		}
		return noBreakPositions;
	}
}
