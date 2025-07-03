package com.pixeltribe.shopsys.malltag.model;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class MallTagDTO implements Serializable{
	Integer mallTagNO;
	@Size(max = 25)
    @NotNull
	String mallTagName;
}
