package edu.litviniuk.mongo.controller;

/*
  @author darin
  @project mongo
  @class MovieRestController
  @version 1.0.0
  @since 17.04.2025 - 18.41
*/

import edu.litviniuk.mongo.model.MovieModel;
import edu.litviniuk.mongo.request.CreateMovieRequest;
import edu.litviniuk.mongo.request.UpdateMovieRequest;
import edu.litviniuk.mongo.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies/")
@RequiredArgsConstructor
public class MovieRestController {

     private final MovieService movieService;

     @GetMapping
     public List<MovieModel> getAllMovies() {
         return movieService.getAllMovies();
     }

    @GetMapping("/{id}")
    public ResponseEntity<MovieModel> getMovieById(@PathVariable int id) {
        MovieModel movie = movieService.getMovieById(id);
        return movie != null
                ? ResponseEntity.ok(movie)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @PostMapping
     public MovieModel addMovie(@RequestBody MovieModel movie) {
         return movieService.addMovie(movie);
     }

    @PutMapping("/{id}")
    public MovieModel updateMovie(@PathVariable int id, @RequestBody MovieModel movie) {
        movie.setId(String.valueOf(id));
        return movieService.updateMovie(movie);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable int id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/dto")
    public ResponseEntity<?> insert(@RequestBody CreateMovieRequest request) {
        try {
            MovieModel created = movieService.create(request);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/dto")
    public ResponseEntity<?> edit(@RequestBody UpdateMovieRequest request) {
        try {
            MovieModel updated = movieService.update(request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Movie not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
