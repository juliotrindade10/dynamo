package com.testcontainer.dynamo.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class LargeNumberConverter implements DynamoDBTypeConverter<String, BigDecimal>{

	@Override
	public String convert(BigDecimal number) {
		log.info("Converting a number {} to string.", number);
		return number.toString();
	}

	@Override
	public BigDecimal unconvert(String str) {
		log.info("Converting a number in a string {} to BigDecimal.", str);
		return new BigDecimal(str);
	}

}
