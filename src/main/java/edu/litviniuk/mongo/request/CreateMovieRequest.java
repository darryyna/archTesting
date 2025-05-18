package edu.litviniuk.mongo.request;

/*
  @author darin
  @project mongo
  @class CreateMovieRequest
  @version 1.0.0
  @since 18.05.2025 - 13.59
*/

public record CreateMovieRequest( String title, String description, String genre) {
}
