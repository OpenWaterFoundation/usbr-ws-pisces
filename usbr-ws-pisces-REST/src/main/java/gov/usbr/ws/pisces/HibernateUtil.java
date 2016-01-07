package gov.usbr.ws.pisces;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Utilities to use with Hibernate persistence.
 * @author sam
 *
 */
public class HibernateUtil {

	/**
	 * Session factory used to get sessions for database interaction.
	 */
	private static SessionFactory sessionFactory = null;
	
	/**
	 * Indicate whether debug is enabled.  Turn this off in the production environment.
	 */
	public static boolean DEBUG_ENABLED = true;
	
	/**
	 * Maximum number of time series to return.
	 */
	public static int MAX_TS_QUERY = 500;
	
	/**
	 * Append a string to a where clause if not empty.
	 * @param where StringBuilder to contain where clause of form WHERE X = ? - will be updated.
	 * @param whereParameterNames empty list of parameter names that may later be used by setWhereParameters()
	 * @param whereParameterValues empty list of parameter values that will later be set to ? with SQLQuery.setParameter() - will be updated in setWhereParameters().
	 * @param parameterName table column name
	 * @param parameterValue value to match
	 */
	public static void appendWhere ( StringBuilder where, List<String> whereParameterNames,
		List<String> whereParameterValues, String parameterName, String parameterValue ) {
		if ( where == null ) {
			where = new StringBuilder();
		}
		// Don't add if null, blank or * (* equivalent to get all so no need for filter)
		if ( (parameterValue != null) && !parameterValue.isEmpty() && !parameterValue.equals("*") ) {
			if ( where.length() == 0 ) {
				where.append(" where ");
			}
			else {
				where.append(" and ");
			}
			if ( parameterValue.startsWith("*") || parameterValue.endsWith("*") ) {
				// Wildcard match
				where.append ( parameterName + " like ?" );
			}
			else if ( parameterName.toUpperCase().endsWith("SITEID") && (parameterValue.indexOf(",") > 0) ) {
				// Allow comma-separated list of siteId - have to add list by name later in setWhereParameters
				where.append ( parameterName + " in (:siteIdList)" );
			}
			else if ( parameterName.toUpperCase().endsWith("PARAMETER") && (parameterValue.indexOf(",") > 0) ) {
				// Allow comma-separated list of parameter - have to add list by name later in setWhereParameters
				where.append ( parameterName + " in (:parameterList)" );
			}
			else {
				// Need exact match
				where.append ( parameterName + "=?" );
			}
			whereParameterNames.add(parameterName);
			whereParameterValues.add(parameterValue);
		}
	}
	
    /**
     * Return the time series interval used for external purposes, which converts:
     *  Daily->day, Monthly->month, Yearly->year
     *  @param timeinterval Pisces time series data interval "Daily", "Monthly", or "Yearly"
     *  @return display interval "day", "month", or "year"
     */
    public static String getIntervalExternal ( String timeinterval ) {
    	if ( timeinterval.equalsIgnoreCase("Daily") ) {
    		return "day";
    	}
    	else if ( timeinterval.equalsIgnoreCase("Monthly") ) {
    		return "month";
    	}
    	else if ( timeinterval.equalsIgnoreCase("Yearly") ) {
    		return "year";
    	}
    	else {
    		return timeinterval;
    	}
    }
    
    /**
     * Return the time series interval used for internal purposes, which converts:
     *  day->Daily, month->Monthly, year->Yearly
     *  @param interval time interval string "day", "month", or "year"
     *  @return Pisces database time interval string "Daily", "Monthly", or "Yearly"
     */
    public static String getIntervalInternal ( String interval ) {
    	if ( interval.equalsIgnoreCase("day") ) {
    		return "Daily";
    	}
    	else if ( interval.equalsIgnoreCase("month") ) {
    		return "Monthly";
    	}
    	else if ( interval.equalsIgnoreCase("year") ) {
    		return "Yearly";
    	}
    	else {
    		return interval;
    	}
    }
    
	/**
	 * Return the Hibernate session to use, which manages the database connection.
	 * @return session that can be used for database interactions
	 */
	public static Session getSession () {
		if ( sessionFactory == null ) {
			init();
		}
		return sessionFactory.openSession();
	}
	
	/**
	 * Initialize the database connections at startup, called from getSession() on first call.
	 */
	private static void init () {
		// A SessionFactory is set up once for an application!
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
				.configure() // configures settings from hibernate.cfg.xml
				.build();
		try {
			sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
		}
		catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
			// so destroy it manually.
			System.out.println( e );
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}
	
	/**
	 * Parse a date time string YYYY, YYYY-MM, or YYYY-MM-DD used with "ts" service.
	 * @param s string to parse
	 * @return Date parsed from string
	 */
	public static Date parseDatetime ( String s ) {
    	SimpleDateFormat sdf = null;
    	if ( (s == null) || s.isEmpty() ) {
    		return null;
    	}
    	if ( s.length() == 10 ) {
    		// Assume day format
    		sdf = new SimpleDateFormat("yyyy-MM-dd");
    	}
    	else if ( s.length() == 7 ) {
    		// Assume month format
    		sdf = new SimpleDateFormat("yyyy-MM");
    	}
    	else if ( s.length() == 4 ) {
    		// Assume year format
    		sdf = new SimpleDateFormat("yyyy");
    	}
    	Date d = null;
    	try {
    		d = sdf.parse(s);
    	}
    	catch ( ParseException e ) {
    		return null;
    	}
    	return d;
	}
	
	/**
	 * Set the parameterized where clause values in the query.
	 * This translates * query parameters to LIKE % % syntax and otherwise assumes =.
	 * @param query SQLQuery instance to modify
	 * @param whereParameterNames list of where parameter names that were constructed by appendWhere().
	 * @param whereParameterValues list of where parameter values that were constructed by appendWhere().
	 */
	public static void setWhereParameters ( SQLQuery query, List<String> whereParameterNames, List<String> whereParameterValues, boolean doDebug, StringBuilder html ) {
	   	for ( int i = 0; i < whereParameterValues.size(); i++ ) {
		   	String wildcardBefore = "";
		   	String wildcardAfter = "";
		   	String whereParameterName = whereParameterNames.get(i);
		   	String whereParameterValue = whereParameterValues.get(i);
		   	if ( whereParameterName.toUpperCase().endsWith("SITEID") && (whereParameterValue.indexOf(",") > 0) ) {
		   		// siteId is allowed to be specified with comma-separated list
		   		String [] whereParts = whereParameterValue.split(",");
		   		List<String> whereList = new ArrayList<String>();
		   		for ( int ip = 0; ip < whereParts.length; ip++ ) { 
		   			whereList.add(whereParts[ip].trim() );
		   		}
		   		query.setParameterList("siteIdList", whereList );
		   	}
		   	if ( whereParameterName.toUpperCase().endsWith("PARAMETER") && (whereParameterValue.indexOf(",") > 0) ) {
		   		// parameter is allowed to be specified with comma-separated list
		   		String [] whereParts = whereParameterValue.split(",");
		   		List<String> whereList = new ArrayList<String>();
		   		for ( int ip = 0; ip < whereParts.length; ip++ ) { 
		   			whereList.add(whereParts[ip].trim() );
		   		}
		   		query.setParameterList("parameterList", whereList );
		   	}
		   	else {
		   		// Generic handling
			   	if ( whereParameterValue.startsWith("*") ) {
			   		wildcardBefore = "%";
			   		whereParameterValue = whereParameterValue.substring(1);
			   	}
			   	if ( whereParameterValue.endsWith("*") ) {
			   		wildcardAfter = "%";
			   		whereParameterValue = whereParameterValue.substring(0,(whereParameterValue.length()-1));
			   	}
			   	String where = wildcardBefore + whereParameterValue + wildcardAfter;
			   	if ( doDebug && html != null ) {
			   		html.append("DEBUG:  Adding where:  " + where + "<br>");
			   	}
				query.setParameter(i, where );
		   	}
	   	}
	}
}