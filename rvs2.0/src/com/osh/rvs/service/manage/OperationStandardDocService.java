package com.osh.rvs.service.manage;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.manage.OperationStandardDocEntity;
import com.osh.rvs.form.manage.OperationStandardDocForm;
import com.osh.rvs.mapper.manage.OperationStandardDocMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.message.ApplicationMessage;

public class OperationStandardDocService {
	/**
	 * 新建
	 * 
	 * @param form
	 * @param conn
	 */
	public void insert(ActionForm form, SqlSessionManager conn) {
		OperationStandardDocMapper dao = conn.getMapper(OperationStandardDocMapper.class);

		OperationStandardDocEntity entity = new OperationStandardDocEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.insert(entity);
	}

	/**
	 * 检索
	 * 
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<OperationStandardDocForm> search(ActionForm form, SqlSession conn) {
		OperationStandardDocMapper dao = conn.getMapper(OperationStandardDocMapper.class);

		OperationStandardDocEntity entity = new OperationStandardDocEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<OperationStandardDocForm> respFormList = new ArrayList<OperationStandardDocForm>();

		List<OperationStandardDocEntity> list = dao.search(entity);
		BeanUtil.copyToFormList(list, respFormList, CopyOptions.COPYOPTIONS_NOEMPTY, OperationStandardDocForm.class);

		return respFormList;
	}

	/**
	 * 删除
	 * 
	 * @param form
	 * @param conn
	 */
	public void delete(ActionForm form, SqlSessionManager conn) {
		OperationStandardDocMapper dao = conn.getMapper(OperationStandardDocMapper.class);

		OperationStandardDocEntity entity = new OperationStandardDocEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		dao.delete(entity);
	}

	/**
	 * 查询所有型号
	 * 
	 * @param conn
	 * @return
	 */
	public List<OperationStandardDocForm> searchAllModel(SqlSession conn) {
		OperationStandardDocMapper dao = conn.getMapper(OperationStandardDocMapper.class);

		List<OperationStandardDocForm> respFormList = new ArrayList<OperationStandardDocForm>();

		List<OperationStandardDocEntity> list = dao.searchAllModel();
		BeanUtil.copyToFormList(list, respFormList, CopyOptions.COPYOPTIONS_NOEMPTY, OperationStandardDocForm.class);

		return respFormList;
	}

	public String getModelOptions(SqlSession conn) {
		List<String[]> mList = new ArrayList<String[]>();
		List<OperationStandardDocForm> allModel = this.searchAllModel(conn);

		for (OperationStandardDocForm model : allModel) {
			String[] mline = new String[3];
			mline[0] = model.getModel_id();
			mline[1] = model.getModel_name();
			mline[2] = model.getCategory_name();
			mList.add(mline);
		}

		String mReferChooser = CodeListUtils.getReferChooser(mList);

		return mReferChooser;
	}

	/**
	 * 明细
	 * 
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<OperationStandardDocForm> searchDetail(ActionForm form, SqlSession conn) {
		OperationStandardDocMapper dao = conn.getMapper(OperationStandardDocMapper.class);

		OperationStandardDocEntity entity = new OperationStandardDocEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<OperationStandardDocForm> respFormList = new ArrayList<OperationStandardDocForm>();

		List<OperationStandardDocEntity> list = dao.searchDetail(entity);
		BeanUtil.copyToFormList(list, respFormList, CopyOptions.COPYOPTIONS_NOEMPTY, OperationStandardDocForm.class);

		return respFormList;
	}

	public void copy(ActionForm form, SqlSessionManager conn, List<MsgInfo> errors) {
		OperationStandardDocMapper dao = conn.getMapper(OperationStandardDocMapper.class);

		OperationStandardDocForm pageForm = (OperationStandardDocForm) form;

		// 复制标记
		String flg = pageForm.getFlg();
		// 型号来源
		String copyModelId = pageForm.getCopy_model_id();
		// 复制工位
		String positionID = pageForm.getPosition_id();

		List<OperationStandardDocEntity> list = new ArrayList<OperationStandardDocEntity>();

		// 复制XXX工位
		if ("0".equals(flg)) {
			// 验证复制的工位是否存在数据
			OperationStandardDocEntity connd = new OperationStandardDocEntity();
			connd.setModel_id(copyModelId);
			connd.setPosition_id(positionID);

			list = dao.searchDetail(connd);

			if (list == null || list.size() == 0) {
				String msg = "来源型号为【" + pageForm.getCopy_model_name() + "】，【" + pageForm.getProcess_code() + "】工位配置";
				MsgInfo error = new MsgInfo();
				error.setErrcode("dbaccess.recordNotExist");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", msg));
				errors.add(error);
			}
		} else if ("1".equals(flg)) {
			// 复制全部工位
			OperationStandardDocEntity connd = new OperationStandardDocEntity();
			connd.setModel_id(copyModelId);
			list = dao.searchDetail(connd);
		}

		if (errors.size() == 0) {
			OperationStandardDocEntity entity = new OperationStandardDocEntity();
			BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

			// 删除
			dao.delete(entity);

			for (OperationStandardDocEntity item : list) {
				item.setModel_id(entity.getModel_id());
				// 新建记录
				dao.insert(item);
			}
		}
	}
}