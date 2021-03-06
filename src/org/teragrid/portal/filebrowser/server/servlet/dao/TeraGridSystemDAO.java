/* 
 * Created on Jun 20, 2008
 * 
 * Developed by: Rion Dooley - dooley [at] tacc [dot] utexas [dot] edu
 * 				 TACC, Texas Advanced Computing Center
 * 
 * https://www.tacc.utexas.edu/
 */

package org.teragrid.portal.filebrowser.server.servlet.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.teragrid.portal.filebrowser.applet.exception.ResourceException;
import org.teragrid.portal.filebrowser.server.servlet.model.resource.TeraGridSystem;
import org.teragrid.portal.filebrowser.server.servlet.model.user.User;
import org.teragrid.portal.filebrowser.server.servlet.persistence.HibernateUtil;

/**
 * Retrieves the systems for which the user has a valid account
 * 
 * @author Rion Dooley < dooley [at] tacc [dot] utexas [dot] edu >
 *
 */
@SuppressWarnings("unchecked")
public class TeraGridSystemDAO {

	private static final Logger logger = Logger.getLogger(TeraGridSystemDAO.class);
	
    public static List<TeraGridSystem> findSystemAccounts(User user) throws ResourceException{
        
        HibernateUtil.beginTransaction();
        
        Session session = HibernateUtil.getSession();
        
        List<TeraGridSystem> systems = new ArrayList<TeraGridSystem>();
        
        try {
        	// find all resources for the user
            String sql = "select s.username, s.resource_name " + 
				"from portal.sav s where s.person_id = " + user.getId();
            
            List<Object[]> results = session.createSQLQuery(sql).list();
            
            logger.debug("Found " + results.size() + " compute results.");
            
            for (Object[] result: results) {
                TeraGridSystem system = new TeraGridSystem();
                system.setResourceName((String)result[1]);
                system.setUserId(user.getId());
                system.setUserName((String)result[0]);
                
                systems.add(system);
            }
            
            sql = "SELECT pops_name as username, resource_name FROM portal.allocable_resources " +
    			"WHERE is_active is FALSE";
            
            List<Object[]> inactiveResults = session.createSQLQuery(sql).list();
            
            logger.debug("Found " + inactiveResults.size() + " inactive resources.");
            
            
            for (Object[] result: inactiveResults) {
                String resourceName = (String)result[1];
//                logger.debug("Resolving inactive resource " + result);
                        
                for (TeraGridSystem system: systems) {
                    // if the system is not active delete it.
                    if (system.getResourceName().equals(resourceName) && !resourceName.equals("lonestar.tacc.teragrid")) {
                    	systems.remove(system);
                        logger.debug("Removing inactive resource " + system.getResourceName() + " from the result set");
                        break;
                    }
                }
            }
            
            return systems;
            
        } catch (HibernateException ex) {
            
            throw new ResourceException("Error retrieving system resources for " + user.getUsername(),ex);
            
        } finally {
            session.close();
        }
    }
    
    public static List<TeraGridSystem> findStorageAccounts(User user) throws ResourceException {
        HibernateUtil.beginTransaction();
        
        Session session = HibernateUtil.getSession();
        
        List<TeraGridSystem> systems = new ArrayList<TeraGridSystem>();
        
        try {
            String sql = "SELECT DISTINCT site_person_id, site_resource_name, proj_on_resource_state " + 
            "FROM acctv2 WHERE person_id = " + user.getId() + 
            " and proj_on_resource_state != 'inactive' and username != ''";
            
            List<Object[]> results = session.createSQLQuery(sql).list();
            
            logger.debug("Found " + results.size() + " storage results.");
            
            for (Object[] result: results) {
                TeraGridSystem system = new TeraGridSystem();
                system.setResourceName((String)result[1]);
                system.setUserId(user.getId());
                system.setUserName((String)result[0]);
                system.setUserState((String)result[2]);
                
                systems.add(system);
            }
            
            return systems;
            
        } catch (HibernateException ex) {
            
            throw new ResourceException("Error retrieving system resources for " + user.getUsername(),ex);
            
        } finally {
            session.close();
        }
    }
    
    // Doesn't work...the resourcename field is returning the same value in every row for whatever reason!!
//    public static List<TeraGridSystem> loadSystemAccounts(User user) throws ResourceException{
//        HibernateUtil.beginTransaction();
//        
//        Session session = HibernateUtil.getSession();
//        
//        List<TeraGridSystem> systems = new ArrayList<TeraGridSystem>();
//        
//        try {
//            
//            systems = session.createCriteria(TeraGridSystem.class)
//                .add(Expression.eq("userId",user.getId()))
//                .add(Expression.ne("userState", "inactive"))
//                .add(Expression.ne("userName", "")).list();
//                        
//            return systems;
//            
//        } catch (HibernateException ex) {
//            
//            throw new ResourceException("Error retrieving system resources for " + user.getUsername(),ex);
//            
//        }
//    }
}
