package org.opendaylight.yangtools;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.opendaylight.yangtools.draft.Draft01;
import org.opendaylight.yangtools.draft.Draft02;

public interface RestRestconfService {

    public static final String XML = "+xml";
    public static final String JSON = "+json";

    @GET
    public Object getRoot();

    @GET
    @Path("/modules")
    @Produces({Draft01.MediaTypes.API+JSON,Draft01.MediaTypes.API+XML,
            Draft02.MediaTypes.API+JSON,Draft02.MediaTypes.API+XML})
    public String getModules();

    @POST
    @Path("/operations/{identifier}")
    @Produces({Draft01.MediaTypes.DATA+JSON,Draft01.MediaTypes.DATA+XML,
            Draft02.MediaTypes.DATA+JSON,Draft02.MediaTypes.DATA+XML,
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Consumes({Draft01.MediaTypes.DATA+JSON,Draft01.MediaTypes.DATA+XML,
            Draft02.MediaTypes.DATA+JSON,Draft02.MediaTypes.DATA+XML,
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public String invokeRpc(@PathParam("identifier") String identifier, @QueryParam("input") String payload);

    @POST
    @Path("/operations/{identifier}")
    @Produces({Draft01.MediaTypes.DATA+JSON,Draft01.MediaTypes.DATA+XML,
            Draft02.MediaTypes.DATA+JSON,Draft02.MediaTypes.DATA+XML,
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public String invokeRpc(@PathParam("identifier") String identifier);

    @GET
    @Path("/config/{identifier:.+}")
    @Produces({Draft02.MediaTypes.DATA+JSON,Draft02.MediaTypes.DATA+XML,
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public String readConfigurationData(@PathParam("identifier") String identifier);

    @GET
    @Path("/operational/{identifier:.+}")
    @Produces({Draft02.MediaTypes.DATA+JSON,Draft02.MediaTypes.DATA+XML,
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public String readOperationalData(@PathParam("identifier") String identifier);

    @PUT
    @Path("/config/{identifier:.+}")
    @Consumes({Draft02.MediaTypes.DATA+JSON,Draft02.MediaTypes.DATA+XML,
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public Response updateConfigurationData(@PathParam("identifier") String identifier,@QueryParam("input") String payload);

    @POST
    @Path("/config/{identifier:.+}")
    @Consumes({Draft02.MediaTypes.DATA+JSON,Draft02.MediaTypes.DATA+XML,
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public Response createConfigurationData(@PathParam("identifier") String identifier, @QueryParam("input") String payload);

    @POST
    @Path("/config")
    @Consumes({Draft02.MediaTypes.DATA+JSON,Draft02.MediaTypes.DATA+XML,
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public Response createConfigurationData( @QueryParam("input") String payload);

    @DELETE
    @Path("/config/{identifier:.+}")
    public Response deleteConfigurationData(@PathParam("identifier") String identifier);


}
