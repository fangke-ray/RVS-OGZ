package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.master.ProcessAssignEntity;
import com.osh.rvs.bean.master.ProcessAssignTemplateEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.master.ProcessAssignForm;
import com.osh.rvs.form.master.ProcessAssignTemplateForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.inline.ProductionAssignMapper;
import com.osh.rvs.mapper.inline.ProductionFeatureMapper;
import com.osh.rvs.mapper.master.ProcessAssignMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;

public class ProcessAssignService {
	private static Logger logger = Logger.getLogger(ProcessAssignService.class);

	private static Map<String, Boolean> hasNsMap = new HashMap<String, Boolean>(); 

	public void insert(ProductionFeatureEntity entity, SqlSession conn) {
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		dao.insertProductionFeature(entity);
	}

	public List<ProcessAssignTemplateForm> searchTemplate(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		// 表单复制到数据对象
		ProcessAssignTemplateEntity conditionBean = new ProcessAssignTemplateEntity();
		BeanUtil.copyToBean(form, conditionBean, null);

		// 从数据库中查询记录
		ProcessAssignMapper dao = conn.getMapper(ProcessAssignMapper.class);
		List<ProcessAssignTemplateEntity> lResultBean = dao.searchProcessAssignTemplate(conditionBean);

		// 建立页面返回表单
		List<ProcessAssignTemplateForm> lResultForm = new ArrayList<ProcessAssignTemplateForm>();

		// 数据对象复制到表单
		BeanUtil.copyToFormList(lResultBean, lResultForm, null, ProcessAssignTemplateForm.class);

		return lResultForm;
	}

	public ProcessAssignTemplateForm getDetail(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		// 取得模板详细
		ProcessAssignMapper dao = conn.getMapper(ProcessAssignMapper.class);
		ProcessAssignTemplateEntity e = dao.getProcessAssignTemplateByID(((ProcessAssignTemplateForm) form).getId());
		ProcessAssignTemplateForm result = new ProcessAssignTemplateForm();

		BeanUtil.copyToForm(e, result, null);

		return result;
	}

	public void insert(ActionForm form, Map<String, String[]> parameterMap, HttpSession session,
			SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		// 表单复制到数据对象
		ProcessAssignTemplateEntity insertBean = new ProcessAssignTemplateEntity();
		BeanUtil.copyToBean(form, insertBean, null);

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		insertBean.setUpdated_by(user.getOperator_id());

		// 插入
		ProcessAssignMapper dao = conn.getMapper(ProcessAssignMapper.class);
		dao.insertProcessAssignTemplate(insertBean);

		CommonMapper cmDao = conn.getMapper(CommonMapper.class);
		String refer_id = cmDao.getLastInsertID();

		List<ProcessAssignForm> processAssigns = new AutofillArrayList<ProcessAssignForm>(ProcessAssignForm.class);
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("process_assign".equals(entity)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("line_id".equals(column)) {
						processAssigns.get(icounts).setLine_id(value[0]);
					} else if ("position_id".equals(column)) {
						processAssigns.get(icounts).setPosition_id(value[0]);
					} else if ("sign_position_id".equals(column)) {
						processAssigns.get(icounts).setSign_position_id(value[0]);
					} else if ("prev_position_id".equals(column)) {
						processAssigns.get(icounts).setPrev_position_id(value[0]);
					} else if ("next_position_id".equals(column)) {
						processAssigns.get(icounts).setNext_position_id(value[0]);
					}
				}
			}
		}

		// 放入数据库
		for (ProcessAssignForm processAssign : processAssigns) {
			ProcessAssignEntity e = new ProcessAssignEntity();
			BeanUtil.copyToBean(processAssign, e, null);
			e.setRefer_id(refer_id);
			e.setRefer_type(1);
			dao.insertProcessAssign(e);
		}
	}

	public void delete(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors)
			throws Exception {
		// 表单复制到数据对象
		ProcessAssignTemplateEntity updateBean = new ProcessAssignTemplateEntity();
		BeanUtil.copyToBean(form, updateBean, null);

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		updateBean.setUpdated_by(user.getOperator_id());

		ProcessAssignMapper dao = conn.getMapper(ProcessAssignMapper.class);
		dao.deleteProcessAssignTemplate(updateBean);
	}

	public void update(ActionForm form, Map<String, String[]> parameterMap, HttpSession session,
			SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		synchronized (hasNsMap) {
			// 表单复制到数据对象
			ProcessAssignTemplateEntity updateBean = new ProcessAssignTemplateEntity();
			BeanUtil.copyToBean(form, updateBean, null);

			LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
			updateBean.setUpdated_by(user.getOperator_id());

			// 更新模板名
			ProcessAssignMapper dao = conn.getMapper(ProcessAssignMapper.class);
			dao.updateProcessAssignTemplate(updateBean);

			String refer_id = updateBean.getProcess_assign_template_id();

			List<ProcessAssignForm> processAssigns = new AutofillArrayList<ProcessAssignForm>(ProcessAssignForm.class);
			Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

			// 整理提交数据
			for (String parameterKey : parameterMap.keySet()) {
				Matcher m = p.matcher(parameterKey);
				if (m.find()) {
					String entity = m.group(1);
					if ("process_assign".equals(entity)) {
						String column = m.group(2);
						int icounts = Integer.parseInt(m.group(3));
						String[] value = parameterMap.get(parameterKey);

						// TODO 全
						if ("line_id".equals(column)) {
							processAssigns.get(icounts).setLine_id(value[0]);
						} else if ("position_id".equals(column)) {
							processAssigns.get(icounts).setPosition_id(value[0]);
						} else if ("sign_position_id".equals(column)) {
							processAssigns.get(icounts).setSign_position_id(value[0]);
						} else if ("prev_position_id".equals(column)) {
							processAssigns.get(icounts).setPrev_position_id(value[0]);
						} else if ("next_position_id".equals(column)) {
							processAssigns.get(icounts).setNext_position_id(value[0]);
						}
					}
				}
			}

			// 删除原有明细
			dao.deleteProcessAssignByTemplateID(refer_id);

			// 放入数据库
			for (ProcessAssignForm processAssign : processAssigns) {
				ProcessAssignEntity e = new ProcessAssignEntity();
				BeanUtil.copyToBean(processAssign, e, null);
				e.setRefer_id(refer_id);
				e.setRefer_type(1);
				dao.insertProcessAssign(e);
			}

			hasNsMap.remove(refer_id);
		}
	}

	public List<Map<String, String>> getInlinePositions(SqlSession conn) {

		// 从数据库中查询记录
		ProcessAssignMapper dao = conn.getMapper(ProcessAssignMapper.class);
		List<Map<String, String>> lResultBean = dao.getInlinePositions();
		return lResultBean;
	}

	public List<Map<String, String>> getExpandPositions(SqlSession conn) {

		// 从数据库中查询记录
		ProcessAssignMapper dao = conn.getMapper(ProcessAssignMapper.class);
		List<Map<String, String>> lResultBean = dao.getExpandPositions();

		for (Map<String, String> position : lResultBean) {
			String process_code = position.get("process_code");
			if (process_code != null) {
				position.put("text",  process_code + "\n" + position.get("text"));
			}
		}
		return lResultBean;
	}

	public List<ProcessAssignForm> getAssigns(String template_id, SqlSession conn, List<MsgInfo> errors) {
		// 从数据库中查询记录
		ProcessAssignMapper dao = conn.getMapper(ProcessAssignMapper.class);
		List<ProcessAssignEntity> entities = dao.getProcessAssignByTemplateID(template_id);
		List<ProcessAssignForm> result = new ArrayList<ProcessAssignForm>();
		BeanUtil.copyToFormList(entities, result, null, ProcessAssignForm.class);
		return result;
	}

	/**
	 * 取得维修流程选择项标签集
	 * 
	 * @param conn 数据库连接
	 * @return String 维修流程选择项标签集
	 */
	public String getOptions(String empty, SqlSession conn) {
		return getOptions(empty, null, conn);
	}
	public String getOptions(String empty, Integer fixType, SqlSession conn) {
		ProcessAssignMapper dao = conn.getMapper(ProcessAssignMapper.class);
		List<ProcessAssignTemplateEntity> l = dao.getAllProcessAssignTemplate(fixType);
		Map<String, String> codeMap = new TreeMap<String, String>();
		for (ProcessAssignTemplateEntity bean : l) {
			codeMap.put(bean.getProcess_assign_template_id(), bean.getName());
		}
		return CodeListUtils.getSelectOptions(codeMap, null, empty, false);
	}

	/**
	 * 取得流程里的首工位(可能复数)
	 * @param template_id
	 * @param line_id
	 * @param conn
	 * @return
	 */
	public List<String> getFirstPositionIds(String template_id, SqlSession conn) {
		return getFirstPositionIds(template_id, "" + RvsConsts.PROCESS_ASSIGN_LINE_BASE, conn);
	}
	public List<String> getFirstPositionIds(String template_id, String pas_line_id, SqlSession conn) {
		ProcessAssignMapper mapper = conn.getMapper(ProcessAssignMapper.class);
		List<String> ret = mapper.getFirstPosition(template_id, pas_line_id);
		List<String> lines = new ArrayList<String> ();
		for (String pos_id : ret) {
			if (pos_id.length() >= 7) {
				lines.add(pos_id);
			}
		}

		for (String lin_id : lines) {
			ret.remove(lin_id);
			ret.addAll(getFirstPositionIds(template_id, lin_id, conn));
		}

		return ret;
	}

	public boolean checkPatHasNs(String pat_id, SqlSession conn) {
		if (pat_id == null ||"00000000008".equals(pat_id) || "0000000009".equals(pat_id)) {
			// 补胶不排NS计划
			return false;
		}
		if (hasNsMap.containsKey(pat_id)) {
			return hasNsMap.get(pat_id);
		} else {
			synchronized (hasNsMap) {
				ProcessAssignMapper mapper = conn.getMapper(ProcessAssignMapper.class);

				boolean has = mapper.checkHasLine(pat_id, "00000000013");
				hasNsMap.put(pat_id, has);
				return has;
			}
		}
	}


	/**
	 * 查询流程包含工程
	 * @param conn 
	 * @param string 
	 * @param material_id
	 * @param conn
	 * @return
	 */
	public List<String> checkPatHasLine(String patId, String level, SqlSession conn) {

		ProcessAssignMapper mapper = conn.getMapper(ProcessAssignMapper.class);
		List<String> lines = mapper.checkHasLines(patId);

		if ("1".equals(level) && lines.contains("00000000013")) {
			lines.remove("00000000013");
		}
		return lines;
	}


	public List<String> getPositionBySign(String material_id, String position_id, SqlSession conn) {
		List<String> ret = new ArrayList<String>();
		MaterialService mServ = new MaterialService();
		MaterialEntity mEntity = mServ.loadSimpleMaterialDetailEntity(conn, material_id);

		if (mEntity != null && mEntity.getPat_id() != null) {
			ProcessAssignMapper mapper = conn.getMapper(ProcessAssignMapper.class);
			ret = mapper.getPositionBySign(mEntity.getPat_id(), position_id);
		}
		return ret;
	}
}
