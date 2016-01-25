package com.smartbear.readyapi.client.assertions;

import io.swagger.client.model.InvalidHttpStatusCodesAssertion;

import static com.smartbear.readyapi.client.Validator.validateNotEmpty;

public class InvalidHttpStatusCodesAssertionBuilder extends ValidHttpStatusCodesAssertionBuilder<InvalidHttpStatusCodesAssertion> {
    InvalidHttpStatusCodesAssertionBuilder() {
    }

    @Override
    public InvalidHttpStatusCodesAssertion build() {
        validateNotEmpty(statusCodes, "Missing status codes. Status codes are mandatory for InvalidHttpStatusCodesAssertion");
        InvalidHttpStatusCodesAssertion invalidHttpStatusCodesAssertion = new InvalidHttpStatusCodesAssertion();
        invalidHttpStatusCodesAssertion.setType("Invalid HTTP Status Codes");
        invalidHttpStatusCodesAssertion.setInvalidStatusCodes(statusCodes);
        return invalidHttpStatusCodesAssertion;
    }
}