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
import edu.litviniuk.mongo.response.ApiResponse;
import edu.litviniuk.mongo.response.BaseMetaData;
import edu.litviniuk.mongo.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieRestController {

    private final MovieService movieService;

    @GetMapping
    public List<MovieModel> getAllMovies() {
        return movieService.getAllMovies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieModel> getMovieById(@PathVariable String id) {
        MovieModel movie = movieService.getMovieById(Integer.parseInt(id));
        return movie != null ? ResponseEntity.ok(movie) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @PostMapping
    public MovieModel addMovie(@RequestBody MovieModel movie) {
        return movieService.addMovie(movie);
    }

    @PutMapping("/{id}")
    public MovieModel updateMovie(@PathVariable String id, @RequestBody MovieModel movie) {
        movie.setId(id);
        return movieService.updateMovie(movie);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable String id) {
        movieService.deleteMovie(Integer.parseInt(id));
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


    // new methods with api response

    @GetMapping("/api-response")
    public ApiResponse<BaseMetaData, MovieModel> getAllMoviesApi() {
        return movieService.getAllMoviesApiResponse();
    }

    @GetMapping("/api-response/{id}")
    public ResponseEntity<ApiResponse<BaseMetaData, MovieModel>> getMovieByIdApi(@PathVariable String id) {
        ApiResponse<BaseMetaData, MovieModel> response = movieService.getMovieByIdApiResponse(id);
        return response.getMeta().isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(response.getMeta().getCode()).body(response);
    }

    @PostMapping("/api-response")
    public ResponseEntity<ApiResponse<BaseMetaData, MovieModel>> createMovieApi(@RequestBody CreateMovieRequest request) {
        ApiResponse<BaseMetaData, MovieModel> response = movieService.createMovieApiResponse(request);
        return ResponseEntity.status(response.getMeta().getCode()).body(response);
    }

    @PutMapping("/api-response")
    public ResponseEntity<ApiResponse<BaseMetaData, MovieModel>> updateMovieApi(@RequestBody UpdateMovieRequest request) {
        ApiResponse<BaseMetaData, MovieModel> response = movieService.updateMovieApiResponse(request);
        return ResponseEntity.status(response.getMeta().getCode()).body(response);
    }

    @DeleteMapping("/api-response/{id}")
    public ResponseEntity<ApiResponse<BaseMetaData, Void>> deleteMovieApi(@PathVariable String id) {
        ApiResponse<BaseMetaData, Void> response = movieService.deleteMovieApiResponse(id);
        return ResponseEntity.status(response.getMeta().getCode()).body(response);
    }
}
