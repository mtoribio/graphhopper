package com.graphhopper.farmy;

import com.graphhopper.GraphHopperAPI;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;

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

}
