package com.addteq.confluence.plugin.excellentable.macro;

import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.addteq.service.excellentable.exc_io.parser.JsonHtmlParser;
import com.addteq.service.excellentable.exc_io.utils.Gzip;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.content.render.image.ImageDimensions;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.macro.*;
import com.atlassian.confluence.macro.xhtml.MacroManager;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.plugin.services.VelocityHelperService;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.TokenType;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


public class ExcellentableMacro extends BaseMacro implements Macro, EditorImagePlaceholder {

    private final ActiveObjects ao;
    private final BootstrapManager bootstrapManager;
    private final PluginLicenseManager licenseManager;
    private final VelocityHelperService velocityHelperService;
    private final I18nResolver i18nResolver;
    private final PageManager pageManager;
    private final PermissionManager permissionManager;
    private final XhtmlContent xhtmlContent;
    private final MacroManager macroManager;
    private final SettingsManager settingsManager;
    private String exportBtnHtml;
    static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExcellentableMacro.class);
    private static String contextPath;
    private static final String IMAGE_PATH = "/download/resources/Addteq.Excellentable/images/addteq-plugin-icon.png";
    private static final String UNLICENCED_IMAGE_PATH = "/download/resources/Addteq.Excellentable/images/excellentable-unlicensed.png";
    private static final String EXPIRED_LICENSE_IMAGE_PATH = "/download/resources/Addteq.Excellentable/images/excellentable-expired-license.png";

    @Autowired
    public ExcellentableMacro(
            @ComponentImport ActiveObjects ao,
            @ComponentImport PageManager pageManager,
            @ComponentImport PermissionManager permissionManager,
            @ComponentImport VelocityHelperService velocityHelperService,
            @ComponentImport PluginLicenseManager licenseManager,
            @ComponentImport BootstrapManager bootstrapManager,
            @ComponentImport I18nResolver i18nResolver,
            @ComponentImport MacroManager macroManager,
            @ComponentImport XhtmlContent xhtmlContent,
            @ComponentImport SettingsManager settingsManager) {

        this.ao = ao;
        this.velocityHelperService = velocityHelperService;
        this.licenseManager = licenseManager;
        this.bootstrapManager = bootstrapManager;
        this.i18nResolver = i18nResolver;
        this.permissionManager = permissionManager;
        this.pageManager = pageManager;
        this.xhtmlContent = xhtmlContent;
        this.macroManager = macroManager;
        this.settingsManager = settingsManager;
        contextPath = bootstrapManager.getWebAppContextPath();
    }

    @Override
    public TokenType getTokenType(Map parameters, String body, RenderContext context) {
        return TokenType.INLINE;
    }

    @Override
    public boolean hasBody() {
        return true;
    }

    @Override
    public RenderMode getBodyRenderMode() {
        return RenderMode.ALL;
    }

    @Override
    public ImagePlaceholder getImagePlaceholder(Map<String, String> map, ConversionContext cc) {
       final int Expired_License_ImageWidth = 328;
       final int Expired_License_ImageHeight = 64;
       final int Unlicensed_ImageWidth = 400;
       final int Unlicensed_ImageHeight= 64;
       final int Licensed_ImageWidth = 110;
       final int Licensed_ImageHeight= 110;



        try {
            if (licenseManager.getLicense().isDefined()) { //If license is applied
                PluginLicense pluginLicense = licenseManager.getLicense().get();
                if (pluginLicense.getError().isDefined()) {
                    return new DefaultImagePlaceholder(EXPIRED_LICENSE_IMAGE_PATH, true, new ImageDimensions(Expired_License_ImageWidth, Expired_License_ImageHeight));
                }
                else{
                    return new DefaultImagePlaceholder(IMAGE_PATH, true, new ImageDimensions(Licensed_ImageWidth, Licensed_ImageHeight));
                }
            } else {
                return new DefaultImagePlaceholder(UNLICENCED_IMAGE_PATH, true, new ImageDimensions(Unlicensed_ImageWidth, Unlicensed_ImageHeight));
            }
        } catch (Exception ex) {
            LOGGER.error("Error while rendering macro in page edit view", ex);
            return new DefaultImagePlaceholder(UNLICENCED_IMAGE_PATH, true, new ImageDimensions(Unlicensed_ImageWidth, Unlicensed_ImageHeight));
        }

    }

    @Override
    public String execute(final Map params, final String body, final RenderContext renderContext) throws MacroException {
        return "<img src=\"" + contextPath + "/" + IMAGE_PATH + " \">";
    }

    /**
     *
     * @param parameters
     * @param body
     * @param conversionContext
     *
     * @return
     * @throws MacroExecutionException
     */
    @Override
    public String execute(Map<String, String> parameters, String body, ConversionContext conversionContext) throws MacroExecutionException {
        try {
            if (licenseManager.getLicense().isDefined()) {
                PluginLicense pluginLicense = licenseManager.getLicense().get();
                if (pluginLicense.getError().isDefined()) {  // handle license error scenario (e.g. expired or user mismatch)
                    String msg = getAuiMessage("Excellentable License: " + pluginLicense.getError().get().name());
                    return msg + "</br>" + body;
                } else {
                    String excellentableId = parameters.get("excellentable-id");
                    if (excellentableId != null && !"".equals(excellentableId)) {
                        // handle valid license scenario
                        Map<String, Object> contextMap = MacroUtils.defaultVelocityContext();

                        long contentEntityId = conversionContext.getEntity().getId();
                        AbstractPage page = pageManager.getAbstractPage(contentEntityId);
                        if (!"Template Preview".equals(conversionContext.getEntity().getTitle()) && page.getOriginalVersionPage() != null) {
                            contentEntityId = page.getOriginalVersionPage().getId();
                        }
                        ExcellentableDB excellentableDB = ao.get(ExcellentableDB.class, Integer.parseInt(excellentableId));

                        if(excellentableDB != null && conversionContext.getPageContext().PDF.equals(conversionContext.getOutputType())){

							try{
                                String evalMessage = i18nResolver.getText("com.addteq.confluence.plugin.excellentable.evaluation.license", settingsManager.getGlobalSettings().getBaseUrl());
                                String decompressedMetaData = Gzip.uncompressString(excellentableDB.getMetaData());

                                //EXC-4564 - To accomodate the fix for pdf exports, we are right now disabling export
                                // of images for html and pdf (last param in the method call below set to false)
                                String tableHtml = JsonHtmlParser.getHTML(decompressedMetaData, pluginLicense.isEvaluation(), evalMessage, false)
                                        + VelocityUtils.getRenderedTemplate("template/excellentable-html-css.vm", MacroUtils.createDefaultVelocityContext());

								return tableHtml;
							}catch(Exception e){

								LOGGER.error("Unable to convert spreadjs json to html", e);
							}

						}

                        if (excellentableDB == null && !"Template Preview".equals(conversionContext.getEntity().getTitle())) {

                        	String macroRenderErrorMessage = getAuiMessage(i18nResolver.getText("com.addteq.confluence.plugin.excellentable.macro.render.error"));
                            return macroRenderErrorMessage + "</br>" + body;

                        } else {

                            contextMap.put("excellentable-id", excellentableId);
                            contextMap.put("body", "");   // Since we dont want anything to render apart from the template excellentable.vm, putting empty string in body param so that upgraded data from 2.5 will discard any text or other input from the macro.

                            if (page != null) {
                                boolean hasEditPermission = permissionManager.hasPermissionNoExemptions(AuthenticatedUserThreadLocal.get(), Permission.EDIT, (Object) page);
                                contextMap.put("hasEditPermission", hasEditPermission);
                            }

                            /**
                             * ******** This code snippet is for attaching
                             * table image to the page in view mode i.e
                             * implemented for achieving the feature of export
                             * to word and PDF but as it is causing issue of too
                             * many notifications, so for now removing this
                             * functionality.Refer: PLUG-2964 ********
                             */
                            ContentEntityObject contentEntityObject = conversionContext.getEntity();
                            String page_id = contentEntityObject.getIdAsString();
                            contextMap.put("content-entity-id", page_id);

                            Macro scrollIgnorMacroPresent = macroManager.getMacroByName("scroll-ignore");
                            String exportBtnWikiContent = getExportBtnHtml(scrollIgnorMacroPresent, contextPath, excellentableId);
                            exportBtnHtml = xhtmlContent.convertStorageToView(exportBtnWikiContent, conversionContext);
                            contextMap.put("exportBtnHtml", exportBtnHtml);
                            if (pluginLicense.isEvaluation()) {
                                contextMap.put("isEvalLicense", true);
                            }

                            return VelocityUtils.getRenderedTemplate("template/excellentable/excellentable.vm", contextMap);
                        }
                    } else {  // If Id is not associated with the macro
                        return "</br>" + body;
                    }
                }
            } else {
                // handle unlicensed scenario
                String unlicensedMsg = getAuiMessage(i18nResolver.getText("com.addteq.confluence.plugin.excellentable.unlicensed.error"));
                return unlicensedMsg + "</br>" + body;
            }
        } catch (Exception ex) {
            String macroRenderErrorMessage = getAuiMessage(i18nResolver.getText("com.addteq.confluence.plugin.excellentable.macro.render.error"));
            return macroRenderErrorMessage + "</br>" + body;
        }

    }

    private Map<String, Object> getMacroVelocityContext() {
        return velocityHelperService.createDefaultVelocityContext();
    }

    @Override
    public BodyType getBodyType() {
        // TODO Auto-generated method stub
        return BodyType.RICH_TEXT;
    }

    @Override
    public OutputType getOutputType() {
        // TODO Auto-generated method stub
        return OutputType.INLINE;
    }

    public String getExportBtnHtml(Macro scrollIgnorMacroPresent, String contextPath, String excellentableId) {

        Map<String, Object> params = MacroUtils.defaultVelocityContext();
        params.put("excellentableId",excellentableId);
        params.put("contextPath", contextPath);

        exportBtnHtml = VelocityUtils.getRenderedTemplate("template/export-form.vm", params);

        if (scrollIgnorMacroPresent != null) {
            exportBtnHtml = "<ac:structured-macro  ac:name=\"scroll-ignore\"><ac:rich-text-body>" + exportBtnHtml
                    + "</ac:rich-text-body></ac:structured-macro>";
        }
        return exportBtnHtml;
    }

    private String getAuiMessage(String msg) {
        return "<div class=\"aui-message warning\">"
                + "<p class=\"title\">"
                + "<span class=\"aui-icon icon-warning\"></span>"
                + "<strong>" + msg + "</strong>"
                + "</p>"
                + "</div>";
    }
}
