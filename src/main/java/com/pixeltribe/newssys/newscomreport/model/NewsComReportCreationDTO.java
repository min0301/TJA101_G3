package com.pixeltribe.newssys.newscomreport.model;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link NewsComReport}
 */
@Value
public class NewsComReportCreationDTO implements Serializable {
    Integer reporterId;
    Integer reportTypeId;
    Integer ncomNoId;
}