package com.addteq.confluence.plugin.excellentable.multiedit.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

/**
 * The AO entity that registers the users attempt to enable collaborative editing.
 * If this table is populated with a True value row, then, this means the user attempted to
 * click the collaborative editing button and that he is educated about this feature. Thereafter,
 * we dont bother any user in that instance with dialogs about collaborative editing.
 * Ref: EXC-5010
 *
 * Created by yagnesh.bhat on 12/19/18.
 */
@Preload
@Table("LiveEditRegister")
public interface LiveEditRegisterDB extends Entity {
    public Boolean isTried();
    public void setTried(Boolean tried);
}
