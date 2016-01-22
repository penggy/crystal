package com.github.crystal.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.github.crystal.util.BusinessException;

/**
 * 统一异常处理
 * 
 * @author pengwu2 2014-8-12
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String handleRuntimeException(RuntimeException ex) {
		Throwable cause = ex;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		if (cause instanceof ConstraintViolationException) {// 对校验异常的处理
			ConstraintViolationException cve = (ConstraintViolationException) cause;
			Set<ConstraintViolation<?>> cvs = cve.getConstraintViolations();
			String msg = cve.getMessage();
			if (CollectionUtils.isNotEmpty(cvs)) {
				msg = cve.getConstraintViolations().iterator().next().getMessage();
			}
			return msg;
		}
		
		if (cause instanceof BusinessException) {
			BusinessException be = (BusinessException) cause;
			return be.getMessage();
		}
		
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		cause.printStackTrace(pw);
		String msg = cause.getMessage();
		if (StringUtils.isEmpty(msg)) {
			msg = "系统内部错误,请联系管理员!";
		}
		logger.error(msg, cause);
		return msg;
	}

}
