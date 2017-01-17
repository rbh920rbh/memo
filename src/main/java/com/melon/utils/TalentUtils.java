package com.melon.utils;

import com.melon.errors.CoreException;
import com.melon.file.FileAdapter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.lang.Character.UnicodeBlock;
import java.net.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TalentUtils {
	private static final String TAG = TalentUtils.class.getSimpleName();
	
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSSS");
	static {
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8")); 
	}
	public static String dateString(Date date) {
		return sdf.format(date);
	}
	public static String dateString(long time) {
		return sdf.format(new Date(time));
	}
	public static String dateString() {
		return sdf.format(new Date());
	}
	
	public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
	    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
	    String query = url.getQuery();
	    if(query == null)
	    	return null;
	    String[] pairs = query.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
	    }
	    return query_pairs;
	}
	
	public static final String PROPERTY_PATH = "props";
	public static Properties loadProperty(String fileName) throws CoreException {
		ClassPathResource resource = new ClassPathResource(PROPERTY_PATH + "/" + fileName);
		Properties props  = null;
		try {
			props  = PropertiesLoaderUtils.loadProperties(resource);
			return props;
		} catch (IOException e) {
			throw new CoreException(e.getMessage());
		}
	}
	
	public static String toString(String[] strs) {
		if(strs == null)
			return null;
		if(strs.length == 0)
			return "";
		StringBuffer buffer = new StringBuffer();
		for(String str : strs) {
			buffer.append(str).append(",");
		}
		return buffer.substring(0, buffer.length() - 1);
	}
	public static String[] fromString(String str) {
		if(str == null)
			return null;
		return str.split(",");
	}
	public static String toString(Collection<String> strs, String sperator) {
		if(strs == null || strs.size() == 0)
			return "";
		StringBuffer buffer = new StringBuffer();
		for(String str : strs) {
			buffer.append(str).append(sperator);
		}
		return buffer.substring(0, buffer.length() - 1);
	}
	public static String toString(Collection<String> strs) {
		return toString(strs, ",");
	}
	/*
	 * get the local host ip
	 */
	private static String getLocalHostIpPrivate(){
		try{
			InetAddress addr=InetAddress.getLocalHost();
			return addr.getHostAddress();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getLocalHostIp() {
		return getLocalHostIp(null, null);
	}
	
	public static String getLocalHostIp(String ipStartWith, String faceStartWith) {
		NetworkInterface iface = null;
		String ethr;
		String myip = null;
		
		if(ipStartWith == null && faceStartWith == null) {
			myip = getLocalHostIpPrivate();
			if(myip != null) 
				return myip;
		}
		try
		{
//			String anyIp = null;
			for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();ifaces.hasMoreElements();)
			{
				iface = (NetworkInterface)ifaces.nextElement();
				ethr = iface.getDisplayName();

				if (faceStartWith == null || ethr.startsWith(faceStartWith))
				{
					InetAddress ia = null;
					for(Enumeration<InetAddress> ips = iface.getInetAddresses();ips.hasMoreElements();)
					{
						ia = (InetAddress)ips.nextElement();
						String anyIp = ia.getHostAddress();
						if (ipStartWith == null || anyIp.startsWith(ipStartWith))
						{
							myip = ia.getHostAddress();
							return myip;
						}
					}
				}
			}
		}
		catch (SocketException e){}
		return myip;
	}
	
	public static String getRuntimePath() {
		return System.getProperty("user.dir");
	}
	
	public static String getUserLanguage() {
		return System.getProperty("user.language");
	}
	
	/*
	 * get the local host name
	 */
	public static String getLocalHostName(){
		try{
			InetAddress addr=InetAddress.getLocalHost();
			return addr.getHostName();
		}catch(Exception e){
			return "";
		}
	}
	
	public static boolean isSortString(String str) {
		if(str != null && str.length() > 2) {
			char sortHead = str.charAt(0);
			char seperator = str.charAt(1);
			char head = str.charAt(2);
			if(seperator == '^') {
				if(sortHead == head || sortHead - head == 32 || isChinese(head)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String cleanUpSortString(String str) {
		if(isSortString(str)) {
			return str.substring(2);
		}
		return str;
	}
	
	private static Set<UnicodeBlock> chineseUnicodeBlocks = new HashSet<UnicodeBlock>() {
		private static final long serialVersionUID = -4141782340333688579L;
	{
	    add(UnicodeBlock.CJK_COMPATIBILITY);
	    add(UnicodeBlock.CJK_COMPATIBILITY_FORMS);
	    add(UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
	    add(UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT);
	    add(UnicodeBlock.CJK_RADICALS_SUPPLEMENT);
	    add(UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
	    add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
	    add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
	    add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B);
	    add(UnicodeBlock.KANGXI_RADICALS);
	    add(UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS);
	}};
	
	public static boolean isChinese(char c) {
		return chineseUnicodeBlocks.contains(UnicodeBlock.of(c));
	}
	
	public static String getAttachmentRelativePath(String path, String resourceId) {
		String attachmentPath = path;
		int pos = attachmentPath.indexOf(resourceId);
		String relativePath = null;
		if(pos != -1) {
			relativePath = attachmentPath.substring(pos);
		} else {
			relativePath = attachmentPath;
		}
		return FilenameUtils.normalize(relativePath, true);
	}
	
	public static void copyStream(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[8192]; // Adjust if you want
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}
	
	/**
	 * 该方法使用了InputStream的skip方法，效率不高，尽量不要在生产环境使用
	 * @param input
	 * @param output
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public static void copyStream(InputStream input, OutputStream output, Integer offset, Integer length)
			throws IOException {
		final int BUFFERSIZE = 8192;
		byte[] buffer = null; // Adjust if you want
		int bytesRead;
		int lengthTemp = length;
		if(offset != null && offset > 0) {
			int actualSkipped = (int) input.skip(offset);
			if(actualSkipped != offset)
				throw new IOException();
		}
		if(length < BUFFERSIZE) 
			buffer = new byte[length];
		else 
			buffer = new byte[BUFFERSIZE];
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
			lengthTemp -= bytesRead;
			if(lengthTemp == 0) 
				break;
			else if(lengthTemp < 0) 
				throw new IOException("Unexpected lengthTemp " + lengthTemp);
			else if(lengthTemp < buffer.length) 
				buffer = new byte[lengthTemp];
		}
	}
	
	public static void main(String[] args) throws IOException {
		File file = new File("/home/Baihua/disk/filepath/resources/2016/05/27/57481c918e53e45105eca852");
//		File file = new File("/home/Baihua/disk/test/a.jpg");
		FileInputStream fis = new FileInputStream(file);
		FileOutputStream fos = new FileOutputStream("/home/Baihua/disk/filepath/resources/2016/05/27/aaaa");
//		FileOutputStream fos = new FileOutputStream("/home/Baihua/disk/test/aaa.jpg");
		copyStream(fis, fos, null, 2758318);
	}

	public static boolean isDatabaseId(String userId) {
		if(userId == null)
			return false;
		return true;
	}

	public static String[] joinStringArray(String[] array, String[] t) {
		if(array == null && t == null)
			return new String[0];
		else if (array == null && t != null)
			return t;
		else if (array != null && t == null)
			return array;
		String[] newArray = new String[array.length + t.length];
		System.arraycopy(array, 0, newArray, 0, array.length);
		System.arraycopy(t, 0, newArray, array.length, t.length);
		return newArray;
	}
	
	public static Collection<String> joinStringArray(Collection<String> array, String t) {
		if(array == null)
			return null;
		if(t == null)
			return array;
		Collection<String> strs = new ArrayList<>();
		strs.addAll(array);
		strs.add(t);
		return strs;
	}
	public static List<String> joinStringList(String[] array, String t) {
		if(array == null)
			return null;
		List<String> strs = new ArrayList<>();
		strs.addAll(Arrays.asList(array));
		strs.add(t);
		return strs;
	}
	public static String[] joinStringArray(String[] array, String t) {
		if(array == null)
			return null;
		if(t == null)
			return array;
		String[] newArray = new String[array.length + 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[newArray.length - 1] = t;
		return newArray;
	}
	
	public static String formatSpringSensitiveString(String str) {
		if(str == null)
			return null;
		return str.replaceAll("[/\\\\;:]", "_").replace("<", "(").replace(">", ")");
	}
	
	private static String specialCharacters = "^()./+[]-"; 
	private static String s3specialCharacters = "~"; 
	private static String fileNameSpecialCharacters = "*:?<>|^()./+[]-\""; 
	public static String formatForRegularExpression(String str) {
		if(str == null)
			return "";
		str = str.replace("\\", "\\\\");
		for(int i = 0; i < specialCharacters.length(); i++) {
			String c = specialCharacters.substring(i, i + 1);
			str = str.replace(c, "\\" + c);
		}
		return str;
	}
	public static String formatAsRegularExpressionForName(String str) {
		if(str == null)
			return "";
		str = str.replace("\\", "\\\\");
		for(int i = 0; i < fileNameSpecialCharacters.length(); i++) {
			String c = fileNameSpecialCharacters.substring(i, i + 1);
			str = str.replace(c, "_");
		}
		return str;
	}
	public static String formatS3Name(String str) {
		if(str == null)
			return "";
		for(int i = 0; i < s3specialCharacters.length(); i++) {
			String c = s3specialCharacters.substring(i, i + 1);
			str = str.replace(c, "_");
		}
		return str;
	}
	
	public static String formatTxt2Html(String str) {
		if (str == null || str.trim().length() == 0) return str;
		return str.replaceAll("(\r\n|\n\r|\r|\n)", "<br/>").replaceAll("(\t)", "&nbsp; &nbsp; ").replaceAll("   ", "&nbsp; &nbsp;").replaceAll("  ", "&nbsp; ");
	}
	
	public static String formatSecureString(String str) {
		if (str == null || str.trim().length() == 0) return str;
		return str.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	public static void sortNumberString(String[] keys) {
		Arrays.sort(keys, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int left = 0, right = 0;
				
				try {
					left = Integer.parseInt(o1);
					right = Integer.parseInt(o2);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return 0;
				}
				
				if (left < right)
					return -1;
				else 
					return 1;
			}
		});
		
	}

	public static void destroyProcess(Process process) {
		if(process == null)
			return;
		process.destroy();
		while(true) {
			try {
				process.exitValue();
				process = null;
				break;
			} catch(IllegalThreadStateException e) {
				try {
					Thread.sleep(200L);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				LoggerEx.debug(TAG, "Waiting for process really destroyed. " + process);
			}
		}		
	}
	
	public static boolean isProcessExist(String name) throws IOException {
		String[] command = new String[]{"tasklist", "/fo", "table", "/nh"};
		ProcessBuilder pb = new ProcessBuilder(command);
		Process tasks = pb.start();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(tasks.getInputStream()));
		String info = null;
		while ((info = reader.readLine()) != null) {
			String pname = info.split(" ")[0];
			
			if (pname.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	
	public static String escapeAngleBracket(String asString) {
		return asString == null ? null : asString.replaceAll("<", "&lt;");
				//replaceAll(">", "&gt;");
	}

	public static String filterCarriageAndWrapChar(String wh) {
		return wh == null ? null : wh.replaceAll("(\r\n|\n\r|\r|\n)", "");
	}

	static SimpleDateFormat gmtFormatter = new SimpleDateFormat("yyyy/MM/dd"); 
	static {
		gmtFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
    public static String getDocumentPath(String resourceId) throws IOException {
    	if(resourceId != null && ObjectId.isValid(resourceId)) {
    		StringBuilder builder = new StringBuilder(FileAdapter.DOC_ROOT_PATH);
    		ObjectId oid = new ObjectId(resourceId);
    		String timePath = null;
			//XXX  多线程环境下，日期formate是非线程安全的，所以在此处加锁，以后需要找更好的解决方案
			synchronized (gmtFormatter) {
				timePath = gmtFormatter.format(oid.getDate());
			}
    		builder.append(timePath).append("/").append(resourceId);
    		return builder.toString();
    	} else {
    		throw new IOException("Invalid resourceId " + resourceId);
    	}
    }
    
    /**
	 * 获取stickersuit的资源路径
	 * @param relativePath	相对路径如:tuzki/public/banner.png 或 tuzki/public/stickers/stickers.json
	 * @return	 stickersuit的根路径 + relativePath
	 * @throws IOException
	 */
    public static String getStickerSuitDocumentPath(String relativePath) throws IOException {
    	if(relativePath != null) {
    		StringBuilder builder = new StringBuilder(FileAdapter.DOC_STICKER_SUIT_ROOT_PATH);
    		builder.append(relativePath);
    		return builder.toString();
    	} else {
    		throw new IOException("relativePath is null.");
    	}
    }
    

    public static String getI18NMessageEx(String message, String localeStr) throws CoreException {
    	String[] messageArray = message.split("#");
    	String msgKey = messageArray[0];
    	String[] nnnStrings = new String[messageArray.length - 1];
    	System.arraycopy(messageArray, 1, nnnStrings, 0, messageArray.length - 1);
    	String result = getI18NMessage(msgKey, nnnStrings, localeStr);
		return result;
	}
    
    private static ResourceBundle getBundle(Locale locale) {
    	ResourceBundle bundle = null;
    	try {
    		bundle = ResourceBundle.getBundle(TalentUtils.PROPERTY_NAME, locale);
		} catch (Exception e) {
		}
    	return bundle;
    }
    
    public static String getI18NMessage(String msgKey, Object[] parameters, String localeStr){
        ResourceBundle bundle = null;
        if (StringUtils.isBlank(localeStr)) {
            bundle = getBundle(Locale.ENGLISH);
        } else {
            if (localeStr != null) {
                Locale locale = new Locale(localeStr);
                
                try {
                    bundle = getBundle(locale);
                } catch (Exception e) {
                    e.printStackTrace();
                    LoggerEx.error(TAG, e.getMessage());
                    bundle = getBundle(Locale.ENGLISH);
                }
            } else {
                LoggerEx.error(TAG, "Missing locale info in request ");
                bundle = getBundle(Locale.ENGLISH);
            }
        }
        
        try{
            String value = bundle.getString(msgKey);
//原来多语言的更换字符标是${}，后发现系统有自己的一套更换机制，如下所示：
            String result = MessageFormat.format(value, parameters);
//            if(value != null && parameters != null) {
//                for(int i = 0; i < parameters.length; i++) {
//                    String key = "${" + i + "}";
//                    if(parameters[i] != null) 
//                    	value = value.replace(key, parameters[i]);
//                }
//            }
            return result;
        } catch(MissingResourceException e){
            LoggerEx.error(TAG, "Missing resource " + msgKey);
            return msgKey;
        }
    }
    
    public static String getFirmDomain(HttpServletRequest request){
        String domain = null;
        domain = request.getHeader("acldomain"); //Only for mobile debuging. 
        if(domain != null)
            return domain;
        String serverName = request.getServerName();
        if(serverName != null) {
            int pos = serverName.indexOf(".");
            if(pos != -1)
                domain = serverName.substring(0, pos);
        }
//      if(domain == null)
//          domain = request.getParameter("firm");
//      if(domain == null)
//          domain = request.getHeader("firm");
        if(domain != null) {
            domain = domain.toLowerCase();
        }
        return domain;
    }

    public static Document successDBObject(Document dbObj) {
        if(dbObj == null) {
            dbObj = new Document();
        }
        dbObj.put("code", 1);
        return dbObj;
    }
    
    public static Document successDBObject() {
        return successDBObject(null);
    }
    
    public static Document failedDBObject(CoreException e, HttpServletRequest request) {
        return failedDBObject(e.getCode(), e.getParameters(), request);
    }
    public static Document failedDBObject(CoreException e, String localeStr) {
        return failedDBObject(e.getCode(), e.getParameters(), localeStr);
    }
    public static Document failedDBObject(int code, String localeStr) {
        return failedDBObject(code, localeStr);
    }
    public static Document failedDBObject(int code, String[] parameters, HttpServletRequest request) {
        String msg = getLocaleMessage(String.valueOf(code), parameters, request);
        return new Document().append("code", code).append("description", msg);
    }
    public static Document failedDBObject(int code, String[] parameters, String localeStr) {
        String msg = getI18NMessage(String.valueOf(code), parameters, localeStr);
        return new Document().append("code", code).append("description", msg);
    }
	public static String getLocaleMessage(String msgKey, String[] parameters, HttpServletRequest request){
		if(request == null) {
			return getI18NMessage(msgKey, parameters, "");
		} else {
			HttpSession httpSession = request.getSession(false);
			String localeStr = null;
			if (httpSession == null)
				localeStr = getLocalFromRequest(request);
			else
				localeStr = (String)httpSession.getAttribute("locale");
			return getI18NMessage(msgKey, parameters, localeStr);
		}
	}
	public static String getLocalFromRequest(HttpServletRequest request) {
		String locale = request.getParameter("locale");
		if (locale == null) {
			Cookie[] cookies = request.getCookies();
			if(cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("locale")) {
						locale = cookie.getValue();
						break;
					}
				}
			}
		}
		return locale;
	}
    public enum MatchArrayOperator {
        all,
        in,
        notIn
    }
    
    public enum QueryDeletedOperator {
        includeDeleted("i"),
        excludedDeleted("e"),
        deleted("d");
        private String flag;

        QueryDeletedOperator(String flag) {
            this.setFlag(flag);
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }
        
        public static QueryDeletedOperator flagOf(String flag) {
            switch (flag) {
                case "i": return includeDeleted;
                case "e": return excludedDeleted;
                case "d": return deleted;
                default: throw new IllegalArgumentException("No flag of " + flag);
            }
        }
    }

    /**
     * Defines the logic condition
     */
    public enum LogicOperator {
        AND, NOT, OR
    }

    public static final String PROPERTY_NAME = "message";
    
    public static void hasText (String para, int errorCode, String msg) throws CoreException {
        if (StringUtils.isEmpty(para))
            throw new CoreException(errorCode, msg);
    }

    public static void notNull(Object obj, int errorCode, String msg) throws CoreException {
        if (obj == null) {
            throw new CoreException(errorCode, msg);
        }
    }
    
    public static void notEmpty(Collection<?> collection, int errorCode, String msg) throws CoreException {
        if (CollectionUtils.isEmpty(collection)) {
            throw new CoreException(errorCode, msg);
        }
    }
    public static void notEmpty(Object[] array, int errorCode, String msg) throws CoreException {
        if (ObjectUtils.isEmpty(array)) {
            throw new CoreException(errorCode, msg);
        }
    }
    public static void notEmpty(Map<?, ?> map, int errorCode, String msg) throws CoreException {
        if (CollectionUtils.isEmpty(map)) {
            throw new CoreException(errorCode, msg);
        }
    }
    
    /**
     * 获取在服务器中统一使用的语言代码
     * @param lang	从客户端传来的语言代码
     * @return	根据谷歌地图api支持的语言统一的值
     * @throws CoreException
     */
	public static String getLanguage(String lang) throws CoreException {
		if(StringUtils.isEmpty(lang))
			return "en";
//		lang = lang.toLowerCase();
		lang = StringUtils.replace(lang, "-", "_");
		
		//阿拉伯语//阿拉伯语，埃及//阿拉伯语，以色列
		if(StringUtils.equals(lang, "ar") || StringUtils.contains(lang, "ar_EG") || StringUtils.contains(lang, "ar_IL"))
			return "ar";
		
		//保加利亚语，保加利亚
		if(StringUtils.equals(lang, "bg") || StringUtils.contains(lang, "bg_BG"))
			return "bg";
		
		//孟加拉语，孟加拉
		if(StringUtils.equals(lang, "bn"))
			return "bn";
		
		//加泰隆语//加泰隆语，西班牙
		if(StringUtils.equals(lang, "ca") || StringUtils.contains(lang, "ca_ES"))
			return "ca";
		
		//捷克语//捷克语，捷克共和国
		if(StringUtils.equals(lang, "cs") || StringUtils.contains(lang, "cs_CZ"))
			return "cs";
		
		//丹麦语//丹麦语，丹麦
		if(StringUtils.equals(lang, "da") || StringUtils.contains(lang, "da_DK"))
			return "da";
		
		//德语//德语，奥地利//德语，瑞士//德语，德国//德语，列支敦士登的
		if(StringUtils.equals(lang, "de") || StringUtils.contains(lang, "de_AT") || StringUtils.contains(lang, "de_CH") || StringUtils.contains(lang, "de_DE") || StringUtils.contains(lang, "de_LI"))
			return "de";
		
		//希腊语//希腊语，希腊
		if(StringUtils.equals(lang, "el") || StringUtils.contains(lang, "el_GR"))
			return "el";

		//英语//英语，加拿大//英语，爱尔兰//英语，印度//英语，新西兰//英语，新加坡//英语，美国//英语，津巴布韦
		if(StringUtils.equals(lang, "en") || StringUtils.contains(lang, "en_CA") || StringUtils.contains(lang, "en_IE") || StringUtils.contains(lang, "en_IN") || StringUtils.contains(lang, "en_NZ") || StringUtils.contains(lang, "en_SG") || StringUtils.contains(lang, "en_US") || StringUtils.contains(lang, "en_ZA"))
			return "en";
		
		//希腊语//希腊语，希腊
		if(StringUtils.contains(lang, "en_AU"))
			return "en_AU";
		
		//希腊语//希腊语，希腊
		if(StringUtils.contains(lang, "en_GB"))
			return "en_GB";
		
		//西班牙语//西班牙//西班牙语，美国
		if(StringUtils.equals(lang, "es") || StringUtils.contains(lang, "es_ES") || StringUtils.contains(lang, "es_US"))
			return "es";
		
		//巴斯克
		if(StringUtils.equals(lang, "eu"))
			return "eu";
		
		//波斯语
		if(StringUtils.equals(lang, "fa"))
			return "fa";
		
		//芬兰语//芬兰语，芬兰
		if(StringUtils.equals(lang, "fi") || StringUtils.contains(lang, "fi_FI"))
			return "fi";
		
		//菲律宾语
		if(StringUtils.equals(lang, "fil"))
			return "fil";
		
		//法语//法语，比利时//法语，加拿大//法语，瑞士//法语，法国
		if(StringUtils.equals(lang, "fr") || StringUtils.contains(lang, "fr_BE") || StringUtils.contains(lang, "fr_CA") || StringUtils.contains(lang, "fr_CH") || StringUtils.contains(lang, "fr_FR"))
			return "fr";
		
		//希伯来语//希伯来语，以色列
		if(StringUtils.equals(lang, "he") || StringUtils.contains(lang, "he_IL"))
			return "he";
		
		//加利西亚
		if(StringUtils.equals(lang, "gl"))
			return "gl";
		
		//古吉拉特语
		if(StringUtils.equals(lang, "gu"))
			return "gu";
		
		//印地语，印度//印地语，印度
		if(StringUtils.equals(lang, "hi") || StringUtils.contains(lang, "hi_IN"))
			return "hi";
		
		//克罗地亚语//克罗地亚语，克罗地亚
		if(StringUtils.equals(lang, "hr") || StringUtils.contains(lang, "hr_HR"))
			return "hr";
		
		//匈牙利语//匈牙利语，匈牙利
		if(StringUtils.equals(lang, "hu") || StringUtils.contains(lang, "hu_HU"))
			return "hu";
		
		//印尼语//印尼语，印尼
		if(StringUtils.equals(lang, "id") || StringUtils.contains(lang, "id_ID"))
			return "id";
		
		//意大利语//意大利语，瑞士//意大利语，意大利
		if(StringUtils.equals(lang, "it") || StringUtils.contains(lang, "it_CH"))
			return "it";
		
		//希伯来语
		if(StringUtils.equals(lang, "iw"))
			return "iw";
		
		//日语
		if(StringUtils.equals(lang, "ja") || StringUtils.contains(lang, "ja_JP"))
			return "ja";
		
		//埃纳德语
		if(StringUtils.equals(lang, "kn"))
			return "kn";
		
		//朝鲜语//朝鲜语
		if(StringUtils.equals(lang, "ko") || StringUtils.contains(lang, "ko_KR"))
			return "ko";
		
		//立陶宛语，立陶宛//立陶宛语，立陶宛
		if(StringUtils.equals(lang, "lt") || StringUtils.contains(lang, "lt_LT"))
			return "lt";
		
		//拉托维亚语，拉托维亚//拉托维亚语，拉托维亚
		if(StringUtils.equals(lang, "lv") || StringUtils.contains(lang, "lv_LV"))
			return "lv";
		
		//马拉雅拉姆语
		if(StringUtils.equals(lang, "ml"))
			return "ml";
		
		//马拉地语
		if(StringUtils.equals(lang, "mr"))
			return "mr";
		
		//挪威语//挪威语，挪威
		if(StringUtils.equals(lang, "nb") || StringUtils.contains(lang, "nb_NO"))
			return "nb";
		
		//荷兰语//荷兰语，比利时//荷兰语，荷兰
		if(StringUtils.equals(lang, "nl") || StringUtils.contains(lang, "nl_BE") || StringUtils.contains(lang, "nl_NL"))
			return "nl";
		
		//波兰语//波兰
		if(StringUtils.equals(lang, "pl") || StringUtils.contains(lang, "pl_PL"))
			return "pl";
		
		//葡萄牙语，巴西//葡萄牙语，巴西//葡萄牙语，巴西
		if(StringUtils.equals(lang, "pt") || StringUtils.contains(lang, "pt_BR"))
			return "pt_BR";
		
		//葡萄牙语，葡萄牙//葡萄牙语
		if(StringUtils.contains(lang, "pt_PT"))
			return "pt_PT";
		
		//罗马尼亚语//罗马尼亚语，罗马尼亚
		if(StringUtils.equals(lang, "ro") || StringUtils.contains(lang, "ro_RO"))
			return "ro";
		
		//俄语//俄语
		if(StringUtils.equals(lang, "ru") || StringUtils.contains(lang, "ru_RU"))
			return "ru";
		
		//斯洛伐克语//斯洛伐克语，斯洛伐克
		if(StringUtils.equals(lang, "sk") || StringUtils.contains(lang, "sk_SK"))
			return "sk";
		
		//斯洛文尼亚语，斯洛文尼亚
		if(StringUtils.contains(lang, "sl_SI"))
			return "sl";
		
		//塞尔维亚语
		if(StringUtils.contains(lang, "sr_RS"))
			return "sr";
		
		//瑞典语//瑞典语，瑞典
		if(StringUtils.equals(lang, "sv") || StringUtils.contains(lang, "sv_SE"))
			return "sv";

		//泰米尔语
		if(StringUtils.equals(lang, "ta"))
			return "ta";
		
		//泰卢固语
		if(StringUtils.equals(lang, "te"))
			return "te";
		
		//泰语//泰语，泰国
		if(StringUtils.equals(lang, "th") || StringUtils.contains(lang, "th_TH"))
			return "th";
		
		//菲律宾语，菲律宾//菲律宾语，菲律宾
		if(StringUtils.equals(lang, "tl") || StringUtils.contains(lang, "tl_PH"))
			return "tl";
		
		//土耳其语//土耳其语，土耳其
		if(StringUtils.equals(lang, "tr") || StringUtils.contains(lang, "tr_TR"))
			return "tr";
		
		//乌克兰语//乌克兰语
		if(StringUtils.equals(lang, "uk") || StringUtils.contains(lang, "uk_UA"))
			return "uk";
		
		//越南语//越南语，越南
		if(StringUtils.equals(lang, "vi") || StringUtils.contains(lang, "vi_VN"))
			return "vi";
		
		//中文，台湾//中文，台湾//繁体中文
		if(StringUtils.contains(lang, "zh_TW") ||StringUtils.contains(lang, "zh_Hant"))
			return "zh_TW";
		
		//中文//中文，中国//中文，中国//简体中文
		if(StringUtils.contains(lang, "zh") ||StringUtils.contains(lang, "zh_CN") || StringUtils.contains(lang, "zh_Hans"))
			return "zh_CN";
		
		//马来西亚语
		if(StringUtils.equals(lang, "ms"))
			return "ms";

		return "en";
    }
    
    /**
     * 将服务器中的语言转化成android所用的语言代码
     * @param lang	从服务器传来的语言代码
     * @return	android使用的语言代码
     * @throws CoreException
     */
	public static String getLanguageForOutputForAndroid(String lang) throws CoreException {
    	switch (lang) {
    	case "ar":
    		return "ar";
    		
    	case "bn":
    		return "bn";
    		
    	case "ca":
    		return "ca";
    		
    	case "cs":
    		return "cs";
    		
    	case "da":
    		return "da";
    		
    	case "de":
    		return "de";
    		
    	case "el":
    		return "el";
    		
    	case "en":
    		return "en";
    		
    	case "en_AU":
    		return "en-AU";
    		
    	case "en_GB":
    		return "en-GB";
    		
    	case "es":
    		return "es";
    		
    	case "eu":
    		return "eu";
    		
    	case "fa":
    		return "fa";
    		
    	case "fi":
    		return "fi";
    		
    	case "fil":
    		return "fil";
    		
    	case "fr":
    		return "fr";
    		
    	case "he":
    		return "he";
    		
    	case "hr":
    		return "hr";
    		
    	case "hu":
    		return "hu";
    		
    	case "id":
    		return "id";
    		
    	case "it":
    		return "it";
    		
    	case "ja":
    		return "ja";
    		
    	case "ko":
    		return "ko";
    		
    	case "nb":
    		return "nb";
    		
    	case "nl":
    		return "nl";
    		
    	case "pl":
    		return "pl";
    		
    	case "pt_BR":
    		return "pt-BR";
    		
    	case "pt_PT":
    		return "pt-PT";
    		
    	case "ro":
    		return "ro";
    		
    	case "ru":
    		return "ru";
    		
    	case "sk":
    		return "sk";
    		
    	case "sv":
    		return "sv";
    		
    	case "th":
    		return "th";
    		
    	case "tr":
    		return "tr";
    		
    	case "uk":
    		return "uk";
    		
    	case "vi":
    		return "vi";
    		
    	case "zh_CN":
    		return "zh-CN";
    		
    	case "zh_TW":
    		return "zh-TW";
    		
    	default:
    		return "en";
    	}
    }
    
    /**
     * 将服务器中的语言转化成iOS所用的语言代码
     * @param lang	从服务器传来的语言代码
     * @return	iOS使用的语言代码
     * @throws CoreException
     */
	public static String getLanguageForOutputForIOS(String lang) throws CoreException {
		switch (lang) {

		case "ar":
			return "ar";

		case "ca":
			return "ca";

		case "cs":
			return "cs";

		case "da":
			return "da";

		case "de":
			return "de";

		case "el":
			return "el";

		case "en":
			return "en";

		case "en_AU":
			return "en-AU";

		case "en_GB":
			return "en-GB";

		case "es":
			return "es";

		case "fi":
			return "fi";

		case "fr":
			return "fr";

		case "he":
			return "he";

		case "hi":
			return "hi";

		case "hr":
			return "hr";

		case "hu":
			return "hu";

		case "id":
			return "id";

		case "it":
			return "it";

		case "ja":
			return "ja";

		case "ko":
			return "ko";

		case "nb":
			return "nb";

		case "nl":
			return "nl";

		case "pl":
			return "pl";

		case "pt_BR":
			return "pt-BR";

		case "pt_PT":
			return "pt-PT";

		case "ro":
			return "ro";

		case "ru":
			return "ru";

		case "sk":
			return "sk";

		case "sv":
			return "sv";

		case "th":
			return "th";

		case "tr":
			return "tr";

		case "uk":
			return "uk";

		case "vi":
			return "vi";

		case "zh_CN":
			return "zh-Hans";

		case "zh_TW":
			return "zh-Hant";

		case "ms":
			return "ms";

		default:
			return "en";
		}
    }
    
}