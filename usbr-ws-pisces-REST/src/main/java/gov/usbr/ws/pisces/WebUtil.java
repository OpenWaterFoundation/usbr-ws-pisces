package gov.usbr.ws.pisces;

import javax.ws.rs.core.UriInfo;

/**
 * Methods to help in constructing web pages.
 * @author sam
 *
 */
public class WebUtil {

	/**
	 * Root URL for website under which all files are located.
	 */
	//private static String webroot = "/";
	public static String WEBROOT_VERSION = "v0";
	public static String WEBROOT = "/usbr-ws-pisces";
	public static String WEBROOT_WITH_VERSION = "/usbr-ws-pisces/" + WEBROOT_VERSION;
	
	/**
	 * Logo to display on website.
	 * - TODO SAM 2015-12-22 would like to be relative to root on site but
	 *   not able to pass static content independent of web service
	 */
	private static String logo = "http://www.usbr.gov/history/images/bor1994.jpg";
	//private static String logo = webroot + "/bor1994.jpg";
	
	/**
	 * Get HTML content listing alternate formats for output.  For example if current output is html,
	 * list alternates as csv and json.
	 * @param uriInfo information about the URI that was used with the calling web service request
	 * @param alternateFormats a list of alternate formats to use (defaults to "csv" and "json").
	 */
	public static String getAlternateFormatHTML ( UriInfo uriInfo, String [] alternateFormats ) {
		// First identify if URI has ?format=X or &format=X.
		// If matched, remove other format and provide links to others
		String uriRequested = uriInfo.getRequestUri().toString();
		int pos = uriRequested.indexOf("format=");
		// Want to keep query parameters from previously requested URI but provide different formats
		// This method should only be returned when HTML was displayed in the first place because other
		// formats lack a hyperlink capability
		String uriClean = uriRequested; 
		if ( pos > 0 ) {
			// previous request included format string so strip out
			int pos2 = uriRequested.indexOf("&",(pos + 1)); // Does another query parameter follow?
			if ( uriRequested.charAt(pos - 1) == '&' ) {
				// Was 2nd or later parameter
				// Copy everything before &format=xxxxx
				uriClean = uriRequested.substring(0,pos - 1);
				if ( pos2 > 0 ) {
					// Append previous end including leading &
					uriClean += uriRequested.substring(pos2);
				}
			}
			else if ( uriRequested.charAt(pos - 1) == '?' ) {
				// Format was at beginning.
				// Copy everything before ?format=xxxxx
				uriClean = uriRequested.substring(0,pos - 1);
				if ( pos2 > 0 ) {
					// Append previous end and re-add remaining parameters but switch & to ? at start
					uriClean += "?" + uriRequested.substring(pos2 + 1);
				}
			}
		}
		StringBuilder b = new StringBuilder();
		b.append("Alternate formats:");
		// Append to URL with previous query criteria but alternate formats
		String s = "?";
		if ( uriClean.indexOf("?") > 0 ) {
			s = "&";
		}
		if ( (alternateFormats == null) || (alternateFormats.length == 0) ) {
			// No alternate formats were specified so use defaults
			alternateFormats = new String [2];
			alternateFormats[0] = "csv";
			alternateFormats[1] = "json"; // TODO add "xml"
		}
		for ( int i = 0; i < alternateFormats.length; i++ ) {
			// Standard default formats
			b.append( "&nbsp&nbsp<a href=\"" + uriClean + s + "format=" + alternateFormats[i] + "\">" + alternateFormats[i] + "</a>");
		}
		return b.toString();
	}
	
	/**
	 * Return a simple web page footer.
	 * TODO SAM 2015-12-21 replace with Reclamation preferences
	 */
	public static String getPageEnd () {
		return "<br>\n"
				+ "<hr>\n"
				+ "<p><a href=\"" + WEBROOT + "/v0\">Web Service Documentation</a> - site created by "
						+ " <a href=\"http://openwaterfoundation.org\">Open Water Foundation</a></p>"
				+ "</body>\n"
				+ "</html>";
	}
	
	/**
	 * Return a simple web page header.
	 * TODO SAM 2015-12-21 replace with Reclamation preferences
	 */
	public static String getPageStart () {
		return "<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + WEBROOT + "/v0/ws.css\">\n"
				+ "<title>Reclamation Water Data Web Services</title>\n"
				+ "</head>\n"
				+ "<body>\n"
				+ "<h1><img src=\"" + logo + "\" alt=\"Reclamation\" style=\"height:100px;\">&nbsp&nbsp&nbsp Reclamation Water Data Web Services</h1>\n"
				+ "<hr>\n";
	}
}