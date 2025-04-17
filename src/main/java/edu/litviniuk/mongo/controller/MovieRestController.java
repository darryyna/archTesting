package edu.litviniuk.mongo.controller;

/*
  @author darin
  @project mongo
  @class MovieRestController
  @version 1.0.0
  @since 17.04.2025 - 18.41
*/

import edu.litviniuk.mongo.model.Movie;
import edu.litviniuk.mongo.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies/")
@RequiredArgsConstructor
public class MovieRestController {

     private final MovieService movieService;

     @GetMapping
     public List<Movie> getAllMovies() {
         return movieService.getAllMovies();
     }

     @GetMapping("/{id}")
     public Movie getMovieById(@PathVariable int id) {
         return movieService.getMovieById(id);
     }

     @PostMapping
     public Movie addMovie(@RequestBody Movie movie) {
         return movieService.addMovie(movie);
     }

    @PutMapping("/{id}")
    public Movie updateMovie(@PathVariable int id, @RequestBody Movie movie) {
        movie.setId(String.valueOf(id));
        return movieService.updateMovie(movie);
    }

     @DeleteMapping("/{id}")
     public void deleteMovie(@PathVariable int id) {
         movieService.deleteMovie(id);
     }
}
