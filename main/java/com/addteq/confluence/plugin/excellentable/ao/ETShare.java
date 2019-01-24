package com.addteq.confluence.plugin.excellentable.ao;

import net.java.ao.*;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Preload
@Table("ETShare")
public interface ETShare extends Entity {

    @Accessor("SecretKey")
    public String getSecretKey();

    @Mutator("SecretKey")
    public void setSecretKey(String secretKey);

     /**
     * getExcellentable is used as value to the reverse element of oneToMany annotation for ETShare getter method,
     * Any changes to it must be updated in the value of reverse element
     */ 
    @Accessor("Excellentable")
    public ExcellentableDB getExcellentable();

    @Mutator("Excellentable")
    public void setExcellentable(ExcellentableDB excDetails);

    @Accessor("FilterString")
    @StringLength(StringLength.UNLIMITED)
    public String getFilterString();

    @Mutator("FilterString")
    public void setFilterString(String filterString);
    
    @Accessor("Reporter")
    public String getReporter();
    
    @Mutator("Reporter")
    public void setReporter(String reporter);

    @OneToMany(reverse="getETShare")
    public ETShareDetails[] getETShareDetails();
}
