package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.inline.MaterialProcessAssignEntity;
import com.osh.rvs.bean.master.LightFixEntity;
import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.bean.master.ProcessAssignEntity;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.inline.MaterialProcessAssignForm;
import com.osh.rvs.form.master.LightFixForm;
import com.osh.rvs.form.master.ProcessAssignForm;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.inline.MaterialCommentMapper;
import com.osh.rvs.mapper.inline.MaterialProcessAssignMapper;
import com.osh.rvs.mapper.master.LightFixMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.message.ApplicationMessage;

/**
 * 
 * @Title MaterialProcessAssignService.java
 * @Project rvs
 * @Package com.osh.rvs.service
 * @ClassName: MaterialProcessAssignService
 * @Description: 维修对象独有修理流程
 * @author gonglm
 * @date 2021-6-19 下午4:02:48
 */
public class MaterialProcessAssignService {
	
	/**
	 * 小修理标准编制
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<LightFixForm> searchLightFixs(ActionForm form,SqlSession conn){
		LightFixEntity lightFixEntity = new LightFixEntity();
		//复制表单数据到对象
		BeanUtil.copyToBean(form, lightFixEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		//小修理标准编制
		LightFixMapper lightFixMapper = conn.getMapper(LightFixMapper.class);
		List<LightFixEntity> list = lightFixMapper.getLightFixByMaterialId(lightFixEntity);
		List<LightFixForm> rList = new ArrayList<LightFixForm>();

		CopyOptions include = new CopyOptions();
		include.excludeEmptyString(); include.excludeNull();
		include.exclude("category_id", "material_id");

		//复制数据表单对象
		BeanUtil.copyToFormList(list, rList, include, LightFixForm.class);

		Map<String,LightFixForm> lightFixMap = new TreeMap<String,LightFixForm>();
		for(int i=0;i<rList.size();i++){
			LightFixForm lightFixForm = rList.get(i);
			String activity_code = lightFixForm.getActivity_code();//code
			
			String position_id = lightFixForm.getPosition_id();//工位
			if(position_id!=null){
				position_id = position_id.replaceAll("^0*", "");
			}

			String key = activity_code;
			if(lightFixMap.containsKey(key)){
				if(position_id!=null) lightFixMap.get(key).getPosition_list().add(position_id);
			}else{
				if(position_id!=null){
					lightFixForm.getPosition_list().add(position_id);
					lightFixMap.put(key, lightFixForm);
				}else{
					lightFixMap.put(key, lightFixForm);
				}
			}
		}
		
		rList = converMapToList(lightFixMap);
		for (LightFixForm item : rList) {
			item.setPosition_id(null);
		}
		
		return rList;
	}

	public List<MaterialProcessAssignForm> searchMaterialProcessAssign(ActionForm form,SqlSession conn){
		MaterialProcessAssignEntity entity = new MaterialProcessAssignEntity();
		//复制表单数据到对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		MaterialProcessAssignMapper materialProcessAssignMapper = conn.getMapper(MaterialProcessAssignMapper.class);
		
		List<MaterialProcessAssignEntity> list = materialProcessAssignMapper.searchMaterialProcessAssign(entity);
		
		List<MaterialProcessAssignForm> rList = new ArrayList<MaterialProcessAssignForm>();
		if(list.size()>0){
			BeanUtil.copyToFormList(list, rList, CopyOptions.COPYOPTIONS_NOEMPTY, MaterialProcessAssignForm.class);
			for(int i= 0;i<rList.size();i++){
				MaterialProcessAssignForm connForm = rList.get(i);
				String position_id = connForm.getPosition_id();//工位
				position_id = position_id.replaceAll("^0*", "");
				rList.get(i).setPosition_id(position_id);
			}
		}
		
		return rList;
	}
	
	
	/**
	 * 查询维修对象选用小修理
	 * @param form
	 * @param conn
	 * @return
	 */
	public List<MaterialProcessAssignForm> searchMaterialLightFix(ActionForm form,SqlSession conn){
		MaterialProcessAssignEntity entity = new MaterialProcessAssignEntity();
		//复制表单数据到对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		MaterialProcessAssignMapper materialProcessAssignMapper = conn.getMapper(MaterialProcessAssignMapper.class);
		List<MaterialProcessAssignEntity> list = materialProcessAssignMapper.searchMaterialLightFix(entity);
		
		
		List<MaterialProcessAssignForm> rList = new ArrayList<MaterialProcessAssignForm>();
		if(list.size()>0){
			BeanUtil.copyToFormList(list, rList, CopyOptions.COPYOPTIONS_NOEMPTY, MaterialProcessAssignForm.class);
		}
		
		return rList;
	}
	 
	/**
	 * Map集合转换List
	 * @param map
	 * @return
	 */
	private <T> List<T> converMapToList(Map<String,T> map){
		Set<String> set = map.keySet();
		Iterator<String> iter = set.iterator();
		
		List<T> list = new ArrayList<T>();
		
		while(iter.hasNext()){
			list.add(map.get(iter.next()));
		}
		return list;
	}

	/**
	 * 重新设定流程
	 * @param material_id
	 * @param request
	 * @param conn
	 * @param renew 重新建立小修流程
	 * @throws Exception
	 */
	public void updateProcessAssign(String material_id, HttpServletRequest request,SqlSessionManager conn, boolean renew) throws Exception {
		MaterialProcessAssignMapper materialProcessAssignMapper = conn.getMapper(MaterialProcessAssignMapper.class);

		List<MaterialProcessAssignForm> lighFixList = new AutofillArrayList<MaterialProcessAssignForm>(MaterialProcessAssignForm.class);
		List<MaterialProcessAssignForm> processAssignList = new AutofillArrayList<MaterialProcessAssignForm>(MaterialProcessAssignForm.class);
		
		Map<String,String[]> map=(Map<String,String[]>)request.getParameterMap();
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		// 整理提交数据
		for (String parameterKey : map.keySet()) {
			 Matcher m = p.matcher(parameterKey);
			 if (m.find()) {
				 String table = m.group(1);
				 if ("material_light_fix".equals(table)) {//维修对象选用小修理
				     String column = m.group(2);
				     int icounts = Integer.parseInt(m.group(3));
					 String[] value = map.get(parameterKey);
					 if ("light_fix_id".equals(column)) {
						 lighFixList.get(icounts).setLight_fix_id(value[0]);
					 }
				 }else if("material_process_assign".equals(table)) {//维修对象独有修理流程
					 String column = m.group(2);
				     int icounts = Integer.parseInt(m.group(3));
					 String[] value = map.get(parameterKey);
					 if ("position_id".equals(column)) {
						 processAssignList.get(icounts).setPosition_id(value[0]);
					 } else if ("line_id".equals(column)) {
						 processAssignList.get(icounts).setLine_id(value[0]);
					 } else if ("next_position_id".equals(column)) {
						 processAssignList.get(icounts).setNext_position_id(value[0]);
					 } else if ("prev_position_id".equals(column)) {
						 processAssignList.get(icounts).setPrev_position_id(value[0]);
					 } else if ("sign_position_id".equals(column)) {
						 processAssignList.get(icounts).setSign_position_id(value[0]);
					 }
				 }
			 }
		}

		List<String> oldHasLines = null;

		MaterialProcessService mpService = new MaterialProcessService();
		if (processAssignList.size() > 0) {
			oldHasLines = mpService.loadMaterialProcessLineIds(material_id, conn); // 取得已存在工程
		}

		if (renew) {
			if (lighFixList.size() > 0) {
				//删除维修对象选用小修理
				materialProcessAssignMapper.deleteMaterialLightFix(material_id);
			}
			if (processAssignList.size() > 0) {
				//删除维修对象独有修理流程
				materialProcessAssignMapper.deleteMaterialProcessAssign(material_id);
			}
		} else {
			//删除维修对象选用小修理
			materialProcessAssignMapper.deleteMaterialLightFix(material_id);
			//删除维修对象独有修理流程
			materialProcessAssignMapper.deleteMaterialProcessAssign(material_id);
		}

		//新建维修对象选用小修理
		for(MaterialProcessAssignForm connForm:lighFixList){
			MaterialProcessAssignEntity entity = new MaterialProcessAssignEntity();
			BeanUtil.copyToBean(connForm, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
			entity.setMaterial_id(material_id);
			materialProcessAssignMapper.insertMaterialLightFix(entity);
		}
		
		//新建维修对象独有修理流程
		if (processAssignList.size() > 0) {

			for(MaterialProcessAssignForm processAssignForm:processAssignList){
				MaterialProcessAssignEntity entity = new MaterialProcessAssignEntity();
				BeanUtil.copyToBean(processAssignForm, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
				String position_id = entity.getPosition_id();
				if (position_id == null) continue;
				entity.setMaterial_id(material_id);

				if (entity.getSign_position_id() == null) 
					entity.setSign_position_id(position_id);
				if (entity.getPrev_position_id() == null)
					entity.setPrev_position_id("0");
				if (entity.getNext_position_id() == null)
					entity.setNext_position_id("9999999");
				materialProcessAssignMapper.insertMaterialProcessAssign(entity);
			}

			oldHasLines = mpService.loadMaterialProcessLineIds(material_id, conn); // 取得已存在工程

			List<String> newHasLines = new ArrayList<String>();
			if (renew && oldHasLines != null && oldHasLines.size() > 0) { // 未投线不需要
				newHasLines = this.checkPatHasLine(material_id, conn); // 新流程对应工程
				mpService.resignMaterialProcess(material_id, oldHasLines, newHasLines, conn);
			}
		}

		MaterialService materialService = new MaterialService();
		// 设定为系统
		String operator_id = "00000000001";
		// 得到小修理信息
		MaterialProcessAssignService mpas = new MaterialProcessAssignService();
		String lightFixStr = mpas.getLightFixesByMaterial(material_id, null, conn);

		String lightFlowStr = request.getParameter("flow_str");
		if (lightFlowStr == null) {
			lightFlowStr = mpas.getLightFixFlowByMaterial(material_id, null, conn);
		}

		String comment = getLightStr(lightFixStr, lightFlowStr);
		materialService.updateMaterialComment(material_id, operator_id, comment, conn);
	}

	private static final int COMMENT_MAX_WIDTH = 250; 
	public String getLightStr(String lightFixStr, String lightFlowStr) {
		String comment = (lightFixStr == null ? "" : "修理项目为：" + lightFixStr + "\n")
				+ "修理工位流程为：" + lightFlowStr;
		if (comment.length() > COMMENT_MAX_WIDTH) {
			if (lightFixStr == null) {
				if (lightFlowStr.length() > COMMENT_MAX_WIDTH) {
					comment = lightFlowStr.substring(0, COMMENT_MAX_WIDTH - 2) + "…";
				} else {
					comment = lightFlowStr;
				}
			} else {
				int subend = COMMENT_MAX_WIDTH - "修理项目为：\n修理工位流程为：".length() - lightFlowStr.length() - 2;
				if (subend > 0) {
					lightFixStr = lightFixStr.substring(0, subend)
							+ "…";
					comment = (lightFixStr == null ? "" : "修理项目为：" + lightFixStr + "\n")
							+ "修理工位流程为：" + lightFlowStr;
				} else {
					if (lightFlowStr.length() > COMMENT_MAX_WIDTH) {
						
						comment = lightFlowStr.substring(0, COMMENT_MAX_WIDTH); 
					} else {
						comment = lightFlowStr;
					}
				}
			}
		}
		return comment;
	}
	/**
	 * 查询流程包含工程
	 * @param material_id
	 * @param conn
	 * @return
	 */
	private List<String> checkPatHasLine(String materialId, SqlSession conn) {
		List<String> ret = new ArrayList<String>();

		MaterialProcessAssignMapper mapper = conn.getMapper(MaterialProcessAssignMapper.class);
		List<LineEntity> lines = mapper.getWorkedLines(materialId);

		for (LineEntity line : lines) {
			ret.add(line.getLine_id());
		}
		return ret;
	}

	public void update(ActionForm form,HttpServletRequest request,SqlSessionManager conn)throws Exception{
		MaterialProcessAssignEntity entity = new MaterialProcessAssignEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		String material_id = entity.getMaterial_id();//维修对象 ID
		String pat_id = entity.getPad_id();

		updateProcessAssign(material_id, request, conn, false);

		MaterialMapper materialMapper = conn.getMapper(MaterialMapper.class);
		
		materialMapper.updateMaterialPat(material_id, pat_id);
		
	}
	
	/**
	 * check D级流水线materialForm
	 * @param material_id
	 * @param conn
	 */
	public void checkDAndInline(ActionForm form,HttpServletRequest request,SqlSession conn,List<MsgInfo> errors){
		MaterialProcessAssignForm materialProcessAssignForm = (MaterialProcessAssignForm)form;
		String level = materialProcessAssignForm.getLevel();//等级
		String fix_type = materialProcessAssignForm.getFix_type();//修理方式
		String pat_id = materialProcessAssignForm.getPad_id();//维修流程
		boolean isLightFix = RvsUtils.isLightFix(level);

		if (isLightFix && "1".equals(fix_type)) {
			if(CommonStringUtil.isEmpty(pat_id)){
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setErrcode("validator.required");
				msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "维修流程"));
				errors.add(msgInfo);
			}
			
			List<MaterialProcessAssignForm> lighFixList = new AutofillArrayList<MaterialProcessAssignForm>(MaterialProcessAssignForm.class);
			Map<String,String[]> map=(Map<String,String[]>)request.getParameterMap();
			Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
			for (String parameterKey : map.keySet()) {
				 Matcher m = p.matcher(parameterKey);
				 if (m.find()) {
					 String table = m.group(1);
					 if ("material_light_fix".equals(table)) {//维修对象选用小修理
					     String column = m.group(2);
					     int icounts = Integer.parseInt(m.group(3));
						 String[] value = map.get(parameterKey);
						 if ("light_fix_id".equals(column)) {
							 lighFixList.get(icounts).setLight_fix_id(value[0]);
						 }
					 }
				 }
			}
			
			if(lighFixList.size()==0){
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setErrcode("validator.required.multidetail");
				msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.multidetail", "中小修理流程"));
				errors.add(msgInfo);
			}
		}
	}

	public List<ProcessAssignForm> getAssigns(String material_id, SqlSession conn) {
		// 从数据库中查询记录
		MaterialProcessAssignMapper mapper = conn.getMapper(MaterialProcessAssignMapper.class);
		List<ProcessAssignEntity> entities = mapper.getProcessAssignByMaterialID(material_id);
		List<ProcessAssignForm> result = new ArrayList<ProcessAssignForm>();
		BeanUtil.copyToFormList(entities, result, null, ProcessAssignForm.class);
		return result;
	}

	public List<String> getFirstPositionId(String material_id, SqlSession conn) {
		MaterialProcessAssignMapper mapper = conn.getMapper(MaterialProcessAssignMapper.class);
		List<ProcessAssignEntity> firstPositions = mapper.getFirstPositions(material_id);
		List<String> ret = new ArrayList<String>();

		for (ProcessAssignEntity firstPosition : firstPositions) {
			String posId = firstPosition.getPosition_id();
			if (posId.length() > 6 && posId.startsWith("9")) {
				List<String> partStart = mapper.getPartStart(material_id, posId);
				ret.addAll(partStart);
			} else {
				ret.add(posId);
			}
		}

		return ret;
	}

	public String getLightFixesByMaterial(String material_id, String position_id,
			SqlSession conn) {
		MaterialProcessAssignMapper mapper = conn.getMapper(MaterialProcessAssignMapper.class);
		return mapper.getLightFixesByMaterial(material_id, position_id);
	}

	/**
	 * 取得维修对象已选择修理内容的全工位（包括当前流程用的和不用的），返工时切换参考流程用
	 * 
	 * @param material_id
	 * @param conn
	 * @return
	 */
	public List<String> getLightPositionsByMaterial(String material_id,
			SqlSession conn) {
		MaterialProcessAssignMapper mapper = conn.getMapper(MaterialProcessAssignMapper.class);
		return mapper.getLightPositionsByMaterial(material_id);
	}

	private static final String FROM_COMMET = "工位流程为：";

	/**
	 * 取得完整小修理流程
	 * @param material_id
	 * @param now_process_code
	 * @param conn
	 * @return
	 */
	public String getLightFixFlowByMaterial(String material_id,
			String now_process_code, SqlSession conn) {
		MaterialCommentMapper mcMapper = conn.getMapper(MaterialCommentMapper.class);
		String cmt = mcMapper.getMyMaterialComment(material_id, "00000000001");
		if (cmt == null) {
			return "";
		}

		String flowText = cmt.substring(cmt.indexOf(FROM_COMMET) + 6);

		if (now_process_code == null) return flowText;

		Set<String> finishPos = new HashSet<String>();
		Set<String> processedPos = new HashSet<String>();
		if (flowText.indexOf("=") >= 0) {
			MaterialProcessAssignMapper mapper = conn.getMapper(MaterialProcessAssignMapper.class);
			List<ProductionFeatureEntity> pl = mapper.getWorkedProcess(material_id);
			for (ProductionFeatureEntity pos : pl) {
				if (pos.getOperate_result() == 1) {
					finishPos.add(pos.getProcess_code());
				} else if (pos.getPace() == 1) {
					processedPos.add(pos.getProcess_code());
				}
			}
		}

		Pattern pMultiTags = Pattern.compile("\\d{3}");
		Matcher mMultiTags = pMultiTags.matcher(flowText);
		StringBuffer sbRemoveMulti = new StringBuffer("");
		while (mMultiTags.find()) {
			String hitText = mMultiTags.group();
			if (now_process_code.equals(hitText)) {
				mMultiTags.appendReplacement(sbRemoveMulti, "<span style='font-weight:bold; text-decoration: underline;'>" + hitText + "</span>");
			} else if (finishPos.contains(hitText)) {
				mMultiTags.appendReplacement(sbRemoveMulti, "<span style='color:green; font-weight:bold;'>" + hitText + "</span>");
			} else if (processedPos.contains(hitText)) {
				mMultiTags.appendReplacement(sbRemoveMulti, "<span style='color:blue; font-style: italic;'>" + hitText + "</span>");
			} else {
				mMultiTags.appendReplacement(sbRemoveMulti, hitText);
			}
		}
		if (sbRemoveMulti.length() > 0) {
			mMultiTags.appendTail(sbRemoveMulti);
			flowText = sbRemoveMulti.toString();
		}

		return flowText;
	}
}
