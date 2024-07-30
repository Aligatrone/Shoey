package org.example;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.UUID;

//@Converter(autoApply = true)
//public class UUIDConverter implements AttributeConverter<UUID, UUID> {
//    @Override
//    public UUID convertToDatabaseColumn(UUID uuid) {
//        return (uuid == null) ? null : uuid;
//    }
//
//    @Override
//    public UUID convertToEntityAttribute(UUID uuid) {
//        return uuid;
//    }
//
//}
