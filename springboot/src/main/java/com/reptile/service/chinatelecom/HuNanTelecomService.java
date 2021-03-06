package com.reptile.service.chinatelecom;

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.reptile.model.NewTelecomBean;
import com.reptile.util.ConstantInterface;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @ClassName: HuNanTelecomService  
 * @Description: TODO  
 * @author: fangshuang
 * @date 2017年12月29日  
 *
 */
@Service
public class HuNanTelecomService {
    private Logger logger= LoggerFactory.getLogger(ShanDongTelecomService.class);
    NewTelecomBean monthNum = new NewTelecomBean();
    List<Object> finallyList = new ArrayList<Object>();
    List<Object> temp = new ArrayList<Object>();
	@SuppressWarnings({ "rawtypes", "resource" })
	public Map<String,Object> hunanimgeCode(HttpServletRequest request,String idCard,String username){
		Map<String,Object> map = new HashMap<String,Object>(10);
        Map<String,Object> data=new HashMap<String,Object>(10);
        
        HttpSession session = request.getSession();
		Object attribute =  session.getAttribute("GBmobile-webclient");
		WebClient webClient=(WebClient)attribute;
		if(webClient==null){
	    	 map.put("errorCode", "0001");
			 map.put("errorInfo", "请先登录!");
			 return map;
	     }else{
	    	 try{
	    		 HtmlPage nextPage=webClient.getPage("http://hn.189.cn/grouplogin?rUrl=http://hn.189.cn/webportal-wt/hnselfservice/billquery/bill-query!queryBill.action&fastcode=10000280&cityCode=hn");
	    		 Thread.sleep(1000);
	    		
	    		 HtmlForm form =  nextPage.getFormByName("checkForm");
	    		 form.getInputByName("cardId").setValueAttribute(idCard);
	    		 form.getInputByName("userName").setValueAttribute(username);
	    		 HtmlPage click1 = form.getElementsByTagName("a").get(0).click();
	    		 
	    		 Thread.sleep(1000);
	    		 String str = click1.asText();
	    		 final String a = "请输入短信验证码";
	    		 if(str.indexOf(a)==-1){
	    			 map.put("ResultInfo","校验错误");
                     map.put("ResultCode","0001");
                     map.put("errorInfo","校验错误");
                     map.put("errorCode","0001");
                     map.put("data",data);
                     return map;
	    		 }
	    		//数据类型select
	    		HtmlSelect selectType=(HtmlSelect) click1.getElementById("selectPatyType");
	    		//选中移动语音
	    		selectType.setSelectedIndex(0);
	    		//号码select
	    		HtmlSelect selectphone=(HtmlSelect) click1.getElementById("mynum");
	    		//选中该用户手机
	    		selectphone.setSelectedIndex(0);
	    		//月份select
				HtmlSelect selectmonth=(HtmlSelect) click1.getElementById("blqYearMonth");
				selectmonth.setSelectedIndex(0);
				click1.getElementById("startDay").setAttribute("value", "1");
				//endday下拉框
				DomNodeList selectday=(DomNodeList) click1.getElementByName("endDay").getElementsByTagName("option");
				//最后一天select
				HtmlSelect selectlday=(HtmlSelect) click1.getElementById("endDay");
				selectlday.setSelectedIndex(selectday.size()-1);
				File file = new File(request.getServletContext().getRealPath("/HNimageCode"));
                if (!file.exists()) {
                    file.mkdirs();
                }
                String fileName = "HN" + System.currentTimeMillis() + ".png";
                HtmlImage imageCode = (HtmlImage) click1.getElementById("validationCode4");
                BufferedImage read = imageCode.getImageReader().read(0);
                ImageIO.write(read, "png", new File(file, fileName));

                data.put("CodePath", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/HNimageCode/" + fileName);
                map.put("data", data);
                map.put("errorCode", "0000");
                map.put("errorInfo", "图片验证码获取成功!");
                request.getSession().setAttribute("HNwebclient", webClient);
                request.getSession().setAttribute("HNhtmlPage", click1);
				HtmlPage infopage = click1.getElementById("blQueryBtn").click();
				Thread.sleep(1000);
            	request.getSession().setAttribute("infoPage", infopage);

		     }catch (Exception e) {
	             e.printStackTrace();
	             map.put("errorCode", "0001");
	             map.put("errorInfo", "网络连接异常!");
	         }
	     }
	    return map;
	}

	/**
	 * 图片验证码
	 * @param request
	 * @param imageCode
	 * @return
	 */
		@SuppressWarnings("resource")
		public Map<String,Object> huNanPhoneCode(HttpServletRequest request,String imageCode){
			Map<String,Object> map = new HashMap<String,Object>(10);
	        Map<String,Object> data=new HashMap<String,Object>(10);
	        Object attribute = request.getSession().getAttribute("HNwebclient");
	        Object htmlpage = request.getSession().getAttribute("HNhtmlPage");
	        
	        if (attribute == null || htmlpage == null) {
	            map.put("errorCode", "0001");
	            map.put("errorInfo", "操作异常!");
	        } else {
	            try {
	            	 WebClient webClient = (WebClient) attribute;
	                 HtmlPage page = (HtmlPage) htmlpage;
	                 
	                 HtmlInput codeinput = (HtmlInput) page.getElementById("randQuery");
	                 codeinput.setValueAttribute(imageCode);
	                 List<String>  alertList=new ArrayList<String>(); 
	     	         CollectingAlertHandler head=new CollectingAlertHandler(alertList);
	     	         webClient.setAlertHandler(head);
	                 HtmlPage infopage = page.getElementById("btnSendCode").click();
	                 Thread.sleep(500);
	                 
	                 request.getSession().setAttribute("HNsendMesPage", infopage);
	                 
	                 Thread.sleep(500);
	                 if(alertList.size()>0){
	                	 final String b = "成功";
	 					if(alertList.get(0).toString().contains(b)){
	 						 map.put("errorCode", "0000");
	 				         map.put("errorInfo", "短信验证码发送成功!");
	 					}else{
	 						map.put("errorCode", "0001");
	 				        map.put("errorInfo", alertList.get(0).toString());
	 					}		
	 				}	
	            } catch (Exception e) {
	                logger.warn(e.getMessage()+"  湖南发送手机验证码  mrlu",e);
	                e.printStackTrace();
	                map.put("errorCode", "0001");
	                map.put("errorInfo", "网络连接异常!");
	            }
	        }
	        
	        data.put("imageCode", imageCode);
	        map.put("data", data);
	        return map;   
		}
		
		/**
		 * 短信验证码以及信息
		 * @param request
		 * @param PassCode
		 * @param imageCode
		 * @param longitude
		 * @param latitude
		 * @param phoneNumber
		 * @param servicepwd
		 * @return
		 */
		@SuppressWarnings({ "rawtypes", "resource", "unused", "unchecked" })
		public Map<String,Object> huNanPhoneDetail(HttpServletRequest request,String passCode,String imageCode,String longitude,String latitude,String phoneNumber,String servicepwd){
			Map<String,Object> map = new HashMap<String,Object>(10);
	        Map<String,Object> data=new HashMap<String,Object>(10);
	        PushState.state(phoneNumber, "callLog",100);
	        Object attribute = request.getSession().getAttribute("HNwebclient");
	        Object htmlpage = request.getSession().getAttribute("HNsendMesPage");
	        if (attribute == null || htmlpage == null) {
	            map.put("errorCode", "0001");
	            map.put("errorInfo", "操作异常!");
	        } else {
	            try {
	            	 WebClient webClient = (WebClient) attribute;
	                 HtmlPage page = (HtmlPage) htmlpage;
	                 page.getElementById("blqvalicode").setAttribute("value", passCode);
	                 HtmlPage nextpage = page.getElementById("blQueryBtn").click();
	                 Thread.sleep(500);
	                 final String c = "验证码错误!";
	                 if(page.asText().contains(c)){
	                	 map.put("errorCode", "0001");
	                     map.put("errorInfo", "验证码输入错误，重新输入");
	                     return map;
	                 }else{
	                	 //月份下拉框
	                	 DomNodeList selectallmonth=(DomNodeList) page.getElementByName("queryMonth").getElementsByTagName("option");
			    		 for(int i=0;i<selectallmonth.size();i++){
			    			 //月份select
			    			 HtmlSelect selectmonth=(HtmlSelect) page.getElementById("blqYearMonth");
				    		 selectmonth.setSelectedIndex(i);
				    		 String month1 = selectmonth.getOption(i).getValueAttribute();
				    		 String month = month1.substring(5);
				    		 String year = month1.substring(0, 4);
				    		 //获取最后一天
				    		 Calendar cal = Calendar.getInstance();
				    		 SimpleDateFormat sdf = new SimpleDateFormat("dd");
				    		 cal.set(Calendar.YEAR, Integer.valueOf(year));
				    		 cal.set(Calendar.MONTH, Integer.valueOf(month));
				    		 cal.set(Calendar.DAY_OF_MONTH, 1);
				    		 cal.add(Calendar.DAY_OF_MONTH, -1);
				    		 String lday = sdf.format(cal.getTime());				    		 			
				    		 Date date = new Date();
			    			 SimpleDateFormat sdftoday =  new SimpleDateFormat( "hh:mm:ss" );
			    			 String today = sdftoday.format(date);	
			    			 HtmlPage infopage1; 
			    			 if(i==0){
			    				 //第一个月
			    				 String loadPath = "http://hn.189.cn/webportal-wt/hnselfservice/billquery/bill-query!queryBillx.action?tm=2035%E4%B8%8B%E5%8D%88"+today+"&tabIndex=2&queryMonth="+month1+"&patitype=2&startDay=1&endDay="+lday+"&valicode="+passCode+"&code="+passCode+"&accNbr=";					    																									
					    		 URL url = new URL(loadPath);
					    		 WebRequest webRequest = new WebRequest(url);
					    		 webRequest.setHttpMethod(HttpMethod.GET);
				                 List<NameValuePair> list = new ArrayList<NameValuePair>();
				                 			    							    			
				                 list.add(new NameValuePair("tm", "2035下午"+today));
				                 list.add(new NameValuePair("queryMonth", month1));
				                 list.add(new NameValuePair("startDay", "1"));
				                 list.add(new NameValuePair("endDay", lday));
				                 list.add(new NameValuePair("patitype", "2"));			                
				                 list.add(new NameValuePair("tabIndex", "2"));
				                 list.add(new NameValuePair("code", passCode));
				                 list.add(new NameValuePair("tabIndex", ""));
				                 list.add(new NameValuePair("accNbr", ""));
				                 list.add(new NameValuePair("valicode", "PassCode"));
			                     webRequest.setRequestParameters(list);
				                 infopage1 = webClient.getPage(webRequest);
				                 Thread.sleep(1000);
			    			 }else{
			    				 //后五个月
			    				 String loadPath = "http://hn.189.cn/webportal-wt/hnselfservice/billquery/bill-query!queryBillx.action?tm=2035%E4%B8%8B%E5%8D%88"+today+"&tabIndex=2&queryMonth="+month1+"&patitype=2&startDay=1&endDay="+lday+"&valicode=&code=undefined&accNbr=";
					    		 																					
					    		 URL url = new URL(loadPath);
					    		 WebRequest webRequest = new WebRequest(url);
					    		 webRequest.setHttpMethod(HttpMethod.GET);
				                 List<NameValuePair> list = new ArrayList<NameValuePair>();
				                 			    							    			
				                 list.add(new NameValuePair("tm", "2035下午"+today));
				                 list.add(new NameValuePair("queryMonth", month1));
				                 list.add(new NameValuePair("startDay", "1"));
				                 list.add(new NameValuePair("endDay", lday));
				                 list.add(new NameValuePair("patitype", "2"));			                
				                 list.add(new NameValuePair("tabIndex", "2"));
				                 list.add(new NameValuePair("code", "undefined"));
				                 list.add(new NameValuePair("tabIndex", ""));
				                 list.add(new NameValuePair("accNbr", ""));
				                 list.add(new NameValuePair("valicode", ""));
			                     webRequest.setRequestParameters(list);
				                 infopage1 = webClient.getPage(webRequest);
				                 Thread.sleep(1000);
			    			 }
				    		 
			                 
		                	 request.getSession().setAttribute("infoPage1", infopage1);
		                	 map=huNanPhoneinfo(request,phoneNumber,month1,lday);
		                	 temp = (List<Object>) map.get("list");
		                	 map.remove("list");
		                	 finallyList.addAll(temp);
			    		 }	    		 	                	
	                 }
	            } catch (Exception e) {
	                e.printStackTrace();
	                map.put("errorCode", "0001");
	                map.put("errorInfo", "网络连接异常!");
	            }
	        }
	        map.put("data",finallyList);
	        map.put("pwd", servicepwd);
	        map.put("phone", phoneNumber);
	        //经度
	        map.put("longitude", longitude);
	        //纬度
	        map.put("latitude", latitude);
	        
	        Resttemplate resttemplate=new Resttemplate();
            map = resttemplate.SendMessage(map, ConstantInterface.port+"/HSDC/message/operator");
	        return map;
	        
		}
		@SuppressWarnings({ "unused", "resource", "rawtypes" })
		public Map<String,Object> huNanPhoneinfo(HttpServletRequest request,String phoneNumber,String month1,String lday){
			Map<String,Object> map = new HashMap<String,Object>(10);
	        Map<String,Object> data=new HashMap<String,Object>(10);
	        List<Object>  listData =new ArrayList<Object>();
	        Object attribute = request.getSession().getAttribute("HNwebclient");
	        WebClient webClient = (WebClient) attribute;
	        try{
	        	HtmlPage infopage1 = (HtmlPage) request.getSession().getAttribute("infoPage1");
	        	String info = infopage1.asText();
	        	final String d = "融合老用户超值大礼包>>速来领取>>";
		        if(infopage1.asText().indexOf(d)==-1){
	       		 	map.put("errorCode", "0001");
	                map.put("errorInfo", "操作异常!");
	                return map;
		       	 }else{	   	       		
		    		int num =  info.indexOf("总页数：");
		    		int num1 =  info.indexOf(" 当前页：");	    
		    		//总页数
		    	    String  phonenum = info.substring(num+4, num1);
		    	    Date date=new Date();
		    		SimpleDateFormat sdf =  new SimpleDateFormat( "yyyy" );
		    		String year = sdf.format(date);
		    		//一个月页数循环
		    	    for(int i=1;i<=Integer.valueOf(phonenum);i++){
		    	    	HtmlTable table = (HtmlTable) infopage1.getElementById("tab_cont_box").getElementsByTagName("table").get(1);
		    	    	DomNodeList trlist = table.getElementsByTagName("tr");
		    	    	for(int tr=2;tr<trlist.size();tr++){
		    	    		//单页每行循环	
	    	    			if(table.getCellAt(tr,0).asText().indexOf(year)!=-1){
	    	    				continue;
	    	    			}	    	
	    	    			monthNum.setCallNumber(table.getCellAt(tr,3).asText());
	    	    			monthNum.setCallType(table.getCellAt(tr,7).asText());
	    	    			monthNum.setCallAddress(table.getCellAt(tr,5).asText());
	    	    			monthNum.setCallWay(table.getCellAt(tr,2).asText());
	    	    			monthNum.setCallMoney(Double.valueOf(table.getCellAt(tr,6).asText()));
	    	    			monthNum.setCallTime(table.getCellAt(tr,1).asText());
	    	    			monthNum.setCallDuration(table.getCellAt(tr,4).asText());
	    	    			JSONObject jsonObject = JSONObject.fromObject(monthNum);
	    	    			String jsonhuNanBean = jsonObject.toString();
	    	    			//每一行
	    	    			listData.add(jsonhuNanBean);   	    			
		    	    	}
		    	    	
		    	    	String loadPath = "http://hn.189.cn/webportal-wt/hnselfservice/billquery/bill-query!queryBillx.action?tabIndex=2&queryMonth="+month1+"&patitype=2&startDay=1&endDay="+lday+"&pageNo="+(i+1)+"&valicode=undefined&accNbr=";		    	    	                     
			    		 URL url = new URL(loadPath);
			    		 WebRequest webRequest = new WebRequest(url);
			    		 webRequest.setHttpMethod(HttpMethod.GET);
		                 List<NameValuePair> list = new ArrayList<NameValuePair>();	               
		                 list.add(new NameValuePair("queryMonth", month1));
		                 list.add(new NameValuePair("startDay", "1"));
		                 list.add(new NameValuePair("endDay", lday));
		                 list.add(new NameValuePair("patitype", "2"));			                
		                 list.add(new NameValuePair("tabIndex", "2"));
		                 list.add(new NameValuePair("pageNo", String.valueOf(i+1)));
		                 list.add(new NameValuePair("accNbr", ""));
		                 list.add(new NameValuePair("valicode", "undefined"));                             
	                     webRequest.setRequestParameters(list);
	                     infopage1 = webClient.getPage(webRequest);
	                     Thread.sleep(1000);
	                     System.out.println(infopage1.asText());
		    	    }
		    	    
		       	}
		        
		        //一个月        
		        map.put("list", listData);	
	        } catch (Exception e) {
                e.printStackTrace();
                map.put("errorCode", "0001");
                map.put("errorInfo", "网络连接异常!");
            }
	        return map;        
		}
}
