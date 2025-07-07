package com.pixeltribe.forumsys.exception;

public class ForumCategoryAlreadyExistsException extends RuntimeException {
    public ForumCategoryAlreadyExistsException(String message) {
        super(message);
    }
}
