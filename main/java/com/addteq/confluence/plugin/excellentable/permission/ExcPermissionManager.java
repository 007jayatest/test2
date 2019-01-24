package com.addteq.confluence.plugin.excellentable.permission;

import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.google.gson.JsonObject;

/**
 *
 * @author addteq
 */
public interface ExcPermissionManager {
    
    JsonObject hasPermissionOnExcellentableById(int excellentableId, String permissionType);

    @Deprecated
    JsonObject hasPermissionOnExcellentable(ExcellentableDB[] excellentableDB, String permissionType);

    JsonObject hasPermissionOnExcellentable(ExcellentableDB excellentableDB, String permissionType);

    JsonObject getPermissionOnExcellentable(ExcellentableDB excellentableDB);

    JsonObject hasPermissionOnContentEntity(Long contentEntityId, String contentType, String spaceKey, String permissionType);

}
