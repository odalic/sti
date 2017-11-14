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

### ACKNOWLEDGEMENT
Odalic STI came to life as a student project at http://www.mff.cuni.cz .

### LICENCE
Apache 2.0

### Installation guide and documentation
[PDF version](https://odalic.github.io/download/ODALIC.Project.Documentation.pdf)

[Web](https://odalic.github.io/)
