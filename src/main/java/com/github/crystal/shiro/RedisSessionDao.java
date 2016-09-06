package com.github.crystal.shiro;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.SerializationUtils;

import com.github.crystal.dao.RedisDao;
import com.google.common.collect.Lists;

public class RedisSessionDao extends AbstractSessionDAO {

	private static final Logger logger = LoggerFactory.getLogger(RedisSessionDao.class);
	
	private String sessionKey = "SESSION_";
	
	private RedisDao redisDao;
	
	public RedisDao getRedisDao() {
		return redisDao;
	}

	public void setRedisDao(RedisDao redisDao) {
		this.redisDao = redisDao;
	}
	
	@PostConstruct
	public void init() {
		this.setSessionIdGenerator(new SessionIdGenerator() {
			public Serializable generateId(Session session) {
				return UUID.randomUUID().toString().replace("-", "");
			}
		});
	}

	@Override
	public void update(Session session) throws UnknownSessionException {
		saveSession(session);
	}

	@Override
	public void delete(Session session) {
		redisDao.delete(sessionKey + session.getId().toString());
	}

	@Override
	public Collection<Session> getActiveSessions() {
		List<Session> sessions = Lists.newArrayList();
		Set<byte[]> keys = redisDao.keys(sessionKey + "*");
		if (!CollectionUtils.isEmpty(keys)) {
			for (byte[] key : keys) {
				byte[] val = redisDao.get(sessionKey + new String(key));
				Session session = (Session) SerializationUtils.deserialize(val);
				sessions.add(session);
			}
		}
		return sessions;
	}

	@Override
	protected Serializable doCreate(Session session) {
		Serializable sessionId = generateSessionId(session);
		assignSessionId(session, sessionId);

		saveSession(session);
		return sessionId;
	}

	protected void saveSession(Session session) {
		if (session == null || session.getId() == null) {
			logger.error("session or session id is null");
			return;
		}
		byte[] val = SerializationUtils.serialize(session);
		redisDao.set(sessionKey + session.getId().toString(), val,
				(int) session.getTimeout() / 1000);
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		if (sessionId == null) {
			return null;
		}
		byte[] val = redisDao.get(sessionKey + sessionId.toString());
		if (val == null) {
			return null;
		}
		Session session = (Session) SerializationUtils.deserialize(val);
		if (session != null) {
			redisDao.expire(sessionKey + sessionId.toString(), (int) session.getTimeout() / 1000);
		}
		return session;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	
}
