package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.inline.MaterialProcessAssignEntity;
import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.bean.master.ProcessAssignEntity;
import com.osh.rvs.form.inline.MaterialProcessAssignForm;
import com.osh.rvs.form.master.ProcessAssignForm;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.inline.MaterialProcessAssignMapper;
import com.osh.rvs.mapper.master.PositionMapper;

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
 * @author lxb
 * @date 2015-8-19 下午4:02:48
 */
public class MaterialProcessAssignService {
	
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
	 * Map集合转换List
	 * @param map
	 * @return
	 */
	@SuppressWarnings("unused")
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
	 * 更新维修对象流程与维修对象工程计划
	 * @param material_id
	 * @param request
	 * @param conn
	 * @param renew 流程已开始
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
					 } else if ("next_position_id".equals(column)) {
						 processAssignList.get(icounts).setNext_position_id(value[0]);
					 } else if ("prev_position_id".equals(column)) {
						 processAssignList.get(icounts).setPrev_position_id(value[0]);
					 }
				 }
			 }
		}
		

		//新建维修对象独有修理流程
		if (processAssignList.size() > 0) {

			for(MaterialProcessAssignForm processAssignForm:processAssignList){
				MaterialProcessAssignEntity entity = new MaterialProcessAssignEntity();
				BeanUtil.copyToBean(processAssignForm, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
				String position_id = entity.getPosition_id();
				if (position_id == null) continue;
				entity.setMaterial_id(material_id);
				entity.setLine_id("9000000");
				entity.setSign_position_id(position_id);
				if (entity.getPrev_position_id() == null)
					entity.setPrev_position_id("0");
				if (entity.getNext_position_id() == null)
					entity.setNext_position_id("9999999");
				materialProcessAssignMapper.insertMaterialProcessAssign(entity);
			}

			MaterialProcessService mpService = new MaterialProcessService();
			List<String> oldHasLines = mpService.loadMaterialProcessLineIds(material_id, conn); // 取得已存在工程

			List<String> newHasLines = new ArrayList<String>();
			if (renew && oldHasLines != null && oldHasLines.size() > 0) { // 未投线不需要
				newHasLines = this.checkPatHasLine(material_id, conn); // 新流程对应工程
				mpService.resignMaterialProcess(material_id, oldHasLines, newHasLines, conn);
			}
		}
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
		
		if(("9".equals(level) || "91".equals(level) || "92".equals(level) || "93".equals(level)) && "1".equals(fix_type)){
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
				msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.multidetail", "小修理流程"));
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

	public String getFirstPositionId(String material_id, SqlSession conn) {
		MaterialProcessAssignMapper mapper = conn.getMapper(MaterialProcessAssignMapper.class);
		ProcessAssignEntity firstPosition = mapper.getFirstPosition(material_id);

//		if ("25".equals(firstPosition.getPosition_id())
//					|| "00000000025".equals(firstPosition.getPosition_id()))
//			return firstPosition.getNext_position_id();
//		else if ("60".equals(firstPosition.getPosition_id())
//				|| "00000000060".equals(firstPosition.getPosition_id()))  {
//			if ("25".equals(firstPosition.getNext_position_id())
//					|| "00000000025".equals(firstPosition.getNext_position_id())) {
//				return firstPosition.getSign_position_id();
//			} else {
//				return firstPosition.getNext_position_id();
//			}
//		}
//		else 
		return firstPosition.getPosition_id();
	}

//	public String getBeforeInlinePositions(String material_id, SqlSession conn) {
//		MaterialProcessAssignMapper mapper = conn.getMapper(MaterialProcessAssignMapper.class);
//		ProcessAssignEntity firstPosition = mapper.getFirstPosition(material_id);
//
//		if ("25".equals(firstPosition.getPosition_id())
//			|| "00000000025".equals(firstPosition.getPosition_id()))
//			return "CCD";
//		else if ("60".equals(firstPosition.getPosition_id())
//			|| "00000000060".equals(firstPosition.getPosition_id()))  {
//			if ("25".equals(firstPosition.getNext_position_id())
//					|| "00000000025".equals(firstPosition.getNext_position_id())) {
//				return "LG+CCD";
//			} else {
//				return "LG";
//			}
//		}
//		return "";
//	}

	/**
	 * 取得完整小修理流程
	 * @param material_id
	 * @param now_process_code
	 * @param conn
	 * @return
	 */
	public String getLightFixFlowByMaterial(String material_id,
			String now_process_code, SqlSessionManager conn) {
		MaterialProcessAssignMapper mapper = conn.getMapper(MaterialProcessAssignMapper.class);
		ProcessAssignEntity checkedPosition = mapper.getFirstPosition(material_id);
		if (checkedPosition == null)
			return null;

		PositionMapper pMapper = conn.getMapper(PositionMapper.class);
		String position_id = checkedPosition.getPosition_id();
		String ret = getProcessInterf(pMapper.getPositionByID(position_id), now_process_code);
		while (position_id != null) {
			List<PositionEntity> nextPositions = mapper.getNextPositions(material_id, position_id);
			if (nextPositions.size() > 0) {
				for (PositionEntity nextPosition : nextPositions) {
					ret += " -> " + getProcessInterf(nextPosition, now_process_code);
					position_id = nextPosition.getPosition_id();
				}
			} else {
				position_id = null;
			}
		}
		return ret;
	}

	private String getProcessInterf(PositionEntity position,
			String now_process_code) {
		if (position == null)
			return "";
		if (now_process_code == null || !now_process_code.equals(position.getProcess_code())) {
			if (position.getLight_division_flg() == 1) {
				return position.getProcess_code() + "B";
			} else {
				return position.getProcess_code();
			}
		}
		else {
			if (position.getLight_division_flg() != null && position.getLight_division_flg() == 1) {
				return "<span style='font-weight:bold; text-decoration: underline;'>" + position.getProcess_code() + "B</span>";
			} else {
				return "<span style='font-weight:bold; text-decoration: underline;'>" + position.getProcess_code() + "</span>";
			}
		}
	}

}
