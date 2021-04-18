package com.osh.rvs.service.manage;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.master.OperatorNamedEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.mapper.master.OperatorMapper;
import com.osh.rvs.service.OperatorService;

import framework.huiqing.common.util.CodeListUtils;

public class ProcessInspectService {
	/**
	 * 取得线长以上人员(含兼任)
	 * @param conn
	 * @return
	 */
	public String getInspectors(Integer department, SqlSession conn) {

		List<String[]> lst = new ArrayList<String[]>();
		OperatorMapper dao = conn.getMapper(OperatorMapper.class);
		int privacy_id = RvsConsts.PRIVACY_LINE;// 线长
		List<OperatorNamedEntity> list = dao.getOperatorWithPrivacy(privacy_id, department);

		lst = OperatorService.getSetReferChooser(list, true);

		String pReferChooser = CodeListUtils.getReferChooser(lst);
		return pReferChooser;
	}
}
