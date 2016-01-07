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
 * Resource exposed at "/v0/parameter" path)
 *  - ignore case in spelling
 */
@Entity
@Immutable
// TODO SAM 2015-12-21 Decide whether need this...if so move the select statement to after annotation
//@Subselect
@Path("{parameter : (?i)parameter}")
public class Parameter {

	/**
	 * Parameter description.
	 */
	private String description = "";
	
	/**
	 * Parameter name.
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
    //@Produces({"text/csv"})
    public Response getParameterCSV(
    	@DefaultValue("") @QueryParam("format") String format
    	) {
    	StringBuilder csv = new StringBuilder ();
    	
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<Parameter> result = readParameters ( session );
    	session.getTransaction().commit();
    	session.close();
    	
    	csv.append("name,description\n");
    	for ( int i = 0; i < result.size(); i++ ) {
    		Parameter parameter = result.get(i);
    		csv.append("\"" + parameter.getName() + "\",");
    		csv.append("\"" + parameter.getDescription() + "\"");
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
    public Response getParameterJSON(
    	@DefaultValue("") @QueryParam("format") String format
    	) {
    	StringBuilder json = new StringBuilder ();
    	
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<Parameter> result = readParameters ( session );
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
    public Response getParameterHTML(
    	@DefaultValue("") @QueryParam("format") String format ) {
    	// If requested format is different, call the other method
    	if ( format.equalsIgnoreCase("csv") ) {
    		return getParameterCSV(format);
    	}
    	else if ( format.equalsIgnoreCase("json") ) {
    		return getParameterJSON(format);
    	}
    	
    	StringBuilder html = new StringBuilder(WebUtil.getPageStart());
    	html.append("<h1>Parameter List</h1><p>");
    	
		Session session = HibernateUtil.getSession();
		session.beginTransaction();
    	List<Parameter> result = readParameters ( session );
    	session.getTransaction().commit();
    	session.close();
    	
    	html.append("<p>" + result.size() + " parameters returned</p>\n");
    	html.append("<p>" + WebUtil.getAlternateFormatHTML(uriInfo,null) + "</p>");
    	
    	html.append("<table>\n");
    	html.append("<tr><th class=\"hasTooltip\">name<span>Name for parameter (data type)</span></th>"
    		+ "<th class=\"hasTooltip\">description<span>Description for parameter (data type)</span></th></tr>\n");
    	for ( int i = 0; i < result.size(); i++ ) {
    		Parameter parameter = result.get(i);
    		html.append("<tr>");
    		html.append("<td>" + parameter.getName() + "</td>");
    		html.append("<td>" + parameter.getDescription() + "</td>");
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
     * Get the parameter name.
     */
    public String getName () {
    	return this.name;
    }
    
    /**
     * Get the parameter description.
     */
    public String getDescription () {
    	return this.description;
    }
    
    /**
     * Read a list of parameters, shared by each get method.
     */
    private List<Parameter> readParameters ( Session session ) {
    	// There is no Parameter definitions table so do a distinct query of view_seriescatalog
    	// to get the "parameter" values and map to this class as Parameter.name
    	// Could be a future table design for Parameter table with name and description columns.
    	SQLQuery query = session.createSQLQuery("select distinct p.parameter as {p.name}, '' as {p.description} "
           		+ "from seriescatalog p where p.parameter <> '' and p.parameter is not null order by p.parameter" );
           	query.addEntity("p",Parameter.class);
        	List<Parameter> result = query.list();
        	return result;
    }
}