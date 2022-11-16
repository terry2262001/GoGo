package com.example.gogo.Model;

public class MapRoom {
   private String imageURL ;
   private String publisher;
   private double rating;
   private String roomid;
   private String roomname;

   public MapRoom() {
   }

   public MapRoom(String imageURL, String publisher, double rating, String roomid, String roomname) {
      this.imageURL = imageURL;
      this.publisher = publisher;
      this.rating = rating;
      this.roomid = roomid;
      this.roomname = roomname;
   }

   public String getImageURL() {
      return imageURL;
   }

   public void setImageURL(String imageURL) {
      this.imageURL = imageURL;
   }

   public String getPublisher() {
      return publisher;
   }

   public void setPublisher(String publisher) {
      this.publisher = publisher;
   }

   public double getRating() {
      return rating;
   }

   public void setRating(double rating) {
      this.rating = rating;
   }

   public String getRoomid() {
      return roomid;
   }

   public void setRoomid(String roomid) {
      this.roomid = roomid;
   }

   public String getRoomname() {
      return roomname;
   }

   public void setRoomname(String roomname) {
      this.roomname = roomname;
   }
}
