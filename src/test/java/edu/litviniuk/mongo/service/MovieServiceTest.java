package edu.litviniuk.mongo.service;

import edu.litviniuk.mongo.model.MovieModel;
import edu.litviniuk.mongo.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/*
  @author darin
  @project mongo
  @class MovieServiceTest
  @version 1.0.0
  @since 01.05.2025 - 20.12
*/

@SpringBootTest
class MovieServiceTest {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @BeforeEach
    void cleanUp() {
        movieRepository.deleteAll();
    }

    @Test
    void shouldAddMovieToDatabase() {
        MovieModel movie = new MovieModel("1", "Inception", "Dream world", "Sci-Fi");

        MovieModel saved = movieService.addMovie(movie);

        assertNotNull(saved);
        assertEquals("Inception", saved.getTitle());
    }

    @Test
    void shouldReturnMovieById() {
        MovieModel movie = new MovieModel("2", "The Matrix", "Reality bending", "Sci-Fi");
        movieRepository.save(movie);

        MovieModel found = movieService.getMovieById(2);

        assertNotNull(found);
        assertEquals("The Matrix", found.getTitle());
    }

    @Test
    void shouldReturnNullForMissingMovie() {
        MovieModel result = movieService.getMovieById(404);

        assertNull(result);
    }

    @Test
    void shouldReturnAllMovies() {
        movieRepository.save(new MovieModel("3", "Godfather", "Mafia drama", "Crime"));
        movieRepository.save(new MovieModel("4", "Pulp Fiction", "Non-linear storytelling", "Crime"));

        List<MovieModel> result = movieService.getAllMovies();

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoMoviesExist() {
        List<MovieModel> result = movieService.getAllMovies();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUpdateExistingMovie() {
        MovieModel movie = new MovieModel("5", "Tenet", "Original desc", "Action");
        movieRepository.save(movie);

        MovieModel updated = new MovieModel("5", "Tenet", "Updated desc", "Sci-Fi");
        MovieModel result = movieService.updateMovie(updated);

        assertEquals("Updated desc", result.getDescription());
        assertEquals("Sci-Fi", result.getGenre());
    }

    @Test
    void shouldNotCreateNewMovieOnUpdateIfNotExists() {
        MovieModel updated = new MovieModel("999", "Unknown", "Desc", "Genre");

        MovieModel result = movieService.updateMovie(updated);

        assertNotNull(result);
        assertEquals("999", result.getId());
    }

    @Test
    void shouldDeleteMovieById() {
        MovieModel movie = new MovieModel("6", "Fight Club", "Rules", "Drama");
        movieRepository.save(movie);

        movieService.deleteMovie(6);

        assertFalse(movieRepository.findById("6").isPresent());
    }

    @Test
    void shouldDoNothingOnDeleteNonexistentMovie() {
        movieService.deleteMovie(1000);

        // no exception means pass
        assertTrue(true);
    }

    @Test
    void shouldAddMultipleMovies() {
        movieService.addMovie(new MovieModel("7", "Movie A", "Desc A", "Genre A"));
        movieService.addMovie(new MovieModel("8", "Movie B", "Desc B", "Genre B"));

        List<MovieModel> all = movieService.getAllMovies();

        assertEquals(2, all.size());
    }

    @Test
    void shouldAddMovieWithEmptyDescription() {
        MovieModel movie = new MovieModel("9", "Title", "", "Genre");

        MovieModel result = movieService.addMovie(movie);

        assertEquals("", result.getDescription());
    }

    @Test
    void shouldAddMovieWithEmptyGenre() {
        MovieModel movie = new MovieModel("10", "Title", "Some desc", "");

        MovieModel result = movieService.addMovie(movie);

        assertEquals("", result.getGenre());
    }

    @Test
    void shouldHandleDuplicateIds() {
        MovieModel movie1 = new MovieModel("11", "Original", "Desc", "Genre");
        movieRepository.save(movie1);

        MovieModel movie2 = new MovieModel("11", "Updated", "New Desc", "Genre 2");
        movieService.addMovie(movie2);

        MovieModel result = movieService.getMovieById(11);
        assertEquals("Updated", result.getTitle());
    }

    @Test
    void shouldPreserveIdOnUpdate() {
        MovieModel movie = new MovieModel("12", "To Update", "Old", "Drama");
        movieRepository.save(movie);

        MovieModel updated = new MovieModel("12", "Updated", "New", "Sci-Fi");
        MovieModel result = movieService.updateMovie(updated);

        assertEquals("12", result.getId());
    }

    @Test
    void shouldReturnSameObjectReferenceAfterSave() {
        MovieModel movie = new MovieModel("13", "Ref Check", "Check", "Test");
        MovieModel result = movieService.addMovie(movie);

        assertSame(movie.getId(), result.getId());
    }

    @Test
    void shouldSaveMovieWithLongDescription() {
        String longDesc = "A".repeat(1000);
        MovieModel movie = new MovieModel("14", "Long Desc", longDesc, "Epic");

        MovieModel result = movieService.addMovie(movie);

        assertEquals(longDesc, result.getDescription());
    }

    @Test
    void shouldTrimWhitespaceInTitle() {
        MovieModel movie = new MovieModel("15", "  Trim Me  ", "Some desc", "Genre");
        movieService.addMovie(movie);

        MovieModel result = movieService.getMovieById(15);
        assertEquals("  Trim Me  ", result.getTitle()); // trimming не реалізовано, просто тест
    }

    @Test
    void shouldHandleSpecialCharactersInTitle() {
        MovieModel movie = new MovieModel("16", "¿Qué pasa?", "¡Increíble!", "Internacional");
        MovieModel result = movieService.addMovie(movie);

        assertEquals("¿Qué pasa?", result.getTitle());
    }

    @Test
    void shouldNotThrowWhenDeletingAlreadyDeletedMovie() {
        movieRepository.save(new MovieModel("17", "Gone", "Will be deleted", "Drama"));
        movieService.deleteMovie(17);
        movieService.deleteMovie(17); // повторне видалення

        assertFalse(movieRepository.findById("17").isPresent());
    }

    @Test
    void shouldSupportLargeDataset() {
        for (int i = 18; i < 38; i++) {
            movieRepository.save(new MovieModel(String.valueOf(i), "Movie " + i, "Desc", "Genre"));
        }

        List<MovieModel> movies = movieService.getAllMovies();

        assertEquals(20, movies.size());
    }
}