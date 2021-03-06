package com.graphhopper.farmy;

import com.google.gson.JsonObject;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;

import java.util.ArrayList;
import java.util.HashMap;

public class IdentifiedGHPoint3D extends GHPoint3D {

    protected String id;
    protected String direction;
    protected double serviceTime;
    protected TimeWindow timeWindow;
    protected double plannedTime;
    protected double endTime;
    protected double weight;
    protected double distance;
    protected double earliestOperationStartTime;
    protected double latestOperationStartTime;

    public IdentifiedGHPoint3D(double lat, double lon, double elevation, String id) {
        super(lat, lon, elevation);
        this.id = id;
    }

    public IdentifiedGHPoint3D(double lat, double lon, double elevation, int id) {
        super(lat, lon, elevation);
        this.id = Integer.toString(id);
    }

    public IdentifiedGHPoint3D(GHPoint3D point, int id) {
        super(point.getLat(), point.getLon(), point.getElevation());
        this.id = Integer.toString(id);
    }

    public IdentifiedGHPoint3D(GHPoint3D point, String id) {
        super(point.getLat(), point.getLon(), point.getElevation());
        this.id = id;
    }

    public IdentifiedGHPoint3D(GHPoint point, String id) {
        super(point.getLat(), point.getLon(), 0);
        this.id = id;
    }

    public IdentifiedGHPoint3D(Location point, String id) {
        super(point.getCoordinate().getX(), point.getCoordinate().getY(), 0);
        this.id = id;
    }

    public IdentifiedGHPoint3D(ArrayList<Double> arrayList, String id) {
        super(arrayList.get(0), arrayList.get(1), 0);
        this.id = id;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public double getServiceTime() {
        return serviceTime;
    }

    public IdentifiedGHPoint3D setServiceTime(double serviceTime) {
        this.serviceTime = serviceTime;
        return this;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public IdentifiedGHPoint3D setTimeWindow(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
        return this;
    }

    public IdentifiedGHPoint3D setTimeWindow(double start, double end) {
        this.timeWindow = new TimeWindow(start, end);
        return this;
    }

    public String getDirection() {
        return direction;
    }

    public IdentifiedGHPoint3D setDirection(String direction) {
        this.direction = direction;
        return this;
    }

    public double getPlannedTime() {
        return plannedTime;
    }

    public IdentifiedGHPoint3D setPlannedTime(double plannedTime) {
        this.plannedTime = plannedTime;
        return this;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public double getWeight() {
        return weight;
    }

    public IdentifiedGHPoint3D setWeight(double weight) {
        this.weight = weight;
        return this;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getEarliestOperationStartTime() {
        return earliestOperationStartTime;
    }

    public void setEarliestOperationStartTime(double earliestOperationStartTime) {
        this.earliestOperationStartTime = earliestOperationStartTime;
    }

    public double getLatestOperationStartTime() {
        return latestOperationStartTime;
    }

    public void setLatestOperationStartTime(double latestOperationStartTime) {
        this.latestOperationStartTime = latestOperationStartTime;
    }

    @Override
    public String toString() {
        return super.toString() + "," + id;
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", this.getId());
        jsonObject.addProperty("latitude", String.valueOf(lat));
        jsonObject.addProperty("longitude", String.valueOf(lon));
        jsonObject.addProperty("elevation", String.valueOf(ele));
        jsonObject.addProperty("timeWindowStart", getTimeWindow() != null ? String.valueOf(getTimeWindow().getStart()) : "");
        jsonObject.addProperty("timeWindowEnd", getTimeWindow() != null ? String.valueOf(getTimeWindow().getEnd()) : "");
        jsonObject.addProperty("serviceTime", String.valueOf(getServiceTime()));
        jsonObject.addProperty("direction", getDirection());
        jsonObject.addProperty("plannedTime", String.valueOf(getPlannedTime()));
        jsonObject.addProperty("plannedEndTime", String.valueOf(getEndTime()));
        jsonObject.addProperty("weight",  String.valueOf(getWeight()));
        jsonObject.addProperty("distance", String.valueOf(getDistance()));
        jsonObject.addProperty("earliestOperationStartTime", String.valueOf(getEarliestOperationStartTime()));
        jsonObject.addProperty("latestOperationStartTime", String.valueOf(getLatestOperationStartTime()));
        return jsonObject;
    }


    public Location getLocation() {
        return Location.newInstance(this.getLat(), this.getLon());
    }

    public HashMap<String, Object> toGeoJsonWithId() {
        return new HashMap<String, Object>() {{
            put("id", id);
            put("latitude", String.valueOf(lat));
            put("longitude", String.valueOf(lon));
            put("elevation", String.valueOf(ele));
            put("timeWindowStart", getTimeWindow() != null ? String.valueOf(getTimeWindow().getStart()) : "");
            put("timeWindowEnd", getTimeWindow() != null ? String.valueOf(getTimeWindow().getEnd()) : "");
            put("serviceTime", String.valueOf(getServiceTime()));
            put("direction", getDirection());
            put("plannedTime", String.valueOf(getPlannedTime()));
            put("weight", String.valueOf(getWeight()));
            put("distance", String.valueOf(getDistance()));
        }};
    }
}
