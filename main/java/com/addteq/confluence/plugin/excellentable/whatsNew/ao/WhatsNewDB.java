package com.addteq.confluence.plugin.excellentable.whatsNew.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Default;
import net.java.ao.schema.Table;

/**
 * Saves
 *  userkey
 *  location (example - view, etc)
 *  attempts
 *  version (notification identifier example- 1,2)
 *  subscription
 */
@Preload
@Table("WhatsNewDB")
public interface WhatsNewDB extends Entity{

    void setUser(String user);
    String getUser();

    void setLocation(String location);
    String getLocation();

    @Default("0")
    void setAttempts(int attempts);
    int getAttempts();

    void setVersion(int version);
    int getVersion();

    void setSubscription(Boolean subscription);
    Boolean getSubscription();
}
