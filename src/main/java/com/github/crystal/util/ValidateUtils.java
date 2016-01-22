package com.github.crystal.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * 校码工具
 * 
 * @author pengwu2
 *
 */
public class ValidateUtils {

	private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	/**
	 * 验证一个对象,只返回一条验证结果,返回空字符串表示验证通过
	 * 
	 * @param t
	 * @return 验证结果
	 */
	public static <T> String validateMsg(T t) {
		Set<ConstraintViolation<T>> violations = validator.validate(t);
		Iterator<ConstraintViolation<T>> it = violations.iterator();
		if (it.hasNext()) {
			ConstraintViolation<T> violation = it.next();
			return violation.getMessage();
		}
		return "";
	}

	/**
	 * 验证一个对象,返回全部验证结果,返回空集合表示验证通过
	 * 
	 * @param t
	 * @return
	 */
	public static <T> List<String> validateAllMsg(T t) {
		List<String> messages = new ArrayList<String>();
		Set<ConstraintViolation<T>> violations = validator.validate(t);
		Iterator<ConstraintViolation<T>> it = violations.iterator();
		if (it.hasNext()) {
			ConstraintViolation<T> violation = it.next();
			messages.add(violation.getMessage());
		}
		return messages;
	}

	/**
	 * 校验一个对像,当校验失败时,抛 ConstraintViolationException 异常
	 * @param t
	 * @return
	 * @throws ConstraintViolationException 当校验失败时
	 */
	public static <T> void validate(T t) {
		Set<ConstraintViolation<T>> constraintViolations = validator.validate(t);
		if (constraintViolations.size() > 0) {
			Set<ConstraintViolation<?>> propagatedViolations = new HashSet<ConstraintViolation<?>>(
					constraintViolations.size());
			Set<String> classNames = new HashSet<String>();
			for (ConstraintViolation<?> violation : constraintViolations) {
				propagatedViolations.add(violation);
				classNames.add(violation.getLeafBean().getClass().getName());
			}
			StringBuilder builder = new StringBuilder();
			builder.append("校验失败 for classes ");
			builder.append(classNames);
			builder.append("\n校验条件:[\n");
			for (ConstraintViolation<?> violation : constraintViolations) {
				builder.append("\t").append(violation.toString()).append("\n");
			}
			builder.append("]");

			throw new ConstraintViolationException(builder.toString(), propagatedViolations);
		}
	}
}
