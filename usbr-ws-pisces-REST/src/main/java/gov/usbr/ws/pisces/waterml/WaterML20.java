package gov.usbr.ws.pisces.waterml;

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import gov.usbr.ws.pisces.TS;
import gov.usbr.ws.pisces.TSData;

import net.opengis.gml.v_3_2_1.FeatureCollectionType;
import net.opengis.gml.v_3_2_1.FeaturePropertyType;
import net.opengis.gml.v_3_2_1.ResultType;
import net.opengis.gml.v_3_2_1.TimePositionType;
import net.opengis.om.v_2_0.OMObservationPropertyType;
//import net.opengis.sampling.v_2_0.SFSamplingFeatureCollectionType;
//import net.opengis.waterml.v_2_0.CollectionType;
//import net.opengis.waterml.v_2_0.DocumentMetadataType;
//import net.opengis.waterml.v_2_0.MeasureTVPType;
//import net.opengis.waterml.v_2_0.MeasurementTSMetadataPropertyType;
//import net.opengis.waterml.v_2_0.MeasurementTimeseriesType;
//import net.opengis.waterml.v_2_0.MonitoringPointType;
//import net.opengis.waterml.v_2_0.ObservationMetadataType;
//import net.opengis.waterml.v_2_0.SamplingFeatureMemberPropertyType;
//import net.opengis.waterml.v_2_0.CollectionType.LocalDictionary;
//import net.opengis.waterml.v_2_0.MeasurementTimeseriesType.Point;

/**
 * Class to hold WaterML 2.0 XML marshalling code as a non-working artifact until full java.xml.bind approach can be implemented.
 * THIS CODE DOES NOT WORK.
 * @author sam
 *
 */
public class WaterML20 {
	
	/**
	 * 
	 */
	public WaterML20 () {
		
	}
	
	public String marshalXml ( List<TS> tslist ) {
		/*
    	// WaterML is organized by location first, so get the unique list of siteIds from the time series
    	// Example for instantaneous values is:  http://waterservices.usgs.gov/nwis/iv/?format=waterml,2.0&sites=06752280,06752260&startDT=2015-12-21&endDT=2015-12-28&parameterCd=00060,00065
    	// Example for daily values is:  http://waterservices.usgs.gov/nwis/dv/?format=waterml,2.0&sites=06752280,06752260&startDT=2015-12-21&endDT=2015-12-28&parameterCd=00060,00065

    	// Add WaterML top-level wrapper objects - try a couple of variations to see what works

    	// Seems to match WaterML documentation examples:
    	// - http://external.opengeospatial.org/twiki_public/WaterML/WaterML2GettingStarted
    	CollectionType collection = new CollectionType();
    	List<LocalDictionary> localDictionaryList = new ArrayList<LocalDictionary>();
    	collection.setLocalDictionary(localDictionaryList);
		List<SamplingFeatureMemberPropertyType> samplingFeatureMemberList = new ArrayList<SamplingFeatureMemberPropertyType>();
		//collection.setSamplingFeatureMember(samplingFeatureMemberList);
    	List<OMObservationPropertyType> observationMemberList = new ArrayList<OMObservationPropertyType>();
		//collection.setObservationMember(observationMemberList);

		// Seems to match USGS...
    	// gml:FeatureCollection
    	FeatureCollectionType featureCollection = new FeatureCollectionType();
    	List<FeaturePropertyType> featureList = new ArrayList<FeaturePropertyType>();
		featureCollection.setFeatureMember(featureList);
    	
    	// Get the unique sites that are involved with the time series
    	List<String> siteIdList = determineUniqueSiteIdList ( tslist );
    	
    	for ( String siteId2: siteIdList ) {
    		// Get the time series that are for the siteId
    		List<TS> tslistForSiteId = getTimeSeriesForSiteId ( tslist, siteId2 );
    		if ( tslistForSiteId.size() == 0 ) {
    			// No time series for site so don't process
    			continue;
    		}
    		if ( 1 == 1 ) {
    			continue;
    		}
    		// Add the site
    		// gml:featureMember (element of gml:FeatureCollection)
        	//SamplingFeatureMemberPropertyType featureMember = new SamplingFeatureMemberPropertyType();
    		FeaturePropertyType featureProperty = new FeaturePropertyType();
        	featureList.add(featureProperty);
        	// Add a collection of locations
        	// wml2:Collection (element of gml:featureMember)
        	//featureProperty.set
        	//SFSamplingFeatureCollectionType collection = new SFSamplingFeatureCollectionType();
    		// Loop through the time series
        	// First add the metadata for sites
        	for ( TS ts : tslistForSiteId ) {
        		//SFSamplingFeatureType samplingFeatureType = new SFSamplingFeatureType();
        		SamplingFeatureMemberPropertyType samplingFeatureType = new SamplingFeatureMemberPropertyType();
        		SFSamplingFeatureCollectionType samplingFeatureCollection = new SFSamplingFeatureCollectionType();
        		samplingFeatureType.setSFSamplingFeatureCollection(samplingFeatureCollection);
        		samplingFeatureMemberList.add(samplingFeatureType);
        	}
    		for ( TS ts : tslistForSiteId ) {
    			tsmeta = ts.getTsmeta();
    			// om:result
    			ResultType result = new ResultType();
    			// wml2:MeasurementTimeSeries (element under om:results)
    			MeasurementTimeseriesType measurementTimeSeries = new MeasurementTimeseriesType();
    			result.setAny ( measurementTimeSeries );
    			// TODO SAM 2015-12-28 Could also add these...
    			// wml2:defaultPointMetadata (element under wml2:MeasurementTimeseries)
    			
    			// wml2:point is a sequence under wml2:MeasurementTimeSeries (add list and then add points to list below)
    			List<Point> pointList = new ArrayList<Point>();
    			measurementTimeSeries.setPoint(pointList);
    			// Period for output will be requested period or default to time series full period
    	    	Date outputStart = null;
    	    	if ( queryStart != null ) {
    	    		outputStart = queryStart;
    	    	}
    	    	else {
    	    		// Get the earliest date from time series
    	    		outputStart = determineEarliestDate(ts);
    	    	}
    	    	Date outputEnd = null;
    	    	if ( queryEnd != null ) {
    	    		outputEnd = queryEnd;
    	    	}
    	    	else {
    	    		// Get the latest date from time series
    	    		outputEnd = determineLatestDate(ts);
    	    	}
    	    	// Time series metadata
        		boolean doDay = false;
        		boolean doMonth = false;
        		boolean doYear = false;
        		if ( tsmeta.getInterval().equalsIgnoreCase("day") ) {
        			doDay = true;
        		}
        		else {
        			waterml20.append("<error>Only daily interval data are supported</error>");
                    return Response.status(500).
                    		entity(waterml20.toString()).
                        	header("Access-Control-Allow-Origin","*").
                        	header("Access-Control-Allow-Methods","GET").
                        	build();
        		}
        		// TODO SAM 2015-12-28 only daily supported
        		//else if ( tsmeta.getInterval().equalsIgnoreCase("month") ) {
        		//	doMonth = true;
        		//}
        		//else if ( tsmeta.getInterval().equalsIgnoreCase("year") ) {
        		//	doYear = true;
        		//}
        	
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
    	    	int tslistSize = tslist.size();
    	    	TSData tsdata2;
    	    	DecimalFormat valueFormat = new DecimalFormat("###.00");
    	    	Double value;
    	    	while (!gcal.getTime().after(outputEnd) ) {
    	    	    Date d = gcal.getTime();
	    	    	tsdata2 = ts.lookupTsdata(d);
	    	    	if ( tsdata2 == null ) {
			    		// No need to output
	    	    		continue;
	    	    	}
	    	    	else {
	    	    		value = tsdata2.getValue();
	    	    		if ( (value == null) || value.isNaN() ) {
	    	    			// No need to output
	    	    			continue;
	    	    		}
	    	    		else {
	    	    			// wml2:point (element under wml2:MeasurementTimeSeries
	    	    			Point point = new Point();
	    	    			pointList.add(point);
	    	    			// wml2:MeasurementTVP (element under wml2:point)
	    	    			MeasureTVPType measureTvp = new MeasureTVPType();
	    	    			point.setMeasurementTVP(measureTvp);
	    	    			// wml2:time (element under wml:MeasurementTVP
	    	    			TimePositionType timePositionType = new TimePositionType();
	    	    			List<String> s = new ArrayList<String>();
	    	    			timePositionType.setValue(s);
	    	    			measureTvp.setTime(timePositionType);
	    	    			//JAXBElement<String> measureType = new JAXBElement<String>(
	    	    			//	new QName(String.getClass.getSimpleName()), String.class, "" + value );
	    	    			//measureType.setValue(measureType);
	    	    			//measureTvp.setValue(measureType);
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
    	    	}
    		}
    		//collection
    		//Observation
    		ObservationMetadataType observationMember = new ObservationMetadataType();
    		DocumentMetadataType documentMetadata = new DocumentMetadataType();
    		MeasurementTSMetadataPropertyType measurementTSMetadataProperty = new MeasurementTSMetadataPropertyType();
    		MeasurementTimeseriesType measurementTimeseries = new MeasurementTimeseriesType();
    		ResultType result = new ResultType();
        	MonitoringPointType monitoringPoint = new MonitoringPointType();
        	//monitoringPoint.setId(monitoringPoint_Id);
        	
        	try {
	    		//waterml20.append("DEBUG:  Before marshal code");
	    		WaterML20 waterml20Instance = new WaterML20();
	    		waterml20.append ( waterml20Instance.marshalXml(tslist) );
	    		boolean doCollection = true;
	    		if ( doCollection ) {
	    			// Collection
		    		//JAXBContext context = JAXBContext.newInstance(CollectionType.class);
		    		JAXBContext context = JAXBContext.newInstance(
		    			"net.opengis.waterml.v_2_0:"
		    			+ "net.opengis.sampling.v_2_0:"
		    			+ "net.opengis.gml.v_3_2_1:"
		    			+ "net.opengis.samplingspatial.v_2_0:"
		    			+ "net.opengis.om.v_2_0:"
		    			+ "net.opengis.swecommon.v_2_0");
		    		Marshaller marshaller = context.createMarshaller();
		    		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		    		StringWriter stringWriter = new StringWriter();
		    		//ObjectFactory objectFactory = new ObjectFactory();
		    		//JAXBElement<CollectionType> je = objectFactory.createCollectionType(collection);
		    		marshaller.marshal(collection,stringWriter);
		    		waterml20.append(stringWriter.getBuffer().toString());
		    		stringWriter.close();
	    		}
	    		else {
	    			// FeatureCollection
		    		JAXBContext context = JAXBContext.newInstance(FeatureCollectionType.class);
		    		Marshaller marshaller = context.createMarshaller();
		    		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		    		StringWriter stringWriter = new StringWriter();
		    		marshaller.marshal(featureCollection,stringWriter);
		    		waterml20.append(stringWriter.getBuffer().toString());
		    		stringWriter.close();
	    		}
	    	}
	    	catch ( JAXBException e ) {
	    		waterml20.append("DEBUG:  Error marshalling XML: " + e );
	            return Response.status(500).
	            		entity(waterml20.toString()).
	                	header("Access-Control-Allow-Origin","*").
	                	header("Access-Control-Allow-Methods","GET").
	                	build();
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
    	*/
		return "";
	}

}
