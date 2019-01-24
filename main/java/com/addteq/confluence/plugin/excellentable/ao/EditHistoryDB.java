/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Default;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

import java.util.Date;

/**
 *
 * @author vikashkumar
 */
@Preload
@Table("EditHistoryDB")
public interface EditHistoryDB extends Entity {
 
    void setTableDB(ExcellentableDB tableDB);
    /**
     * getTableDB is used as value to the reverse element of oneToMany annotation for EditHistoryDB getter method,
     * Any changes to it must be updated in the value of reverse element
     */ 
    ExcellentableDB getTableDB();
    /**
     * {@link com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB}
     * Property metaData is same as body of this AO
     * @param body 
     */
    @StringLength(StringLength.UNLIMITED)
    void setBody(String body);
    String getBody();
    
    
    void setComment(String comment);
    String getComment();
    
    // Action: 0 -> Not Deleted, 1 -> Deleted
    @Default("0")
    void setAction(String action);
    String getAction();
    
    void setCreated(Date created);
    Date getCreated();
    
    void setCreator(String creator);
    String getCreator();
}
