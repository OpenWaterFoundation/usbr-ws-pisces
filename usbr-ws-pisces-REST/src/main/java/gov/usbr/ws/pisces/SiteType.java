package gov.usbr.ws.pisces;

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
 * Resource exposed at "/v0/siteType" path)
 *  - ignore case in spelling
 */
@Entity
@Immutable
// TODO SAM 2015-12-21 Decide whether need this...if so move the select statement to after annotation
//@Subselect
@Path("{siteType : (?i)siteType}")
public class SiteType {

	/**
	 * Site type description.
	 */
	private String description = "";
	
	/**
	 * Site type name.
	 */
	@Id
	private String name = "";
	
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
    public Response getSiteTypeCSV(
    	@DefaultValue("") @QueryParam("format") String format
    	) {
    	StringBuilder csv = new StringBuilder ();
    	
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<SiteType> result = readSiteTypes ( session );
    	session.getTransaction().commit();
    	session.close();
    	
    	csv.append("name,description\n");
    	for ( int i = 0; i < result.size(); i++ ) {
    		SiteType siteType = result.get(i);
    		csv.append("\"" + siteType.getName() + "\",");
    		csv.append("\"" + siteType.getDescription() + "\"");
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
     * to the client as "application/json" media type for JSON.
     *
     * @return String that will be returned as a application/json response.
     */
    //@GET
    //@Produces(MediaType.APPLICATION_JSON)
    public Response getSiteTypeJSON(
    	@DefaultValue("") @QueryParam("format") String format
    	) {
    	StringBuilder json = new StringBuilder ();
    	
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<SiteType> result = readSiteTypes ( session );
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
    public Response getSiteTypeHtml(
    	@DefaultValue("") @QueryParam("format") String format ) {
    	// If requested format is different, call the other method
    	if ( format.equalsIgnoreCase("csv") ) {
    		return getSiteTypeCSV(format);
    	}
    	else if ( format.equalsIgnoreCase("json") ) {
    		return getSiteTypeJSON(format);
    	}
    	
    	StringBuilder html = new StringBuilder(WebUtil.getPageStart());
    	html.append("<h1>Site Type List</h1><p>");
    	
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<SiteType> result = readSiteTypes ( session );
    	session.getTransaction().commit();
    	session.close();
    	
    	html.append("<p>" + result.size() + " site types returned</p>\n");
    	html.append("<p>" + WebUtil.getAlternateFormatHTML(uriInfo,null) + "</p>");
    	
    	html.append("<table>\n");
    	html.append("<tr><th class=\"hasTooltip\">name<span>Name for site type</span></th>"
    		+ "<th class=\"hasTooltip\">description<span>Description for site type</span></th></tr>\n");
    	for ( int i = 0; i < result.size(); i++ ) {
    		SiteType siteType = result.get(i);
    		html.append("<tr>");
    		html.append("<td>" + siteType.getName() + "</td>");
    		html.append("<td>" + siteType.getDescription() + "</td>");
    		html.append("</tr>");
    	}
    	html.append("\n</table>\n");
    	
    	// Build and return the response
    	// - HTTP status code 200 means OK
    	// - Access control settings allow CORS access restricted to GET
    	html.append("</p>" + WebUtil.getPageEnd());
        return Response.status(200).
        	entity(html.toString()).
        	header("Access-Control-Allow-Origin","*").
        	header("Access-Control-Allow-Methods","GET").
        	build();
    }
    
    /**
     * Get the site type name.
     */
    public String getName () {
    	return this.name;
    }
    
    /**
     * Get the site type description.
     */
    public String getDescription () {
    	return this.description;
    }
    
    /**
     * Read a list of site types, shared by each get method.
     */
    private List<SiteType> readSiteTypes ( Session session ) {
    	// There is no SiteType definitions table so do a distinct query of sitecatalog
    	// to get the "type" values and map to this class as SiteType.name
    	// Could be a future table design for SiteType table with name and description columns.
    	SQLQuery query = session.createSQLQuery("select distinct s.type as {s.name}, '' as {s.description} "
           		+ "from sitecatalog s where s.type <> '' and s.type is not null order by s.type" );
           	query.addEntity("s",SiteType.class);
        	List<SiteType> result = query.list();
        	return result;
    }
}