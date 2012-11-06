The indexing_configuration.xml file needs to be manually copied to the /repositories/magnolia/workspaces/website folder. On a fresh installation the latter will be present
only at the end of the install process. The location of the repositories folder is specified in magnolia.properties file with key magnolia.repositories.home.
Once copied, you'll need to stop Magnolia, delete the index folder and restart Magnolia so the the index is recreated according to the new configuration.
The usage of JackaRabbit's indexing configuration is experimental and subject to change. Should it be kept in the final release we hope to remove the need for the manual copy.
