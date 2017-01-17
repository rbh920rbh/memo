package com.melon.services.impl;

import com.melon.data.ResourceData;
import com.melon.errors.ChatErrorCodes;
import com.melon.errors.CoreException;
import com.melon.file.FileAdapter;
import com.melon.services.IRequestService;
import com.melon.utils.CommonUtils;
import com.melon.utils.LoggerEx;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

public class RequestService implements IRequestService {
	private static final String TAG = "RequestService";
	@Resource
	private FileAdapter fileAdapter;

	@Override
	public String generateDomainAccount(String domain, String account) throws CoreException {
		if(StringUtils.isBlank(account))
			throw new CoreException(ChatErrorCodes.ERROR_ACCOUNTNAME_ILLEGAL, "Account name is illegal " + account);
		if(StringUtils.isBlank(domain)) 
			throw new CoreException(ChatErrorCodes.ERROR_ACCOUNTDOMAIN_ILLEGAL, "Account domain is illegal " + domain);
		return domain.toLowerCase().trim() + DOMAIN_ACCOUNT_SEPERATOR + account.toLowerCase().trim();
	}
	
	public static abstract class RichMessageHandler {
		private String existingPageId = null;
		public RichMessageHandler(){}
		public RichMessageHandler(String existingPageId){ this.existingPageId = existingPageId; }
		public abstract String revise(String pageId, String pageString, Map<String, ResourceData> idMap) throws CoreException;
		public String getExistingPageId() {
			return existingPageId;
		}
	}
	@Override
	public Document readJSON(HttpServletRequest request) throws CoreException{
		String content = readContent(request);
		try {
			if(content == null || content.trim().equals(""))
				return null;
			return Document.parse(content);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_PARSE_REQUEST_FAILED, "Parse post content to json failed, " + e.getMessage() + ". content " + content);
		}
	}
	
	public static final String FIELD_PAGE = "page";
	
	@Override
	public boolean hasRichMessage(FileItemResource fileItemResource) {
		return (fileItemResource != null && fileItemResource.getFileItemMap() != null) ? fileItemResource.getFileItemMap().containsKey(FIELD_PAGE) : false;
	}
	
	public FileItemResource saveRichMessage(HttpServletRequest request, Collection<String> names, RichMessageHandler handler, FileAdapter.MetadataEx metadata) throws CoreException {
		if(names == null)
			names = new ArrayList<>();
		names.add(FIELD_PAGE);
		
		FileItemResource fileItemResource = readForFileItems(request, names);
		return saveRichMessage(fileItemResource, handler, metadata);
	}
	@Override
	public FileItemResource saveRichMessage(FileItemResource fileItemResource, RichMessageHandler handler, FileAdapter.MetadataEx metadata) throws CoreException {
		long time = System.currentTimeMillis();
		List<FileItem> pageItems = fileItemResource.getFileItemMap().get(FIELD_PAGE);
		FileItem pageContent = null;
		if(pageItems != null && !pageItems.isEmpty())
			pageContent = pageItems.get(0);
		if(pageContent == null) 
			return null;
		String pageStr = null;
		try {
			pageStr = pageContent.getString("utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_ILLEGAL_ENCODE, "Read content failed for decode utf8");
		}
		if(StringUtils.isBlank(pageStr))
			throw new CoreException(ChatErrorCodes.ERROR_READCONTENT_FAILED, "Parse page content failed");

		String pageId = handler.getExistingPageId();
		if(pageId == null)
			pageId = ObjectId.get().toString();
		
		List<FileItem> fileItems = fileItemResource.getOtherFileItems();
		Map<String, ResourceData> idMap = new HashMap<String, ResourceData>();
		for (FileItem resourceItem : fileItems) {
			String resourceId = ObjectId.get().toString();
			String name = resourceItem.getFieldName();
			if(StringUtils.isBlank(name))
				continue;
			name = "{" + name + "}";
			
			if(pageStr.contains(name)) {
				InputStream is = null;
				try {
					is = resourceItem.getInputStream();
		            fileAdapter.saveFile(is,
		                    new FileAdapter.PathEx(CommonUtils.getDocumentPath(resourceId),
		                            resourceId, metadata), FileAdapter.FileReplaceStrategy.REPLACE);
		        } catch (IOException e) {
		            e.printStackTrace();
		            throw new CoreException(ChatErrorCodes.ERROR_UPLOAD_FAILED, new String[] {e.getMessage()},
		                    "Save upload file failed, " + e.getMessage());
		        } finally {
		        	if(is != null)
		        		IOUtils.closeQuietly(is);
		        }
				
				pageStr = pageStr.replace(name, generateResourceUrl(pageId, resourceId, time));
				ResourceData resource = new ResourceData(resourceId, resourceItem.getContentType(), resourceItem.getName());
				idMap.put(resourceItem.getFieldName(), resource);
			}
		}
		if(handler != null) 
            pageStr = handler.revise(pageId, pageStr, idMap);
		if(pageStr != null) {
			InputStream is = null;
    		try {
    			is = new ByteArrayInputStream(pageStr.getBytes("utf8"));
                fileAdapter.saveFile(is,
                        new FileAdapter.PathEx(CommonUtils.getDocumentPath(pageId),
                        		pageId, metadata), FileAdapter.FileReplaceStrategy.REPLACE);
            } catch (IOException e) {
                e.printStackTrace();
                throw new CoreException(ChatErrorCodes.ERROR_UPLOAD_FAILED, new String[] {e.getMessage()},
                        "Save upload file failed, " + e.getMessage());
            } finally {
            	if(is != null)
            		IOUtils.closeQuietly(is);
            }
		}
//			onlineUser.uploadPageResource(new ByteArrayInputStream(pageStr.getBytes("utf8")), "pageJson", "application/json", pageId, pageId, time, terminal);
		return fileItemResource;
	}
	
	
	
	@Override
	public String saveMultipartUploads(HttpServletRequest request, CheckContentHandler handler, FileAdapter.MetadataEx metadata) throws CoreException {
		return saveMultipartUploads(request, null, metadata);
	}
	@Override
	public String saveMultipartUploads(HttpServletRequest request, String contentFieldName, CheckContentHandler handler, FileAdapter.MetadataEx metadata) throws CoreException {
		
		FileItemResource fileItemResource = readForFileItems(request, contentFieldName == null ? null : Arrays.asList(contentFieldName));
		
		List<FileItem> fieldNametems = null;
		if(contentFieldName != null)
			fieldNametems = fileItemResource.getFileItemMap().get(contentFieldName);
		FileItem pageContent = null;
		if(fieldNametems != null && !fieldNametems.isEmpty())
			pageContent = fieldNametems.get(0);
		String contentStr = null;
		if(pageContent == null) {
			contentStr = fileItemResource.getContent();
		} else {
			try {
				contentStr = pageContent.getString("utf8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new CoreException(ChatErrorCodes.ERROR_ILLEGAL_ENCODE, "Read content failed for decode utf8");
			}
		}
		if(StringUtils.isBlank(contentStr))
			throw new CoreException(ChatErrorCodes.ERROR_READCONTENT_FAILED, "Parse page content failed");

		List<FileItem> fileItems = fileItemResource.getOtherFileItems();
		Map<String, FileItem> needSaveFileItems = new HashMap<>();
		for (FileItem resourceItem : fileItems) {
			String resourceId = ObjectId.get().toString();
			String name = resourceItem.getFieldName();
			if(StringUtils.isBlank(name))
				continue;
			name = "{" + name + "}";
			
			if(contentStr.contains(name)) {
				needSaveFileItems.put(resourceId, resourceItem);
				contentStr = contentStr.replace(name, resourceId);
			}
		}
		if(handler != null)
			handler.checkContent(contentStr, needSaveFileItems);
		String userName = (String) request.getAttribute("log.user");
		LoggerEx.info(TAG, (userName != null ? userName : "") + request.getRequestURL().toString() + " saveMultipartUploads content = " + contentStr);
		for(Entry<String, FileItem> entry : needSaveFileItems.entrySet()) {
			saveFile(entry.getKey(), entry.getValue(), metadata);
		}
		return contentStr;
	}
	
	@Override
	public void saveFile(String resourceId, FileItem fileItem, FileAdapter.MetadataEx metadata) throws CoreException {
		InputStream is = null;
		try {
			is = fileItem.getInputStream();
            fileAdapter.saveFile(is,
                    new FileAdapter.PathEx(CommonUtils.getDocumentPath(resourceId),
                    		resourceId, metadata), FileAdapter.FileReplaceStrategy.REPLACE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_UPLOAD_FAILED, new String[] {e.getMessage()},
                    "Save upload file failed, " + e.getMessage());
        } finally {
        	if(is != null)
        		IOUtils.closeQuietly(is);
        }
	}
	
	@Override
	public void saveFile(String resourceId, InputStream is, FileAdapter.MetadataEx metadata) throws CoreException {
		try {
            fileAdapter.saveFile(is, new FileAdapter.PathEx(CommonUtils.getDocumentPath(resourceId),
                    		resourceId, metadata), FileAdapter.FileReplaceStrategy.REPLACE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_UPLOAD_FAILED, new String[] {e.getMessage()},
                    "Save upload file failed, " + e.getMessage());
        }		
	}

	protected String generateResourceUrl(String pageId, String resourceId, long time) {
		return "resource://" + pageId + "/" + resourceId + "/" + time;
	}
	@Override
	public FileItem[] readFileItems(HttpServletRequest request) throws CoreException {
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List<FileItem> items = upload.parseRequest(request);
//			if(items != null) {
//				FileItem[] fileItems = new FileItem[items.size()];
//				items.toArray(fileItems);
//				return fileItems;
//			}
			List<FileItem> newItems = new ArrayList<FileItem>();
			for(FileItem fi : items) {
				if(fi.getSize() <= 1024*1024*0)		//上传文件小于等于0k抛出错误
					throw new CoreException(ChatErrorCodes.ERROR_FILE_EMPTY, "File is empty.");
				if(fi.getSize() > 1024*1024*10)		//上传文件超过10k抛出错误
					throw new CoreException(ChatErrorCodes.ERROR_CHARACTER_OVER_MAXIMUM_LIMITS, "FileItem has over the maximum limits : 10k.");
				newItems.add(fi);
			}
			if(newItems.size() > 0)
				return newItems.toArray(new FileItem[newItems.size()]);
		} catch (FileUploadException e) {
			e.printStackTrace();
			throw new CoreException(ChatErrorCodes.ERROR_UPLOAD_FAILED, "Upload failed, " + e.getMessage());
		}
		return null;
	}

	public static class FileItemResource {
		private Map<String, List<FileItem>> fileItemMap;
		private List<FileItem> otherFileItems;
		private String content;
		
		public Map<String, List<FileItem>> getFileItemMap() {
			return fileItemMap;
		}
		public void setFileItemMap(Map<String, List<FileItem>> fileItemMap) {
			this.fileItemMap = fileItemMap;
		}
		public List<FileItem> getOtherFileItems() {
			return otherFileItems;
		}
		public void setOtherFileItems(List<FileItem> otherFileItems) {
			this.otherFileItems = otherFileItems;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
	}
	@Override
	public FileItemResource readForFileItems(HttpServletRequest request, Collection<String> names) throws CoreException {
		String contentType = request.getContentType();
		String content = null;
		Map<String, List<FileItem>> fileItemMap = new HashMap<>();
		List<FileItem> otherFileItems = new ArrayList<>();
		if(contentType != null && contentType.contains("multipart/form-data")) {
			FileItem[] fileItems = readFileItems(request);
			boolean hit = false;
			for(FileItem item : fileItems) {
				if(names != null) {
					for(String name : names) {
						List<FileItem> items = fileItemMap.get(name);
						if(items == null) {
							items = new ArrayList<>();
							fileItemMap.put(name, items);
						}
						if(item.getFieldName() != null && (name.endsWith("*") ? item.getFieldName().regionMatches(0, name, 0, name.length() - 1) : item.getFieldName().equals(name))) {
							items.add(item);
							hit = true;
							break;
						}
					}
				}
				if(!hit)
					otherFileItems.add(item);
				else
					hit = false;
			}
		} else {
			content = readContent(request);
		}
		if(content == null && (fileItemMap.isEmpty() && otherFileItems.isEmpty())) 
			throw new CoreException(ChatErrorCodes.ERROR_READCONTENT_FAILED, "content is empty");
		FileItemResource iconResource = new FileItemResource();
		iconResource.setContent(content);
		iconResource.setFileItemMap(fileItemMap);
		iconResource.setOtherFileItems(otherFileItems);
		return iconResource;
	}
	
	/**
	 * 以UTF-8编码读取网络请求的内容
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@Override
	public String readContent(HttpServletRequest request) throws CoreException{
		InputStream is = null;
	    try {
    		is = new BufferedInputStream(request.getInputStream());
//	    	InputStream is = request.getInputStream();
    		String contentEncoding = request.getHeader("Content-Encoding");
//    		String contentLength = request.getHeader("Content-Length");
//    		AcuLogger.debug(TAG, "Request contentLength = " + contentLength);
//    		if(contentLength != null && contentLength.equals("0")) {
//    			return "";
//    		}
    		if(contentEncoding != null && contentEncoding.indexOf("gzip") != -1) {
    			is = new GZIPInputStream(is);
    		}
    		
    		String json = IOUtils.toString(is, "utf8");
    		String userName = (String) request.getAttribute("log.user");
    		LoggerEx.info(TAG, (userName != null ? userName : "") + request.getRequestURL().toString() + " Request Body = " + json);
    		return json;
	    } catch (IOException e) {
	        e.printStackTrace();
	        throw new CoreException(ChatErrorCodes.ERROR_READCONTENT_FAILED, e.getMessage());
	    } finally {
	    	if(is != null)
	    		IOUtils.closeQuietly(is);
	    }
	}
	
	
	public FileAdapter getFileAdapter() {
		return fileAdapter;
	}

	
	public void setFileAdapter(FileAdapter fileAdapter) {
		this.fileAdapter = fileAdapter;
	}
}
