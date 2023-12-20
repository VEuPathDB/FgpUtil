# FgpUtil

## Description
Contains a set of utility scripts and software libraries shared across the VEuPathDB project and available for general use.  These include:

* fgpJava: a convenient way to run a Java class (containing a main method) while auto-configuring the classpath to use all JARs in $GUS_HOME/lib/java
* validateXmlWithRng: a way to validate a set of XML documents using a RelaxNG schema

A build produces a set of Java artifacts (JARs), each addressing a utility category.  They are:

* Core: a wide assortment of utility classes providing functionality not addressed in the categories below
* AccountDB: access and configuration of user accounts stored in an AccountDB schema
* Cache: in-memory cache system
* Cli: command line utilities
* Client: HTTP client utilities
* Db: database configuration, vendor-specific code, connection pooling
* Events: asynchronous event triggering and subscription
* Json: utilities for reading, validating, typing, and serializing JSON
* Server: extensible Grizzly-based HTTP server implementing JAX-RS
* Web: vendor-neutral HTTP utility classes and interfaces
* Servlet: Servlet-based implementations of the interfaces in Web
* Solr: SOLR response parsing
* Test: support for unit tests
* Xml: XML parsing and validation

## Build and Dependencies
### Java
FgpUtil's Java code requires Java 11 or higher and can be built by Maven 3+ directly or as part of the GUS build system (also requiring Maven 3+).  It depends on [VEuPathDB's base POM](https://github.com/VEuPathDB/base-pom) for dependency management (version definitions).  To explore individual component dependencies, review the sub-module pom.xml files for those components.

### Scripts
Nearly all scripts in FgpUtil are written in bash or perl, and many are wrappers around Java code.  The perl code in FgpUtil expects Perl 5.x.  To access the scripts, one would typically build FgpUtil using the GUS build system and add $GUS_HOME/bin to their $PATH.
