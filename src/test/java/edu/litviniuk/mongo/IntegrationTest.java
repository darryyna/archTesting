package edu.litviniuk.mongo;

/*
  @author darin
  @project mongo
  @class IntegrationTest
  @version 1.0.0
  @since 18.05.2025 - 14.51
*/

import edu.litviniuk.mongo.model.MovieModel;
import edu.litviniuk.mongo.repository.MovieRepository;
import edu.litviniuk.mongo.request.CreateMovieRequest;
import edu.litviniuk.mongo.request.UpdateMovieRequest;
import edu.litviniuk.mongo.utils.JsonConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository repository;

    private List<MovieModel> initialMovies = new ArrayList<>();

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        initialMovies.clear();
        initialMovies.add(MovieModel.builder().id("5").title("Initial Movie 1").description("Desc 1").genre("Genre A").createDate(LocalDateTime.now()).updateDate(new ArrayList<>()).build());
        initialMovies.add(MovieModel.builder().id("6").title("Initial Movie 2").description("Desc 2").genre("Genre B").createDate(LocalDateTime.now()).updateDate(new ArrayList<>()).build());
        initialMovies.add(MovieModel.builder().id("7").title("Initial Movie 3").description("Desc 3").genre("Genre C").createDate(LocalDateTime.now()).updateDate(new ArrayList<>()).build());

        repository.saveAll(initialMovies);
    }

    @AfterEach
    void tearsDown(){
        repository.deleteAll();
    }


    @DisplayName("POST /api/v1/movies/dto - Create new Movie. Happy Path")
    @Test
    void itShouldCreateNewMovie() throws Exception {
        CreateMovieRequest request = new CreateMovieRequest(
                "New Movie Title", "New Movie Description", "New Genre");
        ResultActions perform = mockMvc.perform(post("/api/v1/movies/dto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConverter.toJson(request)));
        perform.andExpect(status().isOk());
        assertThat(repository.existsByTitle(request.title())).isTrue();

        MovieModel createdMovie = repository.findByTitle(request.title()).orElse(null);

        assertNotNull(createdMovie);
        assertNotNull(createdMovie.getId());
        assertThat(createdMovie.getId()).isNotEmpty();
        assertThat(createdMovie.getId().length()).isEqualTo(24);
        assertThat(createdMovie.getDescription()).isEqualTo(request.description());
        assertThat(createdMovie.getTitle()).isEqualTo(request.title());
        assertThat(createdMovie.getGenre()).isEqualTo(request.genre());
        assertThat(createdMovie.getUpdateDate()).isEmpty();
        assertThat(createdMovie.getCreateDate()).isNotNull();
        perform.andExpect(jsonPath("$.title").value(request.title()))
                .andExpect(jsonPath("$.description").value(request.description()))
                .andExpect(jsonPath("$.genre").value(request.genre()));
    }

    @DisplayName("POST /api/v1/movies/dto - Should not create Movie with existing Title")
    @Test
    void itShouldNotCreateMovieWithExistingTitle() throws Exception {
        CreateMovieRequest request = new CreateMovieRequest("Initial Movie 1", "Some Description", "Some Genre");
        ResultActions perform = mockMvc.perform(post("/api/v1/movies/dto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConverter.toJson(request)));
        perform.andExpect(status().isBadRequest());
        assertThat(repository.findAll().size()).isEqualTo(initialMovies.size());
    }

    @DisplayName("PUT /api/v1/movies/dto - Update existing Movie. Happy path")
    @Test
    void itShouldUpdateMovieSuccessfully() throws Exception {
        MovieModel existingMovie = initialMovies.get(0);

        UpdateMovieRequest updateRequest = new UpdateMovieRequest(
                existingMovie.getId(),
                "Updated Movie Title",
                "Updated Description",
                "Updated Genre"
        );

        ResultActions perform = mockMvc.perform(put("/api/v1/movies/dto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConverter.toJson(updateRequest)));

        perform.andExpect(status().isOk());

        MovieModel updatedMovie = repository.findById(existingMovie.getId()).orElseThrow();

        assertThat(updatedMovie.getTitle()).isEqualTo("Updated Movie Title");
        assertThat(updatedMovie.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedMovie.getGenre()).isEqualTo("Updated Genre");
        assertThat(updatedMovie.getUpdateDate()).hasSize(1);
        perform.andExpect(jsonPath("$.title").value("Updated Movie Title"));
    }

    @DisplayName("PUT /api/v1/movies/dto - Should fail to update Nonexistent Movie")
    @Test
    void itShouldFailToUpdateNonexistentMovie() throws Exception {
        String nonExistentId = "000000000000000000000000";
        UpdateMovieRequest request = new UpdateMovieRequest(
                nonExistentId, "Ghost Movie", "Ghost Desc", "Ghost Genre");

        ResultActions perform = mockMvc.perform(put("/api/v1/movies/dto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConverter.toJson(request)));
        perform.andExpect(status().isNotFound());
    }

    @DisplayName("PUT /api/v1/movies/dto - Should not update Movie with Duplicate Title")
    @Test
    void itShouldNotUpdateMovieWithDuplicateTitle() throws Exception {
        // given
        MovieModel movieToUpdate = initialMovies.get(0);
        MovieModel otherMovie = initialMovies.get(1);
        UpdateMovieRequest request = new UpdateMovieRequest(
                movieToUpdate.getId(),
                otherMovie.getTitle(),
                "Updated description",
                "Updated genre"
        );

        ResultActions perform = mockMvc.perform(put("/api/v1/movies/dto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConverter.toJson(request)));

        perform.andExpect(status().isBadRequest());
        Optional<MovieModel> movieAfterUpdate = repository.findById(movieToUpdate.getId());
        assertThat(movieAfterUpdate).isPresent();
        assertThat(movieAfterUpdate.get().getTitle()).isEqualTo(movieToUpdate.getTitle());
    }


    @DisplayName("GET /api/v1/movies/{id} - Return Movie by ID")
    @Test
    void itShouldReturnMovieById() throws Exception {
        MovieModel existingMovie = initialMovies.get(1);
        ResultActions perform = mockMvc.perform(get("/api/v1/movies/{id}", existingMovie.getId())
                .contentType(MediaType.APPLICATION_JSON));

        perform.andExpect(status().isOk());
        perform.andExpect(jsonPath("$.id").value(existingMovie.getId()));
        perform.andExpect(jsonPath("$.title").value(existingMovie.getTitle()));
        perform.andExpect(jsonPath("$.description").value(existingMovie.getDescription()));
        perform.andExpect(jsonPath("$.genre").value(existingMovie.getGenre()));
        assertNotNull(perform.andReturn().getResponse().getContentAsString());
        assertThat(perform.andReturn().getResponse().getContentAsString()).contains("\"createDate\"");
    }

    @DisplayName("GET /api/v1/movies/{id} - Return Not Found when getting Nonexistent Movie")
    @Test
    void itShouldReturnNotFoundWhenGettingNonexistentMovie() throws Exception {
        String nonExistentId = "000000000000000000000000";

        ResultActions perform = mockMvc.perform(get("/api/v1/movies/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON));

        perform.andExpect(status().isNotFound());
    }

    @DisplayName("DELETE /api/v1/movies/{id} - Delete Movie Successfully")
    @Test
    void itShouldDeleteMovieSuccessfully() throws Exception {
        MovieModel toDelete = initialMovies.get(2);
        ResultActions perform = mockMvc.perform(delete("/api/v1/movies/{id}", toDelete.getId()));
        perform.andExpect(status().isNoContent());
        assertThat(repository.findById(toDelete.getId())).isEmpty();
        assertThat(repository.findAll().size()).isEqualTo(initialMovies.size() - 1);
    }

    @DisplayName("GET /api/v1/movies/ - Return All Movies")
    @Test
    void itShouldReturnAllMovies() throws Exception {
        ResultActions perform = mockMvc.perform(get("/api/v1/movies/")
                .contentType(MediaType.APPLICATION_JSON));

        perform.andExpect(status().isOk());
        perform.andExpect(jsonPath("$.length()").value(initialMovies.size()));

        perform.andExpect(jsonPath("$[0].id").value(initialMovies.get(0).getId()));
        perform.andExpect(jsonPath("$[1].title").value(initialMovies.get(1).getTitle()));
    }

    @DisplayName("POST /api/v1/movies/ - Add Movie with MovieModel body")
    @Test
    void itShouldAddMovieWithMovieModelBody() throws Exception {

        MovieModel newMovie = MovieModel.builder()
                .title("Movie From Model Post")
                .description("Description from model")
                .genre("Model Genre")
                .build();

        ResultActions perform = mockMvc.perform(post("/api/v1/movies/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConverter.toJson(newMovie)));
        perform.andExpect(status().isOk());
        perform.andExpect(jsonPath("$.title").value("Movie From Model Post"));
        perform.andExpect(jsonPath("$.id").isNotEmpty());
        assertThat(repository.existsByTitle("Movie From Model Post")).isTrue();
        assertThat(repository.findAll().size()).isEqualTo(initialMovies.size() + 1);
    }

    @DisplayName("PUT /api/v1/movies/{id} - Update Movie with MovieModel body")
    @Test
    void itShouldUpdateMovieWithMovieModelBody() throws Exception {
        MovieModel existingMovie = initialMovies.get(0);
        MovieModel updatedMovieData = MovieModel.builder()
                .id(existingMovie.getId())
                .title("Updated Title Via Model Put")
                .description("Updated Desc Via Model Put")
                .genre("Updated Genre Via Model Put")
                .createDate(existingMovie.getCreateDate())
                .updateDate(new ArrayList<>(existingMovie.getUpdateDate()))
                .build();

        ResultActions perform = mockMvc.perform(put("/api/v1/movies/{id}", existingMovie.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonConverter.toJson(updatedMovieData)));

        perform.andExpect(status().isOk());
        MovieModel movieAfterUpdate = repository.findById(existingMovie.getId()).orElseThrow();
        assertThat(movieAfterUpdate.getTitle()).isEqualTo("Updated Title Via Model Put");
        assertThat(movieAfterUpdate.getDescription()).isEqualTo("Updated Desc Via Model Put");
        assertThat(movieAfterUpdate.getGenre()).isEqualTo("Updated Genre Via Model Put");
        perform.andExpect(jsonPath("$.title").value("Updated Title Via Model Put"));

    }
}