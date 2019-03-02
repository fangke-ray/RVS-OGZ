package com.osh.rvs.service.equipment;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.equipment.DeviceSpareAdjustEntity;
import com.osh.rvs.form.equipment.DeviceSpareAdjustForm;
import com.osh.rvs.mapper.equipment.DeviceSpareAdjustMapper;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

public class DeviceSpareAdjustService {
	public List<DeviceSpareAdjustForm> searchAdjustRecord(ActionForm form, SqlSession conn) {
		DeviceSpareAdjustMapper dao = conn.getMapper(DeviceSpareAdjustMapper.class);

		DeviceSpareAdjustEntity entity = new DeviceSpareAdjustEntity();
		// 复制表单数据
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<DeviceSpareAdjustEntity> list = dao.searchAdjustRecord(entity);

		List<DeviceSpareAdjustForm> respList = new ArrayList<DeviceSpareAdjustForm>();

		if (list != null && list.size() > 0) {
			BeanUtil.copyToFormList(list, respList, CopyOptions.COPYOPTIONS_NOEMPTY, DeviceSpareAdjustForm.class);
		}

		return respList;
	}
}
