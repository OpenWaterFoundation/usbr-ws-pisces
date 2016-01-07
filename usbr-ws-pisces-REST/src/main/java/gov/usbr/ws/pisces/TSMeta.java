package gov.usbr.ws.pisces;
 
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
 
/**
 * Resource exposed as /v0/tsmeta
 * - ignore case in spelling
 * 
 * Note that this class uses a composite identifier of siteId, server, parameter, and interval.
 * If not handled queries may show duplicate records, not actual values as expected.
 * Multiple Id annotations are used and the class implements serializable.
 */
@Entity
@Immutable
@Table(name="view_seriescatalog")
@Path("{tsmeta : (?i)tsmeta}")
@JsonIgnoreProperties({"timeinterval", "tablename"})
public class TSMeta implements Serializable {
	private static final long serialVersionUID = 42L; // Will be recalculated by Hibernate
	
    /**
     * Data count.
     */
	private String count = "";
	
    /**
     * Site identifier.
     */
	private String description = "";
	
    /**
     * Data interval (timestep).
     */
	@Column(name="timeinterval")
	private String timeinterval = "";
	
    /**
     * Data interval (timestep) converted to generalized form ("Daily" converted to "day", etc.).
     */
	@Transient
	@Id
	private String interval = "";
	
    /**
     * Notes.
     */
	private String notes = "";
	
    /**
     * Data parameter.
     */
	@Id
	private String parameter = "";

    /**
     * Data period end.
     */
	@Column(name="t2")
	private String periodEnd = "";
	
    /**
     * Data period start.
     */
	@Column(name="t1")
	private String periodStart = "";
	
    /**
     * (Agency) provider.
     */
	private String provider = "";
	
    /**
     * Server (for data management).
     */
	@Id
	private String server = "";
	
    /**
     * Site identifier.
     */
	@Id
	@Column(name="siteid")
	private String siteId = "";
	
    /**
     * Table name.
     */
	private String tablename = "";
	
    /**
     * Time series identifier (similar to TSTool use).
     */
	@Transient
	private String tsid = "";
	
    /**
     * Data units.
     */
	private String units = "";
	
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
    public Response getTSMetaCSV(
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("description") String description,
    	@DefaultValue("") @QueryParam("format") String format,
    	@DefaultValue("") @QueryParam("interval") String interval,
    	@DefaultValue("") @QueryParam("notes") String notes,
    	@DefaultValue("") @QueryParam("parameter") String parameter,
    	@DefaultValue("") @QueryParam("provider") String provider,
    	@DefaultValue("") @QueryParam("server") String server,
    	@DefaultValue("") @QueryParam("siteId") String siteId,
    	@DefaultValue("") @QueryParam("units") String units
    	) {
    	StringBuilder csv = new StringBuilder ();
    	
    	boolean doDebug = false;
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<TSMeta> result = readTSMeta ( session, doDebug, null, description, interval, notes,
    	    parameter, provider, server, siteId, units );
    	session.getTransaction().commit();
    	session.close();
    	
    	csv.append("siteId,description,server,provider,parameter,interval,units,periodStart,periodEnd,count,tsid,notes\n");
    	for ( int i = 0; i < result.size(); i++ ) {
    		TSMeta tsmeta = result.get(i);
    		csv.append("\"" + tsmeta.getSiteId() + "\",");
    		csv.append("\"" + tsmeta.getDescription() + "\",");
    		csv.append("\"" + tsmeta.getServer() + "\",");
    		csv.append("\"" + tsmeta.getProvider() + "\",");
    		csv.append("\"" + tsmeta.getParameter() + "\",");
    		csv.append("\"" + HibernateUtil.getIntervalExternal(tsmeta.getInterval()) + "\",");
    		csv.append("\"" + tsmeta.getUnits() + "\",");
    		csv.append("\"" + tsmeta.getPeriodStart() + "\",");
    		csv.append("\"" + tsmeta.getPeriodEnd() + "\",");
    		csv.append("" + tsmeta.getCount() + ",");
    		csv.append("\"" + tsmeta.getTsid() + "\",");
    		csv.append("\"" + tsmeta.getNotes() + "\"");
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
    public Response getTSMetaJSON(
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("description") String description,
    	@DefaultValue("") @QueryParam("format") String format,
    	@DefaultValue("") @QueryParam("interval") String interval,
    	@DefaultValue("") @QueryParam("notes") String notes,
    	@DefaultValue("") @QueryParam("parameter") String parameter,
    	@DefaultValue("") @QueryParam("provider") String provider,
    	@DefaultValue("") @QueryParam("server") String server,
    	@DefaultValue("") @QueryParam("siteId") String siteId,
    	@DefaultValue("") @QueryParam("units") String units
    	) {
    	StringBuilder json = new StringBuilder ();
    	
    	boolean doDebug = false;
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<TSMeta> tsmetaList = readTSMeta ( session, doDebug, null, description, interval, notes,
    	    parameter, provider, server, siteId, units );
    	session.getTransaction().commit();
    	session.close();
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    	try {
    		json.append(objectMapper.writeValueAsString(tsmetaList));
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
    public Response getTSMetaHtml(
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("description") String description,
    	@DefaultValue("") @QueryParam("format") String format,
    	@DefaultValue("") @QueryParam("interval") String interval,
    	@DefaultValue("") @QueryParam("notes") String notes,
    	@DefaultValue("") @QueryParam("parameter") String parameter,
    	@DefaultValue("") @QueryParam("provider") String provider,
    	@DefaultValue("") @QueryParam("server") String server,
    	@DefaultValue("") @QueryParam("siteId") String siteId,
    	@DefaultValue("") @QueryParam("units") String units
    	) {
    	// If requested format is different, call the other method
    	if ( format.equalsIgnoreCase("csv") ) {
    		return getTSMetaCSV(debug, description, format, interval, notes, parameter, provider, server, siteId, units);
    	}
    	else if ( format.equalsIgnoreCase("json") ) {
    		return getTSMetaJSON(debug, description, format, interval, notes, parameter, provider, server, siteId, units);
    	}
    	// Create string with HTML output
    	StringBuilder html = new StringBuilder(WebUtil.getPageStart() + "<h1>Time Series Metadata List</h1><p>");
    	html.append("<p>" + WebUtil.getAlternateFormatHTML(uriInfo,null) + "</p>");
    	// Print debug content if requested
    	boolean doDebug = false;
    	if ( debug.equalsIgnoreCase("true") && HibernateUtil.DEBUG_ENABLED ) {
    		doDebug = true;
	    	html.append("DEBUG: description=\"" + description + "\"<br>" );
	    	html.append("DEBUG: format=\"" + format + "\"<br>" );
	    	html.append("DEBUG: interval=\"" + interval + "\"<br>" );
	    	html.append("DEBUG: notes=\"" + notes + "\"<br>" );
	    	html.append("DEBUG: parameter=\"" + parameter + "\"<br>" );
	    	html.append("DEBUG: provider=\"" + provider + "\"<br>" );
	    	html.append("DEBUG: server=\"" + server + "\"<br>" );
	    	html.append("DEBUG: siteId=\"" + siteId + "\"<br>" );
	    	html.append("DEBUG: units=\"" + units + "\"<br>" );
    	}

		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<TSMeta> result = readTSMeta ( session, doDebug, html, description, interval, notes,
    	    	parameter, provider, server, siteId, units );
    	session.getTransaction().commit();
    	session.close();
    	
    	html.append("<p>" + result.size() + " time series metadata returned</p>\n<br>\n");
    	html.append("<table>\n");
    	html.append("<tr><th class=\"hasTooltip\">ts<span>Link to time series for this metadata</span></th>"
    		+ "<th class=\"hasTooltip\">siteId<span>Unique site identifier</span></th>"
    		+ "<th class=\"hasTooltip\">description<span>Description for time series</span></th>"
    		+ "<th class=\"hasTooltip\">server<span>Reclamation server (source) for data</span></th>"
    		+ "<th class=\"hasTooltip\">provider<span>Data provider/generator (Reclamation convention)</span></th>"
    		+ "<th class=\"hasTooltip\">parameter<span>Parameter (data type) name - see parameter web service for available list</span></th>"
    		+ "<th class=\"hasTooltip\">interval<span>Data interval</span></th>"
    		+ "<th class=\"hasTooltip\">units<span>Data units</span></th>"
    		+ "<th class=\"hasTooltip\">periodStart<span>Available data period start</span></th>"
    		+ "<th class=\"hasTooltip\">periodEnd<span>Available data period end</span></th>"
    		+ "<th class=\"hasTooltip\">count<span>Count of nonmissing values in data period</span></th>"
    		+ "<th class=\"hasTooltip\">tsid<span>Unique time series identifier as period-delimited concatenation of siteId, server, parameter, and interval</th>"
    		+ "<th class=\"hasTooltip\">notes<span>Notes about time series</span></th></tr>\n");
    	URI uriRequested = this.uriInfo.getRequestUri();
    	int port = uriRequested.getPort();
    	String host = uriRequested.getHost();
    	String tsLinkPrefix = "http://" + host + ":" + port + WebUtil.WEBROOT_WITH_VERSION;
    	for ( int i = 0; i < result.size(); i++ ) {
    		TSMeta tsmeta = result.get(i);
    		html.append("<tr>");
    		html.append("<td><a href=\"" + tsLinkPrefix + "/ts?siteId=" + tsmeta.getSiteId() + "&parameter=" +
    			tsmeta.getParameter() + "&server=" + tsmeta.getServer() + "&interval=" + tsmeta.getInterval() +
    			"\">ts</a></td>");
    		html.append("<td>" + tsmeta.getSiteId() + "</td>");
    		html.append("<td>" + tsmeta.getDescription() + "</td>");
    		html.append("<td>" + tsmeta.getServer() + "</td>");
    		html.append("<td>" + tsmeta.getProvider() + "</td>");
    		html.append("<td>" + tsmeta.getParameter() + "</td>");
    		html.append("<td>" + HibernateUtil.getIntervalExternal(tsmeta.getInterval()) + "</td>");
    		html.append("<td>" + tsmeta.getUnits() + "</td>");
    		html.append("<td>" + tsmeta.getPeriodStart() + "</td>");
    		html.append("<td>" + tsmeta.getPeriodEnd() + "</td>");
    		html.append("<td>" + tsmeta.getCount() + "</td>");
    		html.append("<td>" + tsmeta.getTsid() + "</td>");
    		html.append("<td>" + tsmeta.getNotes() + "</td>");
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
     * Return the data count.
     */
    public String getCount () {
    	return this.count;
    }

    /**
     * Return the description.
     */
    public String getDescription () {
    	return this.description;
    }
    
    /**
     * Return the interval (external convention like "day").
     */
    public String getInterval () {
    	return this.interval;
    }
    
    /**
     * Return the notes.
     */
    public String getNotes () {
    	return this.notes;
    }
    
    /**
     * Return the parameter.
     */
    public String getParameter () {
    	return this.parameter;
    }

    /**
     * Return the period end.
     */
    public String getPeriodEnd () {
    	return this.periodEnd;
    }
    
    /**
     * Return the period start.
     */
    public String getPeriodStart () {
    	return this.periodStart;
    }
    
    /**
     * Return the provider.
     */
    public String getProvider () {
    	return this.provider;
    }
    
    /**
     * Return the server.
     */
    public String getServer () {
    	return this.server;
    }
    
    /**
     * Return the siteid.
     */
    public String getSiteId () {
    	return this.siteId;
    }
    
    /**
     * Return the tablename.
     */
    public String getTablename () {
    	return this.tablename;
    }
    
    /**
     * Return the interval (internal convention like "Daily").
     */
    public String getTimeinterval () {
    	return this.timeinterval;
    }
    
    /**
     * Return the time series identifier.
     */
    public String getTsid () {
    	return this.tsid;
    }
    
    /**
     * Return the data units.
     */
    public String getUnits () {
    	return this.units;
    }
    
    /**
     * Read a list of TSMeta, shared by each get method.
     */
    protected List<TSMeta> readTSMeta ( Session session, boolean doDebug, StringBuilder html, String description, String interval, String notes,
    	String parameter, String provider, String server, String siteId, String units ) {
    	String timeinterval = HibernateUtil.getIntervalInternal(interval);
		StringBuilder where = new StringBuilder();
		List<String> whereParameterNames = new ArrayList<String>();
		List<String> whereParameterValues = new ArrayList<String>();
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.description", description );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.timeinterval", timeinterval );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.notes", notes );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.parameter", parameter );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.provider", provider );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.server", server );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.siteid", siteId );
		HibernateUtil.appendWhere ( where, whereParameterNames, whereParameterValues, "s.units", units );
		if ( doDebug && html != null ) {
			html.append("DEBUG: where clause is: " + where + "<br>" );
		}
	
		String where2 = " where ";
		if ( where.length() > 0 ) {
			where2 = " and ";
		}
		SQLQuery query = session.createSQLQuery(
				"select s.siteid as {s.siteId},"
				+ " s.name as {s.description},"
				+ " s.units as {s.units},"
				+ " s.timeinterval as {s.timeinterval},"
				+ " s.parameter as {s.parameter},"
				+ " s.provider as {s.provider},"
				+ " s.server as {s.server},"
				+ " s.notes as {s.notes},"
				+ " s.t1 as {s.periodStart},"
				+ " s.t2 as {s.periodEnd},"
				+ " s.count as {s.count},"
				+ " s.tablename as {s.tablename}"
				+ " from view_seriescatalog s " + where
				+ where2 + " s.siteid <> '' order by s.siteid, s.server, s.parameter, s.timeinterval" );
	   	query.addEntity("s",TSMeta.class);
	   	if ( doDebug && html != null ) {
			html.append("DEBUG: query after setup: " + query + "<br>");
		}
	   	// Now set the query parameter values (this approach avoids SQL injection)
	   	HibernateUtil.setWhereParameters(query,whereParameterNames,whereParameterValues,doDebug,html);
		List<TSMeta> result = query.list();
		// Loop through data and convert database interval ("Daily") to normalized interval (e.g., "day")
		// Otherwise JSON serialization will show the original
		// Also create TSID
		for ( TSMeta tsmeta : result ) {
			tsmeta.interval = HibernateUtil.getIntervalExternal(tsmeta.timeinterval);
			tsmeta.tsid = tsmeta.siteId + "." + tsmeta.server + "." + tsmeta.parameter + "." + tsmeta.interval;
		}
		return result;
    }
    
    /**
     * Set the interval (external convention like "day").
     * @param interval time series data interval.
     */
    public void setInterval ( String interval ) {
    	this.interval = interval;
    }
}