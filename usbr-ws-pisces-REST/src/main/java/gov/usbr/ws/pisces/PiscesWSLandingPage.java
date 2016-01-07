package gov.usbr.ws.pisces;
 
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * Landing page at /v0/
 */
@Path("/")
public class PiscesWSLandingPage {
	
	/**
	 * URI for the requested resource.
	 */
	@Context
	private UriInfo uriInfo;
 
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getLandingPageTxt() {
        return "Reclamation Water Data Web Services";
    }
    
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/html" media type.
     *
     * @return String that will be returned as a text/html response.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getLandingPageHtml() {
    	URI uriRequested = this.uriInfo.getRequestUri();
    	int port = uriRequested.getPort();
    	String host = uriRequested.getHost();
    	String path = "http://" + host + ":" + port + uriRequested.getPath();
    	
    	// Example of logging to show how to log in Tomcat
    	Logger logger = LoggerFactory.getLogger(this.getClass().getPackage().getName());
    	logger.info("Generating web service landing page HTML");
    	
    	if ( port == 80 ) {
    		// Default for normal HTML web traffic so don't need port
    		path = "http://" + host + uriRequested.getPath();
    	}
    	if ( path.endsWith("/") ) {
    		// Trim off trailing / because expect it to not be present below
    		// For example, request for /v0 (top URL) will return /leadingpath/v0
    		// Request for /v0/#tsmeta will return /leadingpath/v0/
    		path = path.substring(0,path.length() - 1);
    	}
        StringBuilder b = new StringBuilder(WebUtil.getPageStart()
        		+ "<h1>Reclamation Water Data Web Services API</h1>"
        		//+ "DEBUG: uriRequested.getPath(): " + uriRequested.getPath()
        		+ "<p>"
        		+ "The water data web services provide access to data for sites and associated time series.  "
        		+ "In some cases the data originate with Reclamation and in other cases Reclamation is republishing data from other data sources."
        		+ "<p>"
        		+ "This page summarizes the water data web services API (application programming interface).  "
        		+ "The API's <a href=\"https://en.wikipedia.org/wiki/Uniform_Resource_Identifier\">Universal Resource Indicator (URI)</a> "
        		+ "strings can be specified in a web browser or other tool to retrieve data in various forms.  "
        		+ "</p>"
        		+ "<h1>Version History</h1>"
        		+ "The API version history is as follows.  The version is part of the URI to allow changes in the future."
        		+ "</p>"
        		+ "<table>"
        		+ "<tr><th>Version</th><th>Release Date</th><th>Comment</th></tr>"
        		+ "<tr><td>v0</td><td>2015-12-24</td><td>Initial version of web services developed by </a href=\"http://openwaterfoundation.org\">Open Water Foundation</a>.</td></tr>"
        		+ "</table>"
        		+ "</p>"
        		+ "<h1>API Syntax Overview</h1>"
        		+ "<p>"
        		+ "The API consists of the following web service resources:"
        		+ "<ul>"
        		+ "<li><a href=\"#definitions\">Definitions</a></li>"
        		+ "<li><a href=\"#site\">Site</a></li>"
        		+ "<li><a href=\"#tsmeta\">Time Series Metadata</a></li>"
        		+ "<li><a href=\"#ts\">Time Series</a></li>"
        		+ "</ul>"
        		+ "</p>"
        		+ "<p>For all web services, URIs are of the form:</p><p>"
        		+ "<code>" + path + "/ServiceName?queryParam1=abc&queryParam2=abc&queryParam3=abc</code></p>"
        		+ "where <code>ServiceName</code> is a web service resource as described in this API and <code>queryParam1</code> (etc.) "
        		+ "are optional query parameters for each web service resource (described below).  The following guidelines apply to URIs:"
        		+ "</p>"
        		+ "<p>"
        		+ "<ul>"
        		+ "<li>resource names and query parameters are case-sensitive</li>"
        		+ "<li>values specified for query parameters are not case-sensitive</li>"
        		+ "<li>for string query parameters, use <code>*</code> at beginning and/or end of query value to match part of the string</li>"
        		+ "<li><code>siteId</code> and <code>parameter</code> can be specified with comma-separated list of values to match</li>"
        		+ "</ul>"
        		+ "</p>"
        		+ "<h1 id=\"definitions\">Definitions</h1>"
        		+ "<p>"
        		+ "The following services return lists of valid values for defined types.  These types are used as query parameters."
        		+ "</p>"
        		+ "<a href=\"" + path + "/siteType\"><code>" + path + "/siteType</code></a> - returns a list of site types (diversion, reservoir, etc.)<br>"
        		+ "<a href=\"" + path + "/parameter\"><code>" + path + "/parameter</code></a> - returns a list of parameters (data types) available as time series at sites<br>"
        		+ "<p>"
        		+ "Optional query parameters are listed in the following table."
        		+ "</p>"
        		+ "<p>"
        		+ "<table>"
        		+ "<tr><th>Parameter</th><th>Description</th><th>Default</th></tr>"
        		+ "<tr><td><code>format</code></td><td>Format of output as one of the following:"
        		+ "<ul><li><code>csv</code> - comma-separated values with header as first row (use header to determine contents of data columns) - useful for importing tables into Excel and other tools</li>"
        		+ "<li><code>html</code> - <a href=\"https://en.wikipedia.org/wiki/HTML\">HyperText Markup Language (HTML)</a> - default used for viewing nicely formatted data</li>"
        		+ "<li><code>json</code> - <a href=\"http://www.json.org/\">Javascript Object Notation (JSON)</a> - useful for software integration</li></ul></td>"
        		+ "</td><td><code>html</code></td></tr>"
        		+ "</table>"
        		+ "</p>"
        		+ "<h1 id=\"site\">Site</h1>"
        		+ "<p>"
        		+ "The following service return lists of sites with associated metadata:"
        		+ "</p>"
        		+ "<a href=\"" + path + "/site\"><code>" + path + "/site</code></a>"
        		+ "</p>"
        		+ "<p>"
        		+ "Optional query parameters are listed in the following table.  The default is to return all sites unless filtered with query parameters.  "
        		+ "The following example retrieves all sites for the state of Idaho:"
        		+ "</p>"
        		+ "<p>"
        		+ "<a href=\"" + path + "/site?state=ID\"><code>" + path + "/site?state=ID</code></a>"
        		+ "</p>"
        		+ "<p>"
        		+ "The following example retrieves all sites for the state of Idaho output in comma-separated-value format:"
        		+ "</p>"
        		+ "<p>"
        		+ "<a href=\"" + path + "/site?state=ID&format=csv\"><code>" + path + "/site?state=ID&format=csv</code></a>"
        		+ "<p>"
        		+ "<table>"
        		+ "<tr><th>Parameter</th><th>Description</th><th>Default</th></tr>"
        		+ "<tr><td><code>agencyRegion</code></td><td><a href=\"http://www.usbr.gov/main/offices.html\">Reclamation agency region</a> (e.g., GP=Great Plains).</td><td>All</td></tr>"
        		+ "<tr><td><code>description</code></td><td>Site description.</td><td>All</td></tr>"
        		+ "<tr><td><code>format</code></td><td><p>Format of output as one of the following:</p><p>"
        		+ "<ul><li><code>csv</code> - comma-separated values with header as first row (use header to determine contents of data columns) - useful for importing tables into Excel and other tools</li>"
        		+ "<li><code>html</code> - <a href=\"https://en.wikipedia.org/wiki/HTML\">HyperText Markup Language (HTML)</a> - default used for viewing nicely formatted data</li>"
        		+ "<li><code>json</code> - <a href=\"http://www.json.org/\">Javascript Object Notation (JSON)</a> - useful for software integration</li></ul></p>"
        		+ "</td><td><code>html</code></td></tr>"
        		+ "<tr><td><code>responsibility</code></td><td>Site responsibility (agency/office that is responsible for site maintenance).</td><td>All</td></tr>"
        		+ "<tr><td><code>siteID</code></td><td>Site identifier.  The site identifier is a unique identifier.  Specify as single identifier, use <code>*</code> at start or end"
        		+ " as wildcard, or specify comma-separated list of <code>siteId</code> values.</td><td>All</td></tr>"
        		+ "<tr><td><code>siteType</code></td><td><a href=\""+path+"/siteType\">Site type.</a></td><td>All</td></tr>"
        		+ "<tr><td><code>state</code></td><td>State abbreviation.</td><td>All</td></tr>"
        		+ "<tr><td><code>timezone</code></td><td>Site time zone.</td><td>All</td></tr>"
        		+ "</table>"
        		+ "</p>"
        		//+ "elevationMin<br>"
        		//+ "elevationMax<br>"
        		//+ "latitudeMin"
        		//+ "latitudeMin"
        		//+ "longitudeMin"
        		//+ "longitudeMax"
        		+ "</p>"
        		+ "<p>"
        		+ "Output includes many of the query parameters listed in the above table, and additionally:"
        		+ "</p>"
        		+ "<ul>"
        		+ "<li><code>tsmeta</code> - URI (active link) for time series metadata corresponding to the site</li>"
        		+ "</ul>"
        		+ "</p>"
        		+ "<h1 id=\"tsmeta\">Time Series Metadata</h1>"
        		+ "<p>"
        		+ "The following service returns lists of time series metadata (units, available period, etc.) associated with sites and time series.  "
        		+ "This service is useful to get a list of time series without the overhead of processing large data sets.  "
        		+ "The service is also useful for simple formats such as comma-separated-value because the time series format does not include metadata."
        		+ "</p>"
        		+ "<p>"
        		+ "<a href=\"" + path + "/tsmeta\"><code>" + path + "/tsmeta</code></a>"
        		+ "</p>"
        		+ "<p>"
        		+ "Optional query parameters are listed in the following table.  The default is to return the metadata for all time series.  "
        		+ "The following example retrieves time series metadata for daily time series for a single site:"
        		+ "</p>"
        		+ "<p>"
        		+ "<a href=\"" + path + "/tsmeta?siteId=abei&interval=day\"><code>" + path + "/tsmeta?siteId=abei&interval=day</code></a>"
        		+ "</p>"
        		+ "<p>"
        		+ "<table>"
        		+ "<tr><th>Parameter</th><th>Description</th><th>Default</th></tr>"
        		+ "<tr><td><code>description</code></td><td>Time series description.</td><td>All</td></tr>"
        		+ "<tr><td><code>interval</code></td><td>Data interval as <code>day</code>, <code>month</code>, or <code>year</code></td><td>All</td></tr>"
        		+ "<tr><td><code>format</code></td><td>Format of output as one of the following:"
        		+ "<ul><li><code>csv</code> - comma-separated values with header as first row (use header to determine contents of data columns) - useful for importing tables into Excel and other tools</li>"
        		+ "<li><code>html</code> - <a href=\"https://en.wikipedia.org/wiki/HTML\">HyperText Markup Language (HTML)</a> - default used for viewing nicely formatted data (currently limited)</li>"
        		+ "<li><code>json</code> - <a href=\"http://www.json.org/\">Javascript Object Notation (JSON)</a> - useful for software integration</li></ul></p>"
        		+ "</td><td><code>html</code></td></tr>"
        		+ "<tr><td><code>notes</code></td><td>Notes.</td><td>All</td></tr>"
        		+ "<tr><td><code>parameter</code></td><td><a href=\"" + path + "/parameter\">Parameter (data type).</a>  Specify as single parameter, use <code>*</code> at start or end"
        		+ " as wildcard, or specify comma-separated list of <code>parameter</code> values.</td><td>All</td></tr>"
        		+ "<tr><td><code>provider</code></td><td>Data provider (system).</td><td>All</td></tr>"
        		+ "<tr><td><code>server</code></td><td>Data server (regional office database).</td><td>All</td></tr>"
        		+ "<tr><td><code>siteID</code></td><td>Site identifier.  The site identifier is a unique identifier.  Specify as single identifier, use <code>*</code> at start or end"
        		+ " as wildcard, or specify comma-separated list of <code>siteId</code> values.</td><td>All</td></tr>"
        		+ "<tr><td><code>units</code></td><td>Data units.</td><td>All</td></tr>"
        		+ "</table>"
        		+ "</p>"
        		+ "<p>"
        		+ "Output includes many of the query parameters listed in the above table, and additionally:"
        		+ "</p>"
        		+ "<ul>"
        		+ "<li><code>ts</code> - URI (active link) for time series corresponding to the metadata</li>"
        		+ "<li><code>tsid</code> - period-delimited concatenation of <code>siteId</code>, <code>server</code>, <code>parameter</code>, and <code>interval</code>, which provides a unique identifier for time series</li>"
        		+ "</ul>"
        		+ "</p>"
        		+ "<h1 id=\"ts\">Time Series</h1>"
        		+ "<p>"
        		+ "The following service returns lists of time series associated with sites.  For simple formats such as comma-separated-value the "
        		+ "result may omit metadata and consequently the <a href=\"#tsmeta\">time series metadata</a> service may be needed.  "
        		+ "For complex formats such as WaterML and JSON, the time series format includes metadata."
        		+ "</p>"
        		+ "<p>"
        		+ "<a href=\"" + path + "/ts\"><code>" + path + "/ts</code></a>"
        		+ "</p>"
        		+ "<p>"
        		+ "Optional query parameters are listed in the following table.  The default is to return all time series (may be limited on server due to large amount of data).  "
        		+ "The following example retrieves daily time series for a single site:"
        		+ "</p>"
        		+ "<p>"
        		+ "<a href=\"" + path + "/ts?siteId=abei&interval=day\"><code>" + path + "/ts?siteId=abei&interval=day</code></a>"
        		+ "</p>"
        		+ "<p>"
        		+ "<table>"
        		+ "<tr><th>Parameter</th><th>Description</th><th>Default</th></tr>"
        		+ "<tr><td><code>dataFlag</code></td><td>Data flag - if <code>false</code>, don't include flag column in <code>csv</code> and <code>html</code> format output.  Do include always in formats where flag can be included cleanly.</td><td><code>true</code></td></tr>"
        		+ "<tr><td><code>description</code></td><td>Time series description.</td><td>All</td></tr>"
        		+ "<tr><td><code>interval</code></td><td>Data interval as <code>day</code>, <code>month</code>, or <code>year</code></td><td>All (<b>curently only <code>day</code> is supported</b>)</td></tr>"
        		+ "<tr><td><code>format</code></td><td>Format of output as one of the following:"
        		+ "<ul><li><code>csv</code> - comma-separated values with header as first row (use header to determine contents of data columns) - useful for importing tables into Excel and other tools</li>"
        		+ "<li><code>html</code> - <a href=\"https://en.wikipedia.org/wiki/HTML\">HyperText Markup Language (HTML)</a> - default used for viewing nicely formatted data</li>"
        		//+ "<li><code>hydrojson</code> - <a href=\"https://github.com/gunnarleffler/hydroJSON\">HydroJSON</a> - experimental JSON format</li>"
        		+ "<li><code>json</code> - <a href=\"http://www.json.org/\">Javascript Object Notation (JSON)</a> - useful for software integration</li>"
        		//+ "<li><code>waterml20</code> - <a href=\"http://www.opengeospatial.org/standards/waterml\">WaterML 2.0</a> - XML format often used by federal agencies for data exchange</li></ul></td>"
        		+ "<li><code>waterml20minimal</code> - <a href=\"http://www.opengeospatial.org/standards/waterml\">WaterML 2.0</a> - XML format often used by federal agencies for data exchange - "
        		+ "<b>this web service implements a minimal version of WaterML 2.0 only including some elements</b></li>"
        		+ "<li><code>waterml20minimaljson</code> - JSON version of <code>waterml20minimal</code></li>"
        		+ "</ul></td>"
        		+ "</td><td><code>html</code></td></tr>"
        		+ "<tr><td><code>notes</code></td><td>Notes.</td><td>All</td></tr>"
        		+ "<tr><td><code>parameter</code></td><td><a href=\"" + path + "/parameter\">Parameter (data type)</a>.  Specify as single parameter, use <code>*</code> at start or end"
        		+ " as wildcard, or specify comma-separated list of <code>parameter</code> values.</td><td>All</td></tr>"
        		+ "<tr><td><code>periodStart</code></td><td>Start of period for query.  The format of the date/time string is as follows and should be specified with precision that "
        		+ "is consistent with the <code>interval</code> query parameter:"
        		+ "<ul>"
        		+ "<li><code>YYYY-MM-DD</code> - day interval</li>"
        		+ "<li><code>YYYY-MM</code> - month interval</li>"
        		+ "<li><code>YYYY</code> - year interval</li>"
        		+ "</ul></td><td>Start of period is unbounded - return all available data.</td></tr>"
        		+ "<tr><td><code>periodEnd</code></td><td>End of period for query.  See <code>periodStart</code> for format.</td><td>End of period is unbounded - return all available data.</td></tr>"
        		+ "<tr><td><code>provider</code></td><td>Data provider (system).</td><td>All</td></tr>"
        		+ "<tr><td><code>server</code></td><td>Data server (regional office database).</td><td>All</td></tr>"
        		+ "<tr><td><code>siteID</code></td><td>Site identifier.  The site identifier is a unique identifier.  Specify as single identifier, use <code>*</code> at start or end"
        		+ " as wildcard, or specify comma-separated list of <code>siteId</code> values.</td><td>All</td></tr>"
        		+ "<tr><td><code>units</code></td><td>Data units.</td><td>All</td></tr>"
        		+ "</table>"
        		+ "</p>"
        		+ "</p>"
        		+ "Output includes many of the query parameters listed in the above table, and additionally:"
        		+ "</p>"
        		+ "<ul>"
        		
        		+ "</ul>"
        		+ "</p>"
        		+ WebUtil.getPageEnd() );
        return Response.status(200).
        	entity(b.toString()).
        	header("Access-Control-Allow-Origin","*").
        	header("Access-Control-Allow-Methods","GET").
        	build();
    }
}