package edu.litviniuk.mongo.service;

/*
  @author darin
  @project mongo
  @class MovieServiceMockTest
  @version 1.0.0
  @since 18.05.2025 - 14.30
*/

import edu.litviniuk.mongo.model.MovieModel;
import edu.litviniuk.mongo.repository.MovieRepository;
import edu.litviniuk.mongo.request.CreateMovieRequest;
import edu.litviniuk.mongo.request.UpdateMovieRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest
public class MovieServiceMockTest {

    @Mock
    private MovieRepository mockRepository;

    private MovieService underTest;

    @Captor
    private ArgumentCaptor<MovieModel> argumentCaptor;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new MovieService(mockRepository);
        argumentCaptor = ArgumentCaptor.forClass(MovieModel.class);
    }

    @AfterEach
    void tearsDown() throws Exception {
        autoCloseable.close();
    }

    @DisplayName("Create new Movie. Happy Path")
    @Test
    void whenInsertNewMovieAndTitleNotExistsThenOk() {
        CreateMovieRequest request = new CreateMovieRequest("Inception", "Mind-bending movie", "Sci-Fi");

        given(mockRepository.existsByTitle(request.title())).willReturn(false);

        given(mockRepository.save(any(MovieModel.class))).willAnswer(invocation -> {
            MovieModel savedMovie = invocation.getArgument(0);
            if (savedMovie.getId() == null) {
                savedMovie.setId("generated-id-123");
            }
            if(savedMovie.getCreateDate() == null) savedMovie.setCreateDate(LocalDateTime.now());
            if(savedMovie.getUpdateDate() == null) savedMovie.setUpdateDate(new ArrayList<>());
            return savedMovie;
        });

        MovieModel result = underTest.create(request);

        then(mockRepository).should().save(argumentCaptor.capture());
        MovieModel itemToSave = argumentCaptor.getValue();

        assertThat(itemToSave.getTitle()).isEqualTo(request.title());
        assertThat(itemToSave.getDescription()).isEqualTo(request.description());
        assertThat(itemToSave.getGenre()).isEqualTo(request.genre());
        assertNotNull(itemToSave.getCreateDate());

        assertTrue(itemToSave.getCreateDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertThat(itemToSave.getUpdateDate()).isEmpty();
        assertNotNull(result);
        assertThat(result.getId()).isEqualTo("generated-id-123");
        assertThat(result).isEqualTo(itemToSave);
        then(mockRepository).should(times(1)).existsByTitle(request.title());
        then(mockRepository).should(times(1)).save(any(MovieModel.class));
    }

    @Test
    @DisplayName("Update existing movie. Happy path")
    void updateMovie_whenValid_thenUpdated() {
        String id = "movie-id-456";
        LocalDateTime created = LocalDateTime.of(2023, 10, 20, 10, 30);
        List<LocalDateTime> updates = new ArrayList<>();
        updates.add(LocalDateTime.of(2024, 1, 15, 11, 0));
        MovieModel existing = MovieModel.builder()
                .id(id)
                .title("Old Title")
                .description("Old Desc")
                .genre("Old Genre")
                .createDate(created)
                .updateDate(updates)
                .build();

        UpdateMovieRequest request = new UpdateMovieRequest(
                id,
                "Updated Title",
                "Updated Desc",
                "Updated Genre"
        );

        given(mockRepository.findById(id)).willReturn(Optional.of(existing));
        given(mockRepository.existsByTitle(request.title())).willReturn(false);
        given(mockRepository.save(any(MovieModel.class))).willAnswer(invocation -> invocation.getArgument(0));
        MovieModel result = underTest.update(request);
        then(mockRepository).should().save(argumentCaptor.capture());
        MovieModel saved = argumentCaptor.getValue();

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getTitle()).isEqualTo("Updated Title");
        assertThat(saved.getDescription()).isEqualTo("Updated Desc");
        assertThat(saved.getGenre()).isEqualTo("Updated Genre");
        assertThat(saved.getCreateDate()).isEqualTo(created);
        assertThat(saved.getUpdateDate()).hasSize(2);
        assertTrue(saved.getUpdateDate().contains(updates.get(0)));
        assertTrue(saved.getUpdateDate().get(1).isAfter(updates.get(0)));
        assertTrue(saved.getUpdateDate().get(1).isBefore(LocalDateTime.now().plusSeconds(1)));
        assertThat(result).isEqualTo(saved);
        then(mockRepository).should(times(1)).findById(id);
        then(mockRepository).should(times(1)).existsByTitle(request.title());
        then(mockRepository).should(times(1)).save(saved);
    }

    @Test
    @DisplayName("Do not update if the new movie title already exists")
    void updateMovie_whenNewTitleExists_thenThrowException() {
        String id = "some-id";
        String existingTitle = "Existing Movie Title";
        UpdateMovieRequest request = new UpdateMovieRequest(
                id, existingTitle, "desc", "genre"
        );

        MovieModel existing = MovieModel.builder().id(id).title("Original Title").build();
        given(mockRepository.findById(id)).willReturn(Optional.of(existing));

        given(mockRepository.existsByTitle(existingTitle)).willReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            underTest.update(request);
        }, "Expected IllegalArgumentException when new title exists");

        then(mockRepository).should(times(1)).findById(id);
        then(mockRepository).should(times(1)).existsByTitle(existingTitle);
        then(mockRepository).should(never()).save(any());
    }


    @Test
    @DisplayName("Do not create movie if title already exists")
    void whenCreateMovieWithExistingTitle_thenThrowException() {
        CreateMovieRequest request = new CreateMovieRequest("Existing Movie Title", "desc", "genre");
        given(mockRepository.existsByTitle("Existing Movie Title")).willReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            underTest.create(request);
        }, "Expected IllegalArgumentException when creating with existing title");

        then(mockRepository).should(times(1)).existsByTitle(request.title());
        then(mockRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Do not update if movie not found by ID")
    void updateMovie_whenNotFound_thenThrowException() {
        String nonExistentId = "nonexistent-movie-id";
        UpdateMovieRequest request = new UpdateMovieRequest(
                nonExistentId, "New Title", "desc", "genre"
        );

        given(mockRepository.findById(nonExistentId)).willReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.update(request);
        }, "Expected IllegalArgumentException when movie not found");

        then(mockRepository).should(times(1)).findById(nonExistentId);
        then(mockRepository).should(never()).save(any());
        then(mockRepository).should(never()).existsByTitle(anyString());
    }


    @Test
    @DisplayName("Update with same data should still update timestamp")
    void updateMovie_whenSameData_thenUpdateTimestamp() {
        String id = "movie-id-789";
        LocalDateTime created = LocalDateTime.of(2023, 1, 1, 10, 0);
        List<LocalDateTime> updates = new ArrayList<>();
        MovieModel existing = MovieModel.builder()
                .id(id)
                .title("Same Title")
                .description("Same Desc")
                .genre("Same Genre")
                .createDate(created)
                .updateDate(updates)
                .build();

        UpdateMovieRequest request = new UpdateMovieRequest(
                id, "Same Title", "Same Desc", "Same Genre"
        );

        given(mockRepository.findById(id)).willReturn(Optional.of(existing));
        given(mockRepository.save(any(MovieModel.class))).willAnswer(invocation -> invocation.getArgument(0));
        MovieModel result = underTest.update(request);
        then(mockRepository).should().save(argumentCaptor.capture());
        MovieModel saved = argumentCaptor.getValue();

        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getTitle()).isEqualTo("Same Title");
        assertThat(saved.getDescription()).isEqualTo("Same Desc");
        assertThat(saved.getGenre()).isEqualTo("Same Genre");
        assertThat(saved.getCreateDate()).isEqualTo(created);
        assertThat(saved.getUpdateDate()).hasSize(1);
        assertTrue(saved.getUpdateDate().get(0).isBefore(LocalDateTime.now().plusSeconds(1)));

        assertThat(result).isEqualTo(saved);
        then(mockRepository).should(times(1)).findById(id);
        then(mockRepository).should(times(1)).save(saved);
        then(mockRepository).should(never()).existsByTitle(anyString());
    }


    @Test
    @DisplayName("Create movie with null values in request")
    void createMovie_whenNullValues_thenHandleAppropriately() {
        CreateMovieRequest request = new CreateMovieRequest(null, "A movie with no title or description", null);
        given(mockRepository.existsByTitle(request.title())).willReturn(false);

        given(mockRepository.save(any(MovieModel.class))).willAnswer(invocation -> {
            MovieModel savedMovie = invocation.getArgument(0);
            if (savedMovie.getId() == null) {
                savedMovie.setId("generated-id-456");
            }
            if(savedMovie.getCreateDate() == null) savedMovie.setCreateDate(LocalDateTime.now());
            if(savedMovie.getUpdateDate() == null) savedMovie.setUpdateDate(new ArrayList<>());
            return savedMovie;
        });

        MovieModel result = underTest.create(request);
        then(mockRepository).should().save(argumentCaptor.capture());
        MovieModel captured = argumentCaptor.getValue();

        assertThat(captured.getTitle()).isNull();
        assertThat(captured.getDescription()).isEqualTo("A movie with no title or description");
        assertThat(captured.getGenre()).isNull();
        assertNotNull(captured.getCreateDate());
        assertTrue(captured.getCreateDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertThat(captured.getUpdateDate()).isEmpty();

        assertNotNull(result);
        assertThat(result.getId()).isEqualTo("generated-id-456");
        assertThat(result).isEqualTo(captured);

        then(mockRepository).should(times(1)).existsByTitle(request.title());
        then(mockRepository).should(times(1)).save(any(MovieModel.class));
    }

    @Test
    @DisplayName("Update movie with only description changed")
    void updateMovie_whenOnlyDescriptionChanged_thenUpdate() {
        String id = "movie-id-987";
        LocalDateTime created = LocalDateTime.of(2024, 2, 10, 14, 0);
        List<LocalDateTime> updates = new ArrayList<>();

        MovieModel existing = MovieModel.builder()
                .id(id)
                .title("Movie Title")
                .description("old desc")
                .genre("Genre")
                .createDate(created)
                .updateDate(updates)
                .build();

        UpdateMovieRequest request = new UpdateMovieRequest(
                id, "Movie Title", "new desc", "Genre"
        );

        given(mockRepository.findById(id)).willReturn(Optional.of(existing));

        given(mockRepository.save(any(MovieModel.class))).willAnswer(invocation -> invocation.getArgument(0));

        MovieModel result = underTest.update(request);
        then(mockRepository).should().save(argumentCaptor.capture());
        MovieModel updated = argumentCaptor.getValue();

        assertThat(updated.getDescription()).isEqualTo("new desc");
        assertThat(updated.getTitle()).isEqualTo("Movie Title");
        assertThat(updated.getGenre()).isEqualTo("Genre");
        assertThat(updated.getCreateDate()).isEqualTo(created);
        assertThat(updated.getUpdateDate()).hasSize(1);
        assertTrue(updated.getUpdateDate().get(0).isBefore(LocalDateTime.now().plusSeconds(1)));


        assertThat(result).isEqualTo(updated);
        then(mockRepository).should(times(1)).findById(id);
        then(mockRepository).should(times(1)).save(updated);
        then(mockRepository).should(never()).existsByTitle(anyString());
    }

    @Test
    @DisplayName("Update movie setting description to null")
    void updateMovie_whenNullDescription_thenUpdatedWithNull() {
        String id = "movie-id-1011";
        LocalDateTime created = LocalDateTime.of(2023, 12, 1, 9, 0);
        List<LocalDateTime> updates = new ArrayList<>();

        MovieModel existing = MovieModel.builder()
                .id(id)
                .title("Movie Title 2")
                .description("initial desc")
                .genre("Genre 2")
                .createDate(created)
                .updateDate(updates)
                .build();

        UpdateMovieRequest request = new UpdateMovieRequest(
                id, "Movie Title 2", null, "Genre 2"
        );

        given(mockRepository.findById(id)).willReturn(Optional.of(existing));
        given(mockRepository.save(any(MovieModel.class))).willAnswer(invocation -> invocation.getArgument(0));

        MovieModel result = underTest.update(request);
        then(mockRepository).should().save(argumentCaptor.capture());
        MovieModel updated = argumentCaptor.getValue();

        assertThat(updated.getDescription()).isNull();
        assertThat(updated.getTitle()).isEqualTo("Movie Title 2");
        assertThat(updated.getGenre()).isEqualTo("Genre 2");
        assertThat(updated.getCreateDate()).isEqualTo(created);
        assertThat(updated.getUpdateDate()).hasSize(1);
        assertTrue(updated.getUpdateDate().get(0).isBefore(LocalDateTime.now().plusSeconds(1)));


        assertThat(result).isEqualTo(updated);
        then(mockRepository).should(times(1)).findById(id);
        then(mockRepository).should(times(1)).save(updated);
        then(mockRepository).should(never()).existsByTitle(anyString());
    }

    @Test
    @DisplayName("Create movie with empty string values")
    void createMovie_whenEmptyStrings_thenCreateWithEmptyStrings() {
        CreateMovieRequest request = new CreateMovieRequest("", "A movie with empty title and description", "");
        given(mockRepository.existsByTitle(request.title())).willReturn(false);

        given(mockRepository.save(any(MovieModel.class))).willAnswer(invocation -> {
            MovieModel savedMovie = invocation.getArgument(0);
            if (savedMovie.getId() == null) {
                savedMovie.setId("generated-id-789");
            }
            if(savedMovie.getCreateDate() == null) savedMovie.setCreateDate(LocalDateTime.now());
            if(savedMovie.getUpdateDate() == null) savedMovie.setUpdateDate(new ArrayList<>());
            return savedMovie;
        });

        MovieModel result = underTest.create(request);
        then(mockRepository).should().save(argumentCaptor.capture());
        MovieModel captured = argumentCaptor.getValue();

        assertThat(captured.getTitle()).isEqualTo("");
        assertThat(captured.getDescription()).isEqualTo("A movie with empty title and description");
        assertThat(captured.getGenre()).isEqualTo("");
        assertNotNull(captured.getCreateDate());
        assertTrue(captured.getCreateDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertThat(captured.getUpdateDate()).isEmpty();

        assertNotNull(result);
        assertThat(result.getId()).isEqualTo("generated-id-789");
        assertThat(result).isEqualTo(captured);
        then(mockRepository).should(times(1)).existsByTitle(request.title());
        then(mockRepository).should(times(1)).save(any(MovieModel.class));
    }
}
