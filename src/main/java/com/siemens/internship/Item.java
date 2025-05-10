package com.siemens.internship;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an Item with validation annotations.
 * Used to store item data in the database and ensure proper validation.
 */

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;  // Unique identifier for the item

    @NotBlank(message = "name cannot be empty")  // Ensures the name is not empty
    private String name;

    @NotBlank(message = "description cannot be empty")  // Ensures description
    // is not empty
    private String description;

    @NotBlank(message = "status cannot be empty")  // Ensures status is not empty
    private String status;

    // Email with regex validation for proper email format
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$",
            message = "Invalid email format"
    )
    private String email;
}
