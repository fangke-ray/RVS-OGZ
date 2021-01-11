package framework.huiqing.bean.message;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.osh.rvs.servlet.ConsoleServlet;

public class ItemProperties extends Properties {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public ItemProperties() {
		super();
	}

	public static ItemProperties getMessageResources(String propertiesFile) {
		ItemProperties ip = new ItemProperties();
		InputStream in;
		try {
			in = new BufferedInputStream(new
					FileInputStream(BasePathConsts.BASE_PATH + BasePathConsts.PROPERTIES + "\\" + propertiesFile));
			ip.load(in);
		} catch (FileNotFoundException e) {
			ConsoleServlet.nowWarning = e.getMessage();
		} catch (IOException e) {
			ConsoleServlet.nowWarning = e.getMessage();
		}
		return ip;
	}

	public String getMessage(String messageKey, Object... items) {
		String message = this.getProperty(messageKey, "");
		for (int iItem = 0; iItem < items.length ; iItem++) {
			Object item = items[iItem];
			if (item != null) {
				message = message.replaceAll("\\{"+iItem+"\\}", item.toString());
			}
		}
		return message;
	}
}
