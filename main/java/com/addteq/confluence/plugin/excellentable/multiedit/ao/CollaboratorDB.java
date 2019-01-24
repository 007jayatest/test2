package com.addteq.confluence.plugin.excellentable.multiedit.ao;

import net.java.ao.Accessor;
import net.java.ao.Entity;
import net.java.ao.Mutator;
import net.java.ao.Preload;
import net.java.ao.schema.Index;
import net.java.ao.schema.Indexes;
import net.java.ao.schema.Table;

import java.util.Date;

/**
 * @author Vikash Kumar
 * @author saurabh.gupta
 */

/* Composite indexing of excID and userkey */
@Indexes({
    @Index(name = "excIDAndUser", methodNames = {"getExcID", "getUserKey"}),
    @Index(name = "excID", methodNames = "getExcID")
})

/**
 * Table created with name Collaborator_DB in the plugin.
 * To hold the data of collaborators per Excellentable.
 */
@Preload
@Table("CollaboratorDB")
public interface CollaboratorDB  extends Entity {

    /**
     * @return ID of the Excellentable being edited.
     */
    @Mutator("ExcID")
    public Integer  getExcID();
    
    @Accessor("ExcID")
    public void setExcID(Integer excID);

    /**
     * @return UserKey of the user editing the Excellentable.
     */
    @Mutator("UserKey")
    public String getUserKey();
    
    @Accessor("UserKey")
    public void setUserKey(String userKey);

    /**
     * @return Time when last the user has edited the Excellentable.
     */
    @Mutator("LastSeen")
    public Date getLastSeen();
    
    @Accessor("LastSeen")
    public void setLastSeen(Date lastSeen);

    /**
     * @return The version number of the Excellentable. 
     */
    @Mutator("Version")
    public Integer getVersion();
    
    @Accessor("Version")
    public void setVersion(Integer Version);
    
}
