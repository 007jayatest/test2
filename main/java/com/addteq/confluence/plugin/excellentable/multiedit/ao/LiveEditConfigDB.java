package com.addteq.confluence.plugin.excellentable.multiedit.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

/**
 * Stores firebase related connection information and status of collaborative editing
 * if it is enabled or disabled.
 * 
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */
@Preload
@Table("LiveEditConfig")
public interface LiveEditConfigDB extends Entity {

    /**
     * Status represents if the collaborative editing via Firebase is 
     * 1: Enabled, 0: Disabled
     * @return A number which represents the status of collaborative editing
     * is enabled or disabled.
     **/
    public int getStatus();
    public void setStatus(int status);
    
    // Confluence id representing unique instance
    public String getConfluenceId();
    public void setConfluenceId(String confluenceId);
    
    @StringLength(450)
    public String getPublicKey();
    public void setPublicKey(String publicKey);
    
    public String getApiKey();
    public void setApiKey(String apiKey);
    
    public String getFirebaseUrl();
    public void setFirebaseUrl(String firebaseUrl);
    
    public String getFirebaseContext();
    public void setFirebaseContext(String firebaseContext);

}
