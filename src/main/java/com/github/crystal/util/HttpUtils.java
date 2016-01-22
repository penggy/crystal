package com.github.crystal.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author <a href="mailto:peng.wu@foxmail.com">wu.peng</a>
 * 
 */
public class HttpUtils {

	private static final Log log = LogFactory.getLog(HttpUtils.class);

	public static byte[] get(String url) {
		return get(url, 0);
	}

	public static byte[] get(String url, int timeout) {
		HttpClient hc = new HttpClient();
		hc.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
		hc.getHttpConnectionManager().getParams().setSoTimeout(timeout);
		GetMethod method = null;
		try {
			method = new GetMethod(url);
			method.setFollowRedirects(false);
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
			method.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET, "utf-8");
			int code = hc.executeMethod(method);
			if (code == HttpStatus.SC_OK) {
				return method.getResponseBody();
			}
		} catch (Exception e) {
			log.error(e, e);
		} finally {
			if (method != null) {
				method.releaseConnection();
				method = null;
			}
			hc.getHttpConnectionManager().closeIdleConnections(0);
		}
		return new byte[] {};
	}

	public static byte[] post(String url, Map<String, String> params) {
		return post(url, params, 0);
	}

	public static byte[] post(String url, Map<String, String> params, int timeout) {
		HttpClient hc = new HttpClient();
		hc.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
		hc.getHttpConnectionManager().getParams().setSoTimeout(timeout);
		PostMethod method = null;
		try {
			method = new PostMethod(url);
			method.setFollowRedirects(false);
			List<NameValuePair> _params = new ArrayList<NameValuePair>();
			if (params != null) {
				for (String key : params.keySet()) {
					_params.add(new NameValuePair(key, params.get(key)));
				}
			}
			method.setRequestBody(_params.toArray(new NameValuePair[] {}));
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
			method.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET, "utf-8");
			int code = hc.executeMethod(method);
			if (code == HttpStatus.SC_OK) {
				return method.getResponseBody();
			}
		} catch (Exception e) {
			log.error(e, e);
		} finally {
			if (method != null) {
				method.releaseConnection();
				method = null;
			}
			hc.getHttpConnectionManager().closeIdleConnections(0);
		}
		return new byte[] {};
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getParams(ServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		if (request == null) {
			return params;
		}
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			String paramValue = request.getParameter(paramName);
			params.put(paramName, paramValue);
		}
		return params;
	}

	public static String getParam(ServletRequest request, String paramName, String defaultVal) {
		String val = request.getParameter(paramName);
		if (StringUtils.isEmpty(val)) {
			val = defaultVal;
		}
		return val;
	}
	
	/**
	 * 拼接url参数
	 * 
	 * @param url
	 * @param list
	 * @return
	 */
	public static String paramUrl(String url, List<NameValuePair> list) {
		try {
			GetMethod method = new GetMethod(url);
			method.setQueryString(list.toArray(new NameValuePair[] {}));
			return method.getURI().toString();
		} catch (URIException e) {
			log.error(e, e);
			throw new RuntimeException("拼接url参数失败");
		}
	}

	/**
	 * 根据文件url获取文件的流
	 */
	public static InputStream getInputStreamByUrl(String url) {
		try {
			URL _url = new URL(url);
			HttpURLConnection httpURLConnection = (HttpURLConnection) _url.openConnection();
			httpURLConnection.setDoInput(true);// 打开读取属性
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setConnectTimeout(50000);
			httpURLConnection.setReadTimeout(50000);
			httpURLConnection.connect();
			return httpURLConnection.getInputStream();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获得文件的MD516进制字符串
	 */
	public static String getFileMD5(String url) {
		InputStream in = null;
		try {
			in = HttpUtils.getInputStreamByUrl(url);
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte buffer[] = new byte[1024];
			int len;
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			return new String(Hex.encodeHex(digest.digest()));
		} catch (Exception e) {
		} finally {
			IOUtils.closeQuietly(in);
		}
		return "";
	}

	/**
	 * @param url
	 * @return size of the resource or -1 if unknown
	 */
	public static int getFileSize(String url) {
		try {
			URL _url = new URL(url);
			HttpURLConnection httpconn = (HttpURLConnection) _url.openConnection();
			return httpconn.getContentLength();
		} catch (Exception e) {
		}
		return -1;
	}

	public static String getRemortIP(HttpServletRequest request) {
		String realIp = request.getHeader("X-Real-IP");
		if(StringUtils.isEmpty(realIp)){
			return request.getRemoteAddr();
		}
		return realIp;
	}

	public static String getUserAgent(HttpServletRequest request) {
		if (request == null)
			return "";
		return request.getHeader("User-Agent");
	}
	
	public static String getHost(HttpServletRequest request) {
		if(request == null) {
			return "";
		}
		String url = request.getRequestURL().toString();
		String uri = request.getRequestURI();
		int end = url.length() - uri.length();
		String server = url.substring(0, end);
		String context = request.getContextPath();
		return server + context;
	}

	/**
	 * 读取http请求内容
	 * 
	 * @param request
	 * @return
	 */
	public static String readContent(HttpServletRequest request) {
		BufferedReader br = null;
		try {
			br = request.getReader();
			char[] buf = new char[1024];
			int len = 0;
			StringWriter sw = new StringWriter();
			while ((len = br.read(buf)) != -1) {
				sw.write(buf, 0, len);
			}
			return sw.toString();
		} catch (Exception e) {
			log.info(e, e);
		} finally {
			IOUtils.closeQuietly(br);
		}
		return "";
	}

	/**
	 * 对下载文件名进行编码,防止中文乱码
	 * 
	 * @param filename
	 * @param request
	 * @return
	 */
	public static String encodeFilename(String filename, HttpServletRequest request) {
		String agent = request.getHeader("USER-AGENT");
		try {
			if ((agent != null) && (-1 != agent.indexOf("MSIE"))) {
				String newFileName = URLEncoder.encode(filename, "UTF-8");
				newFileName = StringUtils.replace(newFileName, "+", "%20");
				if (newFileName.length() > 150) {
					newFileName = new String(filename.getBytes("GB2312"), "ISO8859-1");
					newFileName = StringUtils.replace(newFileName, " ", "%20");
				}
				return newFileName;
			}
			if ((agent != null) && (-1 != agent.indexOf("Mozilla")))
				return MimeUtility.encodeText(filename, "UTF-8", "B");

			return filename;
		} catch (Exception ex) {
			return filename;
		}
	}
	
	/**
	 * 判断一个请求是否是ajax请求
	 * @param request
	 * @return
	 */
	public static boolean isAjax(HttpServletRequest request){
		String xmlHttpRequest = request.getHeader("X-Requested-With");
		return StringUtils.equalsIgnoreCase(xmlHttpRequest, "XMLHttpRequest");
	}

}
