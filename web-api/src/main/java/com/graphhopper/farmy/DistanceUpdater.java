package com.graphhopper.farmy;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

public class DistanceUpdater implements StateUpdater, ActivityVisitor {

    private final StateManager stateManager;

    private final VehicleRoutingTransportCostsMatrix costMatrix;

    //        private final StateFactory.StateId distanceStateId;    //v1.3.1
    private final StateId distanceStateId; //head of development - upcoming release

    private VehicleRoute vehicleRoute;

    private double distance = 0.;

    private TourActivity prevAct;

    //        public DistanceUpdater(StateFactory.StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix costMatrix) { //v1.3.1
    public DistanceUpdater(StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts) { //head of development - upcoming release (v1.4)
        this.costMatrix = transportCosts;
        this.stateManager = stateManager;
        this.distanceStateId = distanceStateId;
    }

    @Override
    public void begin(VehicleRoute vehicleRoute) {
        distance = 0.;
        prevAct = vehicleRoute.getStart();
        this.vehicleRoute = vehicleRoute;
    }

    @Override
    public void visit(TourActivity tourActivity) {
        distance += getDistance(prevAct, tourActivity);
        prevAct = tourActivity;
    }

    @Override
    public void finish() {
        distance += getDistance(prevAct, vehicleRoute.getEnd());
//            stateManager.putTypedRouteState(vehicleRoute,distanceStateId,Double.class,distance); //v1.3.1
        stateManager.putRouteState(vehicleRoute, distanceStateId, distance); //head of development - upcoming release (v1.4)
    }

    double getDistance(TourActivity from, TourActivity to) {
        return costMatrix.getDistance(from.getLocation().getId(), to.getLocation().getId());
    }
}
