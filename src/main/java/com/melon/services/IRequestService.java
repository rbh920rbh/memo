package com.melon.services;

import com.melon.errors.CoreException;
import com.melon.file.FileAdapter;
import com.melon.services.impl.RequestService;
import org.apache.commons.fileupload.FileItem;
import org.bson.Document;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public interface IRequestService {
	public interface CheckContentHandler {
			public void checkContent(String content, Map<String, FileItem> needSaveFileItems) throws CoreException;
	}
	
	public static final String DOMAIN_ACCOUNT_SEPERATOR = ".";
	RequestService.FileItemResource saveRichMessage(HttpServletRequest request,
													Collection<String> names, RequestService.RichMessageHandler handler, FileAdapter.MetadataEx metadata)
			throws CoreException;

	Document readJSON(HttpServletRequest request) throws CoreException;

	FileItem[] readFileItems(HttpServletRequest request) throws CoreException;

	RequestService.FileItemResource readForFileItems(HttpServletRequest request,
													 Collection<String> names) throws CoreException;

	String readContent(HttpServletRequest request) throws CoreException;

	RequestService.FileItemResource saveRichMessage(RequestService.FileItemResource fileItemResource,
													RequestService.RichMessageHandler handler, FileAdapter.MetadataEx metadata) throws CoreException;

	boolean hasRichMessage(RequestService.FileItemResource fileItemResource);

	String generateDomainAccount(String domain, String account) throws CoreException;

	String saveMultipartUploads(HttpServletRequest request,
								CheckContentHandler handler, FileAdapter.MetadataEx metadata) throws CoreException;

	String saveMultipartUploads(HttpServletRequest request, String contentFieldName,
								CheckContentHandler handler, FileAdapter.MetadataEx metadata) throws CoreException;

	void saveFile(String resourceId, FileItem fileItem, FileAdapter.MetadataEx metadata) throws CoreException;

	void saveFile(String resourceId, InputStream is, FileAdapter.MetadataEx metadata)
			throws CoreException;

}
