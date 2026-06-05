package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

@Converter(autoApply=false)
public class MoneyConverter implements AttributeConverter<BigDecimal, Long> {

    @Override
    public Long convertToDatabaseColumn(BigDecimal value) {
        return value == null ? null : value.multiply(BigDecimal.valueOf(100)).longValue();
    }

    @Override
    public BigDecimal convertToEntityAttribute(Long cents) {
        return cents == null ? null : BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100));
    }
}
