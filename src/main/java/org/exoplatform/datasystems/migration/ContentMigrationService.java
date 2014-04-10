/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.datasystems.migration;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;


import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.apache.commons.lang.StringUtils;

public class ContentMigrationService {
	private static final Log LOG = ExoLogger
			.getLogger(ContentMigrationService.class);

	public static String dispatchEvent(String event) {
		
		if (ConversationState.getCurrent().getCurrent().getIdentity() == null) return "please connect with an admin user before starting the migration";
		else if (event.equals("migrateContents")){
			migrateContents();
			return event+" with success";
		}
		else if (event.equals("migrateLinks")){
			migrateLinks();
			return event+" with success";
		}
		else if (event.equals("migrateNavigations")){
			migrateNavigations();
			return event+" with success";
		}
		else if (event.equals("migrateTaxonomyAction")){
			migrateTaxonomyAction();
			return event+" with success";
		}
		else if (event.equals("moveContent")){
			movecontent();
			return event+" with success";
		}
		else if (event.equals("cleanupPublication")){
			cleanupPublication();
			return event+" with success";
		}
		

		return "No action found corresponding to this event :"+event+"  please check your rest link!";

	}



	private static void migrateContents() {
		try {

			RepositoryService repoService_ = (RepositoryService) ExoContainerContext
					.getContainerByName("portal").getComponentInstanceOfType(
							RepositoryService.class);
			ManageableRepository repo = repoService_
					.getRepository("repository");
			Session session = repo.getSystemSession("collaboration");
			String statement = "select * from nt:resource ORDER BY exo:name DESC";
			QueryResult result = session.getWorkspace().getQueryManager()
					.createQuery(statement, Query.SQL).execute();
			NodeIterator nodeIter = result.getNodes();
			System.out.println("=====Start migrate data for all contents=====");

			while (nodeIter.hasNext()) {
				Node ntResource = nodeIter.nextNode();
				String mimeType = ntResource.getProperty("jcr:mimeType")
						.getString();
				if (mimeType.startsWith("text")) {
					String jcrData = ntResource.getProperty("jcr:data")
							.getString();
					if (jcrData.contains("/sites content/live/")
							|| jcrData.contains("/ecmdemo/")
							|| jcrData.contains("/rest-ecmdemo/")) {

						LOG.info("Content found with old data to update :"
								+ ntResource.getParent().getPath());
					   //pattern	background: url("/ecmdemo/rest-ecmdemo/jcr/repository/collaboration/sites content/live/intranet/web contents/site artifacts/Welcome/medias/images/
						//after	background: url("/portal/rest/jcr/repository/collaboration/sites/intranet/web contents/site artifacts/Welcome/medias/images/
						String newData = StringUtils.replaceEachRepeatedly(
								jcrData, new String[] { "/ecmdemo/",
										"/rest-ecmdemo/",
										"/sites content/live/" }, new String[] {
										"/portal/", "/rest/", "/sites/" });

						// String realData = StringUtils.replace(jcrData,
						// "/rest-ecmdemo/","/rest/");
						// System.out.println(realData);
						ntResource.setProperty("jcr:data", newData);
						session.save();
						LOG.info("migrate :" + ntResource.getParent().getName()
								+ " with success");

					}
				}

			}
			System.out.println("=====end migrate data for all contents=====");

		} catch (Exception e) {
			LOG.error(
					"An unexpected error occurs when migrating old Content: ",
					e);
		}
	}

	private static void migrateNavigations() {
		try {
			RepositoryService repoService_ = (RepositoryService) ExoContainerContext
					.getContainerByName("portal").getComponentInstanceOfType(
							RepositoryService.class);
			ManageableRepository repo = repoService_
					.getRepository("repository");
			Session session = repo.getSystemSession("portal-system");
			System.out.println("=====Start migrate old preferences=====");
			String statement = "select * from mop:portletpreference where mop:value like '%/sites content/live/%'";
			QueryResult result = session.getWorkspace().getQueryManager()
					.createQuery(statement, Query.SQL).execute();
			NodeIterator nodeIter = result.getNodes();
			while (nodeIter.hasNext()) {
				Node preferenceNode = nodeIter.nextNode();
				String oldPath = preferenceNode.getProperty("mop:value")
						.getValues()[0].getString();
				String newPath = StringUtils.replace(oldPath,
						"/sites content/live/", "/sites/");

				preferenceNode.setProperty("mop:value",
						new String[] { newPath });
			}
			session.save();

			System.out.println("===== Portlet preference upgrade completed =====");

		} catch (Exception e) {

			LOG.error(
					"An unexpected error occurs when migrating old preferences: ",
					e);
		}

	}

	private static void migrateLinks() {
	    try {
			RepositoryService repoService_ = (RepositoryService) ExoContainerContext
			.getContainerByName("portal").getComponentInstanceOfType(
					RepositoryService.class);
	ManageableRepository repo = repoService_
			.getRepository("repository");
	Session session = repo.getSystemSession("collaboration");
	        if (LOG.isInfoEnabled()) {
	        	System.out.println("=====Start migrate old link in contents=====");
	        }
	        String statement = "select * from exo:linkable where exo:links like '%/sites content/live/%'";
	        QueryResult result = session.getWorkspace().getQueryManager().createQuery(statement, Query.SQL).execute();
	        NodeIterator nodeIter = result.getNodes();
	        while(nodeIter.hasNext()) {
	          Node contentNode = nodeIter.nextNode();
	          if (LOG.isInfoEnabled()) {
	            LOG.info("=====Migrating content '"+contentNode.getPath()+"' =====");
	          }
	          Value[] oldLinks = contentNode.getProperty("exo:links").getValues();
	          List<String> newLinks = new ArrayList<String>();
	          for(Value linkValue : oldLinks) {
	            newLinks.add(StringUtils.replace(linkValue.getString(), "/sites content/live/", "/sites/"));
	          }
	          contentNode.setProperty("exo:links", newLinks.toArray(new String[newLinks.size()]));
	        }
	        session.save();
	        if (LOG.isInfoEnabled()) {
	        	System.out.println("===== Migrate content links completed =====");
	        }
	      } catch (Exception e) {
	        if (LOG.isErrorEnabled()) {
	          LOG.error("An unexpected error occurs when migrating content links: ", e);
	        }
	      }
	}
	
	private static void migrateTaxonomyAction() {

		  SessionProvider sessionProvider = null;
		  try {
			  RepositoryService reposervice = (RepositoryService)ExoContainerContext.getContainerByName("portal").getComponentInstanceOfType(RepositoryService.class);
			  sessionProvider = SessionProvider.createSystemProvider();
			  String wsName = reposervice.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
			  Session session = sessionProvider.getSession(wsName, reposervice.getCurrentRepository());

			  if (LOG.isInfoEnabled()) {
				  System.out.println("=====Start to migrate taxonomy actions=====");
			  }
			  String statement = 
					  "select * from exo:taxonomyAction where (exo:targetPath like '%/sites content/live/%' or exo:storeHomePath like '%/sites content/live/%')";
			  QueryResult result = session.getWorkspace().getQueryManager().createQuery(statement, Query.SQL).execute();
			  NodeIterator nodeIter = result.getNodes();
			  while(nodeIter.hasNext()) {
				  Node taxoAction = nodeIter.nextNode();
				  String targetPath = taxoAction.getProperty("exo:targetPath").getString();
				  String homePath = taxoAction.getProperty("exo:storeHomePath").getString();
				  taxoAction.setProperty("exo:targetPath", StringUtils.replace(targetPath, "/sites content/live/", "/sites/"));
				  taxoAction.setProperty("exo:storeHomePath", StringUtils.replace(homePath, "/sites content/live/", "/sites/"));
			  }
			  session.save();
			  if (LOG.isInfoEnabled()) {
				  System.out.println("=====Completed the migration for taxonomy action=====");
			  }
		  } catch (Exception e) {
			  if (LOG.isErrorEnabled()) {
				  LOG.error("An unexpected error occurs when migrating for taxonomy actions: ", e);
			  }
		  } finally {
			  if (sessionProvider != null) {
				  sessionProvider.close();
			  }
		  }
	  }
	private static void movecontent() {
		 System.out.println("Start moving contenst.............");
	    

	    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
	    try{
	      RepositoryService reposervice = (RepositoryService)ExoContainerContext.getContainerByName("portal").getComponentInstanceOfType(RepositoryService.class);
	      ExtendedSession session = (ExtendedSession)sessionProvider.getSession("collaboration", reposervice.getCurrentRepository());
	      String nodePath = "/sites content/live";
	      String destNode="/sites";
		Node sourceNode = session.getRootNode().getNode(nodePath.substring(1));
		Node targetNode = session.getRootNode().getNode(destNode.substring(1));
	      if (sourceNode.hasNodes()){
	        NodeIterator iter = sourceNode.getNodes();
	        while (iter.hasNext()){
	          Node child = (Node) iter.next();
	          if (targetNode.hasNode(child.getName())) {
	        	targetNode.getNode(child.getName()).remove();
	        	session.save();
	          }
	          LOG.info("Move " + nodePath + "/" + child.getName() + " to " + destNode + "/" + child.getName());
	          session.move(nodePath + "/" + child.getName(), destNode + "/" + child.getName(), false);
	          //session.move("/sites content/live/testsite","/sites/testsite",false);
	          session.save();
	        }
	        //Remove source node
	        sourceNode.remove();
	        session.save();
	        System.out.println("=====Completed moving old contents=====");
	      }
	    }catch(Exception e){
	    	LOG.error("Unexpected error happens in moving nodes", e.getMessage());
	    }finally {
	      if (sessionProvider != null) {
	        sessionProvider.close();
	      }
	    }
	  
		
	}
	private static void cleanupPublication() {
		SessionProvider sessionProvider = null;
		  RepositoryService reposervice = (RepositoryService)ExoContainerContext.getContainerByName("portal").getComponentInstanceOfType(RepositoryService.class);
		  sessionProvider = SessionProvider.createSystemProvider();
		  System.out.println("=====Start cleanup Publication=====");
		  String wsName;
		try {
			wsName = reposervice.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
	
		  Session session = sessionProvider.getSession(wsName, reposervice.getCurrentRepository());
        QueryManager manager = session.getWorkspace().getQueryManager();
        String statement = "select * from nt:base";
        Query query = manager.createQuery(statement.toString(), Query.SQL);
        NodeIterator iter = query.execute().getNodes();
        while (iter.hasNext()) {
          Node node = iter.nextNode();
          if (node.hasProperty("publication:liveRevision")
              && node.hasProperty("publication:currentState")) {
            if (LOG.isInfoEnabled()) {
              LOG.info("\"" + node.getName() + "\" publication lifecycle has been cleaned up");
            }
            node.setProperty("publication:liveRevision", "");
            node.setProperty("publication:currentState", "published");
          }

        }
        session.save();
        System.out.println("=====End cleanup Publication=====");
		} catch (RepositoryException e) {
			LOG.error("Unexpected error happens when clean up contents", e.getMessage());
		}
		
	}
	
		
	}


