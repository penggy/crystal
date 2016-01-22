package com.github.crystal.dao;

import java.io.Serializable;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;

@SuppressWarnings("unchecked")
public class HDao {

	private static final Logger logger = LoggerFactory.getLogger(HDao.class);

	private String url;
	private String username;
	private String password;

	private String driverClass = "com.mysql.jdbc.Driver";
	private String dialect = "org.hibernate.dialect.MySQLDialect";
	private String hbm2ddl = "none";
	private String showSql = "false";
	private String packagesToScan = "";

	private SessionFactory sessionFactory;

	private ThreadLocal<Session> localSession = new ThreadLocal<Session>();

	@PostConstruct
	public void init() {
		Configuration cfg = new Configuration();

		cfg.setProperty("hibernate.connection.driver_class", driverClass);
		cfg.setProperty("hibernate.connection.url", url);
		cfg.setProperty("hibernate.connection.username", username);
		cfg.setProperty("hibernate.connection.password", password);
		cfg.setProperty("hibernate.show_sql", showSql);
		cfg.setProperty("hibernate.connection.release_mode", "on_close");
		cfg.setProperty("hibernate.dialect", dialect);
		cfg.setProperty("hibernate.hbm2ddl.auto", hbm2ddl);
		cfg.setProperty("hibernate.c3p0.min_size", "5");
		cfg.setProperty("hibernate.c3p0.max_size", "20");

		// hibernate.c3p0.timeout,获得连接的超时时间,如果超过这个时间,会抛出异常，单位秒
		cfg.setProperty("hibernate.c3p0.timeout", "100");

		// hibernate.c3p0.max_statements,最大的PreparedStatement的数量
		cfg.setProperty("hibernate.c3p0.max_statements", "50");

		// hibernate.c3p0.idle_test_period,允许连接空闲的时间,单位秒
		cfg.setProperty("hibernate.c3p0.idle_test_period", "100");

		// 连接验证
		cfg.setProperty("hibernate.c3p0.validate", "true");

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		String[] pkgs = packagesToScan.split("[,|;|\\s+]");
		for (String packageToScan : pkgs) {
			packageToScan = StringUtils.trim(packageToScan);
			if (StringUtils.isEmpty(packageToScan)) {
				continue;
			}
			for (BeanDefinition bd : scanner.findCandidateComponents(packageToScan)) {
				String name = bd.getBeanClassName();
				try {
					Class<?> clazz = Class.forName(name);
					cfg.addAnnotatedClass(clazz);
				} catch (Exception e) {
					logger.error("scan package error.", e);
				}
			}
		}
		ServiceRegistry reg = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();
		this.sessionFactory = cfg.buildSessionFactory(reg);
	}

	@PreDestroy
	public void destroy() {
		Iterator<?> it = C3P0Registry.getPooledDataSources().iterator();
		while (it.hasNext()) {
			try {
				PooledDataSource dataSource = (PooledDataSource) it.next();
				dataSource.close();
			} catch (Exception e) {
				logger.error("fail to close c3p0 DataSource", e);
			}
		}

		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			try {
				DriverManager.deregisterDriver(drivers.nextElement());
			} catch (SQLException e) {
				logger.error("fail to deregist driver", e);
			}
		}

		try {
			AbandonedConnectionCleanupThread.shutdown();
		} catch (InterruptedException e) {
			logger.error("fail to shutdown mysql abandoned connection clean up thread", e);
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public String getHbm2ddl() {
		return hbm2ddl;
	}

	public void setHbm2ddl(String hbm2ddl) {
		this.hbm2ddl = hbm2ddl;
	}

	public String getShowSql() {
		return showSql;
	}

	public void setShowSql(String showSql) {
		this.showSql = showSql;
	}

	public String getPackagesToScan() {
		return packagesToScan;
	}

	public void setPackagesToScan(String packagesToScan) {
		this.packagesToScan = packagesToScan;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Session openSession() {
		return sessionFactory.openSession();
	}

	public Session currentSession() {
		Session session = localSession.get();
		if (session == null || !session.isOpen()) {
			session = sessionFactory.openSession();
			localSession.set(session);
		}
		return session;
	}

	private boolean closeSessionIfNecessary() {
		Session session = localSession.get();
		if (session != null && session.isOpen() && !session.getTransaction().isActive()) {
			session.close();
			return true;
		}
		return false;
	}

	public void doInTx(Atom atom) {
		Transaction tx = currentSession().getTransaction();
		boolean isAlreadyActive = tx.isActive();
		if (!isAlreadyActive) {
			tx.begin();
		}
		try {
			atom.exec();
			if (!isAlreadyActive) {
				tx.commit();
			}
		} catch (Exception e) {
			if (!isAlreadyActive) {
				tx.rollback();
			}
			throw new RuntimeException(e);
		} finally {
			closeSessionIfNecessary();
		}
	}

	public <T> void save(final T entity) {
		doInTx(new Atom() {
			public void exec() {
				currentSession().saveOrUpdate(entity);
			}
		});
	}

	/**
	 * 一次连接,保存多个实体
	 * 
	 * @param entitys
	 */
	public <T> void saveAll(final Collection<T> entitys) {
		doInTx(new Atom() {
			public void exec() {
				for (T entity : entitys) {
					currentSession().saveOrUpdate(entity);
				}
			}
		});
	}

	public <T> void update(final T entity) {
		doInTx(new Atom() {
			public void exec() {
				currentSession().update(entity);
			}
		});
	}

	public void delete(final Object entity) {
		doInTx(new Atom() {
			public void exec() {
				currentSession().delete(entity);
			}
		});
	}

	/**
	 * 一次连接,删除多个实体
	 * 
	 * @param entitys
	 */
	public <T> void deleteAll(final Collection<T> entitys) {
		doInTx(new Atom() {
			public void exec() {
				for (Object entity : entitys) {
					currentSession().delete(entity);
				}
			}
		});
	}

	public int bulkUpdate(final String hql, final Object... values) {
		final int[] ret = new int[] { 0 };
		doInTx(new Atom() {
			public void exec() {
				Query query = currentSession().createQuery(hql);
				setQueryParam(query, values);
				int cnt = query.executeUpdate();
				ret[0] = cnt;
			}
		});
		return ret[0];
	}

	public int bulkUpdateBySQL(final String sql, final Object... values) {
		final int[] ret = new int[] { 0 };
		doInTx(new Atom() {
			public void exec() {
				Query query = currentSession().createSQLQuery(sql);
				setQueryParam(query, values);
				int cnt = query.executeUpdate();
				ret[0] = cnt;
			}
		});
		return ret[0];
	}

	public <T> T get(Class<T> clazz, Serializable id) {
		if (id == null) {
			return null;
		}
		try {
			return (T) currentSession().get(clazz, id);
		} finally {
			closeSessionIfNecessary();
		}
	}

	public <T> List<T> find(String hql, Object... values) {
		try {
			Query query = currentSession().createQuery(hql);
			setQueryParam(query, values);
			return query.list();
		} finally {
			closeSessionIfNecessary();
		}
	}

	public <T> List<T> find(String hql, String[] paramNames, Object[] params) {
		try {
			Query query = currentSession().createQuery(hql);
			setQueryParam(query, paramNames, params);
			return query.list();
		} finally {
			closeSessionIfNecessary();
		}
	}

	public <T> T findSingle(String hql, Object... values) {
		try {
			Query query = currentSession().createQuery(hql);
			setQueryParam(query, values);
			return (T) query.uniqueResult();
		} finally {
			closeSessionIfNecessary();
		}
	}

	public <T> List<T> findBySQL(String sql, Object... values) {
		try {
			Query query = currentSession().createSQLQuery(sql);
			setQueryParam(query, values);
			return query.list();
		} finally {
			closeSessionIfNecessary();
		}
	}

	public <T> List<T> findBySQL(String sql, String[] paramNames, Object[] params) {
		try {
			Query query = currentSession().createSQLQuery(sql);
			setQueryParam(query, paramNames, params);
			return query.list();
		} finally {
			closeSessionIfNecessary();
		}
	}

	public <T> T findSingleBySQL(String sql, Object... values) {
		try {
			Query query = currentSession().createSQLQuery(sql);
			setQueryParam(query, values);
			T ret = (T) query.uniqueResult();
			return ret;
		} finally {
			closeSessionIfNecessary();
		}
	}

	public <T> PageBean<T> find(PageBean<T> pageBean, String hql, Object... values) {
		try {
			Query query = currentSession().createQuery(hql);
			setQueryParam(query, values);
			if (pageBean.getCount() == 0) {
				Query countQuery = currentSession().createQuery(toCountHQL(hql));
				setQueryParam(countQuery, values);
				Object obj = countQuery.uniqueResult();
				pageBean.setCount(getCount(obj));
			}
			query.setFirstResult(pageBean.getStart());
			query.setMaxResults(pageBean.getLimit());
			pageBean.setData(query.list());
			return pageBean;
		} finally {
			closeSessionIfNecessary();
		}
	}

	public <T> PageBean<T> find(PageBean<T> pageBean, String hql, String[] paramNames, Object[] params) {
		try {
			Query query = currentSession().createQuery(hql);
			setQueryParam(query, paramNames, params);
			if (pageBean.getCount() == 0) {
				Query countQuery = currentSession().createQuery(toCountHQL(hql));
				setQueryParam(countQuery, paramNames, params);
				Object obj = countQuery.uniqueResult();
				pageBean.setCount(getCount(obj));
			}
			query.setFirstResult(pageBean.getStart());
			query.setMaxResults(pageBean.getLimit());
			pageBean.setData(query.list());
			return pageBean;
		} finally {
			closeSessionIfNecessary();
		}
	}

	public <T> PageBean<T> findBySQL(PageBean<T> pageBean, String sql, Object... values) {
		try {
			Query query = currentSession().createSQLQuery(sql);
			setQueryParam(query, values);
			if (pageBean.getCount() == 0) {
				Query countQuery = currentSession().createSQLQuery(toCountSQL(sql));
				setQueryParam(countQuery, values);
				Object obj = countQuery.uniqueResult();
				pageBean.setCount(getCount(obj));
			}
			query.setFirstResult(pageBean.getStart());
			query.setMaxResults(pageBean.getLimit());
			pageBean.setData(query.list());
			return pageBean;
		} finally {
			closeSessionIfNecessary();
		}
	}

	public <T> PageBean<T> findBySQL(PageBean<T> pageBean, String sql, String[] paramNames, Object[] params) {
		try {
			Query query = currentSession().createSQLQuery(sql);
			setQueryParam(query, paramNames, params);
			if (pageBean.getCount() == 0) {
				Query countQuery = currentSession().createSQLQuery(toCountSQL(sql));
				setQueryParam(countQuery, paramNames, params);
				Object obj = countQuery.uniqueResult();
				pageBean.setCount(getCount(obj));
			}
			query.setFirstResult(pageBean.getStart());
			query.setMaxResults(pageBean.getLimit());
			pageBean.setData(query.list());
			return pageBean;
		} finally {
			closeSessionIfNecessary();
		}
	}

	public long getCount(Object obj) {
		if (obj instanceof Object[]) {
			return getCount(((Object[]) obj)[0]);
		}
		if (obj instanceof List) {
			return getCount(((List<?>) obj).get(0));
		}
		if (obj instanceof Number) {
			Number count = (Number) obj;
			return count.longValue();
		}
		return 0;
	}

	public String toCountHQL(String hql) {
		if (null == hql || hql.trim().equals(""))
			return "";
		String formatQl = hql;
		String pStr = "^\\s*((s|S)(e|E)(l|L)(e|E)(c|C)(t|T))?(.*?)(f|F)(r|R)(o|O)(m|M)\\s";
		String pOrderStr = "\\s*(o|O)(r|R)(d|D)(e|E)(r|R)\\s+(b|B)(y|Y).*$";
		Pattern p = Pattern.compile(pStr, Pattern.DOTALL);
		Matcher m = p.matcher(hql);
		if (m.find()) {
			StringBuffer countHeader = new StringBuffer("SELECT COUNT(*)");
			if (m.group(8) != null && !m.group(8).trim().equals("")) {
				countHeader.append(", " + m.group(8).trim());
			}
			countHeader.append(" FROM ");
			formatQl = formatQl.replaceFirst(pStr, countHeader.toString());
		}
		formatQl = formatQl.replaceFirst(pOrderStr, "");
		return formatQl;
	}

	public String toCountSQL(String sql) {
		if (null == sql || sql.trim().equals(""))
			return "";
		return "SELECT COUNT(*) FROM (" + sql + ") as COUNT";
	}

	public void setQueryParam(Query query, Object... values) {
		for (int i = 0; i < values.length; i++) {
			query.setParameter(i, values[i]);
		}
	}

	public void setQueryParam(Query query, String[] paramNames, Object[] params) {
		if (paramNames == null || params == null)
			return;
		for (int i = 0; i < paramNames.length && i < params.length; i++) {
			query.setParameter(paramNames[i], params[i]);
		}
	}

	public void setListQueryParam(Query query, String[] listParamNames, Collection<?>[] listParams) {
		if (listParamNames == null || listParams == null)
			return;
		for (int i = 0; i < listParamNames.length; i++) {
			query.setParameterList(listParamNames[i], listParams[i]);
		}
	}
}