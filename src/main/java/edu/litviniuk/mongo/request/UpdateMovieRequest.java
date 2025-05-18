package edu.litviniuk.mongo.request;

/*
  @author darin
  @project mongo
  @class UpdateMovieRequest
  @version 1.0.0
  @since 18.05.2025 - 14.00
*/

public record UpdateMovieRequest(String id, String title, String description, String genre) {
}
