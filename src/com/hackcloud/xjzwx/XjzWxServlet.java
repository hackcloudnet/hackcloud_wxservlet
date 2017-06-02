package com.hackcloud.xjzwx;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.common.util.http.URIUtil;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
//import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import java.sql.*;

/**
 * Servlet implementation class xjzwxservlet
 */
public class XjzWxServlet extends HttpServlet {
	  private static final long serialVersionUID = 1L;
	  protected WxMpInMemoryConfigStorage wxMpConfigStorage;
	  protected WxMpService wxMpService;
	  protected WxMpMessageRouter wxMpMessageRouter;
	  protected boolean wxMenuBuilding() //建立菜单的方法
	  {
		WxMenu wxMenu = new WxMenu();  
		
		List<WxMenuButton> lwmbutton=new ArrayList<WxMenuButton>();
		WxDTO menuDTO = new WxDTO();
		ResultSet bootMenu = menuDTO.wxExecuteQuery("select * from wx_menu where menu_button_type=0"); 
		try {
			while (bootMenu.next())//建立主菜单
			{
			WxMenuButton wxmButton=new WxMenuButton();
			wxmButton.setName(bootMenu.getString("menu_name"));
			if (bootMenu.getInt("menu_havesub")!=0)//有子菜单的处理
			{
				List<WxMenuButton> lwmSubButton=new ArrayList<WxMenuButton>();
				WxDTO subMenuDTO = new WxDTO();
				ResultSet subMenu=subMenuDTO.wxExecuteQuery("select * from wx_menu where menu_root="+bootMenu.getInt("id"));
				while (subMenu.next())//建立子菜单
				{
					WxMenuButton wxSubButton=new WxMenuButton();
					wxSubButton.setName(subMenu.getString("menu_name"));
					wxSubButton.setType(subMenu.getString("menu_type"));
					wxSubButton.setUrl(subMenu.getString("menu_url"));
					wxSubButton.setKey(subMenu.getString("menu_key"));
					wxSubButton.setMediaId(subMenu.getString("menu_media_id"));
					lwmSubButton.add(wxSubButton);
				
				}
				wxmButton.setSubButtons(lwmSubButton);
				
			}else //没子菜单的处理
			{
				
				wxmButton.setName(bootMenu.getString("menu_name"));
				wxmButton.setType(bootMenu.getString("menu_type"));
				wxmButton.setUrl(bootMenu.getString("menu_url"));
				wxmButton.setKey(bootMenu.getString("menu_key"));
				wxmButton.setMediaId(bootMenu.getString("menu_media_id"));
				
				
			}
			lwmbutton.add(wxmButton);
			
			
			
			}
		} catch (SQLException e) {
			
			e.printStackTrace();
			return false;
		}
		  
		  
		try {
			wxMenu.setButtons(lwmbutton);
			wxMpService.getMenuService().menuCreate(wxMenu);
		} catch (WxErrorException e) {
			
			e.printStackTrace();
			return false;
		}  
		  
		return true;  
	  }
	  
	  protected boolean wxRouteBuilding()  //载入微信关键字route 的方法
	  {
		  wxMpMessageRouter = new WxMpMessageRouter(wxMpService);   
	     
			try {
				 WxDTO wxDTO= new WxDTO();
				 ResultSet wxRule = wxDTO.wxExecuteQuery("select * from wx_rule where wx_rule_disable=0 order by wx_rule_priority");
				    
				while (wxRule.next())
				{
					if (wxRule.getString("wx_rule_replytype").equals("text"))
					{
						int wxRuleReplyId = wxRule.getInt("wx_rule_replyid");
						WxDTO replyDTO = new WxDTO();
						ResultSet wxTextRule = replyDTO.wxExecuteQuery("select * from reply_text where id="+wxRuleReplyId);
					
						wxTextRule.next();
						final String replyText = wxTextRule.getString("reply_text_content");
					    wxTextRule.close();
						replyDTO.closeWxDTO();
						wxMpMessageRouter
						.rule()
						.async(wxRule.getInt("wx_rule_async")==1)  //是否异步，一般为同步
						.content(wxRule.getString("wx_rule_wordkey"))//关键字
						.msgType(wxRule.getString("wx_rule_msgtype"))//消息类型
						.event(wxRule.getString("wx_rule_event"))   //事件名
						.eventKey(wxRule.getString("wx_rule_eventkey"))//二维码参数 选用
						.handler(new WxMpMessageHandler() {
					       public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService,WxSessionManager sessionManager) {
							
								   WxMpXmlOutTextMessage m
								         = WxMpXmlOutMessage.TEXT().content(replyText).fromUser(wxMessage.getToUser())
								           .toUser(wxMessage.getFromUser()).build();							        			
								   return m ;  
								  }
								 
						} )
						.end();
					}//text回复建立
				
					if (wxRule.getString("wx_rule_replytype").equals("news"))
					{
						int wxRuleReplyId = wxRule.getInt("wx_rule_replyid");
						WxDTO replyDTO = new WxDTO();
						ResultSet wxNewsRule = replyDTO.wxExecuteQuery("select * from reply_news where id="+wxRuleReplyId);
					
						wxNewsRule.next();
						String url = wxNewsRule.getString("reply_news_url");
						if (wxNewsRule.getInt("reply_news_Oauth")==1)
						{
							url = wxMpService.oauth2buildAuthorizationUrl(url,WxConsts.OAUTH2_SCOPE_USER_INFO, null);
						}
						final WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
						  item.setDescription(wxNewsRule.getString("reply_news_description"));
						  item.setPicUrl(wxNewsRule.getString("reply_news_picurl"));
						  item.setTitle(wxNewsRule.getString("reply_news_title"));
						  item.setUrl(url);
					    wxNewsRule.close();
						replyDTO.closeWxDTO();
						
						
						wxMpMessageRouter
						.rule()
						.async(wxRule.getInt("wx_rule_async")==1)  //是否异步，一般为同步
						.content(wxRule.getString("wx_rule_wordkey"))//关键字
						.msgType(wxRule.getString("wx_rule_msgtype"))//消息类型
						.event(wxRule.getString("wx_rule_event"))   //事件名
						.eventKey(wxRule.getString("wx_rule_eventkey"))//二维码参数 选用
						.handler(new WxMpMessageHandler() {
					       public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService,WxSessionManager sessionManager) {
							
					    	   WxMpXmlOutNewsMessage m = WxMpXmlOutMessage.NEWS()
										  .fromUser(wxMessage.getToUser())
										  .toUser(wxMessage.getFromUser())
										  .addArticle(item)
										  .build();						        			
								   return m ;  
								  }
								 
						} )
						.end();
					}//回复图文
					
				}
				wxRule.close();
				wxDTO.closeWxDTO();
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			};
		
			return true;
					
					
	
		}
	  
	  @Override public void init() throws ServletException {
	    super.init();
	    WxDTO wxDTO = new WxDTO(); //微信后台数据库连接初始化 
	    ResultSet wxDTORs = wxDTO.wxExecuteQuery("select * from wx_config" );//从微信后台数据库读取公众号设置
	    String wxAppID = "";
	    String wxAppsecret = "";
	    String wxToken = "";
	    try { 
	    	//给微信公众号信息赋值
	    	wxDTORs.next();
	    	wxAppID = wxDTORs.getString("wx_appid");
	    	wxAppsecret = wxDTORs.getString("wx_appsecret");
	    	wxToken = wxDTORs.getString("wx_token");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	    wxMpConfigStorage = new WxMpInMemoryConfigStorage();
	    wxMpConfigStorage.setAppId(wxAppID); // 设置微信公众号的appid
	    wxMpConfigStorage.setSecret(wxAppsecret); // 设置微信公众号的app corpSecret
	    wxMpConfigStorage.setToken(wxToken); // 设置微信公众号的token
	    wxMpConfigStorage.setAesKey("pZLw3UETyRFspipTl5zqSGSZL9IsWTHJsr8hIRJLMQM");//config.setAesKey("..."); // 设置微信公众号的EncodingAESKey
	 //   wxMpConfigStorage.setOauth2redirectUri("http://www.jzh666.xyz/xjzwxservlet");//设定需要授权访问的URL
	    wxMpService = new WxMpServiceImpl();
	    wxMpService.setWxMpConfigStorage(wxMpConfigStorage); //存储公众号配置到wxMpService
	  
	 
		wxRouteBuilding();//建立关键字route
	
	  }
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 response.setContentType("text/html;charset=utf-8");
		 response.setStatus(HttpServletResponse.SC_OK);
		
	 //接受ajax 请求 侦听开始
		
		 
		String action=request.getParameter("action");//action参数侦听
		if(!StringUtils.isBlank(action))
		{
			if (action.equals("wxRule_rebuilding"))//action=="wxRule_rebuiding" ,重建  route
			{
				if (wxRouteBuilding())
				{
				response.getWriter().println("success");
				}else
				{
					response.getWriter().println("error");	
				}
			return;
			}
			if (action.equals("wxMenu_rebuilding"))//action=="wxRule_rebuiding" ,重建  route
			{
				if (wxMenuBuilding())
				{
				response.getWriter().println("success");
				}else
				{
					response.getWriter().println("error");	
				}
			return;
			}
			
			if (action.equals("wx_getdate")) 
			{
				 Date date = new Date();
			     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			     String serverDate = df.format(date);
			     response.getWriter().println("{'date':'"+serverDate+"'}");
			     return;
			}
			if (action.equals("wx_gettime")) 
			{
				 Date date = new Date();
			     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			     String servertime = df.format(date);
			     response.getWriter().println(servertime);
			     return;
			}
			
			
			if (action.equals("wx_jsapi"))  //action=="wx_jsapi",返回jsapisignture
			{
				try {
					String url = request.getParameter("url");
					WxJsapiSignature wxJsapiSignture=wxMpService.createJsapiSignature(url);
					
					//发送json格式
				 //需要发送的数据
				 	wxJsapiSignture.getAppId();
					wxJsapiSignture.getUrl();
					wxJsapiSignture.getSignature();
					wxJsapiSignture.getTimestamp();
					wxJsapiSignture.getNonceStr();
					
					
					response.getWriter().write("{'appid':'"+wxJsapiSignture.getAppId()+"','url':'"+wxJsapiSignture.getUrl()+"','signature':'"+wxJsapiSignture.getSignature()+"','timestamp':'"+wxJsapiSignture.getTimestamp()+"','noncestr':'"+wxJsapiSignture.getNonceStr()+"'}");
					
					
					
				} catch (WxErrorException e) {
					
					e.printStackTrace();
											}
				return;
						
			}
			
			
			if (action.equals("wx_saveimg"))//请求保存图片到服务器，并把照片数据写入小记者数据库<接收图片在微信服务器临时素材库的 ID
			{
				String mediaId = request.getParameter("mediaid");
				String xjzId = request.getParameter("xjzid");
				if(!StringUtils.isBlank(mediaId) && !StringUtils.isBlank(xjzId))
				{
					try {
						File file = wxMpService.getMaterialService().mediaDownload(mediaId);
						File desPath = new File("C:"+File.separator+"Program Files"+File.separator+"Apache Software Foundation"+File.separator+"Tomcat 6.0"+File.separator+"webapps"+File.separator+"ROOT"+File.separator+"data"+File.separator+"touxiang"+File.separator+"undefined"+File.separator);
						
						File desFile = new File(desPath,xjzId);
						if (desFile.exists())
						{
							desFile.delete();
							
						}
						if (file.renameTo(desFile))
						{
						String desFileName = desFile.getName();
						
						String imgjson = "[{'storeFileName':'"+desFileName+"','realFileName':'"+desFileName+".jpg'}]";
						String sql = "update xiaojizhe set  照片=\""+imgjson+"\" ,s1='/baas/justep/attachment/simpleFileStore?realFileName="+desFileName+".jpg&storeFileName="+desFileName+"&ownerID=undefined&operateType=browse' where id="+xjzId;
					//	response.getWriter().write(sql);
						WxDTO wxDTO = new WxDTO();
						wxDTO.wxExecuteUpdate(sql);
						response.getWriter().write(desFileName+"   success!");
						}else
						{
							response.getWriter().write("error!");
						}
					} catch (WxErrorException e) {
						
						e.printStackTrace();
					}
					
				return;	
					
				}
			
				response.getWriter().write("error");
				
				
				return;
			}
		return;		
		}
		
		
		
	//授权访问	
	String code=request.getParameter("code");	//Oauth2 授权code回调响应
		if(!StringUtils.isBlank(code))
		{
			
		try {
			WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
			WxMpUser wxMpUser = wxMpService.oauth2getUserInfo(wxMpOAuth2AccessToken, null);
			String nickName = wxMpUser.getNickname();
		//	String nickName = "";
			String headImgUrl = wxMpUser.getHeadImgUrl();
			String sex = wxMpUser.getSex();
			String openID = wxMpUser.getOpenId();
			String country = wxMpUser.getCountry();
			String province = wxMpUser.getProvince();
			String city = wxMpUser.getCity();
			String language = wxMpUser.getLanguage();
			String lang = "zh_CN"; //语言
			WxMpUser user;
			user = wxMpService.getUserService().userInfo(openID,lang);
			Boolean subscribe = user.getSubscribe();
			 Date date = new Date();
		     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		     String serverDate = df.format(date);
			response.getWriter().write("{'serverdate':'"+serverDate+"','nickName':'"+nickName+"','headImgUrl':'"+headImgUrl+"','sex':'"+sex+"','openID':'"+openID+"','country':'"+country+"','province':'"+province+"','city':'"+city+"','language':'"+language+"','subscribe':"+subscribe+"}");
		
		
			} catch (WxErrorException e) {
				response.getWriter().write("error");
				
		
											}
		return;
		}
	//微信后台接入验证	
    String signature = request.getParameter("signature");
		    
    String nonce = request.getParameter("nonce");
		    String timestamp = request.getParameter("timestamp");

		    if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
		      // 消息签名不正确，说明不是公众平台发过来的消息
	      response.getWriter().println("非法的请求");
	      return;
	    }

	    String echostr = request.getParameter("echostr");
		    if (echostr!=""  && echostr!=null) {
		      // 说明是一个仅仅用来验证的请求，回显echostr
		      response.getWriter().println(echostr);
		      return;
		    }
	//微信后台接收信息处理，并传入route
	    String encryptType = StringUtils.isBlank(request.getParameter("encrypt_type")) ?
		        "raw" :
		        request.getParameter("encrypt_type");

		    if ("raw".equals(encryptType)) {
		      // 明文传输的消息
		      WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
		      WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
		      response.getWriter().write(outMessage.toXml());
		      return;
		    }

		    if ("aes".equals(encryptType)) {
		      // 是aes加密的消息
		      String msgSignature = request.getParameter("msg_signature");
		      WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(request.getInputStream(), wxMpConfigStorage, timestamp, nonce, msgSignature);
		      WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
		      response.getWriter().write(outMessage.toEncryptedXml(wxMpConfigStorage));
		      return;
		    }

		    response.getWriter().println("不可识别的加密类型");
		    return;	
	    
} 

}
