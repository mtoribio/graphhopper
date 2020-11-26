package com.graphhopper.farmy;

import com.graphhopper.GraphHopperAPI;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;

public class SingleRouteOptimizer extends BaseRouteOptimizer {

    public SingleRouteOptimizer(GraphHopperAPI graphHopper, FarmyOrder[] farmyOrders, FarmyVehicle[] farmyVehicles, IdentifiedGHPoint3D depotPoint) throws Exception {
        super(graphHopper, farmyOrders, farmyVehicles, depotPoint);
    }

    protected VehicleImpl buildVehicle(FarmyVehicle vehicle) {
        VehicleType type = VehicleTypeImpl.Builder.newInstance(String.format("[TYPE] #%s", vehicle.getId()))
                .setFixedCost(vehicle.getFixedCosts()) //Fixe Bedienzeit
                .setCostPerDistance(vehicle.getCostsPerDistance())
                .setCostPerTransportTime(vehicle.getCostsPerTransportTime())
                .setCostPerServiceTime(vehicle.getCostsPerServiceTime())
                .setCostPerWaitingTime(vehicle.getCostPerWaitingTime())
                .addCapacityDimension(0, 99999999)
                .setMaxVelocity(vehicle.isPlus() ? 50.0/3.6 : 80.0/3.6) // ~50km/h // ~80km/h // 1 ~= 3.85
                .build();

        return VehicleImpl.Builder.newInstance(String.format("%s", vehicle.getId()))
                .setStartLocation(this.getDepotLocation())
                .setEndLocation(this.getDepotLocation())
                .setReturnToDepot(vehicle.isReturnToDepot())
                .setEarliestStart(vehicle.getEarliestDeparture()) // 14:00
                .setLatestArrival(vehicle.getLatestArrival())
                .setType(type)
                .build();


    }

    protected Service buildService(IdentifiedGHPoint3D point) {
        // setting timeWindowEnd to Inf to remove constraint of time window ()
        point.setTimeWindow(point.getTimeWindow().getStart(), point.getTimeWindow().getEnd() + 100000000);
        return Delivery.Builder.newInstance(point.getId())
                .addSizeDimension(0, (int) point.getWeight())
                .setLocation(Location.Builder.newInstance().setId(point.getId()).setCoordinate(
                        Coordinate.newInstance(point.getLat(), point.getLon())).build()
                )
                .setTimeWindow(point.getTimeWindow())
                .setServiceTime(point.getServiceTime())
                .build();
    }

}
