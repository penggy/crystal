package com.github.crystal.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.github.crystal.util.DateUtils;
import com.github.crystal.util.HttpUtils;
import com.github.crystal.util.JsonRawString;
import com.github.crystal.util.PropertiesUtils;
import com.google.common.collect.Lists;

@Controller
public class GlobalUploadController {

	private static final Logger logger = LoggerFactory.getLogger(GlobalUploadController.class);

	@RequestMapping(value = "/file/upload", method = RequestMethod.POST)
	@ResponseBody
	public JsonRawString upload(HttpServletRequest request, HttpServletResponse response) throws IllegalStateException,
			IOException {
		String dir = PropertiesUtils.getProperty("upload.dir", "/tmp/upload");
		String time = DateUtils.format(new Date(), "yyyyMMddHHmmss");
		String path = dir + "/" + time + "/";
		String host = HttpUtils.getHost(request);
		List<String> urls = Lists.newArrayList();
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession()
				.getServletContext());
		if (multipartResolver.isMultipart(request)) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			Iterator<String> iter = multiRequest.getFileNames();
			while (iter.hasNext()) {
				MultipartFile file = multiRequest.getFile(iter.next());
				if (file != null) {
					String fileName = file.getOriginalFilename();
					String filePath = path + fileName;
					FileOutputStream fos = null;
					InputStream is = null;
					try {
						is = file.getInputStream();
						fos = FileUtils.openOutputStream(new File(filePath));
						IOUtils.copy(is, fos);
					} catch (Exception e) {
						logger.error("save file failed. filename={}, msg={}", fileName, e.getMessage());
					} finally {
						IOUtils.closeQuietly(fos);
						IOUtils.closeQuietly(is);
					}
					urls.add(host + "/file/download/" + time + "/" + fileName);
				}
			}
		}
		return new JsonRawString(StringUtils.join(urls, ","));
	}

	@RequestMapping(value = "/file/download/{time}/{fileName}", method = RequestMethod.GET)
	public void file(HttpServletRequest request, HttpServletResponse response, @PathVariable String time,
			@PathVariable String fileName) {
		String dir = PropertiesUtils.getProperty("upload.dir", "/tmp/upload");
		String filePath = dir + "/" + time + "/" + fileName;
		InputStream in = null;
		OutputStream out = null;
		try {
			response.reset();
			response.setBufferSize(Integer.MAX_VALUE);
			response.setContentType("multipart/form-data");
			response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
			out = response.getOutputStream();
			in = new FileInputStream(filePath);
			int len = IOUtils.copy(in, out);
			response.setContentLength(len);
			response.setHeader("Content-Length",String.valueOf(len));
			out.flush();
			logger.debug("len : {}", len);
		} catch (Exception e) {
			String ename = e.getClass().getName();
			logger.debug(ename);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
