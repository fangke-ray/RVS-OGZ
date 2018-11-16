package com.osh.rvs.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.QualityTipEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.form.master.QualityTipForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.master.QualityTipMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.FileUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;

public class QualityTipService {
	private static final Integer BIND_BY_CATEGORY = 1;
	private static final Integer BIND_BY_MODEL = 2;

	/**
	 * 检索记录列表
	 * @param form 提交表单
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @return List<QualityTipForm> 查询结果表单
	 */
	public List<QualityTipForm> search(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		// 表单复制到数据对象
		QualityTipEntity coditionBean = new QualityTipEntity();
		BeanUtil.copyToBean(form, coditionBean, null);

		// 从数据库中查询记录
		QualityTipMapper dao = conn.getMapper(QualityTipMapper.class);

		List<QualityTipEntity> lResultBean = dao.search(coditionBean);

		// 建立页面返回表单
		List<QualityTipForm> lResultForm = new ArrayList<QualityTipForm>();
		// 数据对象复制到表单
		BeanUtil.copyToFormList(lResultBean, lResultForm, null, QualityTipForm.class);

		return lResultForm;
	}

	/**
	 * 执行插入
	 * @param qform 提交表单
	 * @param session 当前用户会话
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @throws Exception
	 */
	public void insert(QualityTipForm qform, LoginData user, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		// 表单复制到数据对象
		QualityTipEntity insertBean = new QualityTipEntity();
		BeanUtil.copyToBean(qform, insertBean, null);

		insertBean.setUpdated_by(user.getOperator_id());

		// 新建记录插入到数据库中
		QualityTipMapper dao = conn.getMapper(QualityTipMapper.class);

		dao.insertQualityTip(insertBean);

		CommonMapper commonMapper = conn.getMapper(CommonMapper.class);
		String lastInsertId = CommonStringUtil.fillChar(commonMapper.getLastInsertID(), '0', 11, true);
		insertBean.setQuality_tip_id(lastInsertId);

		insertBind(qform, insertBean, dao);
	}

	/**
	 * 执行插入
	 * @param qform 提交表单
	 * @param insertBean
	 * @param dao
	 */
	public void insertBind(QualityTipForm qform, QualityTipEntity insertBean, QualityTipMapper dao) throws Exception {
		List<String> insertPositions = qform.getCategorys();
		for (String sPosition_id : insertPositions) {
			insertBean.setBind_type(BIND_BY_CATEGORY);
			insertBean.setBind_id(sPosition_id);
			dao.insertQualityTipBind(insertBean);
		}

		List<String> insertModels = qform.getModels();
		for (String sModel_id : insertModels) {
			insertBean.setBind_type(BIND_BY_MODEL);
			insertBean.setBind_id(sModel_id);
			dao.insertQualityTipBind(insertBean);
		}

		// 把图片拷贝到目标文件夹下
		String today = DateUtil.toString(new Date(), "yyyyMM");
		String photo_file_name = qform.getPhoto_file_name().replaceAll("&amp;", "&");
		String tempFilePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + today + "\\" + photo_file_name;
		String targetPath = PathConsts.BASE_PATH + PathConsts.PHOTOS + "\\quality_tip\\" + insertBean.getQuality_tip_id();
		File confFile = new File(tempFilePath);
		if (confFile.exists()) {
			FileUtils.copyFile(tempFilePath, targetPath, true);
		}
	}

	/**
	 * 按照主键检索单条记录
	 * @param form 提交表单
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @return SectionForm 查询结果表单
	 */
	public QualityTipForm getDetail(QualityTipForm qform, SqlSession conn, List<MsgInfo> errors) {
		// 从数据库中查询记录
		QualityTipMapper dao = conn.getMapper(QualityTipMapper.class);
		QualityTipEntity resultBean = dao.getQualityTipByID(qform.getQuality_tip_id());

		// 建立页面返回表单
		QualityTipForm resultForm = new QualityTipForm();
		BeanUtil.copyToForm(resultBean, resultForm, null);

		List<QualityTipEntity> lBindBean = dao.getQualityTipBindByID(qform.getQuality_tip_id());

		List<String> categorys = new AutofillArrayList<String>(String.class);
		List<HashMap> modelBeans = new AutofillArrayList<HashMap>(HashMap.class);
		int i = 0;
		for (QualityTipEntity bindBean : lBindBean) {
			if (bindBean.getBind_type() == 1) {
				categorys.add(bindBean.getBind_id());
			} else {
				modelBeans.get(i).put("id", bindBean.getBind_id());
				modelBeans.get(i).put("name", bindBean.getBind_name());
				modelBeans.get(i).put("category_name", bindBean.getCategory_name());
				i++;
			}
		}
		resultForm.setCategorys(categorys);
		resultForm.setModelBeans(modelBeans);

		return resultForm;
	}

	/**
	 * 执行更新
	 * @param form 提交表单
	 * @param session 当前用户会话
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @throws Exception
	 */
	public void update(QualityTipForm qform, LoginData user, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		// 表单复制到数据对象
		QualityTipEntity updateBean = new QualityTipEntity();
		BeanUtil.copyToBean(qform, updateBean, null);

		updateBean.setUpdated_by(user.getOperator_id());

		// 更新数据库中记录
		QualityTipMapper dao = conn.getMapper(QualityTipMapper.class);

		dao.updateQualityTip(updateBean);

		dao.deleteQualityTipBind(qform.getQuality_tip_id());
		insertBind(qform, updateBean, dao);
	}

	/**
	 * 执行逻辑删除
	 * @param qform 提交表单
	 * @param user 当前用户会话
	 * @param conn 数据库连接
	 * @param errors 错误内容列表
	 * @throws Exception
	 */
	public void delete(QualityTipForm qform, SqlSessionManager conn, List<MsgInfo> errors) throws Exception {
		String quality_tip_id = qform.getQuality_tip_id();

		// 在数据库中逻辑删除记录
		QualityTipMapper dao = conn.getMapper(QualityTipMapper.class);

		dao.deleteQualityTip(quality_tip_id);
		dao.deleteQualityTipBind(quality_tip_id);
	}

	/**
	 * 取得用于显示的技术提示信息
	 * @param material_id 维修对象 ID
	 * @param position_id 工位 ID
	 * @param qt4 按机型上次显示时间
	 * @param conn 数据库连接
	 * @throws Exception
	 */
	public QualityTipForm getQualityTipOfMaterialAtPosition(String material_id,
			String position_id, String qt4, SqlSession conn) {
		QualityTipMapper dao = conn.getMapper(QualityTipMapper.class);
		QualityTipEntity result = dao.getQualityTipOfMaterialAtPosition(material_id, position_id);
		if (result == null) {
			return null;
		}

		if (result.getBind_type() == BIND_BY_CATEGORY) {
			if (qt4 != null) {
				Long iQt4 = Long.parseLong(qt4);
				if (new Date().getTime() - iQt4 < 7200000) {
					return null;
				}
			}
		}

		QualityTipForm resultForm = new QualityTipForm();
		BeanUtil.copyToForm(result, resultForm, null);

		return resultForm;
	}
}
