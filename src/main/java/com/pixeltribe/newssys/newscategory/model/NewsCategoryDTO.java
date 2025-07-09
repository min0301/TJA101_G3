package com.pixeltribe.newssys.newscategory.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewsCategoryDTO {
    private Integer categoryId;
    @Size(max = 50)
    @NotNull
    private String categoryName;
}
