package com.graphhopper.farmy;

import com.graphhopper.GraphHopperAPI;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverService;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.UnassignedJobReasonTracker;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.util.shapes.GHPoint;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class RouteOptimize {

    private VehicleRoutingProblemSolution solution;
    private Location depotLocation;
    private IdentifiedPointList pointList;
    private final GraphHopperAPI graphHopper;

    public VehicleRoutingTransportCostsMatrix vrtcm;


    public RouteOptimize(GraphHopperAPI graphHopper, InputStream fileStream, GHPoint depotPoint) throws Exception {
        this.graphHopper = graphHopper;
        buildSolution(fileStream, depotPoint);
    }

//    public RouteOptimize(GraphHopperAPI graphHopper, FarmyOrder[] farmyOrders) throws Exception {
//        this.graphHopper = graphHopper;
//        buildSolution(farmyOrders);
//    }

    public RouteOptimize(GraphHopperAPI graphHopper, FarmyOrder[] farmyOrders, FarmyCourier[] farmyCouriers) throws Exception {
        this.graphHopper = graphHopper;
        buildSolution(farmyOrders, farmyCouriers);
    }


    public void buildSolution(InputStream fileStream, GHPoint depotPointParms) throws Exception {
        RoutePlanReader routePlanReader = new RoutePlanReader(fileStream);
        this.depotLocation = Location.newInstance(depotPointParms.getLat(), depotPointParms.getLon());
        build(routePlanReader.getIdentifiedPointList());
    }

    public void buildSolution(FarmyOrder[] farmyOrders, FarmyCourier[] farmyCouriers) throws Exception {
        RoutePlanReader routePlanReader = new RoutePlanReader(farmyOrders);
        this.depotLocation = Location.newInstance(47.4133906, 8.5170937);
        build(routePlanReader.getIdentifiedPointList(), farmyCouriers);
    }

    public HashMap<String, HashMap<String, Object>> getOptimizedRoutes() {
        HashMap<String, HashMap<String, Object>> allMap = new HashMap<>();
        for (VehicleRoute route : solution.getRoutes()) {
            HashMap<String, Object> vehicleHashMap = new HashMap<>();
            IdentifiedGHPoint3D firstPoint = null;
            IdentifiedGHPoint3D lastPoint = null;
            ArrayList<GHPoint> waypoints = new ArrayList<>();
            waypoints.add(new IdentifiedGHPoint3D(depotLocation.getCoordinate().getX(), depotLocation.getCoordinate().getY(), 0, "DEPOT"));
            for (TourActivity activity : route.getActivities()) {
                Service service = ((DeliverService) activity).getJob();
                IdentifiedGHPoint3D idPoint = this.pointList.find(service.getId());
                idPoint.setPlannedTime(activity.getArrTime());
                if (lastPoint != null) idPoint.setDistance(this.vrtcm.getDistance(idPoint.getId(), lastPoint.getId()));
                waypoints.add(idPoint);

                if(firstPoint == null) firstPoint = idPoint;
                lastPoint = idPoint;
            }
//          allMap.get(route.getVehicle().getId()).put(allMap.get(route.getVehicle().getId()).get("waypoints"), waypoints);
            vehicleHashMap.put("waypoints", waypoints.toArray());
            if (this.vrtcm != null && firstPoint != null) vehicleHashMap.put("distance", this.vrtcm.getDistance(firstPoint.getId(), lastPoint.getId()));

//          Set vehicle to all hashmap
            allMap.put(route.getVehicle().getId(), vehicleHashMap);
        }
        return allMap;
    }

    public VehicleRoutingProblemSolution getSolution() {
        return solution;
    }

    public Location getDepotLocation() {
        return depotLocation;
    }

    public IdentifiedPointList getPointList() {
        return pointList;
    }

    public GraphHopperAPI getGraphHopper() {
        return graphHopper;
    }

    private void build(IdentifiedPointList pointList) throws Exception {

        //        Load the map
        this.pointList = pointList;

        if (this.pointList.size() == 0) {
            System.out.println(this.pointList);
            throw new Exception("Point List is Empty");
        }


        IdentifiedGHPoint3D depotPoint = new IdentifiedGHPoint3D(this.getDepotLocation(), "DEPOT");

        PointMatrixList pointMatrixList = new PointMatrixList(graphHopper, this.pointList, depotPoint);

        this.depotLocation = Location.Builder.newInstance().setId(depotPoint.getId()).setCoordinate(Coordinate.newInstance(depotPoint.getLat(), depotPoint.getLon())).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
                .setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE);
        /*
         * Adding Vehicles
         */

        vrpBuilder.addJob(Pickup.Builder.newInstance("DEPOT")
                .setLocation(depotLocation)
                .addSizeDimension(0, 416).build());


        for (int i = 0; i < 1; i++) {
            VehicleType type = VehicleTypeImpl.Builder.newInstance("vt1" + i)
                    .setFixedCost(0)
                    .setCostPerDistance(0)
                    .addCapacityDimension(0, 200)
                    .build();
            vrpBuilder.addVehicle(VehicleImpl.Builder.newInstance("vt1" + i + "-vehicle" + i)
                    .setStartLocation(depotLocation)
                    .setType(type)
                    .setReturnToDepot(true)
                    .build());
        }

        for (int i = 0; i < 2; i++) {
            VehicleType type = VehicleTypeImpl.Builder.newInstance("vt2" + i)
                    .setFixedCost(0)
                    .setCostPerDistance(0)
                    .addCapacityDimension(0, 200)
                    .build();
            vrpBuilder.addVehicle(VehicleImpl.Builder.newInstance("vt2" + i + "-vehicle" + i)
                    .setStartLocation(depotLocation)
                    .setType(type)
                    .setReturnToDepot(true)
                    .build());
        }

        /*
         * Adding Services
         */

        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);

        for (IdentifiedGHPoint3D point : this.pointList) {
            if (point.getId().equals(depotPoint.getId())) continue;
            vrpBuilder.addJob(Delivery.Builder.newInstance(point.getId())
                    .addSizeDimension(0, (int) point.getWeight())
                    .setLocation(Location.Builder.newInstance().setId(point.getId()).setCoordinate(Coordinate.newInstance(point.getLat(), point.getLon())).build())
//                    .addTimeWindow(point.getTimeWindow())
                    .setServiceTime(point.getServiceTime())
                    .build());
        }
        for (PointMatrix pointMatrix : pointMatrixList) {
            costMatrixBuilder.addTransportDistance(pointMatrix.getPoint1().getId(), pointMatrix.getPoint2().getId(), pointMatrix.getDistance());
            costMatrixBuilder.addTransportTime(pointMatrix.getPoint1().getId(), pointMatrix.getPoint2().getId(), pointMatrix.getTime());
        }
        this.vrtcm = costMatrixBuilder.build();

        vrpBuilder.setRoutingCost(this.vrtcm);


        VehicleRoutingProblem vrp = vrpBuilder.build();


        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        this.solution = Solutions.bestOf(solutions);


//        new Plotter(vrp).plot("./output/main2_problem2.png", "p01");
//        new Plotter(vrp, Solutions.bestOf(solutions)).plot("./output/main2_sol2.png", "po");
//        new GraphStreamViewer(vrp, Solutions.bestOf(solutions)).labelWith(GraphStreamViewer.Label.ID).setRenderDelay(200).display();
//        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, Solutions.bestOf(solutions), vrp.getTransportCosts());
        System.out.println("tp_distance: " + analyser.getDistance());
        System.out.println("tp_time: " + analyser.getTransportTime());
        System.out.println("waiting: " + analyser.getWaitingTime());
        System.out.println("service: " + analyser.getServiceTime());
        System.out.println("#picks: " + analyser.getNumberOfPickups());
        System.out.println("#deliveries: " + analyser.getNumberOfDeliveries());

//        for (VehicleRoute route : solution.getRoutes()) {
//            StringBuilder urlStr = new StringBuilder("http://localhost:8989/maps/");
//            urlStr.append("?point=").append(depotLocation.getCoordinate().getX()).append(",").append(depotLocation.getCoordinate().getY());
//            for (TourActivity activity : route.getActivities()) {
//                Coordinate coordinate = activity.getLocation().getCoordinate();
//                urlStr.append("&point=").append(coordinate.getX()).append(",").append(coordinate.getY());
//            }
//            System.out.println(urlStr);
//        }
    }

    private void build(IdentifiedPointList pointList, FarmyCourier[] farmyCouriers) throws Exception {

        //        Load the map
        this.pointList = pointList;

        if (this.pointList.size() == 0) {
            System.out.println(this.pointList);
            throw new Exception("Point List is Empty");
        }


        IdentifiedGHPoint3D depotPoint = new IdentifiedGHPoint3D(this.getDepotLocation(), "DEPOT");

        PointMatrixList pointMatrixList = new PointMatrixList(graphHopper, this.pointList, depotPoint);

        this.depotLocation = Location.Builder.newInstance().setId(depotPoint.getId()).setCoordinate(Coordinate.newInstance(depotPoint.getLat(), depotPoint.getLon())).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
                .setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE);
        /*
         * Adding Vehicles
         */

        vrpBuilder.addJob(Pickup.Builder.newInstance("DEPOT")
                .setLocation(depotLocation)
                .addSizeDimension(0, this.pointList.size()).build());



        for (FarmyCourier courier : farmyCouriers) {
            VehicleType type = VehicleTypeImpl.Builder.newInstance(
                    String.format("[TYPE] #%s", courier.getId())
            )
                    .setFixedCost(0)
                    .setCostPerDistance(0)
                    .addCapacityDimension(0, 80)
                    .setMaxVelocity(courier.getIsPlus() ? 13.0 : 20.8) // ~50km/h
    //                .setMaxVelocity(26.0) // ~100km/h
                    .build();
            vrpBuilder.addVehicle(VehicleImpl.Builder.newInstance(
                    String.format("%s#%s%s", courier.getName(), courier.getId(), courier.getIsPlus() ? " (" + courier.getFarmyVehicle().getName() + ")" : "")
            )
                    .setStartLocation(depotLocation)
                    .setEndLocation(depotLocation)
                    .setType(type)
                    .build());
        }



        /*
         * Adding Services
         */

        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);

        for (IdentifiedGHPoint3D point : this.pointList) {
            if (point.getId().equals(depotPoint.getId())) continue;
            vrpBuilder.addJob(Delivery.Builder.newInstance(point.getId())
                    .addSizeDimension(0, (int) point.getWeight())
                    .setLocation(Location.Builder.newInstance().setId(point.getId()).setCoordinate(Coordinate.newInstance(point.getLat(), point.getLon())).build())
                    .addTimeWindow(point.getTimeWindow())
                    .setServiceTime(point.getServiceTime())
                    .build());
        }
        for (PointMatrix pointMatrix : pointMatrixList) {
            costMatrixBuilder.addTransportDistance(pointMatrix.getPoint1().getId(), pointMatrix.getPoint2().getId(), pointMatrix.getDistance());
            costMatrixBuilder.addTransportTime(pointMatrix.getPoint1().getId(), pointMatrix.getPoint2().getId(), pointMatrix.getTime());
        }

        this.vrtcm = costMatrixBuilder.build();
        vrpBuilder.setRoutingCost(this.vrtcm);


        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                .setProperty(Jsprit.Parameter.THREADS, "5")
                .setProperty(Jsprit.Parameter.FIXED_COST_PARAM, "1.")
                .buildAlgorithm();

        UnassignedJobReasonTracker reasonTracker = new UnassignedJobReasonTracker();

        vra.addListener(reasonTracker);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        this.solution = Solutions.bestOf(solutions);


//        new Plotter(vrp).plot("./output/main2_problem2.png", "p01");
//        new Plotter(vrp, Solutions.bestOf(solutions)).plot("./output/main2_sol2.png", "po");
//        new GraphStreamViewer(vrp, Solutions.bestOf(solutions)).labelWith(GraphStreamViewer.Label.ID).setRenderDelay(200).display();
//        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, Solutions.bestOf(solutions), vrp.getTransportCosts());
        System.out.println("tp_distance: " + analyser.getDistance());
        System.out.println("tp_time: " + analyser.getTransportTime());
        System.out.println("waiting: " + analyser.getWaitingTime());
        System.out.println("service: " + analyser.getServiceTime());
        System.out.println("#picks: " + analyser.getNumberOfPickups());
        System.out.println("#deliveries: " + analyser.getNumberOfDeliveries());
        System.out.println("#load_delivered: " + analyser.getLoadDelivered());
        System.out.println("#capacity_violation: " + analyser.getCapacityViolation());
        System.out.println("#time_window_violation: " + analyser.getTimeWindowViolation());

//        for (VehicleRoute route : solution.getRoutes()) {
//            StringBuilder urlStr = new StringBuilder("http://localhost:8989/maps/");
//            urlStr.append("?point=").append(depotLocation.getCoordinate().getX()).append(",").append(depotLocation.getCoordinate().getY());
//            for (TourActivity activity : route.getActivities()) {
//                Coordinate coordinate = activity.getLocation().getCoordinate();
//                urlStr.append("&point=").append(coordinate.getX()).append(",").append(coordinate.getY());
//            }
//            System.out.println(urlStr);
//        }
    }
}
