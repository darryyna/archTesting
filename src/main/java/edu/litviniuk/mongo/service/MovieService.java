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
import edu.litviniuk.mongo.request.CreateMovieRequest;
import edu.litviniuk.mongo.request.UpdateMovieRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public MovieModel create(CreateMovieRequest request) {
        if (movieRepository.existsByTitle(request.title())) {
            throw new IllegalArgumentException("Title already exists");
        }
        MovieModel movie = mapToEntity(request);
        movie.setCreateDate(LocalDateTime.now());
        movie.setUpdateDate(new ArrayList<>());
        return movieRepository.save(movie);
    }

    public MovieModel updateMovie(MovieModel movie) {
        return movieRepository.save(movie);
    }

    public MovieModel update(UpdateMovieRequest request) {
        MovieModel movieToUpdate = movieRepository.findById(request.id()).orElseThrow(
                () -> new IllegalArgumentException("Movie not found")
        );

        if (!request.title().equals(movieToUpdate.getTitle())
                && movieRepository.existsByTitle(request.title())) {
            throw new IllegalArgumentException("Movie already exists");
        }

        List<LocalDateTime> updateDates = movieToUpdate.getUpdateDate();
        updateDates.add(LocalDateTime.now());

        MovieModel itemToUpdate = MovieModel.builder()
                .id(request.id())
                .title(request.title())
                .genre(request.genre())
                .description(request.description())
                .createDate(movieToUpdate.getCreateDate())
                .updateDate(updateDates)
                .build();

        return movieRepository.save(itemToUpdate);
    }

    public void deleteMovie(int id) {
        movieRepository.deleteById(String.valueOf(id));
    }

    private MovieModel mapToEntity(CreateMovieRequest request) {
        return (MovieModel) new MovieModel(request.title(), request.description(), request.genre());
    }
}
