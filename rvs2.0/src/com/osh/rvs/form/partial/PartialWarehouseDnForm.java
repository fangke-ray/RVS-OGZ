package com.osh.rvs.form.partial;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

/**
 * 零件入库DN编号
 *
 * @author liuxb
 *
 */
public class PartialWarehouseDnForm extends ActionForm {
	/**
	 *
	 */
	private static final long serialVersionUID = -8290243914588233234L;

	@BeanField(title = "KEY", name = "key", length = 11, notNull = true, primaryKey = true)
	private String key;

	@BeanField(title = "序号", name = "seq", type = FieldType.Integer, length = 2, notNull = true, primaryKey = true)
	private String seq;

	@BeanField(title = "日期", name = "warehouse_date", type = FieldType.Date, notNull = true)
	private String warehouse_date;

	@BeanField(title = "DN 编号", name = "dn_no", type = FieldType.String, length = 16, notNull = true)
	private String dn_no;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getWarehouse_date() {
		return warehouse_date;
	}

	public void setWarehouse_date(String warehouse_date) {
		this.warehouse_date = warehouse_date;
	}

	public String getDn_no() {
		return dn_no;
	}

	public void setDn_no(String dn_no) {
		this.dn_no = dn_no;
	}

}
