# usbr-ws-pisces
Java REST web services for Pisces database, which provides access to the United States Bureau of Reclamation's Hydromet and HDB database contents.  The following technologies are used:

1. MySQL (Pisces database implementation)
2. Java 8 (to support various components)
2. Hibernate (for persistence - read from Pisces database)
3. Tomcat 8 (servlet container)
4. Jersey (for REST web service implementation)
5. Jackson (for JSON serialization)
6. WaterML 2.0 (attempt to use Maven artifacts - needs more work)

# Documentation

See the documentation under the "doc" folder.  The  appendix in the documentation describes how the project was initialized.  The main body of the documentation describes how to set up a development environment from this repository.

# Using REST Web Services

When implemented in an operational system, access the web service API documentation with, for example:  http://localhost:8080/usbr-ws-pisces/v0.

# Developer Information

The initial project was developed by Steve Malers at the Open Water Foundation in collaboration with Karl Tarbet at Reclamation, and delivered in January 2016.  We will see how additional work progresses...