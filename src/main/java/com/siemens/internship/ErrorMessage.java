package com.siemens.internship;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an error message with details.
 * Used to return validation errors with specific field messages.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {
    private String message;  // The main error message
    private Map<String, String> details = new HashMap<>();  // Map to store field-specific error messages
}
