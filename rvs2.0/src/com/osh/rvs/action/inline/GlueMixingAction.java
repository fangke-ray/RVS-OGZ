package com.osh.rvs.action.inline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.master.PartialEntity;
import com.osh.rvs.common.ReverseResolution;
import com.osh.rvs.form.inline.GlueMixingForm;
import com.osh.rvs.service.inline.GlueMixingService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.RequiredValidator;
import framework.huiqing.common.util.validator.Validators;

/**  
 * @Title: GlueMixingAction.java
 * @Package com.osh.rvs.action.inline
 * @Description: 胶水调制
 * @author liuxb
 * @date 2017-12-14 下午4:40:16
 */
public class GlueMixingAction extends BaseAction{
	private Logger log = Logger.getLogger(getClass());
	
	/**
	 * 初始化
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void init(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSession conn)throws Exception{
		log.info("GlueMixingAction.init start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		GlueMixingService service = new GlueMixingService();
		
		service.getGlueMixing(listResponse,request,conn);
		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);
		
		log.info("GlueMixingAction.init end");
	}
	
	
	/**
	 * 检查物料号是否存在
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void checkCode(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSession conn)throws Exception{
		log.info("GlueMixingAction.checkCode start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		GlueMixingForm glueMixingForm = (GlueMixingForm)form;
		String code = glueMixingForm.getCode();
		
		//根据物料号查询物料
		PartialEntity partialEntity = ReverseResolution.getPartialEntityByCode(code, conn);
		
		//物料不存在
		if(partialEntity == null){
			MsgInfo error = new MsgInfo();
			error.setComponentid("code");
			error.setErrcode("dbaccess.recordNotExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", code + "胶水原材料物料号"));
			errors.add(error);
		}else{
			listResponse.put("partial", partialEntity);
		}
		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);
		
		log.info("GlueMixingAction.checkCode end");
	}
	
	/**
	 * 检查有效期
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void checkExpiration(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSession conn)throws Exception{
		log.info("GlueMixingAction.checkExpiration start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		GlueMixingForm glueMixingForm = (GlueMixingForm)form;
		
		GlueMixingService service = new GlueMixingService();
		service.checkExpiration(glueMixingForm, errors);
		
		if(errors.size() == 0){
			// 取得调制品名自动补词数据
			Map<String, String[]> resultMap = service.getAutocomp(glueMixingForm,conn);
			// 查询结果放入Ajax响应对象
			listResponse.put("autocomp", resultMap);
		}
		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);
		
		log.info("GlueMixingAction.checkExpiration end");
	}
	
	/**
	 * 开始调制
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */ 
	public void doStart(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSessionManager conn)throws Exception{
		log.info("GlueMixingAction.doStart start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("code", new RequiredValidator("胶水原材料物料号"));
		v.add("expiration", new RequiredValidator("有效期"));
		v.add("lot_no", new RequiredValidator("LOT NO."));
		v.add("binder_name", new RequiredValidator("调制品名"));
		
		List<MsgInfo> errors = v.validate();
		
		if(errors.size() == 0){
			GlueMixingService service = new GlueMixingService();
			GlueMixingForm glueMixingForm = (GlueMixingForm)form;
			service.startGlueMixing(glueMixingForm, request,conn);
			
			listResponse.put("glueMixingForm", glueMixingForm);
		}
		
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);
		
		log.info("GlueMixingAction.doStart end");
	}
	
	/**
	 * 暂停调制
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void doPause(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSessionManager conn)throws Exception{
		log.info("GlueMixingAction.doPause start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("glue_mixing_process_id", new RequiredValidator("胶水调制作业 ID"));
				
		List<MsgInfo> errors = v.validate();
		
		if(errors.size() == 0){
			GlueMixingService service = new GlueMixingService();
			service.pauseGlueMixing(form, request, conn);
		}
		
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);
		
		log.info("GlueMixingAction.doPause end");
	}
	
	/**
	 * 重开调制
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void doContinue(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSessionManager conn)throws Exception{
		log.info("GlueMixingAction.doContinue start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("glue_mixing_process_id", new RequiredValidator("胶水调制作业 ID"));
				
		List<MsgInfo> errors = v.validate();
		
		if(errors.size() == 0){
			GlueMixingService service = new GlueMixingService();
			service.continueGlueMixing(form, request, conn);
		}
		
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);
		
		log.info("GlueMixingAction.doContinue end");
	}
	
	/**
	 * 完成调制
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void doFinish(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSessionManager conn)throws Exception{
		log.info("GlueMixingAction.doFinish start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("glue_mixing_process_id", new RequiredValidator("胶水调制作业 ID"));
				
		List<MsgInfo> errors = v.validate();
		
		if(errors.size() == 0){
			GlueMixingService service = new GlueMixingService();
			service.finishGlueMixing(form,request, conn);
		}
		
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);
		
		log.info("GlueMixingAction.doFinish end");
	}
	
}
