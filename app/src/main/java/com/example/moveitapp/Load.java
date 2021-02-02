package com.example.moveitapp;

public class Load {
  String category, dateTime, destination, driverID, pickup, driverLocation, status, userID, vehicleWanted;
  Double deliveryFees, weight;
  public Load() { }

  public Load(String category, String dateTime, String destination, String driverID, String pickup, String driverLocation, String status, String userID, String vehicleWanted, Double deliveryFees, Double weight, Boolean isBooked) {
    this.category = category;
    this.dateTime = dateTime;
    this.destination = destination;
    this.driverID = driverID;
    this.pickup = pickup;
    this.driverLocation = driverLocation;
    this.status = status;
    this.userID = userID;
    this.vehicleWanted = vehicleWanted;
    this.deliveryFees = deliveryFees;
    this.weight = weight;
    //this.isBooked = isBooked;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getDateTime() {
    return dateTime;
  }

  public void setDateTime(String dateTime) {
    this.dateTime = dateTime;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public String getDriverID() {
    return driverID;
  }

  public void setDriverID(String driverID) {
    this.driverID = driverID;
  }

  public String getPickup() {
    return pickup;
  }

  public void setPickup(String pickup) {
    this.pickup = pickup;
  }

  public String getDriverLocation() {
    return driverLocation;
  }

  public void setDriverLocation(String driverLocation) {
    this.driverLocation = driverLocation;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getUserID() {
    return userID;
  }

  public void setUserID(String userID) {
    this.userID = userID;
  }

  public String getVehicleWanted() {
    return vehicleWanted;
  }

  public void setVehicleWanted(String vehicleWanted) {
    this.vehicleWanted = vehicleWanted;
  }

  public Double getDeliveryFees() {
    return deliveryFees;
  }

  public void setDeliveryFees(Double deliveryFees) {
    this.deliveryFees = deliveryFees;
  }

  public Double getWeight() {
    return weight;
  }

  public void setWeight(Double weight) {
    this.weight = weight;
  }
}
