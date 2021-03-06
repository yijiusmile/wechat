package com.soecode.wxtools.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import com.soecode.wxtools.bean.WxAccessToken;
import com.soecode.wxtools.exception.WxErrorException;
import com.soecode.wxtools.util.StringUtils;
import com.zd.wechat.entity.WechatConfig;
import com.zd.wechat.repository.WechatConfigRepository;

/**
 * 微信全局配置对象-从配置文件读取
 * 
 * @author antgan
 * @datetime 2016/12/14
 *
 */
@Service
public class WxConfig implements ApplicationRunner {

	// private static final String configFile = "/wx.properties";

	private static WxConfig config;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private WechatConfigRepository wechatConfigRepository;

	@PostConstruct
	private void init() {
		config = this;
	}

	// 程序启动自动从数据库加载微信的配置
	@Override
	public void run(ApplicationArguments var1) {
		try {
			WechatConfig config = wechatConfigRepository.findAll().get(0);
			initConfig(config);
		} catch (IndexOutOfBoundsException e) {
			logger.error("获取微信配置失败,请检查数据库[Wechat_T_Config]表的配置,或到config.jsp配置");
		} catch (Exception e) {
			logger.error("获取微信配置失败", e);
		}
	}

	private void initConfig(WechatConfig wechatConfig) {
		config.appId = wechatConfig.getAppId();
		config.appSecret = wechatConfig.getAppSecret();
		config.token = wechatConfig.getToken();
		config.aesKey = wechatConfig.getAesKey();
		config.mchId = wechatConfig.getMchId();
		config.apiKey = wechatConfig.getApiKey();
	}

	public WxConfig resetConfig(WechatConfig wechatConfig) {
		initConfig(wechatConfig);
		wechatConfigRepository.deleteAll();
		wechatConfigRepository.save(wechatConfig);
		return config;
	}

	// 配置文件读取项
	private volatile String appId;
	private volatile String appSecret;
	private volatile String token;
	private volatile String aesKey;
	private volatile String mchId;
	private volatile String apiKey;

	// 内存更新
	private volatile String accessToken;
	private volatile long expiresTime;
	private volatile String jsapiTicket;
	private volatile long jsapiTicketExpiresTime;

//	public WxConfig() { // 写读配置文件代码
//		Properties p = new Properties();
//		InputStream inStream = this.getClass().getResourceAsStream(configFile);
//		if (inStream == null) {
//			try {
//				throw new WxErrorException("根目录找不到文件");
//			} catch (WxErrorException e) {
//				e.printStackTrace();
//			}
//		}
//		try {
//			p.load(inStream);
//			this.appId = p.getProperty("wx.appId");
//			if (StringUtils.isNotBlank(this.appId))
//				this.appId = this.appId.trim();
//			this.appSecret = p.getProperty("wx.appSecret");
//			if (StringUtils.isNotBlank(this.appSecret))
//				this.appSecret = this.appSecret.trim();
//			this.token = p.getProperty("wx.token");
//			if (StringUtils.isNotBlank(this.token))
//				this.token = this.token.trim();
//			this.aesKey = p.getProperty("wx.aesKey");
//			if (StringUtils.isNotBlank(this.aesKey))
//				this.aesKey = this.aesKey.trim();
//			this.mchId = p.getProperty("wx.mchId");
//			if (StringUtils.isNotBlank(this.mchId))
//				this.mchId = this.mchId.trim();
//			this.apiKey = p.getProperty("wx.apiKey");
//			if (StringUtils.isNotBlank(this.apiKey))
//				this.apiKey = this.apiKey.trim();
//			inStream.close();
//		} catch (IOException e) {
//			try {
//				throw new WxErrorException("load wx.properties error,class根目录下找不到wx.properties文件");
//			} catch (WxErrorException e1) {
//				e1.printStackTrace();
//			}
//		}
//		System.out.println("load wx.properties success");
//	}

	/**
	 * 同步获取/加载单例
	 * 
	 * @return
	 */
	public static synchronized WxConfig getInstance() {
		return config;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public boolean isAccessTokenExpired() {
		return System.currentTimeMillis() > this.expiresTime;
	}

	public void expireAccessToken() {
		this.expiresTime = 0;
	}

	public synchronized void updateAccessToken(WxAccessToken accessToken) {
		updateAccessToken(accessToken.getAccess_token(), accessToken.getExpires_in());
	}

	public synchronized void updateAccessToken(String accessToken, int expiresInSeconds) {
		this.accessToken = accessToken;
		this.expiresTime = System.currentTimeMillis() + (expiresInSeconds - 200) * 1000l;
	}

	public String getJsapiTicket() {
		return jsapiTicket;
	}

	public void setJsapiTicket(String jsapiTicket) {
		this.jsapiTicket = jsapiTicket;
	}

	public long getJsapiTicketExpiresTime() {
		return jsapiTicketExpiresTime;
	}

	public void setJsapiTicketExpiresTime(long jsapiTicketExpiresTime) {
		this.jsapiTicketExpiresTime = jsapiTicketExpiresTime;
	}

	public boolean isJsapiTicketExpired() {
		return System.currentTimeMillis() > this.jsapiTicketExpiresTime;
	}

	public synchronized void updateJsapiTicket(String jsapiTicket, int expiresInSeconds) {
		this.jsapiTicket = jsapiTicket;
		// 预留200秒的时间
		this.jsapiTicketExpiresTime = System.currentTimeMillis() + (expiresInSeconds - 200) * 1000l;
	}

	public void expireJsapiTicket() {
		this.jsapiTicketExpiresTime = 0;
	}

	// getter

	public String getAppId() {
		return appId;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public String getToken() {
		return token;
	}

	public String getAesKey() {
		return aesKey;
	}

	public String getMchId() {
		return mchId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	@Override
	public String toString() {
		return "WxConfig [appId=" + appId + ", appSecret=" + appSecret + ", token=" + token + ", aesKey=" + aesKey
				+ ", mchId=" + mchId + ", apiKey=" + apiKey + ", accessToken=" + accessToken + ", expiresTime="
				+ expiresTime + ", jsapiTicket=" + jsapiTicket + ", jsapiTicketExpiresTime=" + jsapiTicketExpiresTime
				+ "]";
	}

}
