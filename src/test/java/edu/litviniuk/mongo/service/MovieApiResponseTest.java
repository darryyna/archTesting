package edu.litviniuk.mongo.service;

/*
  @author darin
  @project mongo
  @class MovieApiResponseTest
  @version 1.0.0
  @since 18.04.2026 - 20.12
*/
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.litviniuk.mongo.model.MovieModel;
import edu.litviniuk.mongo.repository.MovieRepository;
import edu.litviniuk.mongo.request.CreateMovieRequest;
import edu.litviniuk.mongo.request.UpdateMovieRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MovieApiResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        movieRepository.deleteAll();
        for (int i = 19; i <= 25; i++) {
            movieRepository.save(new MovieModel(String.valueOf(i), "Movie " + i, "Desc", "Genre"));
        }
    }

    @Test
    void whenGetAllMovies_thenReturn200ApiResponse() throws Exception {
        mockMvc.perform(get("/api/v1/movies/api-response"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.success").value(true))
                .andExpect(jsonPath("$.meta.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(7));
    }

    @Test
    void whenMovieExists_thenReturn200AndData() throws Exception {
        mockMvc.perform(get("/api/v1/movies/api-response/19"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Movie 19"))
                .andExpect(jsonPath("$.meta.success").value(true));
    }

    @Test
    void whenMovieNotExists_thenReturn404ApiResponse() throws Exception {
        mockMvc.perform(get("/api/v1/movies/api-response/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.meta.success").value(false))
                .andExpect(jsonPath("$.meta.errorMessage").value("Movie not found"));
    }

    @Test
    void whenCreateValidMovie_thenReturn201ApiResponse() throws Exception {
        CreateMovieRequest request = new CreateMovieRequest("New Sci-Fi", "New Desc", "Sci-Fi");

        mockMvc.perform(post("/api/v1/movies/api-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data[0].title").value("New Sci-Fi"))
                .andExpect(jsonPath("$.meta.code").value(201));
    }

    @Test
    void whenCreateDuplicateMovie_thenReturn400ApiResponse() throws Exception {
        CreateMovieRequest request = new CreateMovieRequest("Movie 19", "Desc", "Genre");

        mockMvc.perform(post("/api/v1/movies/api-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.meta.success").value(false))
                .andExpect(jsonPath("$.meta.errorMessage").value("Title already exists"));
    }


    @Test
    void whenUpdateValidMovie_thenReturn200ApiResponse() throws Exception {
        UpdateMovieRequest request = new UpdateMovieRequest("19", "Updated Movie 19", "Desc", "Genre");

        mockMvc.perform(put("/api/v1/movies/api-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Updated Movie 19"));
    }

    @Test
    void whenUpdateNonExistentMovie_thenReturn404ApiResponse() throws Exception {
        UpdateMovieRequest request = new UpdateMovieRequest("999", "No Title", "Desc", "Genre");

        mockMvc.perform(put("/api/v1/movies/api-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.meta.errorMessage").value("Movie not found"));
    }

    @Test
    void whenUpdateToExistingTitle_thenReturn400ApiResponse() throws Exception {
        // Пробуємо змінити Movie 20 на назву Movie 21 (яка вже є в базі)
        UpdateMovieRequest request = new UpdateMovieRequest("20", "Movie 21", "Desc", "Genre");

        mockMvc.perform(put("/api/v1/movies/api-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.meta.errorMessage").value("Movie already exists"));
    }

    @Test
    void whenDeleteExists_thenReturn200ApiResponse() throws Exception {
        mockMvc.perform(delete("/api/v1/movies/api-response/25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.success").value(true));
    }

    @Test
    void whenDeleteNonExistent_thenReturn404ApiResponse() throws Exception {
        mockMvc.perform(delete("/api/v1/movies/api-response/100"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.meta.success").value(false));
    }


    @Test
    void whenRequestIsSuccessful_metaCodeMatchesHttpStatus() throws Exception {
        mockMvc.perform(get("/api/v1/movies/api-response"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.code").value(200));
    }

    @Test
    void whenGetById_DataListShouldContainExactlyOneElement() throws Exception {
        mockMvc.perform(get("/api/v1/movies/api-response/20"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("20"));
    }
}
