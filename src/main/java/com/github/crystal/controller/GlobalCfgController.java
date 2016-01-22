package com.github.crystal.controller;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.crystal.util.PropertiesUtils;
import com.mangofactory.swagger.annotations.ApiIgnore;

@RestController
@RequestMapping("/globalCfg")
@ApiIgnore
public class GlobalCfgController {

	@Resource
	private ServletContext servletContext;
	
	@RequestMapping("/getPropertyMap")
	@ResponseBody
	public Map<String, String> getPropertyMap(String key, String joiner, String pair) {
		joiner = StringUtils.defaultIfEmpty(joiner, ",");
		pair = StringUtils.defaultIfEmpty(pair, "-");
		return PropertiesUtils.getPropertyMap(key, joiner, pair);
	}

	@RequestMapping(value = "/propery/{key}", method = RequestMethod.GET)
	public String getProperty(@PathVariable String key) {
		return PropertiesUtils.getProperty(key, "");
	}

	@PostConstruct
	protected void fillServletContext() {
		Map<String, String> props = PropertiesUtils.getProperties();
		for (Map.Entry<String, String> prop : props.entrySet()) {
			String key = prop.getKey();
			String val = prop.getValue();
			servletContext.setAttribute(key, val);
		}
	}

}
