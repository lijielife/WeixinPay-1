package com.tenpay;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.tenpay.client.TenpayHttpClient;
import com.tenpay.util.ConstantUtil;
import com.tenpay.util.JsonUtil;
import com.tenpay.util.MD5Util;
import com.tenpay.util.Sha1Util;
import com.tenpay.util.XMLUtil;

public class PrepayIdRequestHandler extends RequestHandler {

	public PrepayIdRequestHandler(HttpServletRequest request,
			HttpServletResponse response) {
		super(request, response);
	}
	private static String Key = "42062519900710351220160203187423";  
	/**
	 * 锟斤拷锟斤拷签锟斤拷SHA1
	 * 
	 * @param signParams
	 * @return
	 * @throws Exception
	 */
	public String createSHA1Sign() {
		StringBuffer sb = new StringBuffer();
		Set es = super.getAllParameters().entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			sb.append(k + "=" + v + "&");
		}
		String params = sb.substring(0, sb.lastIndexOf("&"));
		String appsign = Sha1Util.getSha1(params);
		this.setDebugInfo(this.getDebugInfo() + "\r\n" + "sha1 sb:" + params);
		this.setDebugInfo(this.getDebugInfo() + "\r\n" + "app sign:" + appsign);
		return appsign;
	}

	/**
	 * 锟斤拷锟斤拷签锟斤拷SHA1
	 * 
	 * @param signParams
	 * @return
	 * @throws Exception
	 */
	public String createSHA1Sign2(SortedMap<Object,Object> parameters) {
        StringBuffer sb = new StringBuffer();  
        Set es = parameters.entrySet();//所有参与传参的参数按照accsii排序（升序）  
        Iterator it = es.iterator();  
        while(it.hasNext()) {  
            Map.Entry entry = (Map.Entry)it.next();  
            String k = (String)entry.getKey();  
            Object v = entry.getValue();  
            if(null != v && !"".equals(v)   
                    && !"sign".equals(k) && !"key".equals(k)) {  
                sb.append(k + "=" + v + "&");  
            }  
        }  
        sb.append("key=" + Key);  
        String sign = MD5Util.MD5Encode(sb.toString(), "UTF-8").toUpperCase();  
        return sign;  
	}
	
	

	// 锟结交预支锟斤拷
	public String sendPrepay() throws JSONException {
		
        SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();  
        parameters.put("appid", "wx0ede853bd98e9066");  
        parameters.put("attach", "1");  
        parameters.put("body", "1"); 
        parameters.put("mch_id", "1306890701");  
        parameters.put("nonce_str", "1add1a30ac87aa2db72f57a2375d8fec"); 
        parameters.put("notify_url", "http://wxpay.weixin.qq.com/pub_v2/pay/notify.v2.php"); 
        parameters.put("out_trade_no", "1415659990");  
        parameters.put("spbill_create_ip", "14.23.150.211");  
        parameters.put("total_fee", "1");  
        parameters.put("trade_type", "APP");  
        
        
        String sign=this.createSHA1Sign2(parameters);
		
		String prepayid = "";
		StringBuffer sb = new StringBuffer("{");
		Set es = super.getAllParameters().entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"appkey".equals(k)) {
				sb.append("\"" + k + "\":\"" + v + "\",");
			}
		}
		String params = sb.substring(0, sb.lastIndexOf(","));
		params += "}";

		String requestUrl = super.getGateUrl();
		this.setDebugInfo(this.getDebugInfo() + "\r\n" + "requestUrl:"
				+ requestUrl);
		TenpayHttpClient httpClient = new TenpayHttpClient();
		httpClient.setReqContent(requestUrl);
		String resContent = "";
		params="<xml>"+
		"<appid>wx0ede853bd98e9066</appid>"+
		"<attach>1</attach>"+
		"<body>1</body>"+
		"<mch_id>1306890701</mch_id>"+
		"<nonce_str>1add1a30ac87aa2db72f57a2375d8fec</nonce_str>"+
		"<notify_url>http://wxpay.weixin.qq.com/pub_v2/pay/notify.v2.php</notify_url>"+
		"<out_trade_no>1415659990</out_trade_no>"+
		"<spbill_create_ip>14.23.150.211</spbill_create_ip>"+
		"<total_fee>1</total_fee>"+
		"<trade_type>APP</trade_type>"+
		"<sign>"+sign+"</sign>"+
		"</xml>";
		this.setDebugInfo(this.getDebugInfo() + "\r\n" + "post data:" + params);
		if (httpClient.callHttpPost(requestUrl, params)) {
			resContent = httpClient.getResContent();
			Map m=new HashMap();
			try {
				m=XMLUtil.doXMLParse(resContent);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if(m.get("result_code")!=null){
				if (m.get("result_code").toString().equals("SUCCESS")) {
					prepayid = m.get("prepay_id").toString();
				}
			}
			try {
				resContent = new String(resContent.getBytes("GBK"),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			this.setDebugInfo(this.getDebugInfo() + "\r\n" + "resContent:"
					+ resContent);
		}
		return prepayid;
	}

	// 锟叫讹拷access_token锟角凤拷失效
	public String sendAccessToken() {
		String accesstoken = "";
		StringBuffer sb = new StringBuffer("{");
		Set es = super.getAllParameters().entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"appkey".equals(k)) {
				sb.append("\"" + k + "\":\"" + v + "\",");
			}
		}
		String params = sb.substring(0, sb.lastIndexOf(","));
		params += "}";

		String requestUrl = super.getGateUrl();
//		this.setDebugInfo(this.getDebugInfo() + "\r\n" + "requestUrl:"
//				+ requestUrl);
		TenpayHttpClient httpClient = new TenpayHttpClient();
		httpClient.setReqContent(requestUrl);
		String resContent = "";
//		this.setDebugInfo(this.getDebugInfo() + "\r\n" + "post data:" + params);
		if (httpClient.callHttpPost(requestUrl, params)) {
			resContent = httpClient.getResContent();
			if (2 == resContent.indexOf(ConstantUtil.ERRORCODE)) {
				accesstoken = resContent.substring(11, 16);//锟斤拷取锟斤拷应锟斤拷errcode锟斤拷值
			}
		}
		return accesstoken;
	}
}
