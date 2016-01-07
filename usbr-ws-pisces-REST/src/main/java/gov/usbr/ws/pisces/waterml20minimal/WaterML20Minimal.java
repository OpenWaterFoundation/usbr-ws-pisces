package gov.usbr.ws.pisces.waterml20minimal;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import gov.usbr.ws.pisces.Site;
import gov.usbr.ws.pisces.TS;
import gov.usbr.ws.pisces.TSData;
import gov.usbr.ws.pisces.TSMeta;
import gov.usbr.ws.pisces.WebUtil;

/**
 * This class provides basic functionality to write a minimal WaterML 2.0 file.
 * Ideally full Java/XML persistence bindings would be used via java.xml.bind package and class hierarchy.
 * However, a clean mapping API is not readily available or documented so this simple approach is used instead.
 * When a full API is made available this code can be replaced.
 * @author sam
 */
public class WaterML20Minimal {

	/**
	 * List of wml2:observationMember
	 */
	public WaterML20Minimal () {
		
	}
	
	/**
	 * Determine the unique list of time series parameters for the given site.
	 * @param site site to process
	 * @param tslist list of time series to examine to determine parameter list
	 * @return a list of parameters for the site and times series
	 */
	private List<String> getUniqueTimeSeriesParameters ( Site site, List<TS> tslist ) {
		List<String> siteParameterList = new ArrayList<String>();
		for ( TS ts : tslist ) {
			String parameter = ts.getTsmeta().getParameter();
			boolean found = false;
			for ( String parameter0 : siteParameterList ) {
				if ( parameter0.equalsIgnoreCase(parameter) ) {
					found = true;
					break;
				}
			}
			if ( !found ) {
				siteParameterList.add(parameter);
			}
		}
		return siteParameterList;
	}
	
	/**
	 * Create WaterML 2.0 minimal output from a list of time series.
	 */
	public String marshalXML ( List<TS> tslist, UriInfo uriInfo, Date queryStart, Date queryEnd ) {
    	// To format XML indentation...
    	String indent1 = "  ";
    	String indent2 = "    ";
    	String indent3 = "      ";
    	String indent4 = "        ";
    	String indent5 = "          ";
    	String indent6 = "            ";
    	String indent7 = "              ";
    	String nl = "\n";
    	
    	URI uriRequested = uriInfo.getRequestUri();
    	int port = uriRequested.getPort();
    	String host = uriRequested.getHost();
    	String path = "http://" + host + ":" + port + WebUtil.WEBROOT_WITH_VERSION;
    	if ( port == 80 ) {
    		// Default for normal HTML web traffic so don't need port
    		path = "http://" + host + uriRequested.getPath() + WebUtil.WEBROOT_WITH_VERSION;
    	}
    	
		StringBuilder xml = new StringBuilder();
    	// Get the unique sites that are involved with the time series
    	List<Site> siteList = TS.determineUniqueSiteList ( tslist );
    	
    	// Add the header to the file...
    	// From example:  http://external.opengeospatial.org/twiki_public/pub/WaterML/MultipleObservations/MultipleObservations.xml
    	// Comment out what is not needed for minimal implementation
    	xml.append(
    		"<?xml version=\"1.0\" encoding=\"utf-8\"?>" + nl
    		+ "<wml2:Collection xmlns:wml2=\"http://www.opengis.net/waterml/2.0\""
    		+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
    		+ " xmlns:gml=\"http://www.opengis.net/gml/3.2\""
    		+ " xmlns:xlink=\"http://www.w3.org/1999/xlink\""
    		//+ " xmlns:wml=\"http://www.cuahsi.org/waterML/1.1/\""
    		//+ " xmlns:fn=\"http://www.w3.org/2005/xpath-functions\""
    		//+ " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
    		+ " xmlns:om=\"http://www.opengis.net/om/2.0\""
    		+ " xmlns:swe=\"http://www.opengis.net/swe/2.0\""
    		//+ " xmlns:op=\"http://schemas.opengis.net/op\""
    		+ " xmlns:sf=\"http://www.opengis.net/sampling/2.0\""
    		+ " xmlns:sams=\"http://www.opengis.net/samplingSpatial/2.0\""
    		+ " xmlns:sam=\"http://www.opengis.net/sampling/2.0\""
    		//+ " xmlns:wml1_0=\"http://www.cuahsi.org/waterML/1.0/\""
    		+ " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
    		//+ " xmlns:gmd=\"http://www.isotc211.org/2005/gmd\""
    		//+ " xmlns:gco=\"http://www.isotc211.org/2005/gco\""
    		//+ " xmlns:gss=\"http://www.isotc211.org/2005/gss\""
    		//+ " gml:id=\"generated_collection_doc\""
    		+ ">" + nl
    	);
    	// Basic header information...
    	xml.append(indent1 + "<wml2:metadata>" + nl);
    	xml.append(indent2 + "<wml2:DocumentMetadata gml:id=\"doc_md\">" + nl);
    	GregorianCalendar now = new GregorianCalendar();
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    	xml.append(indent3 + "<wml2:generationDate>" + df.format(now.getTime())+ "</wml2:generationDate>" + nl);
    	xml.append(indent3 + "<wml2:version xlink:href=\"http://www.opengis.net/waterml/2.0\" xlink:title=\"WaterML 2.0 RFC\" />" + nl );
    	xml.append(indent3 + "<wml2:generationSystem>Reclamation Pisces web service WaterML 2.0 minimal</wml2:generationSystem>" + nl );
    	xml.append(indent2 + "</wml2:DocumentMetadata>" + nl );
    	xml.append(indent1 + "</wml2:metadata>"+ nl);
    	
    	// Output parameters (data types) as wml2:localDictionary (referred to later in time series metadata)
    	// - the dictionaries can be specified for different definitions such as "phenomenon" (data types measured),
    	//   "quality" (quality of data), "censorCode" (data editing?), "method" (how data measured/logged)
    	// - are these just a compilation from all sites and time series (seems to be for site and parameter)?
    	// - derived parameters such as accumulations seem to be indicated under the time series metadata
    	xml.append(indent1 + "<wml2:localDictionary>"+ nl);
    	xml.append(indent2 + "<gml:Dictionary gml:id=\"phenomena\">"+ nl);
    	for ( Site site: siteList ) {
    		// Get the parameters for the time series reported at the site.  Can't just process time series
    		// because there is a chance that different intervals are provided
    		List<String> siteParameterList = getUniqueTimeSeriesParameters ( site, tslist );
    		for ( String siteParameter : siteParameterList ) {
    			xml.append(indent3 + "<gml:identifier codeSpace=\"" + path + "\">phenomena</gml:identifier>"+ nl);
    			xml.append(indent3 + "<gml:dictionaryEntry>"+ nl);
    			xml.append(indent4 + "<gml:Definition gml:id=\"" + site.getSiteId() + "." + siteParameter + "\">" + nl );
    			xml.append(indent5 + "<gml:description xlink:href=\"" + path + "\" xlink:title=\"" + siteParameter + "\"/>" + nl );
				// TODO SAM 2015-12-31 what is this? (from example)...
    			//xml.append(indent4 + "<gml:identifier codeSpace="http://hiscentral.cuahsi.org/wml/variable">LittleBearRiver:USU9</gml:identifier>
				xml.append(indent5 + "<gml:name codeSpace=\"" + path + "\">" + siteParameter + "</gml:name>" + nl );
				// TODO SAM 2015-12-31 What is this? (from example)...
				//xml.append(indent4 + "<gml:name codeSpace="http://hiscentral.cuahsi.org/wml/vocabulary/LittleBearRiver">Turbidity</gml:name>
				xml.append(indent4 + "</gml:Definition>" + nl );
    			xml.append(indent3 + "</gml:dictionaryEntry>"+ nl);
    		}
    	}
    	xml.append(indent2 + "</gml:Dictionary>"+ nl);
    	xml.append(indent1 + "</wml2:localDictionary>"+ nl);
    	
    	// Output site information as sampling points
    	// -these are cross-referenced below in time series
		xml.append(indent1 + "<wml2:samplingFeatureMember>" + nl);
    	for ( Site site: siteList ) {
    		// Get the time series that are for the siteId
    		String siteId = site.getSiteId();
    		List<TS> tslistForSiteId = TS.getTimeSeriesForSiteId ( tslist, siteId );
    		if ( tslistForSiteId.size() == 0 ) {
    			// No time series for site so don't process site
    			continue;
    		}
    		xml.append(indent2 + "<wml2:MonitoringPoint gml:id=\"" + siteId + "\">" + nl);
    		// TODO SAM 2015-12-29 Could put codeSpace attribute here with URL to site
    		xml.append(indent3 + "<gml:identifier codeSpace=\"" + path + "\">" + siteId + "</gml:identifier>" + nl);
    		xml.append(indent3 + "<gml:name>" + site.getDescription() + "</gml:name>" + nl);
    		// TODO SAM 2015-12-29 - need to enable something here...
    		//xml.append(indent3 + "<sam:sampledFeature></>" + nl);
    		// Add some parameters for the site as an example of what could be done
    		// - these are not measurement parameters - they are essentially site properties
    		// Use main URL to API documentation
    		// State
    		xml.append(indent3 + "<sam:parameter>" + nl);
    		xml.append(indent4 + "<om:NamedValue>" + nl);
    		xml.append(indent5 + "<om:name xlink:href=\"" + path + "\" xlink:title=\"State\"/>" + nl);
    		xml.append(indent5 + "<om:value xsi:type=\"xsd:string\">" + site.getState() + "</om:value>" + nl);
    		xml.append(indent4 + "</om:NamedValue>" + nl);
    		xml.append(indent3 + "</sam:parameter>" + nl);
    		// Region
    		xml.append(indent3 + "<sam:parameter>" + nl);
    		xml.append(indent4 + "<om:NamedValue>" + nl);
    		xml.append(indent5 + "<om:name xlink:href=\"" + path + "\" xlink:title=\"Agency Region\"/>" + nl);
    		xml.append(indent5 + "<om:value xsi:type=\"xsd:string\">" + site.getAgencyRegion() + "</om:value>" + nl);
    		xml.append(indent4 + "</om:NamedValue>" + nl);
    		xml.append(indent3 + "</sam:parameter>" + nl);
    		xml.append(indent2 + "</wml2:MonitoringPoint>" + nl); // End of Site
    		// TODO SAM 2015-12-29 could add sams:shape if spatial data known
    		// TODO SAM 2015-12-29 could add wml2:monitoringType if spatial data known
    	}
		xml.append(indent1 + "</wml2:samplingFeatureMember>" + nl);

		// Output all time series metadata and data (with link to Site above) as wml:observationMember
		TSMeta tsmeta;
		Site site;
    	for ( TS ts: tslist ) {
			tsmeta = ts.getTsmeta();
			site = ts.getSite();
    		xml.append(indent1 + "<wml2:observationMember>" + nl);
    		xml.append(indent2 + "<om:OM_Observation>" + nl);
    			// TODO SAM 2015-12-31 Various metadata could be added but don't have for Pisces
		    		// TODO SAM 2015-12-31 Could add om:phenomenonType
		    		// TODO SAM 2015-12-31 Could add om:resultTime
		    		// TODO SAM 2015-12-31 Could add om:procedure
		    		// TODO SAM 2015-12-31 Could add om:observedProperty
    		// om:observedProperty is link to localDictionary
    		xml.append(indent3 + "<wml2:observedProperty xlink:href=\"#" + site.getSiteId() + "." + tsmeta.getParameter() +
    			"\" xlink:title=\"" + site.getDescription() + "\"/>" + nl);
    		// om:featureOfInterest is link to site (samplingFeatureMember)
    		xml.append(indent3 + "<om:featureOfInterest xlink:href=\"#" + site.getSiteId() +
    			"\" xlink:title=\"\"/>" + nl);
			// om:result - time series 
			xml.append(indent3 + "<om:result>" + nl);
				// TODO SAM 2015-12-31 could add wml2:temporalExtent
			xml.append(indent4 + "<wml2:MeasurementTimeSeries gml:id=\"" + tsmeta.getTsid() + "\">" + nl);
				// TODO SAM 2015-12-31 could add wml2:metadata
					// TODO SAM 2015-12-31 could add wml2:MeasurementTimeSeriesMetadata
						// TODO SAM 2015-12-31 could add wml2:cumulative
						// TODO SAM 2015-12-31 could add wml2:aggregationDuration
			// Data for the quality, etc.
			xml.append(indent5 + "<wml2:defaultPointMetadata>" + nl);
				// TODO SAM 2015-12-31 how does this apply other than units?
			xml.append(indent6 + "<wml2:uom uom=\"" + tsmeta.getUnits() + "\"/>" + nl );
			xml.append(indent5 + "</wml2:defaultPointMetadata>" + nl);
			// Period for output will be requested period or default to time series full period
	    	Date outputStart = null;
	    	if ( queryStart != null ) {
	    		outputStart = queryStart;
	    	}
	    	else {
	    		// Get the earliest date from time series
	    		outputStart = ts.determineEarliestDate(ts);
	    	}
	    	Date outputEnd = null;
	    	if ( queryEnd != null ) {
	    		outputEnd = queryEnd;
	    	}
	    	else {
	    		// Get the latest date from time series
	    		outputEnd = ts.determineLatestDate(ts);
	    	}
	    	// Time series metadata
    		boolean doDay = false;
    		boolean doMonth = false;
    		boolean doYear = false;

    		if ( tsmeta.getInterval().equalsIgnoreCase("day") ) {
    			doDay = true;
    		}
    		/*
    		else {
    			xml.append("<error>Only daily interval data are supported</error>");
                return Response.status(500).
                		entity(waterml20.toString()).
                    	header("Access-Control-Allow-Origin","*").
                    	header("Access-Control-Allow-Methods","GET").
                    	build();
    		}*/
    		else if ( tsmeta.getInterval().equalsIgnoreCase("month") ) {
    			doMonth = true;
    		}
    		else if ( tsmeta.getInterval().equalsIgnoreCase("year") ) {
    			doYear = true;
    		}
    	
			// Loop through the requested period of record
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
	    	TSData tsdata2 = null;
	    	DecimalFormat valueFormat = new DecimalFormat("###.00");
	    	Double value;
	    	//xml.append("DEBUG:  outputStart=" + outputStart + ", outputEnd=" + outputEnd + ", interval=" + tsmeta.getInterval() + "\nl");
	    	while (!gcal.getTime().after(outputEnd) ) {
	    	    Date d = gcal.getTime();
    	    	tsdata2 = ts.lookupTsdata(d);
    	    	if ( tsdata2 != null ) {
    	    		value = tsdata2.getValue();
    	    		if ( (value == null) || value.isNaN() ) {
    	    			// No need to output
    	    			;
    	    		}
    	    		else {
    	    			// wml2:point (element under wml2:MeasurementTimeSeries
    	    			xml.append(indent5 + "<wml2:point>" + nl );
    	    			xml.append(indent6 + "<wml2:MeasurementTVP>" + nl );
    	    			xml.append(indent7 + "<wml2:time>" + dateFormat.format(d) + "</wml2:time>" + nl );
    	    			xml.append(indent7 + "<wml2:value>" + valueFormat.format(value) + "</wml2:value>" + nl );
    	    			// TODO SAM 2015-12-31 what about flag?
    	    			xml.append(indent6 + "</wml2:MeasurementTVP>" + nl );
    	    			xml.append(indent5 + "</wml2:point>" + nl );
		    		}
    	    	}
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
	    	    else {
	    	    	// Unknown interval so break to avoid infinite loop
	    	    	break;
	    	    }
    		}
    	    xml.append(indent4 + "</wml2:MeasurementTimeSeries>" + nl);
    	    xml.append(indent3 + "</om:result>" + nl);
	    	xml.append(indent2 + "</om:OM_Observation>" + nl);
	    	xml.append(indent1 + "</wml2:observationMember>" + nl);
    	}
    	xml.append( "</wml2:Collection>\n" );
		return xml.toString();
	}
}