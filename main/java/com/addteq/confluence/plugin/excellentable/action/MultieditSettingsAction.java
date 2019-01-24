package com.addteq.confluence.plugin.excellentable.action;

import com.addteq.confluence.plugin.excellentable.multiedit.model.MultieditConnectionInfo;
import com.addteq.confluence.plugin.excellentable.multiedit.service.LambdaRestClient;
import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Excellentable collaborative edit setup initialization
 *
 * @author akanksha
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */

public class MultieditSettingsAction extends ConfluenceActionSupport {

    private final LambdaRestClient lambdaRestClient;
    private final PluginLicenseManager licenseManager;
    private Integer status;
    private String licenseErrorMessage;
    private boolean isLicenseOK;
    final static Logger LOGGER = LoggerFactory.getLogger(MultieditSettingsAction.class);

    @Autowired
    public MultieditSettingsAction(
            LambdaRestClient LambdaRestClient,
            @ComponentImport PluginLicenseManager licenseManager) {

        this.lambdaRestClient = LambdaRestClient;
        this.licenseManager = licenseManager;
        this.isLicenseOK = true;
    }

    @Override
    public boolean isPermitted() {
        return permissionManager.hasPermission(getAuthenticatedUser(), Permission.ADMINISTER, PermissionManager.TARGET_APPLICATION);
    }

    @Override
    public String execute() throws Exception {
        if (!isLicenseValid()) {
            isLicenseOK = false;
            return ERROR;
        }
        MultieditConnectionInfo multieditConnectionInfo = lambdaRestClient.isEnabled();
        status = multieditConnectionInfo.getStatus();
        return SUCCESS;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getLicenseError() {
        return licenseErrorMessage;
    }

    public void setLicenseError(String licenseErrorMessage) {
        this.licenseErrorMessage = licenseErrorMessage;
    }

    public boolean getLicenseErrorStatus() {
        return isLicenseOK;
    }

    public void setLicenseErrorStatus(boolean isLicenseOk) {
        this.isLicenseOK = isLicenseOk;
    }

    public boolean isLicenseValid() {
        try {
            if (licenseManager.getLicense().isDefined()) {
                PluginLicense license = licenseManager.getLicense().get();
                if (license.getError().isDefined()) {
                    setLicenseError("Excellentable plugin license is expired.");
                    return false;
                } else {
                    return true;
                }
            } else {
                setLicenseError("Excellentable plugin is unlicensed.");
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Error occured while checking for license: " + e);
            setLicenseError("Error occured while checking for license. Please contact your Confluence Administrator.");
            return false;
        }

    }

}