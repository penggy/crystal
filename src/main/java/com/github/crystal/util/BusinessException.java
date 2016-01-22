/**
 * Copyright 2015 iflytek.com
 * 
 * All right reserved
 *
 * creator : pengwu2
 */
package com.github.crystal.util;

/**
 * @author pengwu2
 *
 */
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = -7746283472080953105L;

	public BusinessException() {
		super("未知错误");
	}

	public BusinessException(String msg) {
		super(msg);
	}

}
