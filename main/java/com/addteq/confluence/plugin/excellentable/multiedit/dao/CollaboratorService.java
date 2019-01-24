package com.addteq.confluence.plugin.excellentable.multiedit.dao;

import com.addteq.confluence.plugin.excellentable.multiedit.ao.CollaboratorDB;
import com.google.gson.JsonObject;

/**
 * @author Vikash Kumar
 * @author saurabh.gupta
 */

/**
 *  Interface for all database operation done for Collaborator
 */
public interface CollaboratorService {
    /**
     * Add a collaborator to the database.
     * @param excellentableID ID of the Excellentable.
     * @param userKey userkey of the collaborator.
     * @param versionNumber The version which collaborator started editing with.
     * @return The ID of the collaborator's entry from the database.
     */
    public Integer addUniqueCollaborator(Integer excellentableID, String userKey, Integer versionNumber);
    
    /**
     * Get collaborator by userKey.
     * @param excellentableID ID of the Excellentable.
     * @param userKey userkey of the collaborator.
     * @return Collaborator for the given userKey for the given Excellentable.
     */
    public CollaboratorDB getCollaboratorByUserKey(Integer excellentableID, String userKey);
    
    /**
     * Get array of collaborators based on ID of Excellentable.
     * @param excellentableID ID of the Excellentable.
     * @return Array of CollaboratorDB.
     */
    public CollaboratorDB[] getCollaborators(Integer excellentableID, Integer getUserUpdatedWithin);

    /**
     * Update a collaborator with the last seen time.
     * @param excellentableID ID of the Excellentable.
     * @param userKey userkey of the collaborator.
     * @param versionNumber The version which collaborator started editing with.
     * @return 1 if the update was success, 0 if the collaborator does not exist then
     * the new collaborator will be added.
     */
    public Integer updateOrCreateCollaborator(Integer excellentableID, String userKey, Integer versionNumber);

    /**
     * Deletes a collaborator.
     * @param excellentableID ID of the Excellentable.
     * @param userKey userkey of the collaborator.
     * @return Number of collaborators deleted. Mostly the return value will be 1 unless there some
     * duplicate entry.
     */
    public Integer deleteCollaborator(Integer excellentableID, String userKey);
    
    /**
     * Authorize currently logged in user for the given excId.
     * @param excId Id of the Excellentable.
     * @return A JSONObject which contains Authorization information.
     */
    public JsonObject authorizeLoggedInUser(int excId);
}