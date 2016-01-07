package gov.usbr.ws.pisces;
 
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Transient;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.hibernate.Session;
import org.hibernate.annotations.Immutable;

import gov.usbr.ws.pisces.waterml.WaterML20;
import gov.usbr.ws.pisces.waterml20minimal.WaterML20Minimal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Resource exposed as /v0/ts
 * - ignore case in spelling
 * 
 * Note that this class uses a composite identifier of siteId, server, parameter, and interval.
 * If not handled queries may show duplicate records, not actual values as expected.
 * Multiple Id annotations are used and the class implements serializable.
 */
@Immutable
@Path("{ts : (?i)ts}")
public class TS {
    /**
     * Whether data flag is output for CSV and HTML.
     */
	@Transient
	private String dataFlag = "";
	
	/**
	 * Last index in data array that was accessed.
	 */
	@Transient
	private int lastDataIndex = -1;
	
	/**
	 * Site for the time series (needed by WaterML).
	 */
	@Transient
	private Site site = null;
	
    /**
     * Time series metadata.
     */
	@Transient
	private TSMeta tsmeta = null;
	
	/**
	 * Time series data records.
	 */
	@Transient
	private List<TSData> tsdata = null;
	
	/**
	 * URI for the requested resource.
	 */
	@Context
	@Transient
	private UriInfo uriInfo;
	
	/**
	 * Calculate a date as an integer for comparisons and lookups.
	 * @param date date to process
	 * @return integer representation of date with digits YYYYMMDD
	 */
	private long calculateDate ( Date date ) {
		return (date.getYear() + 1900)*10000 + (date.getMonth() + 1)*100 + date.getDate();
	}
	
	/**
	 * Determine the earliest date for the time series.
	 * @param ts single time series to evaluate
	 */
	public Date determineEarliestDate ( TS ts ) {
		List<TS> tslist = new ArrayList<TS>();
		tslist.add(ts);
		return determineEarliestDate(tslist);
	}
	
	/**
	 * Determine the earliest date from time series by examining the first date/time in the data array.
	 * This assumes that the data arrays were sorted by datetime when queried.
	 */
	private Date determineEarliestDate ( List<TS> tslist ) {
		long earliestTime = -1;
		Date earliestDate = null;
		for ( TS ts : tslist ) {
			List<TSData> tsdata = ts.getTsdata();
			if ( !tsdata.isEmpty() ) {
				if ( earliestTime < 0 ) {
					earliestDate = tsdata.get(0).getDatetime();
					earliestTime = earliestDate.getTime();
				}
				else {
					Date d = tsdata.get(0).getDatetime();
					long t = d.getTime();
					if ( t < earliestTime ) {
						earliestTime = t;
						earliestDate = d;
					}
				}
			}
		}
		return earliestDate;
	}
	
	/**
	 * Determine the latest date for the time series.
	 * @param ts single time series to evaluate
	 */
	public Date determineLatestDate ( TS ts ) {
		List<TS> tslist = new ArrayList<TS>();
		tslist.add(ts);
		return determineLatestDate(tslist);
	}
	
	/**
	 * Determine the latest date from time series by examining the last date/time in the data array.
	 * This assumes that the data arrays were sorted by datetime when queried.
	 */
	private Date determineLatestDate ( List<TS> tslist ) {
		long latestTime = -1;
		Date latestDate = null;
		for ( TS ts : tslist ) {
			List<TSData> tsdataList = ts.getTsdata();
			if ( !tsdataList.isEmpty() ) {
				if ( latestTime < 0 ) {
					latestDate = tsdataList.get(tsdataList.size() - 1).getDatetime();
					latestTime = latestDate.getTime();
				}
				else {
					Date d = tsdataList.get(tsdataList.size() - 1).getDatetime();
					long t = d.getTime();
					if ( t > latestTime ) {
						latestTime = t;
						latestDate = d;
					}
				}
			}
		}
		return latestDate;
	}
	
	/**
	 * Determine the unique site list
	 * @param tslist list of time series to process
	 * 
	 */
	public static List<Site> determineUniqueSiteList ( List<TS> tslist ) {
		Site site;
		List<Site> siteList = new ArrayList<Site>();
		for ( TS ts : tslist ) {
			site = ts.getSite();
			String siteId = site.getSiteId();
			boolean found = false;
			for ( Site s : siteList ) {
				if ( s.getSiteId().equals(siteId) ) {
					found = true;
					break;
				}
			}
			if ( !found ) {
				siteList.add(site);
			}
		}
		return siteList;
	}
	
	/**
	 * Determine the unique siteId list, which will match features
	 * @param tslist list of time series to process
	 * 
	 */
	public static List<String> determineUniqueSiteIdList ( List<TS> tslist ) {
		TSMeta tsmeta;
		List<String> siteIdList = new ArrayList<String>();
		for ( TS ts : tslist ) {
			tsmeta = ts.getTsmeta();
			String siteId = tsmeta.getSiteId();
			boolean found = false;
			for ( String s : siteIdList ) {
				if ( s.equals(siteId) ) {
					found = true;
					break;
				}
			}
			if ( !found ) {
				siteIdList.add(siteId);
			}
		}
		return siteIdList;
	}
	
    /**
     * Return the site for the time series.
     */
    public Site getSite () {
    	return this.site;
    }
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type for CSV.
     *
     * @return String that will be returned as a text/plain response.
     */
    //@GET
    //@Produces(MediaType.TEXT_PLAIN)
    //@Produces({"text/plain"})
    public Response getTSCSV(
    	@DefaultValue("true") @QueryParam("dataFlag") String dataFlag,
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("description") String description,
    	@DefaultValue("") @QueryParam("format") String format,
    	@DefaultValue("") @QueryParam("interval") String interval,
    	@DefaultValue("") @QueryParam("notes") String notes,
    	@DefaultValue("") @QueryParam("parameter") String parameter,
    	@DefaultValue("") @QueryParam("periodEnd") String periodEnd,
    	@DefaultValue("") @QueryParam("periodStart") String periodStart,
    	@DefaultValue("") @QueryParam("provider") String provider,
    	@DefaultValue("") @QueryParam("server") String server,
    	@DefaultValue("") @QueryParam("siteId") String siteId,
    	@DefaultValue("") @QueryParam("units") String units
    	) {
    	StringBuilder csv = new StringBuilder ();
    	
    	boolean doDebug = false;
    	boolean doDataFlag = true;
    	if ( dataFlag.equalsIgnoreCase("false") ) {
    		doDataFlag = false;
    	}
    	if ( debug.equalsIgnoreCase("true") && HibernateUtil.DEBUG_ENABLED ) {
    		doDebug = true;
    		csv.append("DEBUG: dataFlag=\"" + dataFlag + "\"\n" );
    		csv.append("DEBUG: debug=\"" + debug + "\"\n" );
    		csv.append("DEBUG: description=\"" + description + "\"\n" );
    		csv.append("DEBUG: interval=\"" + interval + "\"\n" );
    		csv.append("DEBUG: notes=\"" + notes + "\"\n" );
    		csv.append("DEBUG: parameter=\"" + parameter + "\"\n" );
    		csv.append("DEBUG: provider=\"" + provider + "\"\n" );
    		csv.append("DEBUG: server=\"" + server + "\"\n" );
    		csv.append("DEBUG: siteId=\"" + siteId + "\"\n" );
    		csv.append("DEBUG: units=\"" + units + "\"\n" );
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
    	List<TS> tslist = readTSList ( session, doDebug, null, description, interval, HibernateUtil.MAX_TS_QUERY,
    		notes, parameter, queryStart, queryEnd, provider, server, siteId, units );
    	session.getTransaction().commit();
    	session.close();
    	
    	// Make sure interval is the same
    	String intervalPrev = null;
    	boolean doDay = false;
    	boolean doMonth = false;
    	boolean doYear = false;
    	TS ts;
    	TSMeta meta;
    	String interval2;
    	for ( int its = 0; its < tslist.size(); its++ ) {
    		ts = tslist.get(its);
    		meta = ts.getTsmeta();
    		interval2 = meta.getInterval();
    		if ( intervalPrev == null ) {
    			intervalPrev = interval2;
    			if ( intervalPrev.equalsIgnoreCase("day") ) {
    				doDay = true;
    				doMonth = false;
    				doYear = false;
    			}
    			else if ( intervalPrev.equalsIgnoreCase("month") ) {
    				doDay = false;
    				doMonth = true;
    				doYear = false;
    			}
    			else if ( intervalPrev.equalsIgnoreCase("year") ) {
    				doDay = false;
    				doMonth = false;
    				doYear = true;
    			}
    		}
    		if ( !interval2.equalsIgnoreCase(intervalPrev) ) {
        		return Response.status(500).
        	        	entity("ERROR: Time series intervals are different - must be same to output csv format").
        	        	header("Access-Control-Allow-Origin","*").
        	        	header("Access-Control-Allow-Methods","GET").
        	        	build();
    		}
    		intervalPrev = interval2;
    	}
    	
    	csv.append("datetime");
    	for ( int its = 0; its < tslist.size(); its++ ) {
    		csv.append(",\"" + tslist.get(its).getTsmeta().getTsid() + " (" + tslist.get(its).getTsmeta().getUnits() + ")\"" );
    		if ( doDataFlag ) {
    			csv.append(",\"" + tslist.get(its).getTsmeta().getTsid() + " flag\"");
    		}
    	}
    	csv.append("\n");
    	
    	Date outputStart = queryStart;
    	if ( outputStart == null ) {
    		// Get earliest date from time series
    		outputStart = determineEarliestDate(tslist);
    	}
    	Date outputEnd = queryEnd;
    	if ( outputEnd == null ) {
    		// Get latest date from time series
    		outputEnd = determineLatestDate(tslist);
    	}

    	GregorianCalendar gcal = new GregorianCalendar();
    	SimpleDateFormat dateFormat = null;
	    if ( doDay ) {
	    	dateFormat = new SimpleDateFormat("YYYY-MM-dd");
	    }
	    else if ( doMonth ) {
	    	dateFormat = new SimpleDateFormat("YYYY-MM");
	    }
	    else if ( doYear ) {
	    	dateFormat = new SimpleDateFormat("YYYY");
	    }
    	gcal.setTime(outputStart);
    	int tslistSize = tslist.size();
    	TSData tsdata2;
    	DecimalFormat valueFormat = new DecimalFormat("###.00");
    	Double value;
    	while (!gcal.getTime().after(outputEnd) ) {
    	    Date d = gcal.getTime();
        	for ( int its = 0; its < tslistSize; its++ ) {
        		ts = tslist.get(its);
        		if ( its == 0 ) {
        			// Output date/time column
            		csv.append("\"" + dateFormat.format(d) + "\"");
        		}
    	    	tsdata2 = ts.lookupTsdata(d);
    	    	if ( tsdata2 == null ) {
    	    		csv.append(",");
		    		if ( doDataFlag ) {
		    			csv.append(",");
		    		}
    	    	}
    	    	else {
    	    		value = tsdata2.getValue();
    	    		if ( (value == null) || value.isNaN() ) {
    	    			csv.append(",");
    	    		}
    	    		else {
    	    			csv.append("," + valueFormat.format(value) );
    	    		}
		    		if ( doDataFlag ) {
		    			csv.append(",\"" + tsdata2.getFlag() + "\"");
		    		}
    	    	}
        	}
    		csv.append("\n");
    	    // Increment date/time
    	    if ( doDay ) {
    	    	gcal.add(Calendar.DAY_OF_YEAR, 1);
    	    }
    	    else if ( doMonth ) {
    	    	gcal.add(Calendar.MONTH, 1);
    	    }
    	    else if ( doYear ) {
    	    	gcal.add(Calendar.YEAR, 1);
    	    }
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
    public Response getTSJSON(
    	@DefaultValue("true") @QueryParam("dataFlag") String dataFlag,
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("description") String description,
    	@DefaultValue("") @QueryParam("format") String format,
    	@DefaultValue("") @QueryParam("interval") String interval,
    	@DefaultValue("") @QueryParam("notes") String notes,
    	@DefaultValue("") @QueryParam("parameter") String parameter,
    	@DefaultValue("") @QueryParam("periodEnd") String periodEnd,
    	@DefaultValue("") @QueryParam("periodStart") String periodStart,
    	@DefaultValue("") @QueryParam("provider") String provider,
    	@DefaultValue("") @QueryParam("server") String server,
    	@DefaultValue("") @QueryParam("siteId") String siteId,
    	@DefaultValue("") @QueryParam("units") String units
    	) {
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
    	List<TS> tslist = readTSList ( session, doDebug, null, description, interval, HibernateUtil.MAX_TS_QUERY,
    		notes, parameter, queryStart, queryEnd, provider, server, siteId, units );
    	session.getTransaction().commit();
    	session.close();
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    	objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    	// Have to process the time series separately because they may be of different interval
    	try {
    		//json.append(objectMapper.writeValueAsString(tslist));
    		json.append("[");
    		for ( int i = 0; i < tslist.size(); i++ ) {
    			TS ts = tslist.get(i);
    			if ( i != 0 ) {
    				json.append(",");
    			}
    			String interval2 = ts.getTsmeta().getInterval();
    			if ( interval2.equalsIgnoreCase("day") ) {
    				objectMapper.setDateFormat(new SimpleDateFormat("YYYY-MM-dd"));
    			}
    			else if ( interval2.equalsIgnoreCase("month") ) {
    				objectMapper.setDateFormat(new SimpleDateFormat("YYYY-MM"));
    			}
    			else if ( interval2.equalsIgnoreCase("year") ) {
    				objectMapper.setDateFormat(new SimpleDateFormat("YYYY"));
    			}
    			json.append(objectMapper.writeValueAsString(ts));
    		}
    		json.append("]");
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
    @Produces({"text/html","text/csv","application/xml","application/json"})
    public Response getTSHtml(
    	@DefaultValue("true") @QueryParam("dataFlag") String dataFlag,
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("description") String description,
    	@DefaultValue("") @QueryParam("format") String format,
    	@DefaultValue("") @QueryParam("interval") String interval,
    	@DefaultValue("") @QueryParam("notes") String notes,
    	@DefaultValue("") @QueryParam("parameter") String parameter,
    	@DefaultValue("") @QueryParam("periodEnd") String periodEnd,
    	@DefaultValue("") @QueryParam("periodStart") String periodStart,
    	@DefaultValue("") @QueryParam("provider") String provider,
    	@DefaultValue("") @QueryParam("server") String server,
    	@DefaultValue("") @QueryParam("siteId") String siteId,
    	@DefaultValue("") @QueryParam("units") String units
    	) {
    	// If requested format is different, call the other method
    	if ( format.equalsIgnoreCase("csv") ) {
    		return getTSCSV(dataFlag, debug, description, format, interval, notes, parameter, periodEnd, periodStart, provider, server, siteId, units);
    	}
    	else if ( format.equalsIgnoreCase("json") ) {
    		return getTSJSON(dataFlag, debug, description, format, interval, notes, parameter, periodEnd, periodStart, provider, server, siteId, units);
    	}
    	else if ( format.equalsIgnoreCase("waterml20minimal") || format.equalsIgnoreCase("waterml20minimaljson") ) {
    		return getTSWaterML20(dataFlag, debug, description, format, interval, notes, parameter, periodEnd, periodStart, provider, server, siteId, units);
    	}
    	// Create string with HTML output
    	StringBuilder html = new StringBuilder(WebUtil.getPageStart() + "<h1>Time Series</h1><p>");
    	String [] alternateFormats = { "csv", "json", "waterml20minimal" };//, "waterml20minimaljson" };
    	html.append("<p>" + WebUtil.getAlternateFormatHTML(uriInfo, alternateFormats) + "</p>");
    	// Print debug content if requested
    	boolean doDebug = false;
    	if ( debug.equalsIgnoreCase("true") && HibernateUtil.DEBUG_ENABLED ) {
    		doDebug = true;
    		html.append("DEBUG: dataFlag=\"" + dataFlag + "\"<br>" );
    		html.append("DEBUG: description=\"" + description + "\"<br>" );
    		html.append("DEBUG: interval=\"" + interval + "\"<br>" );
    		html.append("DEBUG: notes=\"" + notes + "\"<br>" );
    		html.append("DEBUG: parameter=\"" + parameter + "\"<br>" );
    		html.append("DEBUG: provider=\"" + provider + "\"<br>" );
    		html.append("DEBUG: server=\"" + server + "\"<br>" );
    		html.append("DEBUG: siteId=\"" + siteId + "\"<br>" );
    		html.append("DEBUG: units=\"" + units + "\"<br>" );
    	}
    	boolean doDataFlag = true; // Default
    	if ( dataFlag.equalsIgnoreCase("false") ) {
    		doDataFlag = false;
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
    	List<TS> tslist = readTSList ( session, doDebug, html, description, interval, HibernateUtil.MAX_TS_QUERY,
    		notes, parameter, queryStart, queryEnd, provider, server, siteId, units );
    	session.getTransaction().commit();
    	session.close();
    	
    	// Make sure interval is the same
    	String intervalPrev = null;
    	boolean doDay = false;
    	boolean doMonth = false;
    	boolean doYear = false;
    	TS ts;
    	TSMeta meta;
    	String interval2;
    	for ( int its = 0; its < tslist.size(); its++ ) {
    		ts = tslist.get(its);
    		meta = ts.getTsmeta();
    		interval2 = meta.getInterval();
    		if ( intervalPrev == null ) {
    			intervalPrev = interval2;
    			if ( intervalPrev.equalsIgnoreCase("day") ) {
    				doDay = true;
    				doMonth = false;
    				doYear = false;
    			}
    			else if ( intervalPrev.equalsIgnoreCase("month") ) {
    				doDay = false;
    				doMonth = true;
    				doYear = false;
    			}
    			else if ( intervalPrev.equalsIgnoreCase("year") ) {
    				doDay = false;
    				doMonth = false;
    				doYear = true;
    			}
    		}
    		if ( !interval2.equalsIgnoreCase(intervalPrev) ) {
        		return Response.status(500).
        	        	entity("ERROR: Time series intervals are different - must be same to output html format").
        	        	header("Access-Control-Allow-Origin","*").
        	        	header("Access-Control-Allow-Methods","GET").
        	        	build();
    		}
    		intervalPrev = interval2;
    	}
    	
    	Date outputStart = queryStart;
    	if ( outputStart == null ) {
    		// Get earliest date from time series
    		outputStart = determineEarliestDate(tslist);
    	}
    	Date outputEnd = queryEnd;
    	if ( outputEnd == null ) {
    		// Get latest date from time series
    		outputEnd = determineLatestDate(tslist);
    	}
    	
    	URI uriRequested = this.uriInfo.getRequestUri();
    	int port = uriRequested.getPort();
    	String host = uriRequested.getHost();
    	String tsmetaLinkPrefix = "http://" + host + ":" + port + WebUtil.WEBROOT_WITH_VERSION;
    	
    	String maxString = "";
    	if ( tslist.size() == HibernateUtil.MAX_TS_QUERY ) {
    		maxString = " (<b>query limited to maximum of " + HibernateUtil.MAX_TS_QUERY + " time series - use filters to focus query</b>)";
    	}
    	html.append("<p>" + tslist.size() + " time series returned"+ maxString +".  Use the <a href=\"" + tsmetaLinkPrefix +
    		"/#tsmeta\">tsmeta</a> (time series metadata) service to retrieve time series metadata (units, etc.) for <code>csv</code> and <code>html</code> formats.</p>\n");
    	html.append("<table>\n");
    	html.append("<tr><th class=\"hasTooltip\">datetime<span>Date/time for time series value, matching precision of time series interval</span></th>");
    	TSMeta tsmeta;
    	for ( int its = 0; its < tslist.size(); its++ ) {
    		tsmeta = tslist.get(its).getTsmeta();
    		html.append("<th class=\"hasTooltip\">" + tsmeta.getTsid() + " (" + tsmeta.getUnits() + ")<span>Data value for unique time series identifier (period-delimited concatenation of siteId, server, parameter, and interval)</span></th>");
    		if ( doDataFlag ) {
    			html.append("<th class=\"hasTooltip\">" + tsmeta.getTsid() + " flag<span>Data flag for unique time series identifier</span></th>");
    		}
    	}
    	GregorianCalendar gcal = new GregorianCalendar();
    	SimpleDateFormat dateFormat = null;
	    if ( doDay ) {
	    	dateFormat = new SimpleDateFormat("YYYY-MM-dd");
	    }
	    else if ( doMonth ) {
	    	dateFormat = new SimpleDateFormat("YYYY-MM");
	    }
	    else if ( doYear ) {
	    	dateFormat = new SimpleDateFormat("YYYY");
	    }
    	gcal.setTime(outputStart);
    	int tslistSize = tslist.size();
    	TSData tsdata2;
    	DecimalFormat valueFormat = new DecimalFormat("###.00");
    	Double value;
    	while (!gcal.getTime().after(outputEnd) ) {
    	    Date d = gcal.getTime();
        	for ( int its = 0; its < tslistSize; its++ ) {
        		ts = tslist.get(its);
        		if ( its == 0 ) {
        			// Output date/time column
            		html.append("<tr><td>" + dateFormat.format(d) + "</td>");
        		}
    	    	tsdata2 = ts.lookupTsdata(d);
    	    	if ( tsdata2 == null ) {
    	    		html.append("<td></td>");
		    		if ( doDataFlag ) {
		    			html.append("<td></td>");
		    		}
    	    	}
    	    	else {
    	    		value = tsdata2.getValue();
    	    		if ( (value == null) || value.isNaN() ) {
    	    			html.append("<td></td>");
    	    		}
    	    		else {
    	    			html.append("<td class=\"tabnum\">" + valueFormat.format(value) + "</td>");
    	    		}
		    		if ( doDataFlag ) {
		    			html.append("<td>" + tsdata2.getFlag() + "</td>");
		    		}
    	    	}
        	}
    		html.append("</tr>\n");
    	    // Increment date/time
    	    if ( doDay ) {
    	    	gcal.add(Calendar.DAY_OF_YEAR, 1);
    	    }
    	    else if ( doMonth ) {
    	    	gcal.add(Calendar.MONTH, 1);
    	    }
    	    else if ( doYear ) {
    	    	gcal.add(Calendar.YEAR, 1);
    	    }
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
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    //@GET
    //@Produces(MediaType.APPLICATION_XML)
    public Response getTSWaterML20(
    	@DefaultValue("true") @QueryParam("dataFlag") String dataFlag,
    	@DefaultValue("") @QueryParam("debug") String debug,
    	@DefaultValue("") @QueryParam("description") String description,
    	@DefaultValue("") @QueryParam("format") String format,
    	@DefaultValue("") @QueryParam("interval") String interval,
    	@DefaultValue("") @QueryParam("notes") String notes,
    	@DefaultValue("") @QueryParam("parameter") String parameter,
    	@DefaultValue("") @QueryParam("periodEnd") String periodEnd,
    	@DefaultValue("") @QueryParam("periodStart") String periodStart,
    	@DefaultValue("") @QueryParam("provider") String provider,
    	@DefaultValue("") @QueryParam("server") String server,
    	@DefaultValue("") @QueryParam("siteId") String siteId,
    	@DefaultValue("") @QueryParam("units") String units
    	) {
    	StringBuilder waterml20 = new StringBuilder ();
    	
    	boolean doDebug = false;
    	
    	// Parse the period to query if specified
    	Date queryStart = HibernateUtil.parseDatetime(periodStart);
    	if ( !periodStart.isEmpty() && (queryStart == null) ) {
    		return Response.status(500).
    	        	entity("ERROR: error parsingStart - need to be format YYYY, YYYY-MM, or YYYY-MM-DD depending on interval" ).
    	        	header("Access-Control-Allow-Origin","*").
    	        	header("Access-Control-Allow-Methods","GET").
    	        	build();
    	}
    	Date queryEnd = HibernateUtil.parseDatetime(periodEnd);
    	if ( !periodEnd.isEmpty() && (queryEnd == null) ) {
    		return Response.status(500).
    	        	entity("ERROR: error parsingEnd - need to be format YYYY, YYYY-MM, or YYYY-MM-DD depending on interval" ).
    	        	header("Access-Control-Allow-Origin","*").
    	        	header("Access-Control-Allow-Methods","GET").
    	        	build();
    	}
    	
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<TS> tslist = readTSList ( session, doDebug, null, description, interval, HibernateUtil.MAX_TS_QUERY, notes,
    	    parameter, queryStart, queryEnd, provider, server, siteId, units );
    	session.getTransaction().commit();
    	session.close();
    	
		if ( format.equalsIgnoreCase("waterml20") ) {
	    	// Marshal Java classes to XML string
	    	try {
	    		//waterml20.append("DEBUG:  Before marshal code");
	    		WaterML20 waterml20Instance = new WaterML20();
	    		waterml20.append ( waterml20Instance.marshalXml(tslist) );
	    	}
	    	catch ( Exception e ) {
	    		waterml20.append("DEBUG:  Error marshalling XML: " + e );
	    		return Response.status(500).
	            		entity(waterml20.toString()).
	                	header("Access-Control-Allow-Origin","*").
	                	header("Access-Control-Allow-Methods","GET").
	                	build();
	    	}
    	}
		else if ( format.equalsIgnoreCase("waterml20minimal") ) {
	    	// Marshal Java classes to XML string
	    	try {
	    		//waterml20.append("DEBUG:  Before marshal code");
	    		WaterML20Minimal waterml20Minimal = new WaterML20Minimal();
	    		waterml20.append ( waterml20Minimal.marshalXML(tslist, uriInfo, queryStart, queryEnd) );
	    	}
	    	catch ( Exception e ) {
	    		StringWriter sw = new StringWriter();
	    		PrintWriter pw = new PrintWriter(sw);
	    		e.printStackTrace(pw);
	    		waterml20.append("DEBUG:  Error creating XML: " + e + " " + sw.toString() );
	    		return Response.status(500).
	            		entity(waterml20.toString()).
	                	header("Access-Control-Allow-Origin","*").
	                	header("Access-Control-Allow-Methods","GET").
	                	build();
	    	}
    	}
		/* TODO SAM 2015-12-29 Need to enable once figure out USGS/EPA approach
    	else if ( format.equalsIgnoreCase("waterml20minimaljson") ) {
    		// Just dump out the schema as JSON
        	ObjectMapper objectMapper = new ObjectMapper();
        	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        	objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        	// TODO SAM 2015-12-29 Issue - may need to process the sites and time series separately because they may be of different interval
        	try {
       			waterml20.append(objectMapper.writeValueAsString(collection));
        	}
        	catch ( JsonProcessingException e ) {
        		String error = e.toString();
        		return Response.status(500).
    	        	entity(error.toString()).
    	        	header("Access-Control-Allow-Origin","*").
    	        	header("Access-Control-Allow-Methods","GET").
    	        	build();
        	}
    	}
    	*/
    	
    	// Build and return the response
    	// - HTTP status code 200 means OK
    	// - Access control settings allow CORS access restricted to GET
        return Response.status(200).
        	entity(waterml20.toString()).
        	type("application/xml"). // Ensures media type when called from text/html GET request
        	header("Access-Control-Allow-Origin","*").
        	header("Access-Control-Allow-Methods","GET").
        	build();
    }
        
    /**
     * Return the interval normalized, which converts the internal/database interval to:
     *  Daily->day, Monthly->month, Yearly->year
     */
    public String getIntervalNormalized ( String interval ) {
    	if ( interval.equalsIgnoreCase("Daily") ) {
    		return "day";
    	}
    	else if ( interval.equalsIgnoreCase("Monthly") ) {
    		return "month";
    	}
    	else if ( interval.equalsIgnoreCase("Yearly") ) {
    		return "year";
    	}
    	else {
    		return interval;
    	}
    }
    
    /**
     * Get the list of time series for a SiteId.
     * @param tslist list of time series to check.
     * @param siteId site identifier to find in time series list.
     * @param return list of time series that are for the specified site identifier.
     */
    public static List<TS> getTimeSeriesForSiteId ( List<TS> tslist, String siteId ) {
    	List<TS> tslistForSiteId = new ArrayList<TS>();
    	TSMeta tsmeta;
    	for ( TS ts: tslist ) {
    		tsmeta = ts.getTsmeta();
    		if ( tsmeta.getSiteId().equalsIgnoreCase(siteId) ) {
    			tslistForSiteId.add(ts);
    		}
    	}
    	return tslistForSiteId;
    }

    /**
     * Return the time series data.
     */
    public List<TSData> getTsdata () {
    	return this.tsdata;
    }
    
    /**
     * Return the time series metadata.
     */
    public TSMeta getTsmeta () {
    	return this.tsmeta;
    }
    
    /**
     * Find the TSData matching the give date.  If not found, return null.
     * @param d date to match. Must be exact match.
     */
    public TSData lookupTsdata(Date date) {//,StringBuilder html ) {
    	List<TSData> tsdataList = getTsdata();
    	// If a previous index has been specified, start the search from there
    	if ( this.lastDataIndex < 0 ) {
    		 // Initialize the lookup at the start
    		this.lastDataIndex = 0;
    	}
    	// Do time comparisons on integer YYYYMMDD to ensure matches.  Will need to change if other than day, month, year are used.
    	long t = calculateDate(date);
    	long t0 = calculateDate(tsdataList.get(this.lastDataIndex).getDatetime());
    	//html.append("DEBUG: Looking up date " + date + " t=" + t + " t0=" + t0 + "<br>");
    	if ( t >= t0 ) {
    		// Search forward from previous index
    		for ( int i = this.lastDataIndex; i < tsdataList.size(); i++ ) {
    			t0 = calculateDate(tsdataList.get(i).getDatetime());
    			//html.append("DEBUG: date=" + tsdataList.get(i).getDatetime() + " t0=" + t0 + "<br>");
    			if ( t0 == t ) {
    				// Found date
    				this.lastDataIndex = i;
    				//html.append("DEBUG: Match<br>");
    				return tsdataList.get(i);
    			}
    		}
    	}
    	else {
    		// Search backward from previous index
    		for ( int i = this.lastDataIndex; i >= 0; i-- ) {
    			t0 = calculateDate(tsdataList.get(i).getDatetime());
    			//html.append("DEBUG: date=" + tsdataList.get(i).getDatetime() + " t0=" + t0 + "<br>");
    			if ( t0 == t ) {
    				// Found date
    				this.lastDataIndex = i;
    				//html.append("DEBUG: Match<br>");
    				return tsdataList.get(i);
    			}
    		}
    	}
    	//html.append("DEBUG: No match<br>");
    	return null;
    }
    
    /**
     * Read a data array for time series.
     */
    private TS readTS ( Session session, TSMeta tsmeta, boolean doDebug, StringBuilder html, Date queryStart, Date queryEnd ) {
    	TS ts = new TS();
    	ts.setTsmeta(tsmeta);
    	TSData tsData = new TSData();
    	List<TSData> tsdata = tsData.readTSData(session, doDebug, queryStart, queryEnd, tsmeta.getTablename());
		if ( doDebug && html != null ) {
			html.append("DEBUG:  read " + tsdata.size() + " table records<br>" );
    	}
    	ts.setTsdata ( tsdata );
    	return ts;
    }
    
    /**
     * Read a list of TSMeta, shared by each get method.
     */
    private List<TS> readTSList ( Session session, boolean doDebug, StringBuilder html, String description, String interval, int maxTS,
    	String notes, String parameter, Date queryStart, Date queryEnd, String provider, String server, String siteId, String units ) {
    	// First read the list of TSMeta
    	TSMeta tsmeta = new TSMeta ();
    	List<TSMeta> tsmetaList = tsmeta.readTSMeta ( session, doDebug, html, description, interval, notes,
    	    	parameter, provider, server, siteId, units );
    	if ( doDebug && html != null ) {
    		html.append("DEBUG:  Have " + tsmetaList.size() + " time series meta<br>" );
    	}
  		// Now loop through the TSMeta records and read site and time series data for each
		TS ts;
		Site site0 = new Site(); // Used to call read method since not public static
		Site site;
		List<TS> tslist = new ArrayList<TS>();
		List<Site> siteList = new ArrayList<Site>();
		List<Site> siteListCache = new ArrayList<Site>();
		for ( TSMeta tsmeta2 : tsmetaList ) {
			// Read the site for the metadata - needed by some formats like WaterML
			// - only need to pass the siteId
			// - first see if the site was previously read
			site = null;
			for ( Site iSite : siteListCache ) {
				if ( iSite.getSiteId().equalsIgnoreCase(tsmeta2.getSiteId() ) ) {
					site = iSite;
					break;
				}
			}
			if ( site == null ) {
				// Read the Site
				String agencyRegion = "";
				String description0 = "";
				String responsibility = "";
				String siteType = "";
				String state = "";
				String timezone = "";
				siteList = site0.readSites(session, agencyRegion, doDebug, html, description0, responsibility, tsmeta2.getSiteId(),
					siteType, state, timezone);
				if ( siteList.size() == 1 ) {
					site = siteList.get(0);
					siteListCache.add(site);
				}
			}
			// Read time series for the metadata
			if ( doDebug && html != null ) {
				html.append("DEBUG:  reading table \"" + tsmeta2.getTablename() + "\"<br>" );
	    	}
			ts = readTS ( session, tsmeta2, doDebug, html, queryStart, queryEnd );
			if ( ts != null ) {
				if ( site != null ) {
					ts.setSite(site);
				}
				tslist.add(ts);
			}
			if ( tslist.size() == maxTS ) {
				// Limit the query size to prevent hammering the database
				break;
			}
		}
		return tslist;
    }
    
    /**
     * Set the site.
     */
    public void setSite ( Site site ) {
    	this.site = site;
    }
    
    /**
     * Set the time series data.
     */
    public void setTsdata ( List<TSData> tsdata ) {
    	this.tsdata = tsdata;
    }
    
    /**
     * Set the time series metadata.
     */
    public void setTsmeta ( TSMeta tsmeta ) {
    	this.tsmeta = tsmeta;
    }
}