package gov.usbr.ws.pisces;
 
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
 
/**
 * Resource exposed as /v0/site
 * - ignore case in spelling
 */
@Entity
@Immutable
@Table(name="sitecatalog")
@Path("{site : (?i)site}")
public class Site {
    /**
     * Agency region.
     */
	private String agencyRegion = "";
	
    /**
     * Site identifier.
     */
	private String description = "";
	
    /**
     * (Agency) responsibility.
     */
	private String responsibility = "";
	
    /**
     * Site identifier.
     */
	@Id
	private String siteId = "";
	
    /**
     * Site type.
     */
	private String siteType = "";
	
    /**
     * State.
     */
	private String state = "";
	
    /**
     * Timezone.
     */
	private String timezone = "";
	
	/**
	 * URI for the requested resource.
	 */
	@Context
	@Transient
	private UriInfo uriInfo;

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type for CSV.
     *
     * @return String that will be returned as a text/plain response.
     */
    //@GET
    //@Produces(MediaType.TEXT_PLAIN)
    //@Produces({"text/plain"})
    public Response getSiteCSV(
        @DefaultValue("") @QueryParam("agencyRegion") String agencyRegion,
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("description") String description,
    	//@DefaultValue("") @QueryParam("elevationMin") String elevationMin, // TODO SAM 2015-12-21 not numbers in database
    	//@DefaultValue("") @QueryParam("elevationMax") String elevationMax,
    	@DefaultValue("") @QueryParam("format") String format,
    	//@DefaultValue("") @QueryParam("latitudeMin") String latitudeMin,
    	//@DefaultValue("") @QueryParam("latitudeMax") String latitudeMax,
    	//@DefaultValue("") @QueryParam("longitudeMin") String longitudeMin,
    	//@DefaultValue("") @QueryParam("longitudeMax") String longitudeMax,
    	@DefaultValue("") @QueryParam("responsibility") String responsibility,
    	@DefaultValue("") @QueryParam("siteId") String siteId,
    	@DefaultValue("") @QueryParam("siteType") String siteType,
    	@DefaultValue("") @QueryParam("state") String state,
    	@DefaultValue("") @QueryParam("timezone") String timezone
    	) {
    	StringBuilder csv = new StringBuilder ();
    	
    	boolean doDebug = false;
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<Site> result = readSites ( session, agencyRegion, doDebug, null, description, responsibility, siteId, siteType, state, timezone );
    	session.getTransaction().commit();
    	session.close();
    	
    	csv.append("siteId,description,state,timezone,siteType,responsibility,agencyRegion\n");
    	for ( int i = 0; i < result.size(); i++ ) {
    		Site site = result.get(i);
    		csv.append("\"" + site.getSiteId() + "\",");
    		csv.append("\"" + site.getDescription() + "\",");
    		csv.append("\"" + site.getState() + "\",");
    		//csv.append("\"" + site.getLongitude() + "\",");
    		//csv.append("\"" + site.getLatitude() + "\",");
    		//csv.append("\"" + site.getElevation() + "\",");
    		csv.append("\"" + site.getTimezone() + "\",");
    		csv.append("\"" + site.getSiteType() + "\",");
    		csv.append("\"" + site.getResponsibility() + "\",");
    		csv.append("\"" + site.getAgencyRegion() + "\",");
    		csv.append("\n");
    	}
    	
    	// Build and return the response
    	// - HTTP status code 200 means OK
    	// - Access control settings allow CORS access restricted to GET
        return Response.status(200).
        	entity(csv.toString()).
        	type("text/plain"). // Ensures media type when called from text/html GET request
        	header("Access-Control-Allow-Origin","*").
        	header("Access-Control-Allow-Methods","GET").
        	build();
    }
    
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    //@GET
    //@Produces(MediaType.APPLICATION_JSON)
    public Response getSiteJSON(
        @DefaultValue("") @QueryParam("agencyRegion") String agencyRegion,
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("description") String description,
    	//@DefaultValue("") @QueryParam("elevationMin") String elevationMin, // TODO SAM 2015-12-21 not numbers in database
    	//@DefaultValue("") @QueryParam("elevationMax") String elevationMax,
    	@DefaultValue("") @QueryParam("format") String format,
    	//@DefaultValue("") @QueryParam("latitudeMin") String latitudeMin,
    	//@DefaultValue("") @QueryParam("latitudeMax") String latitudeMax,
    	//@DefaultValue("") @QueryParam("longitudeMin") String longitudeMin,
    	//@DefaultValue("") @QueryParam("longitudeMax") String longitudeMax,
    	@DefaultValue("") @QueryParam("responsibility") String responsibility,
    	@DefaultValue("") @QueryParam("siteId") String siteId,
    	@DefaultValue("") @QueryParam("siteType") String siteType,
    	@DefaultValue("") @QueryParam("state") String state,
    	@DefaultValue("") @QueryParam("timezone") String timezone
    	) {
    	StringBuilder json = new StringBuilder ();
    	
    	boolean doDebug = false;
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<Site> result = readSites ( session, agencyRegion, doDebug, null, description, responsibility, siteId, siteType, state, timezone );
    	session.getTransaction().commit();
    	session.close();
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    	try {
    		json.append(objectMapper.writeValueAsString(result));
    	}
    	catch ( JsonProcessingException e ) {
    		String error = e.toString();
    		return Response.status(500).
	        	entity(error.toString()).
	        	header("Access-Control-Allow-Origin","*").
	        	header("Access-Control-Allow-Methods","GET").
	        	build();
    	}
    	
    	// Build and return the response
    	// - HTTP status code 200 means OK
    	// - Access control settings allow CORS access restricted to GET
        return Response.status(200).
        	entity(json.toString()).
        	type("application/json"). // Ensures media type when called from text/html GET request
        	header("Access-Control-Allow-Origin","*").
        	header("Access-Control-Allow-Methods","GET").
        	build();
    }
    
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/html" media type.
     *
     * @return String that will be returned as a text/html response.
     */
    @GET
    @Produces({"text/html","text/csv","application/json"})
    public Response getSiteHtml(
        @DefaultValue("") @QueryParam("agencyRegion") String agencyRegion,
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("description") String description,
    	//@DefaultValue("") @QueryParam("elevationMin") String elevationMin, // TODO SAM 2015-12-21 not numbers in database
    	//@DefaultValue("") @QueryParam("elevationMax") String elevationMax,
    	@DefaultValue("") @QueryParam("format") String format,
    	//@DefaultValue("") @QueryParam("latitudeMin") String latitudeMin,
    	//@DefaultValue("") @QueryParam("latitudeMax") String latitudeMax,
    	//@DefaultValue("") @QueryParam("longitudeMin") String longitudeMin,
    	//@DefaultValue("") @QueryParam("longitudeMax") String longitudeMax,
    	@DefaultValue("") @QueryParam("responsibility") String responsibility,
    	@DefaultValue("") @QueryParam("siteId") String siteId,
    	@DefaultValue("") @QueryParam("siteType") String siteType,
    	@DefaultValue("") @QueryParam("state") String state,
    	@DefaultValue("") @QueryParam("timezone") String timezone
    	) {
    	// If requested format is different, call the other method
    	if ( format.equalsIgnoreCase("csv") ) {
    		return getSiteCSV(agencyRegion, debug, description, format, responsibility, siteId, siteType, state, timezone);
    	}
    	else if ( format.equalsIgnoreCase("json") ) {
    		return getSiteJSON(agencyRegion, debug, description, format, responsibility, siteId, siteType, state, timezone);
    	}
    	// Create string with HTML output
    	StringBuilder html = new StringBuilder(WebUtil.getPageStart() + "<h1>Site List</h1><p>");
    	html.append("<p>" + WebUtil.getAlternateFormatHTML(uriInfo,null) + "</p>");
    	// Print debug content if requested
    	boolean doDebug = false;
    	if ( debug.equalsIgnoreCase("true") && HibernateUtil.DEBUG_ENABLED ) {
    		doDebug = true;
	    	html.append("DEBUG: agencyRegion=\"" + agencyRegion + "\"<br>" );
	    	html.append("DEBUG: description=\"" + description + "\"<br>" );
	    	html.append("DEBUG: format=\"" + format + "\"<br>" );
	    	html.append("DEBUG: responsibility=\"" + responsibility + "\"<br>" );
	    	html.append("DEBUG: siteId=\"" + siteId + "\"<br>" );
	    	html.append("DEBUG: siteType=\"" + siteType + "\"<br>" );
	    	html.append("DEBUG: state=\"" + state + "\"<br>" );
	    	html.append("DEBUG: timezone=\"" + timezone + "\"<br>" );
    	}

		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<Site> result = readSites ( session, agencyRegion, doDebug, html, description, responsibility, siteId, siteType, state, timezone );
    	session.getTransaction().commit();
    	session.close();
    	
    	html.append("<p>" + result.size() + " sites returned</p>\n<br>\n");
    	html.append("<table>\n");
    	html.append("<tr><th class=\"hasTooltip\">tsmeta<span>Link to time series metadata for this site</span></th>"
    		+ "<th class=\"hasTooltip\">siteId<span>Unique site identifier</span></th>"
    		+ "<th class=\"hasTooltip\">description<span>Description for site</span></th>"
    		+ "<th class=\"hasTooltip\">state<span>Two-character state abbreviation</span></th>"
    		+ "<th class=\"hasTooltip\">timezone<span>Timezone for site</th>"
    		+ "<th class=\"hasTooltip\">siteType<span>Site type - see siteType definitions</a></th>"
    		+ "<th class=\"hasTooltip\">responsibility<span>Organization/office with responsibility for site</span></th>"
    		+ "<th class=\"hasTooltip\">agencyRegion<span>US Bureau of Reclamation region for site</span></th></tr>\n");
    	URI uriRequested = this.uriInfo.getRequestUri();
    	int port = uriRequested.getPort();
    	String host = uriRequested.getHost();
    	String tsmetaLinkPrefix = "http://" + host + ":" + port + WebUtil.WEBROOT_WITH_VERSION;
    	for ( int i = 0; i < result.size(); i++ ) {
    		Site site = result.get(i);
    		html.append("<tr>");
    		html.append("<td><a href=\"" + tsmetaLinkPrefix + "/tsmeta?siteId=" + site.getSiteId() + "\">tsmeta</a></td>");
    		html.append("<td>" + site.getSiteId() + "</td>");
    		html.append("<td>" + site.getDescription() + "</td>");
    		html.append("<td>" + site.getState() + "</td>");
    		//b.append("<td>" + site.getLongitude() + "</td>");
    		//b.append("<td>" + site.getLatitude() + "</td>");
    		//b.append("<td>" + site.getElevation() + "</td>");
    		html.append("<td>" + site.getTimezone() + "</td>");
    		html.append("<td>" + site.getSiteType() + "</td>");
    		html.append("<td>" + site.getResponsibility() + "</td>");
    		html.append("<td>" + site.getAgencyRegion() + "</td>");
    		html.append("</tr>");
    	}
    	html.append("\n</table>\n");
    	html.append("</p>" + WebUtil.getPageEnd() );
    	// Build and return the response
    	// - HTTP status code 200 means OK
    	// - Access control settings allow CORS access restricted to GET
        return Response.status(200).
        	entity(html.toString()).
        	header("Access-Control-Allow-Origin","*").
        	header("Access-Control-Allow-Methods","GET").
        	build();
    }

    /**
     * Return the description.
     */
    public String getAgencyRegion () {
    	return this.agencyRegion;
    }
    
    /**
     * Return the description.
     */
    public String getDescription () {
    	return this.description;
    }
    
    /**
     * Return the responsibility.
     */
    public String getResponsibility () {
    	return this.responsibility;
    }
    
    /**
     * Return the siteid.
     */
    public String getSiteId () {
    	return this.siteId;
    }
    
    /**
     * Return the siteType.
     */
    public String getSiteType () {
    	return this.siteType;
    }
    
    /**
     * Return the state.
     */
    public String getState () {
    	return this.state;
    }
    
    /**
     * Return the timezone.
     */
    public String getTimezone () {
    	return this.timezone;
    }
    
    /**
     * Read a list of sites, shared by each get method.
     */
    protected List<Site> readSites ( Session session, String agencyRegion, boolean doDebug, StringBuilder html, String description,
    	String responsibility, String siteId, String siteType, String state, String timezone ) {
		StringBuilder where = new StringBuilder();
		List<String> whereParameterNames = new ArrayList<String>();
		List<String> whereParameterValues = new ArrayList<String>();
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.agency_region", agencyRegion );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.description", description );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.responsibility", responsibility );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.siteid", siteId );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.state", state );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.timezone", timezone );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.type", siteType );
		//if ( doDebug ) {
		//	html.append("DEBUG: where clause is: " + where + "<br>" );
		//}
	
		SQLQuery query = session.createSQLQuery(
				"select s.siteid as {s.siteId},"
				+ " s.type as {s.siteType},"
				+ " s.description as {s.description},"
				+ " s.state as {s.state},"
				+ " s.timezone as {s.timezone},"
				+ " s.type as {s.siteType},"
				+ " s.responsibility as {s.responsibility},"
				+ " s.agency_region as {s.agencyRegion}"
				+ " from sitecatalog s " + where + " order by s.siteid" );
	   	query.addEntity("s",Site.class);
	   	//if ( doDebug ) {
		//	html.append("DEBUG: query after setup: " + query + "<br>");
		//}
	   	// Now set the query parameter values (this approach avoids SQL injection)
	   	HibernateUtil.setWhereParameters(query,whereParameterNames,whereParameterValues,doDebug,html);
		return query.list();
    }
}