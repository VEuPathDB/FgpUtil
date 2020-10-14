# FgpUtil

## Description
Contains a set of utility scripts and software libraries shared across the VEuPathDB project and available for general use.  These include:

* fgpJava: a convenient way to run a Java class (containing a main method) while auto-configuring the classpath to use all JARs in $GUS_HOME/lib/java
* conifer: a software configuration system based on J2 templates and YAML config vars
* validateXmlWithRng: a way to validate a set of XML documents using a RelaxNG schema

A build produces a set of Java artifacts (JARs), each addressing a utility category.  They are:

* Core: a wide assortment of utility classes providing functionality not addressed in the categories below
* AccountDB: access and configuration of user accounts stored in an AccountDB schema
* Cache: in-memory cache system
* Cli: command line utilities
* Db: database configuration, vendor-specific code, connection pooling
* Events: asynchronous event triggering and subscription
* Json: utilities for reading, validating, typing, and serializing JSON
* Server: extensible Grizzly-based HTTP server implementing JAX-RS
* Web: vendor-neutral HTTP utility classes and interfaces
* Servlet: Servlet-based implementations of the interfaces in Web
* Solr: SOLR response parsing
* Test: support for unit tests
* Xml: XML parsing and validation

FgpUtil also contains a Dependencies directory containing not-generally-available Java libraries in a Maven repository structure.  They can thus be referenced by our pom.xml files as dependencies without including JARs directly in our project repositories.

## Build and Dependencies
### Java
FgpUtil's Java code requires Java 11 or higher and can be built by Maven 3+ directly or as part of the GUS build system (also requiring Maven 3+).  A comprehensive list of its JAR dependencies (and any additional repositories) can be found in the component pom.xml files.

### Scripts
Nearly all scripts in FgpUtil are written in bash or perl, and many are wrappers around Java code.  The perl code in FgpUtil expects Perl 5.x.  To access the scripts, one would typically build FgpUtil using the GUS build system and add $GUS_HOME/bin to their $PATH.
