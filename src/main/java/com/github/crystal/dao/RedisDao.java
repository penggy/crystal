package com.github.crystal.dao;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisDao {
	
	private JedisPool jedisPool = null;
	private String host;
	private String password = "";
	private int port = 6379;
	private int timeout = 0;
	private int expire = 0;

	public RedisDao() {
	}

	@PostConstruct
	public void init() {
		if (password != null && !"".equals(password)) {
			jedisPool = new JedisPool(new JedisPoolConfig(), host, port, timeout, password);
		} else if (timeout != 0) {
			jedisPool = new JedisPool(new JedisPoolConfig(), host, port, timeout);
		} else {
			jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
		}
	}

	public Jedis openJedis() {
		return jedisPool.getResource();
	}

	public void closeJedis(Jedis jedis) {
		jedisPool.returnResource(jedis);
	}

	/**
	 * 获取key对应的值
	 * 
	 * @param key
	 * @return
	 */
	public byte[] get(String key) {
		byte[] value = null;
		Jedis jedis = jedisPool.getResource();
		try {
			value = jedis.get(StringUtils.getBytesUtf8(key));
		} finally {
			jedisPool.returnResource(jedis);
		}
		return value;
	}

	public byte[] hget(String key, String field) {
		byte[] value = null;
		Jedis jedis = jedisPool.getResource();
		try {
			value = jedis.hget(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(field));
		} finally {
			jedisPool.returnResource(jedis);
		}
		return value;
	}

	/**
	 * 删除key
	 * 
	 * @param key
	 */
	public void delete(String key) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.del(StringUtils.getBytesUtf8(key));
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 判断key是否存在
	 * 
	 * @return
	 */
	public boolean keyExists(String key) {
		Jedis jedis = jedisPool.getResource();
		boolean b = false;
		try {
			b = jedis.exists(StringUtils.getBytesUtf8(key));
		} finally {
			jedisPool.returnResource(jedis);
		}
		return b;
	}

	public byte[] hset(String key, String field, byte[] value) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.hset(StringUtils.getBytesUtf8(key), StringUtils.getBytesUtf8(field), value);
			if (expire != 0) {
				jedis.expire(StringUtils.getBytesUtf8(key), expire);
			}
		} finally {
			jedisPool.returnResource(jedis);
		}
		return value;
	}

	public byte[] set(String key, byte[] value) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.set(StringUtils.getBytesUtf8(key), value);
			if (expire != 0) {
				jedis.expire(StringUtils.getBytesUtf8(key), expire);
			}
		} finally {
			jedisPool.returnResource(jedis);
		}
		return value;
	}

	public byte[] set(String key, byte[] value, int expire) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.set(StringUtils.getBytesUtf8(key), value);
			if (expire != 0) {
				jedis.expire(key, expire);
			}
		} finally {
			jedisPool.returnResource(jedis);
		}
		return value;
	}

	/**
	 * 设置过期时间
	 * 
	 * @param key
	 * @param seconds
	 */
	public void expire(String key, int seconds) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.expire(StringUtils.getBytesUtf8(key), seconds);
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 清空数据
	 */
	public void flush() {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.flushDB();
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 获取所有的key数目，包括已过期的
	 * 
	 * @return
	 */
	public Long dbSize() {
		Long dbSize = 0L;
		Jedis jedis = jedisPool.getResource();
		try {
			dbSize = jedis.dbSize();
		} finally {
			jedisPool.returnResource(jedis);
		}
		return dbSize;
	}

	/**
	 * 获取与pattern匹配的所有key
	 * 
	 * @param pattern
	 * @return
	 */
	public Set<byte[]> keys(String pattern) {
		Set<byte[]> vals = new HashSet<byte[]>();
		Jedis jedis = jedisPool.getResource();
		try {
			vals = jedis.keys(pattern.getBytes());
		} finally {
			jedisPool.returnResource(jedis);
		}
		return vals;
	}

}
