package edu.litviniuk.mongo.service;

import edu.litviniuk.mongo.model.MovieModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class MovieTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanAndSetup() {
        mongoTemplate.remove(new org.springframework.data.mongodb.core.query.Query(), MovieModel.class);
        for (int i = 1; i <= 30; i++) {
            mongoTemplate.save(new MovieModel(String.valueOf(i), "Movie " + i, "Desc", "Genre"));
        }
    }

    @Test
    void shouldHaveExactly30Movies() {
        long count = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), MovieModel.class);
        assertEquals(30, count);
    }
}