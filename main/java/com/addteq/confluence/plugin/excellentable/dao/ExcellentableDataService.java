package com.addteq.confluence.plugin.excellentable.dao;

import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.addteq.confluence.plugin.excellentable.model.ExcellentableModel;

public interface ExcellentableDataService {

    ExcellentableDB getTable(final Integer excId);

    ExcellentableModel getTableData(final Integer excId, final ExcellentableDB excellentableDB, final ExcellentableModel excellentableModel);

    ExcellentableModel getContentData(final ExcellentableDB excellentableDB, final ExcellentableModel excellentableModel);
}
