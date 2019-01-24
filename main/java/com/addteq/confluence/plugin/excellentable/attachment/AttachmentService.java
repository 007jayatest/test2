/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.attachment;

import com.google.gson.JsonObject;

/**
 *
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */
public interface AttachmentService {

    public JsonObject parseAttachment(long attachmentId);
}
