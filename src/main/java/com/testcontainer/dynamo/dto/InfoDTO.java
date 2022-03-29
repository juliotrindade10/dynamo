package com.testcontainer.dynamo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class InfoDTO {

	private List<String> directors;
	@JsonProperty(value = "release_date")
	@JsonFormat(pattern="yyyy-MM-dd")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private LocalDate releaseDate;
	private BigDecimal views;
	private Double rating;
	private List<String> genres;
	@JsonProperty(value = "image_url")
	private String imageUrl;
	private String plot;
	@JsonProperty(value = "running_time_secs")
	private Integer runningTimeSecs;
	private List<String> actors;
	
}
