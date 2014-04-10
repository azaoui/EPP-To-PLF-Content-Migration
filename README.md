EPP-To-PLF-Content-Migration
============================
Usage:


# Migrate binnary data(html,js,css..) which URLs contains old path or rest context
http://localhost:8080/portal/rest/migration/epp2plfdatamigration/migrateContents

ex:
before:background: url("/ecmdemo/rest-ecmdemo/jcr/repository/collaboration/sites content/live/intranet/web contents/site artifacts/Welcome/medias/images/
after:  background: url("/portal/rest/jcr/repository/collaboration/sites/intranet/web contents/site artifacts/Welcome/medias/images/

# Cleanup Publication
http://localhost:8080/portal/rest/migration/epp2plfdatamigration/cleanupPublication

# Move data from /sites content/live to /sites 
http://localhost:8080/portal/rest/migration/epp2plfdatamigration/moveContent


# Migrate portlet preferences which contains the "/sites content/live" path to "/sites"
http://localhost:8080/portal/rest/migration/epp2plfdatamigration/migrateNavigations


#Migrate old Links
http://localhost:8080/portal/rest/migration/epp2plfdatamigration/migrateLinks



#Migrate TaxonomyAction
http://localhost:8080/portal/rest/migration/epp2plfdatamigration/migrateTaxonomyAction
