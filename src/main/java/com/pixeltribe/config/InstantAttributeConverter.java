package com.pixeltribe.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.Instant;

@Converter(autoApply = true) // autoApply=true 讓所有 Instant 型別自動使用此轉換器
public class InstantAttributeConverter implements AttributeConverter<Instant, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(Instant attribute) {
        // 從 Java 物件寫入資料庫時的轉換
        return (attribute == null ? null : Timestamp.from(attribute));
    }

    @Override
    public Instant convertToEntityAttribute(Timestamp dbData) {
        // 從資料庫讀取資料到 Java 物件時的轉換
        if (dbData == null) {
            return null;
        }

        // ▼▼▼ 終極偵錯日誌 ▼▼▼
//        System.out.println("==========================================================");
//        System.out.println("【偵錯】JDBC Driver 提供的原始 Timestamp: " + dbData);
//        System.out.println("【偵錯】Timestamp.toInstant() 的結果: " + dbData.toInstant());
//        System.out.println("==========================================================");
        // ▲▲▲ 終極偵錯日誌 ▲▲▲

        return dbData.toInstant();
    }
}