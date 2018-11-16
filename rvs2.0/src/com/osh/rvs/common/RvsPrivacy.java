package com.osh.rvs.common;

public enum RvsPrivacy {
	FM_REVIEW(102), // 现品查询
	PRIVACY_FACT_MATERIAL(103), // 现品操作
	PRIVACY_SCHEDULE(104), // 计划操作
	PRIVACY_PROCESSING(105), // 进度操作
	PRIVACY_LINE(106), // 线长操作
	PRIVACY_POSITION(107), // 工位操作
	PRIVACY_INFO_EDIT(110), // 信息汇总操作
	PRIVACY_INFO_VIEW(111), // 信息汇总浏览
	PRIVACY_FILING(113),
	PRIVACY_SCHEDULE_VIEW(118),
	PRIVACY_READFILE(120), // 文档查看
	PRIVACY_OVEREDIT(121), // 系统管理员更新操作
	PRIVACY_VIEW(122); // 展示

	int dbValue;

	RvsPrivacy(int val) {
		dbValue = val;
	}

	public int dbValue() {
		return this.dbValue;
	}
}
