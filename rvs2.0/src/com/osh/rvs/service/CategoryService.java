package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.CategoryEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.master.CategoryForm;
import com.osh.rvs.mapper.master.CategoryMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.message.ApplicationMessage;

public class CategoryService {

	/**
	 * 取得机种选择项标签集
	 * @param integer 
	 * @param conn 数据库连接
	 * @return String 机种选择项标签集
	 */
	public String getOptions(Integer department, SqlSession conn) {
		if (department == null) {
			return this.getAllOptions(conn);
		} else if (department == RvsConsts.DEPART_REPAIR) {
			return this.getRepairOptions(conn);
		} else {
			return this.getManufactureOptions(conn);
		}
	}

	public String getAllOptions(SqlSession conn) {
		CategoryMapper dao = conn.getMapper(CategoryMapper.class);
		List<CategoryEntity> l = dao.getAllCategory();
		// 内镜组
		Map<String, String> codeMapEndoscope = new TreeMap<String, String>();
		// 周边组
		Map<String, String> codeMapPeripheral = new TreeMap<String, String>();
		// 制造品
		Map<String, String> codeMapManu = new TreeMap<String, String>();
		for (CategoryEntity bean : l) {
			if (bean.getKind() == 7) {
				codeMapPeripheral.put(bean.getCategory_id(), bean.getName());
			} else if (bean.getKind() == 11) {
				codeMapManu.put(bean.getCategory_id(), bean.getName());
			} else {
				codeMapEndoscope.put(bean.getCategory_id(), bean.getName());
			}
		}
		return "<optgroup label=\"\"><option value=\"\"></option></optgroup>" 
			+ "<optgroup label=\"内视镜\">" + CodeListUtils.getSelectOptions(codeMapEndoscope, null, null, false) + "</optgroup>"
			+ "<optgroup label=\"周边设备\">" + CodeListUtils.getSelectOptions(codeMapPeripheral, null, null, false) + "</optgroup>"
			+ "<optgroup label=\"制造\">" + CodeListUtils.getSelectOptions(codeMapManu, null, null, false) + "</optgroup>";
	}

	/**
	 * 取得内窥镜选择项标签集
	 * @param String empty, 
	 * @param conn 数据库连接
	 * @return String 机种选择项标签集
	 */
	public String getEndoscopeOptions(String empty, SqlSession conn) {
		CategoryMapper dao = conn.getMapper(CategoryMapper.class);
		List<CategoryEntity> l = dao.getAllCategory();
		
		//内镜组
		Map<String, String> codeMapEndoscope = new TreeMap<String, String>();
		for (CategoryEntity bean : l) {
			if (bean.getKind() != 7 && bean.getKind() != 11) {
				codeMapEndoscope.put(bean.getCategory_id(), bean.getName());
			} 
		}
		return CodeListUtils.getSelectOptions(codeMapEndoscope, null, empty, false);
	}

	/**
	 * 取得机种选择项标签集
	 * @param conn 数据库连接
	 * @return String 机种选择项标签集
	 */
	public String getRepairOptions(SqlSession conn) {
		CategoryMapper dao = conn.getMapper(CategoryMapper.class);
		List<CategoryEntity> l = dao.getAllCategory();
		// 内镜组
		Map<String, String> codeMapEndoscope = new TreeMap<String, String>();
		// 周边组
		Map<String, String> codeMapPeripheral = new TreeMap<String, String>();
		for (CategoryEntity bean : l) {
			if (bean.getKind() == 11) {
			} else if (bean.getKind() != 7) {
				codeMapEndoscope.put(bean.getCategory_id(), bean.getName());
			} else {
				codeMapPeripheral.put(bean.getCategory_id(), bean.getName());
			}
		}
		return "<optgroup label=\"\"><option value=\"\"></option></optgroup>" 
			+ "<optgroup label=\"内视镜\">" + CodeListUtils.getSelectOptions(codeMapEndoscope, null, null, false) + "</optgroup>"
			+ "<optgroup label=\"周边设备\">" + CodeListUtils.getSelectOptions(codeMapPeripheral, null, null, false) + "</optgroup>";
	}

	/**
	 * 取得显微镜制造选择项标签集
	 * @param conn 数据库连接
	 * @return String 机种选择项标签集
	 */
	public String getManufactureOptions(SqlSession conn) {
		CategoryMapper dao = conn.getMapper(CategoryMapper.class);
		List<CategoryEntity> l = dao.getAllCategory();
		
		//内镜组
		Map<String, String> codeMapEndoscope = new TreeMap<String, String>();
		for (CategoryEntity bean : l) {
			if (bean.getKind() == 11) {
				codeMapEndoscope.put(bean.getCategory_id(), bean.getName());
			} 
		}
		return CodeListUtils.getSelectOptions(codeMapEndoscope, null, "", false);
	}

	/**
	 * 取得内视镜全部型号(参照列表)
	 * @param conn
	 * @return
	 */
	public String getEndoscopeReferChooser(SqlSession conn) {
		CategoryMapper dao = conn.getMapper(CategoryMapper.class);
		List<String[]> cList = new ArrayList<String[]>();
		List<CategoryEntity> l = dao.getAllCategory();
		
		for (CategoryEntity bean : l) {
			if (bean.getKind() != 7 && bean.getKind() != 11) {
				String[] mline = new String[2];
				mline[0] = bean.getCategory_id();
				mline[1] = bean.getName();
				cList.add(mline);
			}
		}

		String mReferChooser = CodeListUtils.getReferChooser(cList);
		
		return mReferChooser;
	}

	/**
	 * 取得全部型号(参照列表)
	 * @param conn
	 * @return
	 */
	public String getReferChooser(SqlSession conn) {
		CategoryMapper dao = conn.getMapper(CategoryMapper.class);
		List<String[]> cList = new ArrayList<String[]>();
		List<CategoryEntity> l = dao.getAllCategory();
		
		for (CategoryEntity bean : l) {
			String[] mline = new String[2];
			mline[0] = bean.getCategory_id();
			mline[1] = bean.getName();
			cList.add(mline);
		}

		String mReferChooser = CodeListUtils.getReferChooser(cList);
		
		return mReferChooser;
	}

	/**
	 * 检索记录列表
	 * @param form 提交表单
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @return List<CategoryForm> 查询结果表单
	 */
	public List<CategoryForm> search(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		// 表单复制到数据对象
		CategoryEntity coditionBean = new CategoryEntity();
		BeanUtil.copyToBean(form, coditionBean, null);

		// 从数据库中查询记录
		CategoryMapper dao = conn.getMapper(CategoryMapper.class);
		List<CategoryEntity> lResultBean = dao.searchCategory(coditionBean);

		// 建立页面返回表单
		List<CategoryForm> lResultForm = new ArrayList<CategoryForm>();

		// 数据对象复制到表单
		BeanUtil.copyToFormList(lResultBean, lResultForm, null, CategoryForm.class);
		
		return lResultForm;
	}

	/**
	 * 按照主键检索单条记录
	 * @param form 提交表单
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @return CategoryForm 查询结果表单
	 */
	public CategoryForm getDetail(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		// 表单复制到数据对象
		CategoryEntity coditionBean = new CategoryEntity();
		BeanUtil.copyToBean(form, coditionBean, null);
		String category_id = coditionBean.getCategory_id();

		CategoryEntity cb = getDetail(category_id, conn);

		if (cb == null) {
			// 检索不到的情况下
			MsgInfo error = new MsgInfo();
			error.setComponentid("category_id");
			error.setErrcode("dbaccess.recordNotExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", "维修对象机种"));
			errors.add(error);
			return null;
		} else {
			// 建立页面返回表单
			CategoryForm cf = new CategoryForm();

			// 数据对象复制到表单
			BeanUtil.copyToForm(cb, cf, null);
			return cf;
		}
	}

	public CategoryEntity getDetail(String category_id, SqlSession conn) {

		// 从数据库中查询记录
		CategoryMapper dao = conn.getMapper(CategoryMapper.class);
		return dao.getCategoryByID(category_id);

	}

	/**
	 * 执行插入
	 * @param form 提交表单
	 * @param session 当前用户会话
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @throws Exception
	 */
	public void insert(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		// 表单复制到数据对象
		CategoryEntity insertBean = new CategoryEntity();
		BeanUtil.copyToBean(form, insertBean, CopyOptions.COPYOPTIONS_NOEMPTY);

		LoginData loginData = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		String userid = loginData.getOperator_id();
		insertBean.setUpdated_by(userid);

		if (insertBean.getDefault_cell_pat_id() == null) {
			insertBean.setDefault_cell_pat_id(insertBean.getDefault_pat_id());
		}

		// 新建记录插入到数据库中
		CategoryMapper dao = conn.getMapper(CategoryMapper.class);
		dao.insertCategory(insertBean);
	}

	/**
	 * 执行更新
	 * @param form 提交表单
	 * @param session 当前用户会话
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @throws Exception
	 */
	public void update(ActionForm form, HttpSession session, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		// 表单复制到数据对象
		CategoryEntity updateBean = new CategoryEntity();
		BeanUtil.copyToBean(form, updateBean, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 脏数据检查

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		updateBean.setUpdated_by(user.getOperator_id());

		// 更新数据库中记录
		CategoryMapper dao = conn.getMapper(CategoryMapper.class);
		dao.updateCategory(updateBean);

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
		CategoryEntity deleteBean = new CategoryEntity();
		BeanUtil.copyToBean(form, deleteBean, null);

		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		deleteBean.setUpdated_by(user.getOperator_id());

		// 在数据库中逻辑删除记录
		CategoryMapper dao = conn.getMapper(CategoryMapper.class);
		dao.deleteCategory(deleteBean);
	}
}
