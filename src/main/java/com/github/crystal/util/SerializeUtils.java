/**
 * Copyright 2015 iflytek.com
 * 
 * All right reserved
 *
 * creator : pengwu2
 */
package com.github.crystal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengwu2
 *
 */
public class SerializeUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(SerializeUtils.class);
	
	/**
	 * 反序列化
	 * 
	 * @param bytes
	 * @return
	 */
	public static Object deserialize(byte[] bytes) {
		Object result = null;
		if (bytes == null || bytes.length == 0) {
			return result;
		}
		ByteArrayInputStream byteStream = null;
		try {
			byteStream = new ByteArrayInputStream(bytes);
			ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);
			result = objectInputStream.readObject();
		} catch (Exception e) {
			logger.error("Failed to deserialize", e);
		} finally {
			IOUtils.closeQuietly(byteStream);
		}
		return result;
	}

	/**
	 * 序列化
	 * 
	 * @param object
	 * @return
	 */
	public static byte[] serialize(Object object) {
		byte[] result = new byte[] {};
		if (object == null) {
			return result;
		}
		ByteArrayOutputStream byteStream = null;
		try {
			byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
			result = byteStream.toByteArray();
		} catch (Exception ex) {
			logger.error("Failed to serialize", ex);
		} finally {
			IOUtils.closeQuietly(byteStream);
		}
		return result;
	}

}
