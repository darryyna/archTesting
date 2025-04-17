package edu.litviniuk.mongo.service;

/*
  @author darin
  @project mongo
  @class MovieService
  @version 1.0.0
  @since 17.04.2025 - 18.17
*/

import edu.litviniuk.mongo.model.MovieModel;
import edu.litviniuk.mongo.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    private List<MovieModel> movies = new ArrayList<>();
    {
        movies.add(new MovieModel("1", "Inception", "A mind-bending thriller", "Sci-Fi"));
        movies.add(new MovieModel("2","The Godfather", "A story about a powerful mafia family", "Crime"));
        movies.add(new MovieModel("3","The Dark Knight", "A superhero battles crime in Gotham", "Action"));
    }

    public List<MovieModel> getAllMovies() {
        return movieRepository.findAll();
    }

    public MovieModel getMovieById(int id) {
        return movieRepository.findById(String.valueOf(id)).orElse(null);
    }

    public MovieModel addMovie(MovieModel movie) {
        return movieRepository.save(movie);
    }

    public MovieModel updateMovie(MovieModel movie) {
        return movieRepository.save(movie);
    }

    public void deleteMovie(int id) {
        movieRepository.deleteById(String.valueOf(id));
    }
}
