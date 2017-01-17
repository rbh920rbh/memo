package com.melon.utils;

import com.melon.data.CleanDocument;
import com.melon.errors.CoreErrorCodes;
import com.melon.errors.CoreException;
import com.melon.services.IRequestService;
import org.apache.commons.io.IOUtils;
import org.bson.Document;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 网络资源基类.
 * @author acucom
 *
 */
public class TalentResource {
	protected static boolean alive = true;
	public static final String LOG_USER = "log.user";
	
	@Resource
	protected IRequestService requestService;
	private static final String TAG = LoggerEx.getClassTag(TalentResource.class);
	
	public static DocumentEx RESPONSE(HttpServletRequest request, HttpServletResponse response, Document dbObj){
		DocumentEx returnObj = new DocumentEx(request, response, null);
		returnObj.putAll(dbObj);
		return returnObj;
	}
	
	public static DocumentEx SUCCESS(HttpServletRequest request, HttpServletResponse response){
		DocumentEx returnObj = new DocumentEx(request, response, null).append("code", 1); 
		return returnObj;
	}
	
	public static DocumentEx FAILED(HttpServletRequest request, HttpServletResponse response, CoreException e){
		return FAILED(request, response, e.getCode(), e.getParameters(), e.getMessage());
	}
	public static DocumentEx FAILED(HttpServletRequest request, HttpServletResponse response, int code){
	    return FAILED(request, response, code, null, null);
	}

    public static DocumentEx FAILED(HttpServletRequest request, HttpServletResponse response,
            int code, String message) {
        return FAILED(request, response, code, null, message);
    }

    public static DocumentEx FAILED(HttpServletRequest request, HttpServletResponse response,
            int code, String[] parameters, String message) {
    	Document dbObj = TalentUtils.failedDBObject(code, parameters, request);
        DocumentEx returnObj = new DocumentEx(request, response, null);
        returnObj.putAll(dbObj);
        LoggerEx.error(TAG, "FAILED: " + request.getRequestURL().toString() + "; " + request.getMethod() + "; " + returnObj.toJson(CommonUtils.MONGODB_JSONSETTINGS) + "; Error message:" + message);
        return returnObj;
    }

    public static DocumentEx FAILED(HttpServletRequest request, HttpServletResponse response,
            int code, String[] parameters) {
        return FAILED(request, response, code, parameters, null);
    }
	
	public static void handleJSCrossDomain(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,PUT,DELETE");
	}
	
	/**
	 * 提供JSON格式的统一的response
	 * @author acucom
	 *
	 */
	public static class DocumentEx extends CleanDocument{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5424037561253402993L;
		private String callback;
		private HttpServletRequest request;
		private HttpServletResponse response;
		private DocumentEx(HttpServletRequest request, HttpServletResponse response, String callback){
			this.callback = callback;
			this.request = request;
			this.response = response;
		}
		
		public DocumentEx append(String key, Object val){
			super.append(key, val);
			return this;
		}
		
		public DocumentEx append(String key, List<?> val){
			super.append(key, val);
			return this;
		}
		
		public String toString(){
			if(callback != null){
				StringBuffer buffer = new StringBuffer();
				buffer.append(callback).append("(");
				buffer.append(super.toJson(CommonUtils.MONGODB_JSONSETTINGS));
				buffer.append(");");
				return buffer.toString();
			}
			return super.toJson(CommonUtils.MONGODB_JSONSETTINGS);
		}
		
		protected String getUser(HttpServletRequest request) {
			return null;
		}
		
		public boolean respond() {
			return respond(true);
		}
		public boolean respond(Boolean print) {
			BufferedOutputStream bos = null;
			try {
				/*//请求参数包含curTime时
				String curTime = request.getParameter("curTime");
				if(curTime == null){
					curTime = request.getHeader("curTime");
				}
				if(curTime != null){
					append("curTime", System.currentTimeMillis());
				}
				*/
				
				String curTime = RequestUtils.getParameter(request, "curTime");
				if(curTime != null)
					append("curTime", System.currentTimeMillis());
				
				OutputStream os = response.getOutputStream();
				bos = new BufferedOutputStream(os);
//				if(isGzipSupport(request)){
//					response.setHeader("Content-Encoding", "gzip");
//					os = new GZIPOutputStream(os);
//				}
				
				//XXX only for debug. Javascript cross domain.
				TalentResource.handleJSCrossDomain(response);
				
				response.setHeader("Cache-Control", "no-cache");
				response.setContentType("application/json");
				
				long toStringTime = System.currentTimeMillis();
				
				String str = toString();
				
				toStringTime = System.currentTimeMillis() - toStringTime;
				
				long formatTime = System.currentTimeMillis();
				if(str != null) {
					Integer terminalType = RequestUtils.getHeaderInteger(request, "t");
					if(terminalType != null) {
						switch (terminalType) {
						case 1:
						case 2:
							//do nothing
							break;
						default:
							str = CommonUtils.formatSecureString(str);		//尖括号转译
							break;
						}
					} else {
						str = CommonUtils.formatSecureString(str);		//尖括号转译
					}
				}
				formatTime = System.currentTimeMillis() - formatTime;
				
				String userName = (String) request.getAttribute(LOG_USER);
				
				long time = System.currentTimeMillis();
				if(isAlive()) {
					IOUtils.write(str, bos, "utf8");
//					bos.write(str.getBytes("utf8"));
				} else 
					response.sendError(CoreErrorCodes.ERROR_SESSION_SERVER_SHUTTINGDOWN);
				if(print) {
					LoggerEx.info(TAG, (userName != null ? userName + ": " : "") + " url = " + request.getRequestURL().toString() + "?" + request.getQueryString() + "; response = " + str + " takes " + (System.currentTimeMillis() - time) + " formatTime " + formatTime + " toStringTime " + toStringTime);
				}
				return true;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "respond met UnsupportedEncodingException " + e.getMessage() + "; URI is " + request.getRequestURI());
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				LoggerEx.error(TAG, "respond met IOException " + e.getMessage() + "; URI is " + request.getRequestURI());
				return false;
			} finally {
				if(bos != null) {
					try {
						bos.close();
					} catch (Throwable e) {
					}
				}
			}
		}
		
		/*private boolean isGzipSupport(HttpServletRequest req) {  
	        String headEncoding = req.getHeader("accept-encoding");  
	        if (headEncoding == null || (headEncoding.indexOf("gzip") == -1)) {   
	            return false;  
	        } else { 
	            return true;  
	        }  
	    }  */
	}

	
	protected String generateResourceUrl(String entityId, String pageId, String resourceId, long time) {
		return "/rest/apis/page/" + pageId + "/resource/" + resourceId + "?e=" + entityId + "&t=" + time;
	}
	
	protected void handleUpdateResult(HttpServletRequest request, HttpServletResponse response, int affectedAmount) {
        if (affectedAmount > 0) {
            SUCCESS(request, response).respond();
        } else {
            FAILED(request, response, CoreErrorCodes.ERROR_UPDATE_FAILED).respond();
        }
    }

    protected void handleDeleteResult(HttpServletRequest request, HttpServletResponse response, int deleted) {
        if (deleted > 0) {
            SUCCESS(request, response).respond();
        } else {
            FAILED(request, response, CoreErrorCodes.ERROR_DELETE_FAILED).respond();
        }
    }

	public static boolean isAlive() {
		return alive;
	}

	public static void setAlive(boolean alive) {
		TalentResource.alive = alive;
	}
}
