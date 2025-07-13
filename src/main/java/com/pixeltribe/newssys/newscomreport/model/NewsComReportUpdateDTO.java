package com.pixeltribe.newssys.newscomreport.model;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link NewsComReport}
 */
@Value
public class NewsComReportUpdateDTO implements Serializable {
    Integer id;
    Character newsComReportStatus;
    Character ncomNoNcomStatus;
}