package org.exoplatform.datasystems.migration;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;

import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("migration/")
public class ContentMigrationRESTService implements ResourceContainer {
	 @GET
	  @Path("epp2plfdatamigration/{event}")
	  public String Migrate(@PathParam("event") String name) {
		  String event= ContentMigrationService.dispatchEvent(name);
	    return event;
	  }

}
