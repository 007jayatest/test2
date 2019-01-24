/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.history;

import com.addteq.confluence.plugin.excellentable.ao.EditHistoryDB;
import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.addteq.confluence.plugin.excellentable.model.ExcellentableModel;
import com.atlassian.activeobjects.tx.Transactional;

import java.util.List;

/**
 *
 * @author vikashkumar
 */
@Transactional
public interface EditHistoryDAO {

    public EditHistoryDB createHistory(ExcellentableDB excellentableDB, ExcellentableModel etModel);

    public ExcellentableModel getHistory(int excId, int historyId);
    
    public List<ExcellentableModel> getAllHistory(int eid, int limit, int offset);

    public List<ExcellentableModel> getAllHistoryOverview(int eid, int limit, int offset);

    public ExcellentableModel restoreTo(ExcellentableModel excellentableDB, int editHistoryId);
    
    public int getHistoryCount(int excId);
}
