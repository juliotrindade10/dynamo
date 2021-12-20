package com.testcontainer.dynamo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.testcontainer.dynamo.dto.MovieDTO;

import io.restassured.RestAssured;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.with;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.hasItems;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DynamoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DynamoControllerTests extends AbstractDynamoInit {
	
	@Autowired
	protected ModelMapper modelMapper;
	
	@Autowired
	protected ObjectMapper mapper;
	
	@LocalServerPort
	private int localServerPort;
	
    @BeforeEach
    public void restAssuredPort() {
    	RestAssured.port = localServerPort;  
   		this.createTable();
    }
    
	@BeforeAll
	public static void init() {
		dynamoDBLocal.start();
	}
    
	@Test
	public void saveMovieTest() throws JsonMappingException, JsonProcessingException {	
		
		var movie = createMovie();
		
		with()
			.header("Content-Type","application/json")
			.body(movie)
			.when()
			.post("/api/v1/movie")
			.then()				
			.statusCode(200);
		
		var x = given().get("/api/v1/movie/{year}/{movie}","2023","Movie title").getBody().asString();
		
		
		given().get("/api/v1/movie/{year}/{movie}","2023","Movie title")
			.then()
			.assertThat()
			.body("year", is(movie.getYear()))
			.body("title", is(movie.getTitle()))
			.body("info.directors", hasItems("Michael Perez", "Carl Harris"))
			.body("info.releaseDate", equalTo("2013-09-02"))
//			.body("info.releaseDate", is(movie.getInfo().getReleaseDate()))
			.body("info.actors", hasItems("actor1", "actor2"));
		
	}
	
	private MovieDTO createMovie() throws JsonMappingException, JsonProcessingException {
        this.createTable();
        
        String jsonObject = "{\n"
				+ "    \"year\":\"2023\",\n"
				+ "    \"title\":\"Movie title\",\n"
				+ "    \"info\": {\n"
				+ "        \"directors\": [\"Michael Perez\", \"Carl Harris\"],\n"
				+ "        \"release_date\": \"2013-09-02T00:00:00Z\",\n"
				+ "        \"actors\": [\"actor 1\",\"actor 2\"]\n"
				+ "    }\n"
				+ "}";
		
		var movieDTO = mapper.readValue(jsonObject, MovieDTO.class);
		
		return movieDTO;
	
	}
	
}
