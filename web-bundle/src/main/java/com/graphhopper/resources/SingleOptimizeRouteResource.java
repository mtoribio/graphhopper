package com.graphhopper.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.farmy.FarmyOrder;
import com.graphhopper.farmy.FarmyVehicle;
import com.graphhopper.farmy.IdentifiedGHPoint3D;
import com.graphhopper.farmy.RouteOptimize;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;

@Path("single-optimize-route")
public class SingleOptimizeRouteResource {

    private final GraphHopperAPI graphHopperAPI;

    @Inject
    public SingleOptimizeRouteResource(GraphHopperAPI graphHopperAPI) {
        this.graphHopperAPI = graphHopperAPI;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(@FormDataParam("orders") String farmyOrdersStr,
                           @FormDataParam("vehicles") String farmyVehicleStr,
                           @FormDataParam("startLocation") String depotPointStr) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        FarmyOrder[] farmyOrders = mapper.readValue(farmyOrdersStr, FarmyOrder[].class);
        FarmyVehicle[] farmyVehicles = mapper.readValue(farmyVehicleStr, FarmyVehicle[].class);

        IdentifiedGHPoint3D depotPoint = new IdentifiedGHPoint3D(mapper.readValue(depotPointStr, ArrayList.class), "Depot");

        RouteOptimize routeOptimize = null;
        try {
            routeOptimize = new RouteOptimize(this.graphHopperAPI, farmyOrders, farmyVehicles, depotPoint);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
        return Response.ok().entity(routeOptimize.getOptimizedRoutes().toString()).build();
    }
}
