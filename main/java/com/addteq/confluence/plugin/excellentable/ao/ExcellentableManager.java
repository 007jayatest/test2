package com.addteq.confluence.plugin.excellentable.ao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.addteq.confluence.plugin.excellentable.history.EditHistoryDAO;
import com.addteq.confluence.plugin.excellentable.model.ExcellentableModel;
import com.addteq.service.excellentable.exc_io.utils.ETDateUtils;
import com.addteq.service.excellentable.exc_io.utils.Gzip;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.user.User;


@Component
public class ExcellentableManager {

    private ActiveObjects ao;
    private UserAccessor userAccessor;
    private final String ANONYMOUS  = "Anonymous";
    private final EditHistoryDAO editHistoryDao;

	@Autowired
	public ExcellentableManager(@ComponentImport ActiveObjects ao, @ComponentImport UserAccessor userAccessor,
			EditHistoryDAO editHistoryDao) {
		this.ao = ao;
		this.userAccessor = userAccessor;
		this.editHistoryDao = editHistoryDao;
	}
	
	public ExcellentableModel getExcellentable(Integer id) {
				
		ExcellentableDB excAO = getExcellentableDB(id);
		
		if(excAO == null)
			return null;
		
		return fromAoToModel(excAO);
	
	}
	
	private ExcellentableDB getExcellentableDB(Integer id) {
		if(id == null)
			return null;
					
		return ao.get(ExcellentableDB.class, id );
	}
	
	public ExcellentableModel updateWithoutHistory(ExcellentableModel excModel) {
		
		ExcellentableDB excAO = getExcellentableDB(excModel.getID());

		return ao.executeInTransaction(new TransactionCallback<ExcellentableModel>() {
			@Override
			public ExcellentableModel doInTransaction() {
				excAO.setMetaData(excModel.getMetaData());
				excAO.setContentType(excModel.getContentType());
				excAO.setContentEntityId(excModel.getContentEntityId());
				excAO.save();

				return excModel;
			}
		});
		
	}
	
	public ExcellentableModel createExcellentable(ExcellentableModel  excModel) {

		return ao.executeInTransaction(new TransactionCallback<ExcellentableModel>(){
				@Override
				public ExcellentableModel doInTransaction() {
	
					ExcellentableDB excAO = ao.create(ExcellentableDB.class); 

					excAO.setMetaData(excModel.getMetaData());
					excAO.setContentEntityId(excModel.getContentEntityId());
					excAO.setContentType(excModel.getContentType());
					excAO.setSpaceKey(excModel.getSpaceKey());
					excAO.setCreated(ETDateUtils.currentTime());
					excAO.setCreator(excModel.getCreator());
					excAO.save();
					
					excModel.setID(excAO.getID());
				
					editHistoryDao.createHistory(excAO, excModel);
		
					return excModel;
				}
			});
	
	}

	// TODO commented setters are not currently used is get excellentable REST point
	// need to investigate if it is required
	private ExcellentableModel fromAoToModel(ExcellentableDB excAO) {
	
		ExcellentableModel excModel = new ExcellentableModel();

		//excModel.setComment();

		excModel.setContentEntityId(excAO.getContentEntityId());
		excModel.setContentType(excAO.getContentType());
		excModel.setCreated(excAO.getCreated());
		excModel.setCreatedDate(ETDateUtils.getFormattedDate(excAO.getCreated()));
		excModel.setCreator(excAO.getCreator());
		excModel.setCreatorFullName(getUserFullName(excAO.getCreator()));

		//excModel.setFilePath("");
		//excModel.setHistory();
		EditHistoryDB[] editHistoryDBS = excAO.getEditHistory();
		
		excModel.setHistoryID(editHistoryDBS.length > 0 ? editHistoryDBS[0].getID() : -1);
		excModel.setID(excAO.getID());

		Boolean isGZipped = Gzip.isCompressed(excAO.getMetaData());
		excModel.setIsGZipped(isGZipped);
		
		excModel.setMetaData(excAO.getMetaData());

		//excModel.setMultieditConnectionInfo();	
		//excModel.setProfilePicPath(excAO);
		
		excModel.setSpaceKey(excAO.getSpaceKey());

		//excModel.setTableId();
		//excModel.setThemeName();
		
		excModel.setUpdated(excAO.getUpdated());
		excModel.setUpdatedDate(ETDateUtils.getFormattedDate(excAO.getUpdated()));
		
		excModel.setUpdater(excAO.getUpdater());
		excModel.setVersionNumber(editHistoryDBS.length);
		
		return excModel;
	
	}

	  private String getUserFullName(String userName){
	        User user = userAccessor.getUserByName(userName);
	        if(user == null){
	            return ANONYMOUS;
	        }else{
	            return user.getFullName();
	        }
	    }
}
