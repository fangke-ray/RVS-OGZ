package com.osh.rvs.common;

import framework.huiqing.common.util.BaseConst;

public class RvsConsts extends BaseConst {

	public static final String SESSION_USER = "userdata";
	public static final String PAGE_LOGIN = "login.do";
	public static final String SUCCESS = "success";
	public static final Integer PRIVACY_SA = 0; // 超级管理员
	public static final Integer PRIVACY_ADMIN = 1; // 系统管理

	public static final Integer PRIVACY_POSITION_VIEW = 100; // 工作数据查询

	public static final Integer PRIVACY_WIP = 102; // 现品查询
	public static final Integer PRIVACY_FACT_MATERIAL = 103; // 现品操作
	public static final Integer PRIVACY_SCHEDULE = 104; // 计划操作
	public static final Integer PRIVACY_PROCESSING = 105; // 进度操作
	public static final Integer PRIVACY_LINE = 106; // 线长操作
	public static final Integer PRIVACY_POSITION = 107; // 工位操作

	public static final Integer PRIVACY_INFO_EDIT = 110; // 信息汇总操作
	public static final Integer PRIVACY_INFO_VIEW = 111; // 信息汇总浏览
	public static final Integer PRIVACY_FILING = 113;
	public static final Integer PRIVACY_PARTIAL_MANAGER = 115; // 零件管理
	public static final Integer PRIVACY_PARTIAL_ORDER = 117; // 零件订购
	public static final Integer PRIVACY_SCHEDULE_VIEW = 118;
	public static final Integer PRIVACY_READFILE = 120; // 文档查看
	public static final Integer PRIVACY_OVEREDIT = 121; // 系统管理员更新操作
	public static final Integer PRIVACY_VIEW = 122; // 展示
	public static final Integer PRIVACY_DT_MANAGE = 123;//设备管理(各工程)
	public static final Integer PRIVACY_TECHNOLOGY=124;//设备管理(设备管理画面)

	public static final int OPERATE_RESULT_NOWORK_WAITING = 0; // 等待区且未操作
	public static final int OPERATE_RESULT_WORKING = 1; // 当前人在作业中
	public static final int OPERATE_RESULT_FINISH = 2; // 当前人完成了
	public static final int OPERATE_RESULT_BREAK = 3; // 中断而结束，或者中断后重新等待
	public static final int OPERATE_RESULT_PAUSE = 4; // 暂停而结束，或者中断后重新等待
	public static final int OPERATE_RESULT_SUPPORT = 5; // 辅助者的作业
	public static final int OPERATE_RESULT_SENDBACK = 6; // 指派为返工，或者品保不通过
	public static final int OPERATE_RESULT_BATCHWORKING = 7; // 批量处理中
	public static final int OPERATE_RESULT_BREAKBACK = 8; // 该工位返工结束
	public static final int PROCESS_ASSIGN_LINE_BASE = 9000000;
	public static final int PROCESS_ASSIGN_LINE_START = 0;
	public static final int PROCESS_ASSIGN_LINE_END = 9999999;
	public static final int WARNING_LEVEL_NORMAL = 1; // 普通警报-不发邮件
	public static final int WARNING_LEVEL_ERROR = 2; // 
	public static final int WARNING_LEVEL_SCHEDULE =3;
	public static final int WARNING_REASON_BREAK = 1; // 不良中断
	public static final int WARNING_REASON_POSITION_OVERTIME = 2; // 工位超时
	public static final int WARNING_REASON_LINE_OVERTIME = 3; // 工程超时
	public static final int WARNING_REASON_WAITING_OVERFLOW = 4; // 等待区超量
	public static final int WARNING_REASON_QAFORBID = 5; // 品保不通过
	public static final int WARNING_REASON_BREAK_SOLO = 6; // 不良中断 - 独立工位
	public static final int WARNING_REASON_INLINE_LATE = 7; // 投线延迟
	public static final int WARNING_REASON_PARTIAL_ON_POISTION = 8; // 零件签收问题
	public static final int WARNING_REASON_INFECT_ERROR = 9; // 点检不通过
	public static final int WARNING_REASON_NOT_REACH_LOAD_RATE = 11; // 负荷率未达成
	public static final int WARNING_REASON_NOT_REACH_ENERGY_RATE = 12; // 能率未达成
	public static final int WARNING_REASON_PROCEDURE_OVERSET = 13; // 作业步骤不一致

	public static final String ROLE_ACCEPTOR = "00000000001";
	public static final String ROLE_QUOTATOR = "00000000002";
	public static final String ROLE_FACTINLINE = "00000000003";
	public static final String ROLE_SCHEDULER = "00000000004";
	public static final String ROLE_LINELEADER = "00000000005";
	public static final String ROLE_OPERATOR = "00000000006";
	public static final String ROLE_QAER = "00000000007";
	public static final String ROLE_SHIPPPER = "00000000008";
	public static final String ROLE_MANAGER = "00000000009";
	public static final String ROLE_SYSTEM = "00000000012";
	public static final String ROLE_PARTIAL_MANAGER = "00000000016";
	public static final String ROLE_DT_MANAGER= "00000000019";
	public static final String ROLE_QA_MANAGER = "00000000017";
	public static final String ROLE_QT_MANAGER = "00000000018";
	public static final String POSITION_ACCEPTANCE = "00000000009";
	public static final String POSITION_QUOTATION_N = "00000000013";
	public static final String POSITION_QUOTATION_D = "00000000014";
	public static final String POSITION_QUOTATION_E = "00000000013";
	public static final String POSITION_QUOTATION_P = "00000000013";
	public static final String POSITION_QA = "00000000046";
	public static final String POSITION_PERI_QA = "00000000065";

	public static final String POSITION_SHIPPING = "00000000047";
	public static final String POSITION_QA_LIGHT = "00000000052";

	public static final String POSITION_PRODUCT_QA = "00000000108";
	public static final String POSITION_PRODUCT_SHIPPING = "00000000109";

	public static final String TEXT_PLAN_HOLD = "另行通知";
	public static final String SEARCH_RESULT = "SEARCH_RESULT";
	public static final String DONT_CARE = "DONT_CARE";
	public static final String JUST_WORKING = "JUST_WORKING";
	public static final String JUST_FINISHED = "JUST_FINISHED";
	public static final Integer BO_FLG_WAITING = 9;

	public static final Integer TIME_LIMIT = 5; // 纳期
	public static final Integer PLANE_INV = 8; // 零件补充周期

	public static final int TICKET_RECEPTOR = 1;
	public static final int TICKET_QUTOTAOR = 2;
	public static final int TICKET_ADDENDA = 0;
	public static final String VERSION = "2.16.605";
	public static final Integer DEPART_REPAIR = 1;
	public static final Integer DEPART_MANUFACT = 2;

	public static final Integer PROCESS_TYPE_FIX_LINE = 1; // 翻修流水线
	public static final Integer PROCESS_TYPE_FIX_CELL = 2; // 翻修非流水线
	public static final Integer PROCESS_TYPE_WASH_INFECT = 3; // 清洗消毒
	public static final Integer PROCESS_TYPE_MANUFACT_LINE = 6; // 显微镜生产流水线
	public static final Integer PROCESS_TYPE_ARM_LINE = 7; // 显微镜ARM流水线
	public static final Integer PROCESS_TYPE_REFIX = 8; // 显微镜ARM流水线
	public static final Integer PROCESS_TYPE_ALL = 999; // 所有
}
