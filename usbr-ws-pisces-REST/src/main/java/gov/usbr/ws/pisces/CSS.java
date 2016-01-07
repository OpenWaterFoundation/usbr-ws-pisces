package gov.usbr.ws.pisces;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
 
/**
 * Resource exposed at "/v0/ws.css" path).
 * Provide style sheet to use with the HTML pages for web service.
 * Would like to serve static resources another way.
 * Hard-coded CSS because can't figure out how to pass back static resources.
 * -see:  http://stackoverflow.com/questions/31883928/spring-jersey-how-to-return-static-content
 * - Started working with this in web-filter-notworking.xml but need to move on
 */
// TODO SAM 2015-12-21 Decide whether need this...if so move the select statement to after annotation
//@Subselect
@Path("ws.css")
public class CSS {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/css" media type.
     *
     * @return String that will be returned as a text/css response.
     */
    @GET
    @Produces({"text/css"})
    public Response getCss() {
    	StringBuilder b = new StringBuilder(
    			"/*\n"
    			+"Styles for Reclamation web services HTML pages\n"
    			+"- Put html defaults at top\n"
    			+"- Override with additional styles below\n"
    			+"*/\n"
    			+"html *\n"
    			+"{\n"
    			+"	font-family:  \"Arial\", Helvetica, sans-serif;\n"
    			+"}\n"
    			+"code {\n"
    			+"	font-family:  \"Courier New\", Courier, monospace;\n"
    			+"}\n"
    			+"h1 {\n"
    			+"	font-family:  \"Tahoma\", Geneva, sans-serif;\n"
    			+"}\n"
    			+"h2 {\n"
    			+"	font-family:  \"Tahoma\", Geneva, sans-serif;\n"
    			+"}\n"
    			+"p {\n"
    			+"	font-family:  \"Arial\", Helvetica, sans-serif;\n"
    			+"}\n"
    			+"table {\n"
    			+"	border-collapse: collapse;\n"
    			+"}\n"
    			+"table, th, td {\n"
    			+"	border: 1px solid black;\n"
    			+"}\n"
    			+"td.tabnum {\n"
    			+"	text-align: right;\n"
    			+"}\n"
    			+".hasTooltip span {"
    			+"  display: none;\n"
    			+"  color: #000;\n"
    			+"  text-decoration: none;\n"
    			+"  padding: 3px;\n"
    			+"}\n"
    			+".hasTooltip:hover span {"
    			+"  display: block;\n"
    			+"  position: absolute;\n"
    			+"  background-color: #FFF;\n"
    			+"  border: 1px solid #CCC;\n"
    			+"  margin: 2px 10px;\n"
    			+"}\n"
    			+"ul {\n"
    			+"	list-style-type: square;\n"
    			+"}\n"
    	);
        return Response.status(200).
        	entity(b.toString()).
        	header("Access-Control-Allow-Origin","*").
        	header("Access-Control-Allow-Methods","GET").
        	build();
    }
}