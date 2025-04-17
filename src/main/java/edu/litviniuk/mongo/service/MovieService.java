package edu.litviniuk.mongo.service;

/*
  @author darin
  @project mongo
  @class MovieService
  @version 1.0.0
  @since 17.04.2025 - 18.17
*/

import edu.litviniuk.mongo.model.Movie;
import edu.litviniuk.mongo.repository.MovieRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    private List<Movie> movies = new ArrayList<>();
    {
        movies.add(new Movie("1", "Inception", "A mind-bending thriller", "Sci-Fi"));
        movies.add(new Movie("2","The Godfather", "A story about a powerful mafia family", "Crime"));
        movies.add(new Movie("3","The Dark Knight", "A superhero battles crime in Gotham", "Action"));
    }

    /* @PostConstruct
    public void init() {
        movieRepository.deleteAll();
        movieRepository.saveAll(movies);
    } */

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie getMovieById(int id) {
        return movieRepository.findById(String.valueOf(id)).orElse(null);
    }

    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public Movie updateMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public void deleteMovie(int id) {
        movieRepository.deleteById(String.valueOf(id));
    }
}
