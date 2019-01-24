package com.addteq.confluence.plugin.excellentable.io;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.atlassian.confluence.pages.AttachmentManager;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.addteq.service.excellentable.exc_io.export.ExportToXlsx;
import com.addteq.service.excellentable.exc_io.parser.JsonHtmlParser;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This file defines all the REST call used for Import/Export Excellentable.
 */
@Path("/")
@Service
@Consumes(MediaType.MULTIPART_FORM_DATA)
public class IORest {

	private final String[] requiredFields = { "type", "data", "name" };
	private final ImportFile importFile;
	private final I18nResolver i18nResolver;
	static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IORest.class);
	private final AttachmentManager attachmentManager;

	@Autowired
	public IORest(ImportFile importFile, @ComponentImport I18nResolver i18nResolver,
				  @ComponentImport AttachmentManager attachmentManager) {
		this.i18nResolver = i18nResolver;
		this.importFile = importFile;
		this.attachmentManager = attachmentManager;
	}

	/**
	 * Imports the xlsx, csv or html passed
	 *
	 * @param request
	 *            : contains two item version and file(json data)
	 * @return : Return in the form of spreadjs jspon
	 * @throws IOException
	 *             : if any error occurred while importing
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/import")
	public Response importData(@Context HttpServletRequest request) throws IOException {
		JsonObject output = importFile.parse(request);
		return Response.ok(output.toString()).build();
	}

	/**
	 * Exports the data into application/json format for Dev mode, pdf is here
	 * for future use currently does the same as json
	 *
	 * @param request
	 *            : contains "type", "data", "name"
	 * @return : returns the file in json format string
	 * @throws FileUploadException
	 * @throws IOException
	 */
	@POST
	@Path("/export")
	@Produces({ "application/json", "application/pdf", "text/html" })
	public Response exportJSONAndPDF(@Context HttpServletRequest request) throws FileUploadException, IOException {
		// Get Form Items
		List<FileItem> formItems = getFormItems(request);
		// Get data of items as string,string map
		Map<String, String> values = getData(formItems, requiredFields);
		if (values == null) {
			return Response.status(400).entity(create400Response()).build();
		}
		String contentDispositionVal = getContentHeader(values);
		String entity = values.get(requiredFields[1]);

		if (values.get(requiredFields[0]).equals("html")) {
			entity = getHTMLexport(entity);
		}

		return Response.ok().entity(entity).header("Content-disposition", contentDispositionVal).build();
	}
	
	private String getHTMLexport(String entity) {
		
		String fonts = "<link href='https://fonts.googleapis.com/css?family=Alegreya|Amatic+SC|Bree+Serif|Cormorant+Garamond|Merriweather|"+
		"Permanent+Marker|Pinyon+Script|Playfair+Display|Roboto|Roboto+Mono|Ultra|Varela' rel='stylesheet'>";

		//EXC-4564 - To accomodate the fix for pdf exports, we are right now disabling export
		// of images for html and pdf (last param in the method call below set to false)
		String sheets = JsonHtmlParser.getHTML(entity, false, "", false);
		String css = VelocityUtils.getRenderedTemplate("template/excellentable-html-css.vm", MacroUtils.createDefaultVelocityContext());
		
		return fonts + sheets + css;
		
	}

	/**
	 * Exports the file passed to this endpoint as xlsx
	 *
	 * @param request
	 *            : contains "type", "data", "name"
	 * @return : returns the file in xlsx StreamingOutput format
	 */
	@POST
	@Path("/export")
	@Produces({ "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" })
	public Response exportXLSX(@Context HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
		String requestURI = request.getRequestURI();
		String baseUrl = null;
		if (requestURL != null && requestURI != null) {
			baseUrl = requestURL.substring(0, requestURL.length() - requestURI.length());
		}
		try {
			// Get Form Items
			List<FileItem> formItems = getFormItems(request);
			// Get data of items as string,string map
			Map<String, String> values = getData(formItems, requiredFields);
			if (values == null) {
				return Response.status(400).entity(create400Response()).build();
			}
			String contentDispositionVal = getContentHeader(values);
			JsonParser jsonParser = new JsonParser();
			JsonObject metaData = (JsonObject) jsonParser.parse(values.get(requiredFields[1]));
			ExportToXlsx exportToXlsx = new ExportToXlsx();
			if (StringUtils.isNotBlank(baseUrl)) {
				exportToXlsx.setBaseUrl(baseUrl);
			}
			exportToXlsx.setAttachmentManager(attachmentManager);
			Workbook workbook = exportToXlsx.createWorkbook(metaData);
			StreamingOutput streamingOutput = workbook::write;
			return Response.ok(streamingOutput).header("Content-disposition", contentDispositionVal).build();
		} catch (IOException | FileUploadException ex) {
			LOGGER.error("Excellentable failed to export xlsx file", ex);
		}
		return Response.status(500).build();
	}

	/**
	 * Returns the content-disposition header required to download the file in
	 * the front-end
	 *
	 * @param values
	 *            : Values is a map which contains "type", "data", "name"
	 * @return : the value to be put in content-disposition header
	 * @throws UnsupportedEncodingException
	 */
	private String getContentHeader(Map<String, String> values) throws UnsupportedEncodingException {
		String filename = URLEncoder.encode(values.get(requiredFields[2]), StandardCharsets.UTF_8.name()) + "."
				+ values.get(requiredFields[0]);
		return "attachment;filename*=UTF-8''" + filename;
	}

	/**
	 * Returns the formItems as List<FileItem> which contains "type", "data",
	 * "name", separated out for unit test
	 *
	 * @param request
	 *            : request came from client (HttpServletRequest)
	 * @return : formItems as List<FileItem>
	 * @throws FileUploadException
	 */
	public List<FileItem> getFormItems(HttpServletRequest request) throws FileUploadException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload fileUpload = new ServletFileUpload(factory);
		return fileUpload.parseRequest(request);
	}

	/**
	 * Parses through list and gets the form items as Map, isolated for null
	 * value check
	 *
	 * @param formItems
	 *            : formItems as list
	 * @param requiredFields
	 *            : keys of the formItems
	 * @return : map
	 * @throws UnsupportedEncodingException
	 */
	private Map<String, String> getData(List<FileItem> formItems, String[] requiredFields)
			throws UnsupportedEncodingException {
		Map<String, String> output = new HashMap<>();
		for (FileItem item : formItems) {
			if (item.isFormField()) {
				if (requiredFields[0].equals(item.getFieldName())) {
					output.put(requiredFields[0], item.getString());
				} else if (requiredFields[1].equals(item.getFieldName())) {
					output.put(requiredFields[1], item.getString(StandardCharsets.UTF_8.name()));
				} else if (requiredFields[2].equals(item.getFieldName()) && !("".equals(item.getString().trim()))) {
					output.put(requiredFields[2], item.getString());
				}
			}
		}
		if ((StringUtils.isEmpty(output.get(requiredFields[0]))) || (StringUtils.isEmpty(output.get(requiredFields[1])))
				|| (StringUtils.isEmpty(output.get(requiredFields[2]))))
			return null;
		return output;
	}

	/**
	 * returns the formItem keys as a string which is comma separated
	 *
	 * @param requiredFields
	 *            : keys which contains "type", "data", "name"
	 * @return string
	 */
	public String arrayAsCSV(String[] requiredFields) {
		if (requiredFields == null || requiredFields.length == 0) {
			return "";
		}
		StringBuilder output = new StringBuilder(" ");
		for (String item : requiredFields) {
			output.append(item).append(", ");
		}
		// Remove last two characters
		output.deleteCharAt(output.length() - 1).deleteCharAt(output.length() - 1);
		return output.toString();
	}

	/**
	 * Returns the error message as string for 400 response
	 *
	 * @return : string
	 * @throws IOException
	 */
	public String create400Response() throws IOException {
		JsonObject errors = new JsonObject();
		errors.addProperty("userMessage",
				i18nResolver.getText("com.addteq.confluence.plugin.excellentable.io.badRequest.userMessage"));
		errors.addProperty("systemMessage", i18nResolver.getText(
				"com.addteq.confluence.plugin.excellentable.io.badRequest.systemMessage", arrayAsCSV(requiredFields)));
		return errors.toString();
	}
}