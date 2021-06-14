package framework.huiqing.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import framework.huiqing.common.util.XssShieldUtil;

public class CheckXssHttpServletRequest extends HttpServletRequestWrapper {

	public CheckXssHttpServletRequest(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getQueryString() {
		return XssShieldUtil.stripXss(super.getQueryString());
	}

	@Override
	public String getParameter(String name) {
		String param = super.getParameter(name);
		if (param != null && param.length() > 16) {
			return XssShieldUtil.stripXss(param);
		}
		return param;
	}
}
