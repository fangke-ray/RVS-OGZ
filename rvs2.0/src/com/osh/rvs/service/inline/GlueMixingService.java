package com.osh.rvs.service.inline;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.validator.DateValidator;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.inline.GlueMixingProcessEntity;
import com.osh.rvs.bean.inline.GlueMixingProcessPaceEntity;
import com.osh.rvs.bean.inline.PauseFeatureEntity;
import com.osh.rvs.bean.master.GlueMixingTypeEntity;
import com.osh.rvs.bean.master.PartialEntity;
import com.osh.rvs.bean.partial.GlueEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.inline.GlueMixingForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.inline.GlueMixingProcessMapper;
import com.osh.rvs.mapper.inline.GlueMixingProcessPaceMapper;
import com.osh.rvs.mapper.inline.PauseFeatureMapper;
import com.osh.rvs.mapper.master.GlueMixingTypeMapper;
import com.osh.rvs.mapper.master.PartialMapper;
import com.osh.rvs.mapper.partial.GlueMapper;
import com.osh.rvs.service.PauseFeatureService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

/**
 * @Title: GlueMixingService.java
 * @Package com.osh.rvs.service.inline
 * @Description: 胶水调制
 * @author liuxb
 * @date 2017-12-14 下午4:48:55
 */
public class GlueMixingService {
	
	/**
	 * 获取当前操作者胶水调制信息 
	 * @param listResponse
	 * @param request
	 * @param conn
	 */
	public void getGlueMixing(Map<String, Object> listResponse,HttpServletRequest request,SqlSession conn){
		// 获取数据库连接
		PartialMapper partialMapper = conn.getMapper(PartialMapper.class);
		GlueMapper glueMapper = conn.getMapper(GlueMapper.class);
		GlueMixingTypeMapper glueMixingTypeMapper = conn.getMapper(GlueMixingTypeMapper.class);
		GlueMixingProcessMapper glueMixingProcessMapper = conn.getMapper(GlueMixingProcessMapper.class);
		GlueMixingProcessPaceMapper glueMixingProcessPaceMapper = conn.getMapper(GlueMixingProcessPaceMapper.class);
		
		// 获取当前操作者信息
		LoginData loginData = (LoginData)request.getSession().getAttribute(RvsConsts.SESSION_USER);
		// 操作者ID
		String operatorId = loginData.getOperator_id();
		
		// 1、查询登录者未调制完成的胶水
		GlueMixingProcessEntity glueMixingProcessEntity = glueMixingProcessMapper.getUnFinishGlueMixing(operatorId);
		
		// 2、没有未完成的胶水
		if(glueMixingProcessEntity == null){
			listResponse.put("state", "no");
			return;
		}
		
		// 胶水调制作业 ID
		String glueMixingProcessId = glueMixingProcessEntity.getGlue_mixing_process_id();
		
		// 3、根据胶水调制作业ID获取胶水调制作业时间未完成信息
		GlueMixingProcessPaceEntity glueMixingProcessPaceEntity  =glueMixingProcessPaceMapper.getUnFinishById(glueMixingProcessId);
		
		// 不存在胶水调制作业时间未完成信息
		if(glueMixingProcessPaceEntity == null){
			// 状态为重开
			listResponse.put("state", "continue");
		}else{
			// 状态为暂停
			listResponse.put("state", "pause");
		}
		
		// 乙材种类 ID
		String glueId = glueMixingProcessEntity.getGlue_id();
		
		// 胶水调制种类 ID
		String glueMixingTypeId = glueMixingProcessEntity.getGlue_mixing_type_id();
		
		// 4、根据胶水ID查询胶水信息
		GlueEntity glueEntity = glueMapper.getGlueByGlueId(glueId);
		
		// 有效期
		String expiration = DateUtil.toString(glueEntity.getExpiration(), "yyyyMMdd");
		
		// LOT NO.
		String lotNo = glueEntity.getLot_no();
		
		// 5、根据胶水调制种类ID查询胶水调制种类信息
		GlueMixingTypeEntity glueMixingTypeEntity = glueMixingTypeMapper.getGlueMixingTypeById(glueMixingTypeId);
		
		// 调制品名
		String binderName = glueMixingTypeEntity.getBinder_name();
		
		// 原料胶水ID
		String partialId = glueMixingTypeEntity.getPartial_id();
		
		// 6、根据零件ID查询零件信息
		PartialEntity partialEntity = partialMapper.getPartialByID(partialId);
		
		// 胶水原材料物料号
		String code = partialEntity.getCode();
		
		
		GlueMixingForm glueMixingForm = new GlueMixingForm();
		
		// 胶水调制作业 ID
		glueMixingForm.setGlue_mixing_process_id(glueMixingProcessId);
		
		// 胶水原材料物料号
		glueMixingForm.setCode(code);
		
		// 零件ID
		glueMixingForm.setPartial_id(partialId);
		
		// 有效期
		glueMixingForm.setExpiration(expiration);
		
		// LOT NO.
		glueMixingForm.setLot_no(lotNo);
		
		// 调制品名
		glueMixingForm.setBinder_name(binderName);
		
		listResponse.put("glueMixingForm", glueMixingForm);
		
	}
	

	/**
	 * 有效期格式检查
	 * 
	 * @param form
	 * @param errors
	 * @param conn
	 */
	public void checkExpiration(GlueMixingForm glueMixingForm, List<MsgInfo> errors) {
		// 有效期
		String strExpiration = glueMixingForm.getExpiration();
		
		// 有效期为空
		if(CommonStringUtil.isEmpty(strExpiration)){
			MsgInfo error = new MsgInfo();
			error.setComponentid("expiration");
			error.setErrcode("dbaccess.recordNotExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "有效期"));
			errors.add(error);
		}else{
			Date dateExpiration = this.expirationFormatValid(strExpiration);
			
			// 验证通过，则判断有效期是否过期
			if(dateExpiration != null){
				//当前日期
				Calendar now = Calendar.getInstance();
				now.set(Calendar.HOUR_OF_DAY, 0);
				now.set(Calendar.MINUTE, 0);
				now.set(Calendar.SECOND, 0);
				now.set(Calendar.MILLISECOND, 0);
				
				// 有效期与当前日期比较
				int over = DateUtil.compareDate(dateExpiration, now.getTime());
				
				// “0”代表有效期小于当前日期，表示有效期过期了
				if(over < 0){
					MsgInfo error = new MsgInfo();
					error.setComponentid("expiration");
					error.setErrcode("info.partial.glueOverTime");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.partial.glueOverTime"));
					errors.add(error);
				}
			}else{
				// 格式不正确
				MsgInfo error = new MsgInfo();
				error.setComponentid("expiration");
				error.setErrcode("validator.invalidParam.invalidDateValue");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidDateValue", "有效期","[yyyyMMdd、MMddyyyy、yyMMdd]其中一种"));
				errors.add(error);
			}
		}
	}
	
	/**
	 * 有效期格式验证
	 * @param strDate
	 * @return 格式正确返回Date,不正确返回NULL
	 */
	private Date expirationFormatValid(String strDate){
		Date date = null;
		
		// 有效期格式验证
		DateValidator dateValidator = DateValidator.getInstance();
		
		// 有效期格式选项
		String [] arrDatePattern = {"yyyyMMdd","MMddyyyy","yyMMdd"};
		
		// 循环验证格式，满足其中一种格式则通过验证
		for(int i = 0;i < arrDatePattern.length;i++){
			boolean validFlg = dateValidator.isValid(strDate, arrDatePattern[i], true);
			
			// 验证通过跳出循环,不再验证
			if(validFlg){
				date = DateUtil.toDate(strDate, arrDatePattern[i]);
				break;
			}
		}
		
		return date;
	}
	
	/**
	 * 取得自动补词数据
	 * @param conn
	 * @return
	 */
	public Map<String, String[]> getAutocomp(GlueMixingForm glueMixingForm,SqlSession conn) {
		Map<String, String[]> mRet = new HashMap<String, String[]>();
		GlueMixingTypeMapper glueMixingTypeMapper = conn.getMapper(GlueMixingTypeMapper.class);
		
		// 取得调制品名自动补词数据
		mRet.put("autoBinderNames", glueMixingTypeMapper.getBinderNameAutoCompletes(glueMixingForm.getPartial_id()));
		
		return mRet;
	}
	
	/**
	 * 调胶作业开始
	 * @param form
	 * @param request
	 * @param errors
	 * @param conn
	 */
	public void startGlueMixing(GlueMixingForm glueMixingForm,HttpServletRequest request,SqlSessionManager conn){
		// 获取数据库连接
		GlueMapper glueMapper = conn.getMapper(GlueMapper.class);
		GlueMixingTypeMapper glueMixingTypeMapper = conn.getMapper(GlueMixingTypeMapper.class);
		GlueMixingProcessMapper glueMixingProcessMapper = conn.getMapper(GlueMixingProcessMapper.class);
		GlueMixingProcessPaceMapper glueMixingProcessPaceMapper = conn.getMapper(GlueMixingProcessPaceMapper.class);
		CommonMapper commonMapper = conn.getMapper(CommonMapper.class);

		GlueEntity glueEntity = new GlueEntity();
		// 赋值表单数据到对象
		BeanUtil.copyToBean(glueMixingForm, glueEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		// 有效期格式转换
		glueEntity.setExpiration(this.expirationFormatValid(glueMixingForm.getExpiration()));
		
		GlueMixingTypeEntity glueMixingTypeEntity = new GlueMixingTypeEntity();
		// 赋值表单数据到对象
		BeanUtil.copyToBean(glueMixingForm, glueMixingTypeEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		/**实际开始调胶**/
		
		// 胶水ID
		String glueId = null;
		
		// 胶水调制种类ID
		String glueMixingTypeId = null;
		
		// 1、获取当前操作者信息
		LoginData loginData = (LoginData)request.getSession().getAttribute(RvsConsts.SESSION_USER);
		// 操作者ID
		String operatorId = loginData.getOperator_id();
		
		// 2、当前时间
		Calendar currentTime = Calendar.getInstance();
		
		// 3、查询胶水基本信息
		GlueEntity glueEntityTemp = glueMapper.getGlueInfo(glueEntity);
		
		// 胶水基本信息不存在
		if(glueEntityTemp == null){
			// 4、新建胶水
			glueMapper.insert(glueEntity);
			
			//获取新建的胶水ID
			glueId = commonMapper.getLastInsertID();
		}else{
			glueId = glueEntityTemp.getGlue_id();
		}

		// 更新者
		glueMixingTypeEntity.setUpdate_by(operatorId);
		
		// 5、获取胶水调制种类基本信息
		GlueMixingTypeEntity glueMixingTypeEntityTemp = glueMixingTypeMapper.getGlueMixingTypeInfo(glueMixingTypeEntity);
		
		// 胶水调制种类信息不存在
		if(glueMixingTypeEntityTemp == null){
			// 6、新建胶水调制种类
			glueMixingTypeMapper.insert(glueMixingTypeEntity);
			
			// 获取新建的胶水调制种类ID
			glueMixingTypeId = commonMapper.getLastInsertID();
		}else{
			glueMixingTypeId = glueMixingTypeEntityTemp.getGlue_mixing_type_id();
		}
		
		GlueMixingProcessEntity glueMixingProcessEntity = new GlueMixingProcessEntity();
		// 调制原料胶水ID
		glueMixingProcessEntity.setGlue_id(glueId);
		// 胶水调制种类 ID
		glueMixingProcessEntity.setGlue_mixing_type_id(glueMixingTypeId);
		// 调制者
		glueMixingProcessEntity.setMixing_operator_id(operatorId);
		// 调制开始时间
		glueMixingProcessEntity.setMixing_action_time(currentTime.getTime());

		// 结束人员暂停
		PauseFeatureService pauseFeatureService = new PauseFeatureService();
		pauseFeatureService.finishPauseFeature(null, null, null, operatorId, null, conn);

		// 7、新建胶水调制作业
		glueMixingProcessMapper.insert(glueMixingProcessEntity);
		
		// 8、获取新建的胶水调制作业ID
		String glueMixingProcessId = commonMapper.getLastInsertID();
		
		GlueMixingProcessPaceEntity glueMixingProcessPaceEntity = new GlueMixingProcessPaceEntity();
		// 胶水调制作业ID
		glueMixingProcessPaceEntity.setGlue_mixing_process_id(glueMixingProcessId);
		// 分段时间
		glueMixingProcessPaceEntity.setPace(1);
		// 调制者
		glueMixingProcessPaceEntity.setMixing_operator_id(operatorId);
		// 调制开始时间
		glueMixingProcessPaceEntity.setMixing_action_time(currentTime.getTime());
		
		// 9、新建胶水调制作业分段时间 
		glueMixingProcessPaceMapper.insert(glueMixingProcessPaceEntity);
		
		// 10、返回数据给前台页面
		// 胶水调制作业ID
		glueMixingForm.setGlue_mixing_process_id(glueMixingProcessId);
	}
	
	/**
	 * 调胶作业暂停
	 * @param form
	 * @param conn
	 */
	public void pauseGlueMixing(ActionForm form,HttpServletRequest request,SqlSessionManager conn)throws Exception{
		// 获取数据库连接
		GlueMixingProcessPaceMapper glueMixingProcessPaceMapper = conn.getMapper(GlueMixingProcessPaceMapper.class);
		PauseFeatureMapper pauseFeatureMapper = conn.getMapper(PauseFeatureMapper.class);

		// 复制表单数据到对象
		GlueMixingProcessPaceEntity entity = new GlueMixingProcessPaceEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		//根据胶水调制作业ID获取胶水调制作业最大分段
		int pace = glueMixingProcessPaceMapper.getMaxPaceByGlueMixingProcessId(entity.getGlue_mixing_process_id());
		
		// 获取当前操作者信息
		LoginData loginData = (LoginData)request.getSession().getAttribute(RvsConsts.SESSION_USER);
		// 操作者ID
		String operatorId = loginData.getOperator_id();
		
		// 调制者
		entity.setMixing_operator_id(operatorId);
		// 调制完了时间
		entity.setMixing_finish_time(Calendar.getInstance().getTime());
		// 分段时间
		entity.setPace(pace);
		
		// 更新新建胶水调制作业分段时间
		glueMixingProcessPaceMapper.update(entity);
		
		PauseFeatureEntity pauseFeatureEntity = new PauseFeatureEntity();
		BeanUtil.copyToBean(form, pauseFeatureEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		// 担当人 ID
		pauseFeatureEntity.setOperator_id(operatorId);
		// 记录暂停原因
		pauseFeatureMapper.makePauseFeature(pauseFeatureEntity);
	}
	
	/**
	 * 调胶作业重开
	 * @param form
	 * @param conn
	 */
	public void continueGlueMixing(ActionForm form,HttpServletRequest request,SqlSessionManager conn){
		// 获取数据库连接
		GlueMixingProcessPaceMapper glueMixingProcessPaceMapper = conn.getMapper(GlueMixingProcessPaceMapper.class);
		
		// 复制表单数据到对象
		GlueMixingProcessPaceEntity entity = new GlueMixingProcessPaceEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		//根据胶水调制作业ID获取胶水调制作业最大分段
		int pace = glueMixingProcessPaceMapper.getMaxPaceByGlueMixingProcessId(entity.getGlue_mixing_process_id());
		pace = pace + 1;
		
		// 获取当前操作者信息
		LoginData loginData = (LoginData)request.getSession().getAttribute(RvsConsts.SESSION_USER);
		// 操作者ID
		String operatorId = loginData.getOperator_id();
		
		// 调制者
		entity.setMixing_operator_id(operatorId);
		// 调制完了时间
		entity.setMixing_action_time(Calendar.getInstance().getTime());
		// 分段时间
		entity.setPace(pace);
		
		// 新建胶水调制作业分段时间 
		glueMixingProcessPaceMapper.insert(entity);
		
		PauseFeatureService pauseFeatureService = new PauseFeatureService();
		// 更新暂停完成时间
		pauseFeatureService.finishPauseFeature(null, null, null, operatorId, null, conn);
	}
	
	
	/**
	 * 调胶作业完成
	 * @param form
	 * @param conn
	 */
	public void finishGlueMixing(ActionForm form,HttpServletRequest request,SqlSessionManager conn){
		// 获取数据库连接
		GlueMixingProcessMapper glueMixingProcessMapper = conn.getMapper(GlueMixingProcessMapper.class);
		GlueMixingProcessPaceMapper glueMixingProcessPaceMapper = conn.getMapper(GlueMixingProcessPaceMapper.class);
		
		// 1、获取当前操作者信息
		LoginData loginData = (LoginData)request.getSession().getAttribute(RvsConsts.SESSION_USER);
		// 操作者ID
		String operatorId = loginData.getOperator_id();
		
		// 2、当前时间
		Calendar currentTime = Calendar.getInstance();
		
		GlueMixingProcessEntity glueMixingProcessEntity = new GlueMixingProcessEntity();
		BeanUtil.copyToBean(form, glueMixingProcessEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		// 调制完了时间
		glueMixingProcessEntity.setMixing_finish_time(currentTime.getTime());
		// 调制者
		glueMixingProcessEntity.setMixing_operator_id(operatorId);
		
		// 3、更新胶水调制作业
		glueMixingProcessMapper.update(glueMixingProcessEntity);
		
		// 4、根据胶水调制作业ID获取胶水调制作业最大分段
		int pace = glueMixingProcessPaceMapper.getMaxPaceByGlueMixingProcessId(glueMixingProcessEntity.getGlue_mixing_process_id());
		
		GlueMixingProcessPaceEntity glueMixingProcessPaceEntity = new GlueMixingProcessPaceEntity();
		BeanUtil.copyToBean(form, glueMixingProcessPaceEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		// 调制完了时间
		glueMixingProcessPaceEntity.setMixing_finish_time(currentTime.getTime());
		// 调制者
		glueMixingProcessPaceEntity.setMixing_operator_id(operatorId);
		// 分段时间
		glueMixingProcessPaceEntity.setPace(pace);
		
		// 5、更新胶水调制作业分段时间调制完了时间
		glueMixingProcessPaceMapper.update(glueMixingProcessPaceEntity);
	}

}
