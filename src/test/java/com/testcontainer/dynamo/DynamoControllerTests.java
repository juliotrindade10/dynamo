package com.testcontainer.dynamo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcontainer.dynamo.dto.MovieDTO;
import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
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

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

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
		
		var directors = movie.getInfo().getDirectors();
		var actors = movie.getInfo().getActors();
		
		given().get("/api/v1/movie/{year}/{movie}","2023","Movie title")
			.then()
			.assertThat()
			.body("year", is(movie.getYear()))
			.body("title", is(movie.getTitle()))
			.body("info.directors", hasItems(directors.get(0), directors.get(1)))
			.body("info.release_date", is(movie.getInfo().getReleaseDate().toString()))
			.body("info.actors", hasItems(actors.get(0), actors.get(1)));
		
	}
	
	private MovieDTO createMovie() throws JsonMappingException, JsonProcessingException {
        this.createTable();
        
        String jsonObject = "{\n"
				+ "    \"year\":\"2023\",\n"
				+ "    \"title\":\"Movie title\",\n"
				+ "    \"info\": {\n"
				+ "        \"directors\": [\"Michael Perez\", \"Carl Harris\"],\n"
				+ "        \"release_date\": \"2013-09-02\",\n"
				+ "        \"actors\": [\"actor 1\",\"actor 2\"]\n"
				+ "    }\n"
				+ "}";
		
		var movieDTO = mapper.readValue(jsonObject, MovieDTO.class);
		
		return movieDTO;
	
	}

	@Test
	public void saveMovieTestWithLargeNumber() throws JsonMappingException, JsonProcessingException {
		var largeNumber = "2023202320232023202320232023202320232023.00";

		var movie = createMovieWithLargeNumber(largeNumber);


		given()
			.config(getRestAssuredConfig())
			.header("Content-Type","application/json")
			.body(movie)
		.when()
			.post("/api/v1/movie")
		.then()
			.statusCode(200);

		var directors = movie.getInfo().getDirectors();
		var actors = movie.getInfo().getActors();

		given()
			.config(getRestAssuredConfig())
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
			.get("/api/v1/movie/{year}/{movie}","2024","Dynamo Movie")
		.then()
				.log().all()
			.assertThat()
			.body("year", is(movie.getYear()))
			.body("title", is(movie.getTitle()))
			.body("info.directors", hasItems(directors.get(0), directors.get(1)))
			.body("info.release_date", is(movie.getInfo().getReleaseDate().toString()))
			.body("info.views", comparesEqualTo(movie.getInfo().getViews()))
			.body("info.actors", hasItems(actors.get(0), actors.get(1)));

	}

	private RestAssuredConfig getRestAssuredConfig() {
		return RestAssuredConfig.newConfig().jsonConfig(JsonConfig.jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
	}

	private MovieDTO createMovieWithLargeNumber(String largeNumber) throws JsonMappingException, JsonProcessingException {
		this.createTable();

		String jsonObject = "{\n"
				+ "    \"year\":\"2024\",\n"
				+ "    \"title\":\"Dynamo Movie\",\n"
				+ "    \"info\": {\n"
				+ "        \"directors\": [\"Michael Perez\", \"Carl Harris\"],\n"
				+ "        \"release_date\": \"2013-09-02\",\n"
				+ "        \"views\": "+largeNumber+",\n"
				+ "        \"actors\": [\"actor 1\",\"actor 2\"]\n"
				+ "    }\n"
				+ "}";

		var movieDTO = mapper.readValue(jsonObject, MovieDTO.class);

		return movieDTO;

	}

}
