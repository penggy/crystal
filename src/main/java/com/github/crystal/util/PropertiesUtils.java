package com.github.crystal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.google.common.collect.Maps;

/**
 * 
 * @author pengwu2
 *
 */
public class PropertiesUtils extends PropertyPlaceholderConfigurer {
	
	private static Map<String, String> properties = new HashMap<String, String>();

	public static void setProperties(Map<String, String> properties) {
		PropertiesUtils.properties.putAll(properties);
	}

	public static Map<String, String> getProperties() {
		return properties;
	}

	public static String getProperty(String key) {
		String val = StringUtils.defaultIfEmpty(properties.get(key), "");
		return StringUtils.trim(val.trim());
	}

	public static String getProperty(String key, String defVal) {
		String val = StringUtils.defaultIfEmpty(properties.get(key), defVal);
		return StringUtils.trim(val);
	}

	public static void removeProperty(String key) {
		properties.remove(key);
	}

	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
		Map<String, String> resolvePrperties = new HashMap<String, String>();
		for (Object key : props.keySet()) {
			String keyStr = key.toString();
			resolvePrperties.put(keyStr, props.getProperty(keyStr));
		}
		PropertiesUtils.setProperties(resolvePrperties);
	}
	
	/**
	 * 从配置文件读取字典数据,形如:0-新建,1-待审批,2-待生效...
	 * 
	 * @param key
	 * @return
	 */
	public static Map<String, String> getPropertyMap(String key) {
		return getPropertyMap(key, ",", "-");
	}

	public static Map<String, String> getPropertyMap(String key, String joiner, String pair) {
		Map<String, String> map = Maps.newTreeMap();
		if (StringUtils.isEmpty(key)) {
			return map;
		}
		String result = properties.get(key);
		if (StringUtils.isEmpty(result)) {
			return map;
		}
		String[] pairs = result.split(joiner);
		for (String _pair : pairs) {
			if (StringUtils.isEmpty(_pair)) {
				continue;
			}
			String[] kvs = _pair.split(pair);
			if (kvs.length == 2) {
				map.put(StringUtils.trim(kvs[0]), StringUtils.trim(kvs[1]));
			}
		}
		return map;
	}
}
