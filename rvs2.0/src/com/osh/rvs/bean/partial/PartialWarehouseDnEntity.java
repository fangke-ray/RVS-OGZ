package com.osh.rvs.bean.partial;

import java.io.Serializable;
import java.util.Date;

/**
 * 零件入库DN编号
 *
 * @author liuxb
 *
 */
public class PartialWarehouseDnEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2192702633005504891L;

	private String key;

	private Integer seq;

	private Date warehouse_date;

	private String dn_no;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public Date getWarehouse_date() {
		return warehouse_date;
	}

	public void setWarehouse_date(Date warehouse_date) {
		this.warehouse_date = warehouse_date;
	}

	public String getDn_no() {
		return dn_no;
	}

	public void setDn_no(String dn_no) {
		this.dn_no = dn_no;
	}

}
