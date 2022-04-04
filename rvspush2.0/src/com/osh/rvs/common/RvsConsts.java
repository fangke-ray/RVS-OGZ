package com.osh.rvs.common;

import framework.huiqing.common.util.BaseConst;

public class RvsConsts extends BaseConst {

	public static final String SESSION_USER = "userdata";
	public static final String PAGE_LOGIN = "login.do";
	public static final String SUCCESS = "success";
	public static final Integer PRIVACY_PROCESSING = 105; // 进度操作
	public static final Integer PRIVACY_LINE = 106; // 线长操作
	public static final Integer PRIVACY_POSITION = 107; // 工位操作
	public static final Integer PRIVACY_SA = 0; // 超级管理员
	public static final Integer PRIVACY_ADMIN = 1; // 系统管理
	public static final Integer PRIVACY_ACCEPTANCE = 100; // 受理操作
	public static final Integer PRIVACY_QUOTATION = 101; // 报价操作
	public static final Integer PRIVACY_WIP = 102; // WIP 管理
	public static final Integer PRIVACY_FACT_MATERIAL = 103; // 现品操作
	public static final Integer PRIVACY_SCHEDULE = 104; // 计划操作
	public static final Integer PRIVACY_QUALITY_ASSURANCE = 108; // 品保操作
	public static final Integer PRIVACY_SHIPPING = 109; // 出货操作
	public static final Integer PRIVACY_INFO_EDIT = 110; // 信息汇总操作
	public static final Integer PRIVACY_INFO_VIEW = 111; // 信息汇总浏览
	public static final Integer PRIVACY_FILING = 113;
	public static final int OPERATE_RESULT_NOWORK_WAITING = 0; // 等待区且未操作
	public static final int OPERATE_RESULT_WORKING = 1; // 当前人在作业中
	public static final int OPERATE_RESULT_FINISH = 2; // 当前人完成了
	public static final int OPERATE_RESULT_BREAK = 3; // 中断而结束，或者中断后重新等待
	public static final int OPERATE_RESULT_PAUSE = 4; // 暂停而结束，或者中断后重新等待
	public static final int OPERATE_RESULT_SUPPORT = 5; // 辅助者的作业
	public static final int OPERATE_RESULT_SENDBACK = 6; // 指派为返工，或者品保不通过
	public static final int OPERATE_RESULT_BATCHWORKING = 7; // 批量处理中
	public static final int PROCESS_ASSIGN_LINE_BASE = 9000000;
	public static final int PROCESS_ASSIGN_LINE_START = 0;
	public static final int PROCESS_ASSIGN_LINE_END = 9999999;
	public static final Integer WARNING_LEVEL_NORMAL = 1; // 普通警报-不发邮件
	public static final Integer WARNING_LEVEL_ERROR = 2; // 
	public static final Integer WARNING_LEVEL_SCHEDULE =3;
	public static final Integer WARNING_REASON_BREAK = 1; // 不良中断
	public static final Integer WARNING_REASON_POSITION_OVERTIME = 2; // 工位超时
	public static final Integer WARNING_REASON_LINE_OVERTIME = 3; // 工程超时
	public static final Integer WARNING_REASON_WAITING_OVERFLOW = 4; // 等待区超量
	public static final Integer WARNING_REASON_QAFORBID = 5; // 品保不通过
	public static final Integer WARNING_REASON_BREAK_SOLO = 6; // 不良中断 - 独立工位
	public static final Integer WARNING_REASON_INLINE_LATE = 7; // 投线延迟
	public static final Integer WARNING_REASON_NOT_REACH_LOAD_RATE = 11; // 负荷率未达成
	public static final Integer WARNING_REASON_NOT_REACH_ENERGY_RATE = 12; // 能率未达成
	public static final String ROLE_MANAGER = "00000000009";
	public static final String ROLE_FACTINLINE = "00000000003";
	public static final String ROLE_SCHEDULER = "00000000004";
	public static final String ROLE_LINELEADER = "00000000005";
	public static final String ROLE_OPERATOR = "00000000006";
	public static final String ROLE_DEVICEMANAGER = "00000000019";

	public static final Integer DEPART_REPAIR = 1;
	public static final Integer DEPART_MANUFACT = 2;

	public static final Integer TIME_LIMIT = 5; // 纳期
	public static final Integer PLANE_INV = 8; // 零件补充周期
}
