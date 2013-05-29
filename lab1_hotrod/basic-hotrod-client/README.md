JDG Lab: Use JDG remotely through Hotrod
================================================
Author: Tristan Tarrant, Martin Gencur, Matt Depue (some)
Summary: Hot Rod client based off of the hotrod-endpoint quickstart.
Target Product: JDG

What is it?
-----------

Hot Rod is a binary TCP client-server protocol used in JBoss Data Grid. The Hot Rod protocol facilitates faster client and server interactions in comparison to other text based protocols and allows clients to make decisions about load balancing, failover and data location operations.

This client is an example of how to connect remotely to JBoss Data Grid (JDG) to store, retrieve, and remove data from cache using the Hot Rod protocol. It is a simple Football Manager console application allows you to add and remove teams, add players to or remove players from teams, or print a list of the current teams and players using the Hot Rod based connector.


System requirements
-------------------

All you need to build this project is Java 6.0 (Java SDK 1.6) or better, Maven 3.0 or better.

The application this project produces is designed to be run on JBoss Data Grid 6.0 

 
Configure Maven
---------------

If you have not yet done so, you must [Configure Maven](../README.md#configure-maven-) before testing the quickstarts.


Configure and Start JDG
-------------

1. Obtain JDG server distribution on Red Hat's Customer Portal at https://access.redhat.com/jbossnetwork/restricted/listSoftware.html

2. Configure JDG for the appropriate type of deployment.  This client is intended to be used for the standalone, replicated, and distributed portions of the lab.  Please see the instructions for each of these labs to define the appropriate JDG configuration file and for starting up the server.

Start JBoss Data Grid 6
------------------------

1. Open a command line and navigate to the root of the JBoss Data Grid directory.
2. The following shows the command line to start the server with the web profile:

        For Linux:   JDG_HOME/bin/standalone.sh
        For Windows: JDG_HOME\bin\standalone.bat


Build and Run the Quickstart
-------------------------

_NOTE: The following build command assumes you have configured your Maven user settings. If you have not, you must include Maven setting arguments on the command line. See [Build and Deploy the Quickstarts](../README.md#buildanddeploy) for complete instructions and additional options._

1. Make sure you have started the JBoss Server as described above.
2. Open a command line and navigate to the root directory of this quickstart.
3. Type this command to build and deploy the archive:

        mvn clean package 
                
4. This will create a file at `target/basic-hotrod-client.jar` 

5. Run the example application in its directory:

        mvn exec:java
 

Using the application
---------------------
Basic usage scenarios can look like this (keyboard shortcuts will be shown to you upon start):

        at  -  add a team
        ap  -  add a player to a team
        rt  -  remove a team
        rp  -  remove a player from a team
        p   -  print all teams and players
        q   -  quit
        
Type `q` one more time to exit the application.        

Debug the Application
------------------------------------

If you want to debug the source code or look at the Javadocs of any library in the project, run either of the following commands to pull them into your local repository. The IDE should then detect them.

    mvn dependency:sources
    mvn dependency:resolve -Dclassifier=javadoc


