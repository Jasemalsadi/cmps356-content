package qu.cms.service;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;

import qu.cms.entity.Contact;
import qu.cms.repository.ContactDBRepository;
import qu.cms.repository.IContactRepository;

@Path("/api/contacts")
public class ContactService {

    IContactRepository contactRepository;
    
    public ContactService() {
    	this.contactRepository = new ContactDBRepository(); //ContactRepository();
    }
    
   
    //You can test it using Postman Chrome App - http://www.getpostman.com/
    //Url: http://localhost:9090/api/contacts using Get request
    //you get either XML if accept="application/xml" or json if accept="application/json"
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Contact> getContacts() {
        return contactRepository.getContacts();
    }

    /*
    @Path("/ui")
    @GET
    @Controller
    @Produces("text/html")
    public String listContacts() {
        List<Contact> contacts = contactRepository.getContacts();
        System.out.println("contacts.size(): " + contacts.size());
        models.put("contacts", contacts);
        return "/contacts.jsp";
    } 
    */
    
    //You can test it using Postman Chrome App - http://www.getpostman.com/
    //Url: http://localhost:9090/api/contacts/1 using Get request
    //you get either XML if accept="application/xml" or json if accept="application/json"
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getContact(@PathParam("id") int contactId) {
        Contact contact = contactRepository.getContact(contactId);
        if (contact != null) {
            return Response.ok(contact).build();
        } else {
            String msg = String.format("Contact # %d not found", contactId);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
    }

    /*You can test it using Postman Chrome App - http://www.getpostman.com/
     Url: http://localhost:9090/api/contacts using Post request
     You can either post XML by setting the Content-Type="application/xml":
     <?xml version="1.0" encoding="UTF-8" standalone="yes"?><ns2:contact xmlns:ns2="qu.cms.model"><firstName>Samir</firstName><lastName>Ali</lastName><address><street>15 Fun St</street><city>Doha</city><country>Qatar</country></address></ns2:contact>
     * 
     Or you can post json by setting the Content-Type="application/json"
     {"firstName":"Ali","lastName":"Saleh","address":{"street":"15 Fun St","city":"Doha","country":"Qatar"}}
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addContact(Contact contact) {
        try {
            contact = contactRepository.addContact(contact);
            String location = String.format("/cms/api/contacts/%s", contact.getId());
            String msg = String.format("contact #%d created successfully", contact.getId());
            return Response.created(new URI(location)).entity(msg).build();
        } catch (Exception ex) {
            String msg = String.format("Adding contact failed because : %s",
                    ex.getCause().getMessage());
            return Response.serverError().entity(msg).build();
        }
    }

    /*You can test it using Postman Chrome App - http://www.getpostman.com/
     Url: http://localhost:9090/api/contacts using Put request
     You can either post XML by setting the Content-Type="application/xml":
     <?xml version="1.0" encoding="UTF-8" standalone="yes"?><ns2:contact xmlns:ns2="qu.cms.model"><contactId>2</contactId><firstName>Mariam</firstName><lastName>Ahmed</lastName><address><addressId>2</addressId><street>11 Fun St</street><city>Kuwait City</city><country>Kuwait</country></address></ns2:contact>
     * 
     Or you can post json by setting the Content-Type="application/json"
     {"contactId":1,"firstName":"Samir","lastName":"Ali","address":{"addressId":1,"street":"15 History St","city":"Fes","country":"Morocco"}
     */
    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateContact(@PathParam("id") int contactId, Contact contact) {
        contact.setId(contactId);
        try {
            contactRepository.updateContact(contact);
            String msg = String.format("Contact #%s updated sucessfully", contact.getId());
            return Response.ok(msg).build();
        } catch (Exception ex) {
            String msg = String.format("Updating contact failed because : \n%s",
                    ex.getMessage());
            return Response.serverError().entity(msg).build();
        }
    }

    /*You can test it using Postman Chrome App - http://www.getpostman.com/
     Url: http://localhost:9090/api/contacts/1 using Delete request
     */
    @DELETE
    @Path("/{id}")
    public Response deleteContact(@PathParam("id") int contactId) {
        try {
            contactRepository.deleteContact(contactId);
            String msg = String.format("Contact #%s deleted sucessfully", contactId);
            return Response.ok(msg).build();
        } catch (Exception ex) {
            String msg = String.format("Deleting contact failed because : %s",
                    ex.getCause().getMessage());
            return Response.serverError().entity(msg).build();
        }
    }
    
    @GET
    @Path("/countries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContries() {
        List<String> countries = contactRepository.getCountries();
        System.out.println("countries count " + countries.size());
        //This is a workaround to serialize a list of strings to json 
        Gson gson = new Gson();
        String json = gson.toJson(countries);
        return Response.ok(json).build();
    }
    
    @GET
    @Path("countries/{country}/cities")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCities(@PathParam("country") String countryCode) {
        System.out.println("ContactService.getCities(coutryCode) :" + countryCode);
        List<String> cities = contactRepository.getCities(countryCode);
        System.out.println("cities count " + cities.size());
        //This is a workaround to serialize a list of strings to json 
        Gson gson = new Gson();
        String json = gson.toJson(cities);
        return Response.ok(json).build();
    }
}
