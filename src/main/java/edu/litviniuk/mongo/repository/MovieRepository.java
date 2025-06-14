package edu.litviniuk.mongo.repository;

import edu.litviniuk.mongo.model.MovieModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
  @author darin
  @project mongo
  @class MovieRepository
  @version 1.0.0
  @since 17.04.2025 - 18.14
*/

@Repository
public interface MovieRepository extends MongoRepository<MovieModel, String> {

    boolean existsByTitle(String title);
    Optional<MovieModel> findByTitle(String title);
}
