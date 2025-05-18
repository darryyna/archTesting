package edu.litviniuk.mongo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/*
  @author darin
  @project mongo
  @class Movie
  @version 1.0.0
  @since 17.04.2025 - 18.10
*/

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document
public class MovieModel {

    @Id
    private String id;
    private String title;
    private String description;
    private String genre;

    private LocalDateTime createDate;
    private List<LocalDateTime> updateDate;

    public MovieModel(String title, String description, String genre) {
        this.title = title;
        this.description = description;
        this.genre = genre;
    }

    public MovieModel(String id, String title, String description, String genre) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.genre = genre;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MovieModel movie)) return false;
        return Objects.equals(id, movie.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
