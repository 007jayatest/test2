package com.addteq.confluence.plugin.excellentable.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.Preload;
import net.java.ao.schema.Default;
import net.java.ao.schema.StringLength;

import java.util.Date;

@Preload
public interface ExcellentableDB extends Entity {

    @StringLength(StringLength.UNLIMITED)
    String getMetaData();
    void setMetaData(String metaData);
    
    long getContentEntityId();
    void setContentEntityId(long tableId);
    
    String getContentType();
    void setContentType(String contentEntityType);
    
    String getSpaceKey();     
    void setSpaceKey(String spaceKey);

     void setCreated(Date created);
    Date getCreated();

    void setCreator(String creator);
    String getCreator();
    
    void setUpdated(Date updated);
    Date getUpdated();
    
    void setUpdater(String updater);
    String getUpdater();
    
    // Action: 1 -> Deleted | 0 -> Not Deleted
    @Default("0")
    void setAction(String action);
    String getAction();
    
    @OneToMany(reverse="getExcellentable")
    public ETShare[] getETShare();
    
    @OneToMany(reverse="getTableDB")
    public EditHistoryDB[] getEditHistory();
}