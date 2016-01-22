package com.github.crystal.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.crystal.util.PropertiesUtils;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.PushPayload;

public class JPushUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(JPushUtil.class);

	private static String appkey = PropertiesUtils.getProperty("jpush.appkey");
	private static String pwd = PropertiesUtils.getProperty("jpush.pwd");
	public static final String JPUSH_ALIAS_PREFIX = "naoni_alias_";
	public static final int pushTime = 3;//3秒之内的立刻推送 
	private static final int maxRetryTimes = 3;//重发次数
	
	public static PushResult push(PushPayload pp){
		PushResult result = null;
		JPushClient client = new JPushClient(pwd, appkey, maxRetryTimes);
		try {
			result = client.sendPush(pp);
		} catch (Exception e) {
			logger.error("jpush 推送失败");
		}
		return result;
	}
	
	public static final class Constants{
		/** 私信聊天类型*/
		public static final int CONTENTTYPE_MSG = 1;
		/** 视频闹你类型*/
		public static final int CONTENTTYPE_VIDEO = 2;
		/** 音频闹你类型*/
		public static final int CONTENTTYPE_VOICE = 3;
		/** 添加视频*/
		public static final int CONTENTTYPE_ADDVIDEO = 4;
		/** 添加视频评论*/
		public static final int CONTENTTYPE_ADDVIDEOCOMMENT = 5;
		/** 添加话题*/
		public static final int CONTENTTYPE_ADDTOPIC = 6;
		/** 添加话题评论*/
		public static final int CONTENTTYPE_ADDTOPICCOMMENT = 7;
		/** 添加关注，推送给被关注者*/
		public static final int CONTENTTYPE_ADDFOLLOW = 8;
		/** 弹幕通知*/
		public static final int CONTENTTYPE_POPCAP = 9;
		/** 回复视频评论 */
		public static final int CONTENTTYPE_REPLYVIDEOCOMMENT = 10;
		/** 回复话题评论 */
		public static final int CONTENTTYPE_REPLYTOPICCOMMENT = 11;
		/** 发起场景聊天 */
		public static final int CONTENTTYPE_SCENETALK_CALL = 12;
		/** 应答场景聊天 */
		public static final int CONTENTTYPE_SCENETALK_ANSWER = 13;
		/** 结束场景聊天 */
		public static final int CONTENTTYPE_SCENETALK_OVER = 14;
		/** 场景聊天推送消息类型 */
		public static final int CONTENTTYPE_SCENETALK_MSG = 15;
		/** 通知对方收到闹你*/
		public static final int CONTENTTYPE_CLOCK_BACK = 16;		
	}
	
}
