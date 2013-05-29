JDG Lab
=======

JBoss Data Grid Lab sessions exploring features of the 6.1 release.

### Notes/Disclaimers
* This lab is intended to be run on any major modern OS (Linux, Mac, Windows), however, the 
lab was created on a Fedora System and some details may have been missed.  Please notify the 
"instructor" if there are any environment-specific issues that were missed.

Pre-Lab:  Setting up your environment
-------------------------------------
((Revisit))

Lab 1:  Hot Rod Client
----------------------

### Tools Used:

* JBoss Developer Studio
* JBoss Data Grid Server 6.1
* maven
* jconsole

Hot Rod is a binary TCP client-server protocol used in JBoss Data Grid. The Hot Rod protocol 
facilitates faster client and server interactions in comparison to other text based protocols 
and allows clients to make decisions about load balancing, failover and data location operations.

This lab will demonstrate how to connect remotely to JBoss Data Grid (JDG) to store, 
retrieve, and remove data from a cache using the Hot Rod protocol.  The application, pulled
directly from the QuickStarts for EAP 6.1, is a simple Football Manager 
console which allows you to add and remove teams, add players to or remove players from teams, 
or print a list of the current teams and players using the Hot Rod based connector.

In this lab, we will connect to caches configured in the different modes, namely the **standalone**
, **replicated**, and **distributed** modes.

### Configuring a Standalone Cache
The **standalone** variety is the simplest cache mode as it does not need to discover or communicate its state with other 
members.  For convenience, JBoss Data Grid Server provides a basic startup script and configuration file demonstrating
a standalone cache.  These files can be found in the following locations:
    
    <JDG_SERVER_HOME>/bin/standalone.[sh/bat]
    <JDG_SERVER_HOME>/standalone/configuration/standalone.xml      

By default, the standalone script starts up a streamlined JBoss EAP server configured with the
standalone.xml file.  Alternatively, the configuration file can be overridden by passing the '-c'
switch to the standalone script.  We will be doing this in the lab in order to preserve the original,
pristine copy.

### Creating the 'Teams' Standalone Cache

Perform the following:

* Make a copy of standalone.xml in the same folder and call it standalone-lab1.xml

* Modify standalone-lab1.xml - Replace the existing datasources subsystem with the following:    
    
    	<subsystem xmlns="urn:jboss:domain:datasources:1.0">            	    
	        <!-- Define this Datasource with jndi name  java:jboss/datasources/ExampleDS -->
	        <datasources>
	            <datasource jndi-name="java:jboss/datasources/ExampleDS" pool-name="ExampleDS" enabled="true" use-java-context="true">
	                <!-- The connection URL uses H2 Database Engine with in-memory database called test -->
	                <connection-url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1</connection-url>
	                <!-- JDBC driver name -->
	                <driver>h2</driver>
	                <!-- Credentials -->
	                <security>
	                    <user-name>sa</user-name>
	                    <password>sa</password>
	                </security>
	            </datasource>
	            <!-- Define the JDBC driver called 'h2' -->
	            <drivers>
	                <driver name="h2" module="com.h2database.h2">
	                    <xa-datasource-class>org.h2.jdbcx.JdbcDataSource</xa-datasource-class>
	                </driver>
	            </drivers>
	        </datasources>	    
		</subsystem>
		
* Modify standalone-lab1.xml - Add the following cache within the 'local' cache-container of the urn:infinispan:server:core subsystem:

		<!-- ADD a local cache called 'teams' -->
		<local-cache name="teams" start="EAGER" batching="false" indexing="NONE" >
			<!-- Define the locking isolation of this cache -->
			<locking isolation="REPEATABLE_READ" acquire-timeout="20000" concurrency-level="500" striping="false" />
			<!-- Disable transactions for this cache -->
			<transaction mode="NONE" />
			<!-- Define the JdbcBinaryCacheStores to point to the ExampleDS previously defined -->
			<string-keyed-jdbc-store datasource="java:jboss/datasources/ExampleDS" passivation="false" preload="false" purge="false">
				<!-- Define the database dialect -->
				<property name="databaseType">H2</property>
				<!-- specifies information about database table/column names and data types -->
				<string-keyed-table prefix="JDG">
					<id-column name="id" type="VARCHAR"/>
					<data-column name="datum" type="BINARY"/>
					<timestamp-column name="version" type="BIGINT"/>
				</string-keyed-table>
			</string-keyed-jdbc-store>
		</local-cache>
		<!-- End of local cache called 'teams' definition -->
		
### Starting up the JBoss Data Grid Server

The server will be started up with the configuration from the previous section.  Run the following
command with an appropriate user to start up the server:

    <JDG_HOME>/bin/standalone.[sh|bat] -c standalone-lab1.xml 


_NOTE: The script searches for the configuration file relative to the configuration folder.  Please ensure the standalone-lab1.xml file is in the correct location_

### Firing up the Client Application

_NOTE: The following build command assumes you have configured your Maven user settings. If you have not, you must include Maven setting arguments on the command line. See [Build and Deploy the Quickstarts](../README.md#buildanddeploy) for complete instructions and additional options._

1. Open a command line and navigate to the lab1-basic-hotrot-client directory in this package.
2. Type this command to build and deploy the archive:

        mvn clean package 
                
3. This will create a file at `target/lab1-basic-hotrot-client.jar` 

4. Run the example application in its directory:

        mvn exec:java
 

### Using the application

Basic usage scenarios can look like this (keyboard shortcuts will be shown to you upon start):

        at  -  add a team
        ap  -  add a player to a team
        rt  -  remove a team
        rp  -  remove a player from a team
        p   -  print all teams and players
        q   -  quit
        
Type `q` one more time to exit the application.

### Inspecting the Cache with JConsole

JBoss Data Grid Server provides various information via MBeans in order to manage and monitor
key components.  Inspect the server by performing the following steps:
    
1.  Run jconsole as the same user that started JBoss Data Grid Server.  jconsole can be found at the following location *<JDK_HOME>/lib/jconsole*.
2.  Select the process starting with jboss-modules.jar
3.  Navigate to the MBEANS tab
4.  For the purpose of this lab, the most interesting MBeans can be found by expanding jboss.infinispan. 
Particularly, the information provided in: 
    * jboss-infinispan -> Cache -> teams(local) -> local -> Statistics -> Attributes
    * jboss-infinispan -> Server -> Hot Rod -> Transport -> Attributes
 

### Configuring JGroups for TCP in the JBoss Data Grid Server

JBoss Data Grid Server uses JGroups for discovery and transport in a clustered topology.  By default, the JBoss Data Grid server uses a 
JGroups stack that uses UDP for both discovery and transport.  We will be using a JGroups stack that uses TCP for discovery and transport
for this lab as the clusters will all reside on the participant's workstation.

