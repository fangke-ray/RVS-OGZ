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

import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.form.inline.MaterialProcessAssignForm;
import com.osh.rvs.service.MaterialProcessAssignService;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.ProcessAssignService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.validator.Validators;

/**
 * 
 * @Title MaterialProcessAssignAction.java
 * @Project rvs
 * @Package com.osh.rvs.action.inline
 * @ClassName: MaterialProcessAssignAction
 * @Description: 维修对象独有修理流程
 * @author lxb
 * @date 2015-8-19 下午3:02:00
 */
public class MaterialProcessAssignAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());

	/**
	 * 获取修理维修内容流程
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void getDetail(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSession conn)throws Exception{
		log.info("MaterialProcessAssignAction.getDetail start");

		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v != null ? v.validate(): new ArrayList<MsgInfo>();

		if(errors.size()==0){
			MaterialProcessAssignService service = new MaterialProcessAssignService();
			List<MaterialProcessAssignForm> processAssigns = service.searchMaterialProcessAssign(form, conn);

			MaterialService materialService = new MaterialService();
			MaterialProcessAssignForm materialProcessAssignForm = (MaterialProcessAssignForm)form;
			MaterialForm materialForm = materialService.loadSimpleMaterialDetail(conn, materialProcessAssignForm.getMaterial_id());

			callbackResponse.put("processAssigns", processAssigns);
			callbackResponse.put("materialForm", materialForm);
		}
		
		//在线工位
		ProcessAssignService processAssignService = new ProcessAssignService();
		List<Map<String, String>> positions = processAssignService.getInlinePositions(conn);
		for (Map<String, String> position : positions) {
			String process_code = position.get("process_code");
			if (process_code != null) {
				position.put("text",  process_code + "\n" + position.get("text"));
			}
		}
		
		
		String category_kind = CodeListUtils.getGridOptions("category_kind");//机种分类
		String arr[]=category_kind.split(";");
		Map<String,String> map = new HashMap<String,String>();
		for(int i=0;i<arr.length;i++){
			String [] value = arr[i].split(":");
			map.put(value[0], value[1]);
			
		}
		callbackResponse.put("categoryKindMap", map);

		// 查询结果放入Ajax响应对象
		callbackResponse.put("list", positions);

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(response, callbackResponse);

		log.info("MaterialProcessAssignAction.getDetail end");
	}

	public void doUpdateMaterialProcessAssign(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSessionManager conn)throws Exception{
		log.info("MaterialProcessAssignAction.doUpdateMaterialProcessAssign start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v != null ? v.validate(): new ArrayList<MsgInfo>();

		MaterialProcessAssignService service = new MaterialProcessAssignService();
		service.checkDAndInline(form ,request, conn, errors);

		if(errors.size()==0){
			service.update(form, request, conn);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(response, callbackResponse);

		log.info("MaterialProcessAssignAction.doUpdateMaterialProcessAssign start");
	}

}
