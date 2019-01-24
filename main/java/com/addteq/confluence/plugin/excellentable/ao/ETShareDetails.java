package com.addteq.confluence.plugin.excellentable.ao;

import net.java.ao.Accessor;
import net.java.ao.Entity;
import net.java.ao.Mutator;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

import java.util.Date;

@Preload
@Table("ETShareDetails")
public interface ETShareDetails extends Entity {
    /**
     * getETShare is used as value to the reverse element of oneToMany annotation for getETShareDetails getter method,
     * Any changes to it must be updated in the value of reverse element
     */ 
    public ETShare getETShare();

    public void setETShare(ETShare share);
    
    @Accessor("Note")
    public String getNote();
    
    @Mutator("Note")
    public void setNote(String note);
    
    @Accessor("Watcher")
    public String getWatcher();
    
    @Mutator("Watcher")
    public void setWatcher(String watcher);

    @Accessor("WatcherType")
    public int getWatcherType();

    @Mutator("WatcherType")
    public void setWatcherType(int watcherType);

    @Accessor("SharedDate")
    public Date getSharedDate();
    
    @Mutator("SharedDate")
    public void setSharedDate(Date sharedDate);

}
