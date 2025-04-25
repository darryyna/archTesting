package edu.litviniuk.mongo.repository;

import edu.litviniuk.mongo.model.MovieModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Optional;

/*
  @author darin
  @project mongo
  @class MovieRepositoryTest
  @version 1.0.0
  @since 25.04.2025 - 13.54
*/


@SpringBootTest
public class MovieRepositoryTest {

    @Autowired
    private MovieRepository underTest;

    private MovieModel movie1;
    private MovieModel movie2;
    private MovieModel movie3;

    @BeforeEach
    void setUp() {
        underTest.deleteAll();

        movie1 = new MovieModel("Inception", "A thief who steals corporate secrets...", "Sci-Fi");
        movie2 = new MovieModel("The Dark Knight", "When the menace known as the Joker...", "Action");
        movie3 = new MovieModel("Parasite", "A poor family schemes to become employed...", "Thriller");

        underTest.saveAll(List.of(movie1, movie2, movie3));
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void shouldSaveMovieAndGenerateId() {
        MovieModel newMovie = new MovieModel("Interstellar", "A team of explorers travel...", "Sci-Fi");
        MovieModel savedMovie = underTest.save(newMovie);

        assertNotNull(savedMovie.getId());
        assertEquals("Interstellar", savedMovie.getTitle());
        assertEquals("A team of explorers travel...", savedMovie.getDescription());
        assertEquals("Sci-Fi", savedMovie.getGenre());
    }

    @Test
    void shouldFindMovieById() {
        MovieModel movieToFind = new MovieModel("Pulp Fiction", "The lives of two mob hitmen...", "Crime");
        MovieModel savedMovieToFind = underTest.save(movieToFind);

        Optional<MovieModel> foundMovieOptional = underTest.findById(savedMovieToFind.getId());
        assertTrue(foundMovieOptional.isPresent());
        MovieModel foundMovie = foundMovieOptional.get();
        assertEquals("Pulp Fiction", foundMovie.getTitle());
        assertEquals("The lives of two mob hitmen...", foundMovie.getDescription());
        assertEquals("Crime", foundMovie.getGenre());
        assertEquals(savedMovieToFind.getId(), foundMovie.getId());
    }


    @Test
    void shouldReturnEmptyWhenIdNotFound() {
        String nonExistentId = "non_existent_id_12345";
        Optional<MovieModel> foundMovie = underTest.findById(nonExistentId);
        assertFalse(foundMovie.isPresent());
    }

    @Test
    void shouldFindAllMovies() {
        List<MovieModel> movies = underTest.findAll();
        assertNotNull(movies);
        assertEquals(3, movies.size());
        assertTrue(movies.contains(movie1));
        assertTrue(movies.contains(movie2));
        assertTrue(movies.contains(movie3));
    }

    @Test
    void shouldUpdateMovie() {
        MovieModel movieToUpdate = new MovieModel("Fight Club", "An insomniac office worker...", "Drama");
        MovieModel savedMovieToUpdate = underTest.save(movieToUpdate);

        savedMovieToUpdate.setDescription("An insomniac office worker looking for a way to change...");
        savedMovieToUpdate.setGenre("Cult Classic");
        underTest.save(savedMovieToUpdate);
        Optional<MovieModel> updatedMovieOptional = underTest.findById(savedMovieToUpdate.getId());

        assertTrue(updatedMovieOptional.isPresent());
        MovieModel updatedMovie = updatedMovieOptional.get();
        assertEquals("Fight Club", updatedMovie.getTitle());
        assertEquals("An insomniac office worker looking for a way to change...", updatedMovie.getDescription());
        assertEquals("Cult Classic", updatedMovie.getGenre());
        assertEquals(savedMovieToUpdate.getId(), updatedMovie.getId());
    }

    @Test
    void shouldDeleteMovieById() {
        MovieModel movieToDelete = new MovieModel("Goodfellas", "The story of Henry Hill...", "Biography");
        MovieModel savedMovieToDelete = underTest.save(movieToDelete);
        String movieIdToDelete = savedMovieToDelete.getId();
        assertNotNull(movieIdToDelete);
        underTest.deleteById(movieIdToDelete);
        Optional<MovieModel> deletedMovie = underTest.findById(movieIdToDelete);
        assertFalse(deletedMovie.isPresent());
        assertEquals(3, underTest.count());
    }

    @Test
    void shouldDeleteAllMovies() {
        assertEquals(3, underTest.count());
        underTest.deleteAll();
        List<MovieModel> movies = underTest.findAll();
        assertTrue(movies.isEmpty());
        assertEquals(0, underTest.count());
    }

    @Test
    void shouldSaveMultipleMovies() {
        underTest.deleteAll();
        MovieModel movieA = new MovieModel("Matrix", "A computer hacker learns...", "Sci-Fi");
        MovieModel movieB = new MovieModel("Lord of the Rings", "A meek Hobbit and eight companions...", "Fantasy");
        List<MovieModel> savedMovies = underTest.saveAll(List.of(movieA, movieB));

        assertNotNull(savedMovies);
        assertEquals(2, savedMovies.size());
        savedMovies.forEach(movie -> assertNotNull(movie.getId()));
        assertEquals(2, underTest.count());
    }

    @Test
    void shouldCountProducts() {
        long count = underTest.count();
        assertEquals(3L, count);
    }

    @Test
    void shouldCheckIfProductExistsById() {
        MovieModel movieToCheck = new MovieModel("Toy Story", "A cowboy doll is profoundly threatened...", "Animation");
        MovieModel savedMovieToCheck = underTest.save(movieToCheck);
        String movieIdToCheck = savedMovieToCheck.getId();
        String nonExistentId = "another_non_existent_id_9876";
        boolean exists = underTest.existsById(movieIdToCheck);
        boolean notExists = underTest.existsById(nonExistentId);
        assertTrue(exists);
        assertFalse(notExists);
    }
}