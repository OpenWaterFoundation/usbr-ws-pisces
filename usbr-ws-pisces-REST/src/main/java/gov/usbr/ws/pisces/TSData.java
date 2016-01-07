package gov.usbr.ws.pisces;
 
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
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
 * Resource exposed as /v0/tsdata
 * - ignore case in spelling
 */
@Entity
@Immutable
@Path("{tsdata : (?i)tsdata}")
public class TSData {
    /**
     * Datetime.
     */
	@Id
	private Date datetime = null;
	
    /**
     * Site identifier.
     */
	private Double value = null;
	
    /**
     * Flag.
     */
	private String flag = "";

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
    public Response getTSDataCSV(
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("format") String format,
    	@DefaultValue("") @QueryParam("periodStart") String periodStart,
    	@DefaultValue("") @QueryParam("periodEnd") String periodEnd,
    	@DefaultValue("") @QueryParam("tablename") String tablename
    	) {
    	// If table name is not specified cannot perform request
    	if ( tablename.equals("") ) {
            return Response.status(500).
                	entity("ERROR: must specify tablename").
                	header("Access-Control-Allow-Origin","*").
                	header("Access-Control-Allow-Methods","GET").
                	build();
    	}
    	StringBuilder csv = new StringBuilder ();
    	
    	boolean doDebug = false;
    	
    	// Parse the period to query if specified
    	Date queryStart = HibernateUtil.parseDatetime(periodStart);
    	if ( !periodStart.isEmpty() && queryStart == null ) {
    		return Response.status(500).
    	        	entity("ERROR: error parsingStart - need to be format YYYY, YYYY-MM, or YYYY-MM-DD depending on interval" ).
    	        	header("Access-Control-Allow-Origin","*").
    	        	header("Access-Control-Allow-Methods","GET").
    	        	build();
    	}
    	Date queryEnd = HibernateUtil.parseDatetime(periodEnd);
    	if ( !periodEnd.isEmpty() && queryEnd == null ) {
    		return Response.status(500).
    	        	entity("ERROR: error parsingEnd - need to be format YYYY, YYYY-MM, or YYYY-MM-DD depending on interval" ).
    	        	header("Access-Control-Allow-Origin","*").
    	        	header("Access-Control-Allow-Methods","GET").
    	        	build();
    	}
    	
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<TSData> result = readTSData ( session, doDebug, queryStart, queryEnd, tablename );
    	session.getTransaction().commit();
    	session.close();
    	
    	csv.append("datetime,value,flag\n");
    	DecimalFormat valueFormat = new DecimalFormat("###.00");
    	Double value;
    	for ( int i = 0; i < result.size(); i++ ) {
    		TSData tsdata = result.get(i);
    		csv.append("\"" + tsdata.getDatetime() + "\",");
    		value = tsdata.getValue();
    		if ( (value == null) || value.isNaN() ) {
    			csv.append("");
    		}
    		else {
    			csv.append(valueFormat.format(value));
    		}
    		csv.append(",\"" + tsdata.getFlag() + "\",");
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
    public Response getTSDataJSON(
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("format") String format,
    	@DefaultValue("") @QueryParam("periodStart") String periodStart,
    	@DefaultValue("") @QueryParam("periodEnd") String periodEnd,
    	@DefaultValue("") @QueryParam("tablename") String tablename
    	) {
    	// If table name is not specified cannot perform request
    	if ( tablename.equals("") ) {
            return Response.status(500).
                	entity("ERROR: must specify tablename").
                	header("Access-Control-Allow-Origin","*").
                	header("Access-Control-Allow-Methods","GET").
                	build();
    	}
    	StringBuilder json = new StringBuilder ();
    	
    	boolean doDebug = false;
    	
    	// Parse the period to query if specified
    	Date queryStart = HibernateUtil.parseDatetime(periodStart);
    	if ( !periodStart.isEmpty() && queryStart == null ) {
    		return Response.status(500).
    	        	entity("ERROR: error parsingStart - need to be format YYYY, YYYY-MM, or YYYY-MM-DD depending on interval" ).
    	        	header("Access-Control-Allow-Origin","*").
    	        	header("Access-Control-Allow-Methods","GET").
    	        	build();
    	}
    	Date queryEnd = HibernateUtil.parseDatetime(periodEnd);
    	if ( !periodEnd.isEmpty() && queryEnd == null ) {
    		return Response.status(500).
    	        	entity("ERROR: error parsingEnd - need to be format YYYY, YYYY-MM, or YYYY-MM-DD depending on interval" ).
    	        	header("Access-Control-Allow-Origin","*").
    	        	header("Access-Control-Allow-Methods","GET").
    	        	build();
    	}
    	
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<TSData> result = readTSData ( session, doDebug, queryStart, queryEnd, tablename );
    	session.getTransaction().commit();
    	session.close();
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    	objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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
    public Response getTSDataHtml(
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("format") String format,
    	@DefaultValue("") @QueryParam("periodStart") String periodStart,
    	@DefaultValue("") @QueryParam("periodEnd") String periodEnd,
    	@DefaultValue("") @QueryParam("tablename") String tablename
    	) {
    	// If requested format is different, call the other method
    	if ( format.equalsIgnoreCase("csv") ) {
    		return getTSDataCSV(debug, format, periodStart, periodEnd, tablename);
    	}
    	else if ( format.equalsIgnoreCase("json") ) {
    		return getTSDataJSON(debug, format, periodStart, periodEnd, tablename);
    	}
    	
    	// Create string with HTML output
    	StringBuilder html = new StringBuilder(WebUtil.getPageStart() + "<h1>Time Series Data (data table only)</h1><p>");
    	html.append("<p>" + WebUtil.getAlternateFormatHTML(uriInfo,null) + "</p>");
    	
    	boolean doDebug = false;
    	if ( debug.equalsIgnoreCase("true") && HibernateUtil.DEBUG_ENABLED ) {
    		doDebug = true;
	    	html.append("DEBUG: format=\"" + format + "\"<br>" );
	    	html.append("DEBUG: periodStart=\"" + periodStart + "\"<br>" );
	    	html.append("DEBUG: periodEnd=\"" + periodEnd + "\"<br>" );
	    	html.append("DEBUG: tablename=\"" + tablename + "\"<br>" );
    	}
    	
    	// If table name is not specified cannot perform request
    	if ( tablename.equals("") ) {
    		html.append("<p>ERROR:  Must specify tablename.</p>");
    		html.append(WebUtil.getPageEnd() );
            return Response.status(500).
                	entity(html.toString()).
                	header("Access-Control-Allow-Origin","*").
                	header("Access-Control-Allow-Methods","GET").
                	build();
    	}
    	
    	// Parse the period to query if specified
    	Date queryStart = HibernateUtil.parseDatetime(periodStart);
    	if ( !periodStart.isEmpty() && queryStart == null ) {
    		return Response.status(500).
    	        	entity("ERROR: error parsingStart - need to be format YYYY, YYYY-MM, or YYYY-MM-DD depending on interval" ).
    	        	header("Access-Control-Allow-Origin","*").
    	        	header("Access-Control-Allow-Methods","GET").
    	        	build();
    	}
    	Date queryEnd = HibernateUtil.parseDatetime(periodEnd);
    	if ( !periodEnd.isEmpty() && queryEnd == null ) {
    		return Response.status(500).
    	        	entity("ERROR: error parsingEnd - need to be format YYYY, YYYY-MM, or YYYY-MM-DD depending on interval" ).
    	        	header("Access-Control-Allow-Origin","*").
    	        	header("Access-Control-Allow-Methods","GET").
    	        	build();
    	}

		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<TSData> result = readTSData ( session, doDebug, queryStart, queryEnd, tablename );
    	session.getTransaction().commit();
    	session.close();
    	
    	html.append("<p>" + result.size() + " time series data records returned for table \"" + tablename + "\"</p>\n<br>\n");
    	html.append("<tr><table>\n");
    	html.append("<th class=\"hasTooltip\">datetime<span>Date/time for time series value, where precision of date/time is general</span></th>"
    		+ "<th class=\"hasTooltip\">value<span>Data value for time series corresponding to datetime</span></th>"
    		+ "<th class=\"hasTooltip\">flag<span>Data flag for time series corresponding to datetime</span></th></tr>\n");
    	TSData tsdata;
    	DecimalFormat valueFormat = new DecimalFormat("###.00");
    	Double value;
    	for ( int i = 0; i < result.size(); i++ ) {
    		tsdata = result.get(i);
    		html.append("<tr>");
    		html.append("<td>" + tsdata.getDatetime() + "</td>");
    		value = tsdata.getValue();
    		if ( (value == null) || value.isNaN() ) {
    			html.append("<td></td>");
    		}
    		else {
    			html.append("<td class=\"tabnum\">" + valueFormat.format(value) + "</td>");
    		}
    		html.append("<td>" + tsdata.getFlag() + "</td>");
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
     * Return the date/time.
     */
    public Date getDatetime () {
    	return this.datetime;
    }

    /**
     * Return the flag.
     */
    public String getFlag () {
    	return this.flag;
    }
    
    /**
     * Return the data value.
     */
    public Double getValue () {
    	return this.value;
    }
    
    /**
     * Read a list of TSData, shared by each get method.
     */
    protected List<TSData> readTSData ( Session session, boolean doDebug, Date queryStart, Date queryEnd, String tablename ) {
		StringBuilder where = new StringBuilder();
		if ( queryStart != null ) {
			where.append ( " where t.datetime >= ?" );
		}
		if ( queryEnd != null ) {
			if ( where.length() > 0 ) {
				where.append(" and t.datetime <= ?" );
			}
			else {
				where.append(" where t.datetime <= ?" );
			}
		}
	
		SQLQuery query = session.createSQLQuery(
				"select t.datetime as {t.datetime},"
				+ " t.value as {t.value},"
				+ " t.flag as {t.flag}"
				+ " from " + tablename + " t " + where
				+ " order by t.datetime" );
	   	query.addEntity("t",TSData.class);
	   	int i = 0;
		if ( queryStart != null ) {
			query.setParameter(i++, queryStart);
		}
		if ( queryEnd != null ) {
			query.setParameter(i++, queryEnd);
		}
		return query.list();
    }

}