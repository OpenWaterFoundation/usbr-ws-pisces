package gov.usbr.ws.pisces;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("services")
public class PiscesREST extends ResourceConfig {
	
    public PiscesREST () {
    	// Example of logging to show how to log in Tomcat
    	Logger logger = LoggerFactory.getLogger(this.getClass().getPackage().getName());
    	logger.info("Starting REST web services");
    	// Can hard-code here but prefer to control with logging.properties
    	//logger.setLevel(Level.FINE);
    	
        // Indicate packages that will be scanned for components (services)
        packages("gov.usbr.ws.pisces");
    }
}