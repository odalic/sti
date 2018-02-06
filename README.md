Master branch: ![Build status for master branch](https://api.travis-ci.org/odalic/sti.svg?branch=master) Develop branch: ![Build status for develop branch](https://api.travis-ci.org/odalic/sti.svg?branch=develop)

# Odalic Semantic Table Interpretation

Odalic Semantic Table Interpretation is a fork of https://github.com/ziqizhang/sti , which is a tool allowing to extract Linked Data from web pages containing HTML tables.

Odalic focuses on CSV as the source of data and introduces several major extensions and improvements, e.g.:
- ODALIC server deployable as WAR, accessible and controllable through REST API.
- Users can provide feedback on results of automatic conversion, which the algorithm now takes into account during subsequent runs.
- Users can add their own resources and use them for feedback and exports.
- Ability to employ multiple knowledge bases at once.
- Export of results conforming to [CSV on the Web](https://www.w3.org/2013/csvw/wiki/Main_Page) or popular RDF serialization formats Turtle and JSON+LD.
- Support for running the conversions in independent tasks and their management.
- Support for multiple users and the administrator, employing token-based authorization and authentication.
- Local and remote CSV files management.
- Task configuration exportable in RDF for easier data provenance.
- Knowledge base proxies management

Together with https://github.com/odalic/odalic-ui , which serves as its graphic user interface, and https://github.com/odalic/odalic-uv-plugin , which allows to exploit the power of Odalic in a mature ETL framework, the Odalic STI provides a comprehensive platform to convert Open Data published in the form of CSV files to Linked Open Data, making them inherently more valuable, easier to query, integrate and share.


## Installation guide and documentation

### Building from source files
- Checkout the sources and accompanying resource from Git repository at https://github.com/odalic/sti or copy them from the installation disc.
- Install the libraries in the lib directory by running the mvninstall.bat in the source files root directory (or in case of other OSs than Windows: running the few mvn commands present there manually), as these are not present in any public Maven repository.
- Run mvn install in the root directory, all the sub-projects will be installed one by one and the produced .war will be placed in odalic/target subdirectory. Copy the .war file to the application server (but do not start the application server yet)

#### Configuration 
- Copy the subdirectories config and resources (only these are needed during runtime) at a desired location (further referred as {sti.home}) and make the necessary changes in the main configuration file sti.properties, located in the config sub-directory. The details of configuration are described in [Configuration](https://odalic.github.io/documentation/Configuration_76975474.html), the minimum needed settings are the following (located in the main configuration file config/sti.properties):
  - sti.home - absolute path to the root working directory {sti.home}.
  - cz.cuni.mff.xrg.odalic.users.admin.email - default (admin) user email (used as login)
  - cz.cuni.mff.xrg.odalic.users.admin.password - default (admin) user password (can be changed later using the API)
  - cz.cuni.mff.xrg.odalic.db.file - absolute path to a file (which will be created if not existing yet) keeping the state of the server
- The Java Virtual Machine running the application server (e.g. Tomcat) itself has to be started with a system property cz.cuni.mff.xrg.odalic.sti set to a path leading to the main configuration file, e.g.: -Dcz.cuni.mff.xrg.odalic.sti={sti.home}/config/sti.properties
where the {sti.home}/config/sti.properties is a location of the main configuration file.
- Start the application server with the .war application

### Knowledge bases
- You can configure knowledge bases directly in the UI. You can also import sample configurations, e.g. for [Dbpedia](https://github.com/odalic/sti/blob/develop/resources/sampleknowledgebases/dbpedia.ttl)
  - in Odalic UI, go to Knowledge bases -> Import and select that [Dbpedia](https://github.com/odalic/sti/blob/develop/resources/sampleknowledgebases/dbpedia.ttl) file

### Full installation and user guide

[PDF version](https://odalic.github.io/download/ODALIC.Project.Documentation.pdf)

[Web](https://odalic.github.io/)

## Acknowledgement
Odalic started as a student project at Charles University in Prague (Vasek Brodec, Josef Janousek, Istvan Satmari, Katerina Bokova, Jan Vana), under the supervision of Tomas Knap, with many useful advices from the original author of the core algorithm - Ziqi Zhang. It is further developed and maintained as part of the [ADEQUATe project](http://adequate.at). 

## License
Apache 2.0

## Odalic research paper to cite
If you refer to Odalic, please cite [this paper](http://dblp.uni-trier.de/rec/bibtex/conf/semweb/Knap17)
