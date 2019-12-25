package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.MaterialEntity;
import com.osh.rvs.form.MaterialForm;
import com.osh.rvs.mapper.MaterialMapper;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

public class MaterialService {
	public List<MaterialForm> searchScheduld(String scheduledDate, SqlSession conn) {
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);

		List<MaterialEntity> list = dao.searchScheduled(scheduledDate);

		List<MaterialForm> respList = new ArrayList<MaterialForm>();
		BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, MaterialForm.class);

		return respList;
	}
}