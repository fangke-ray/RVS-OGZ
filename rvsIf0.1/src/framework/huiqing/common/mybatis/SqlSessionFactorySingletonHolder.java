package framework.huiqing.common.mybatis;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

/**
 * Mybatis 构造
 * @author Gong
 * @since 2012/11/16
 */
public class SqlSessionFactorySingletonHolder {

	private Logger log = Logger.getLogger(getClass());

	static class InnerHolder {
		private static final SqlSessionFactorySingletonHolder INSTANCE;
		static {
			try {
				INSTANCE = new SqlSessionFactorySingletonHolder();
			} catch (Exception e) {
				throw new ExceptionInInitializerError(e);
			}
		}
	}

	public static SqlSessionFactorySingletonHolder getInstance() {
		return InnerHolder.INSTANCE;
	}

	private SqlSessionFactorySingletonHolder() throws Exception {
		init();
	}

	private void init() throws Exception {
		Reader reader = null;
		try {
			log.debug("GET MYBATIS CONFIG");
			reader = Resources.getResourceAsReader("resources/mybatis-config.xml");
			log.debug("READ MYBATIS CONFIG");
			factory = new SqlSessionFactoryBuilder().build(reader);
			// build(InputStream inputStream, String environment) 
			log.debug("BUILD MYBATIS CONFIG");
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

	private static SqlSessionFactory factory;

	/**
	 * @return factory
	 */
	public static SqlSessionFactory getFactory() {
		return factory;
	}
}
