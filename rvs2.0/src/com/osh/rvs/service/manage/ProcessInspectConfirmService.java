package com.osh.rvs.service.manage;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.manage.ProcessInspectConfirmEntity;
import com.osh.rvs.form.manage.ProcessInspectConfirmForm;
import com.osh.rvs.mapper.manage.ProcessInspectConfirmMapper;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 作业监察确认
 * 
 * @Description
 * @author liuxb
 * @date 2021-11-25 上午10:28:33
 */
public class ProcessInspectConfirmService {
	/**
	 * 取得作业监察确认所有信息
	 * @param processInspectKey 作业监察确认KEY
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public List<ProcessInspectConfirmForm> searchAll(String processInspectKey, SqlSession conn) throws Exception {
		ProcessInspectConfirmMapper dao = conn.getMapper(ProcessInspectConfirmMapper.class);
		List<ProcessInspectConfirmEntity> list = dao.searchAll(processInspectKey);

		List<ProcessInspectConfirmForm> respFormList = new ArrayList<ProcessInspectConfirmForm>();
		BeanUtil.copyToFormList(list, respFormList, CopyOptions.COPYOPTIONS_NOEMPTY, ProcessInspectConfirmForm.class);

		return respFormList;
	}
	
	/**
	 * 取得作业监察确认单条信息
	 * @param form
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public ProcessInspectConfirmForm getInspectConfirm(ActionForm form,SqlSession conn)throws Exception{
		ProcessInspectConfirmMapper dao = conn.getMapper(ProcessInspectConfirmMapper.class);
		
		ProcessInspectConfirmEntity conndEntity = new ProcessInspectConfirmEntity();
		BeanUtil.copyToBean(form, conndEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		ProcessInspectConfirmEntity entity = dao.getProcessInspectConfirmByKey(conndEntity.getProcess_inspect_key(), conndEntity.getProcess_name());
		
		ProcessInspectConfirmForm respForm = null;
		if(entity!=null){
			respForm = new ProcessInspectConfirmForm();
			BeanUtil.copyToForm(entity, respForm, CopyOptions.COPYOPTIONS_NOEMPTY);
		}
		
		return respForm;
	}
	
	/**
	 * 新建作业监察确认信息
	 * @param form
	 * @param conn
	 * @throws Exception
	 */
	public void insert(ActionForm form,SqlSessionManager conn)throws Exception{
		ProcessInspectConfirmMapper dao = conn.getMapper(ProcessInspectConfirmMapper.class);
		
		ProcessInspectConfirmEntity entity = new ProcessInspectConfirmEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		dao.insert(entity);
	}
	
	
	/**
	 * 更新作业监察确认信息
	 * @param form
	 * @param conn
	 * @throws Exception
	 */
	public void update(ActionForm form,SqlSessionManager conn)throws Exception{
		ProcessInspectConfirmMapper dao = conn.getMapper(ProcessInspectConfirmMapper.class);
		
		ProcessInspectConfirmEntity entity = new ProcessInspectConfirmEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		dao.update(entity);
	}
	
}