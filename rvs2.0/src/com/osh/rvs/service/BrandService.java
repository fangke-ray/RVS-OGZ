package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.BrandEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.master.BrandForm;
import com.osh.rvs.mapper.master.BrandMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

public class BrandService {

	public List<BrandForm> searchBrand(BrandEntity brandEntity, SqlSession conn) {
		BrandMapper mapper = conn.getMapper(BrandMapper.class);
		List<BrandForm> brandForms = new ArrayList<BrandForm>();

		List<BrandEntity> brandEntities = mapper.searchBrand(brandEntity);

		BeanUtil.copyToFormList(brandEntities, brandForms, CopyOptions.COPYOPTIONS_NOEMPTY,
				BrandForm.class);

		return brandForms;
	}

	public void insertBrand(ActionForm form, SqlSessionManager conn,
			HttpSession session, List<MsgInfo> errors) {
		BrandForm brandForm = (BrandForm) form;
		BrandEntity brandEntity = new BrandEntity();

		BeanUtil.copyToBean(brandForm, brandEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		//当前操作者ID
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		brandEntity.setUpdated_by(user.getOperator_id());
		
		BrandMapper mapper = conn.getMapper(BrandMapper.class);
		
		mapper.insertBrand(brandEntity);
		
	}

	public void deleteBrand(ActionForm form, SqlSessionManager conn,
			HttpSession session, List<MsgInfo> errors) {
		BrandForm brandForm = (BrandForm) form;
		BrandEntity brandEntity = new BrandEntity();

		BeanUtil.copyToBean(brandForm, brandEntity, CopyOptions.COPYOPTIONS_NOEMPTY);

		//当前操作者ID
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		brandEntity.setUpdated_by(user.getOperator_id());
		
		BrandMapper mapper = conn.getMapper(BrandMapper.class);
		mapper.deleteBrand(brandEntity);
		
	}

	public void updateBrand(ActionForm form, SqlSessionManager conn,
			HttpSession session, List<MsgInfo> errors) {
		BrandForm brandForm = (BrandForm) form;
		BrandEntity brandEntity = new BrandEntity();

		BeanUtil.copyToBean(brandForm, brandEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		//当前操作者ID
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		brandEntity.setUpdated_by(user.getOperator_id());
		
		BrandMapper mapper = conn.getMapper(BrandMapper.class);
		mapper.updateBrand(brandEntity);
		
	}

	/**
	 * 取得全部厂商(参照列表)
	 * @param conn
	 * @return
	 */
	public String getOptions(SqlSession conn) {
		List<String[]> bList = new ArrayList<String[]>();
		List<BrandForm> allBrand = this.searchBrand(new BrandEntity(), conn);

		Map<String, String> cdBusinessRelationship = new HashMap<String, String>();
		
		for (BrandForm brand: allBrand) {
			String[] bline = new String[4];
			bline[0] = brand.getBrand_id();
			bline[1] = brand.getName();
			String cBusinessRelationship = brand.getBusiness_relationship();
			if (!cdBusinessRelationship.containsKey(cBusinessRelationship)) {
				String vBusinessRelationship = CodeListUtils.getValue("brand_business_relationship", cBusinessRelationship);
				cdBusinessRelationship.put(cBusinessRelationship, vBusinessRelationship);
			}
			bline[2] = cdBusinessRelationship.get(cBusinessRelationship);
			bline[3] = CommonStringUtil.nullToAlter(brand.getContacts(), " ");
			bList.add(bline);
		}

		String mReferChooser = CodeListUtils.getReferChooser(bList);

		return mReferChooser;
	}

}
