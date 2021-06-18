package com.graphhopper.farmy;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.util.Arrays;

public class DistanceConstraint implements HardActivityConstraint {

    private final StateManager stateManager;

    private final VehicleRoutingTransportCostsMatrix costsMatrix;

    private final FarmyVehicle[] farmyVehicles;

    //        private final StateFactory.StateId distanceStateId; //v1.3.1
    private final StateId distanceStateId; //head of development - upcoming release (v1.4)

    //        DistanceConstraint(double maxDistance, StateFactory.StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix costsMatrix) { //v1.3.1
    DistanceConstraint(FarmyVehicle[] farmyVehicles, StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts) { //head of development - upcoming release (v1.4)
        this.costsMatrix = transportCosts;
        this.farmyVehicles = farmyVehicles;
        this.stateManager = stateManager;
        this.distanceStateId = distanceStateId;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        double additionalDistance = getDistance(prevAct, newAct) + getDistance(newAct, nextAct) - getDistance(prevAct, nextAct);
        Double routeDistance = this.stateManager.getRouteState(iFacts.getRoute(), this.distanceStateId, Double.class);
        FarmyVehicle farmyVehicle = getVehicle(iFacts.getNewVehicle().getId());
        if (routeDistance == null) routeDistance = 0.;
        double newRouteDistance = routeDistance + additionalDistance;
        if (newRouteDistance > farmyVehicle.getMaxDistance()) {
            return ConstraintsStatus.NOT_FULFILLED;
        } else return ConstraintsStatus.FULFILLED;
    }

    double getDistance(TourActivity from, TourActivity to) {
        return this.costsMatrix.getDistance(from.getLocation().getId(), to.getLocation().getId());
    }

    FarmyVehicle getVehicle(String vehicleId) {
        return Arrays.stream(farmyVehicles).filter(farmyVehicle -> farmyVehicle.getId().equals(Integer.parseInt(vehicleId))).findAny().get();
    }
}