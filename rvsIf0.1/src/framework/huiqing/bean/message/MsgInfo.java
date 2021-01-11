package framework.huiqing.bean.message;

import java.io.Serializable;

/**
 * システム名 ： ニッセン立替後払いシステム <br>
 * サブシステム名 ： <br>
 * 機能名 ： DTO<br>
 * 処理概要 ： <br>
 * 
 * @author ニッセン立替後払いシステム
 * @version
 */
public class MsgInfo implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private String componentid;
	private String errcode;
	private String errmsg;
	private String lineno;

	/**
	 * @return the componentid
	 */
	public String getComponentid() {
		return componentid;
	}

	/**
	 * @param componentid
	 *            the componentid to set
	 */
	public void setComponentid(String componentid) {
		this.componentid = componentid;
	}

	/**
	 * @return the errcode
	 */
	public String getErrcode() {
		return errcode;
	}

	/**
	 * @param errcode
	 *            the errcode to set
	 */
	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}

	/**
	 * @return the lineno
	 */
	public String getLineno() {
		return lineno;
	}

	/**
	 * @param lineno
	 *            the lineno to set
	 */
	public void setLineno(String lineno) {
		this.lineno = lineno;
	}

	/**
	 * @return the errmsg
	 */
	public String getErrmsg() {
		return errmsg;
	}

	/**
	 * @param errmsg
	 *            the errmsg to set
	 */
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
}