package com.moneytransfer.utils;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class MoneySerializer extends JsonSerializer<BigDecimal> {

	@Override
	public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeString(value.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
	}

}
