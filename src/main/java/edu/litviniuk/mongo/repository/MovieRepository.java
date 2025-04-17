package edu.litviniuk.mongo.repository;

import edu.litviniuk.mongo.model.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/*
  @author darin
  @project mongo
  @class MovieRepository
  @version 1.0.0
  @since 17.04.2025 - 18.14
*/

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
}
