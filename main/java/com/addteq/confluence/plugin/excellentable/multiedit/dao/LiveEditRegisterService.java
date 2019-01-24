package com.addteq.confluence.plugin.excellentable.multiedit.dao;

import com.atlassian.activeobjects.tx.Transactional;

/**
 * Service class to access the LiveEditRegister AO table
 * Created by yagnesh.bhat on 12/19/18.
 */
@Transactional
public interface LiveEditRegisterService {
    void registerMultiEditAttempt();
    boolean getRegisterMultiEditAttempt();
}
