package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for managing items.
 * Provides endpoints for CRUD operations and asynchronous processing.
 */

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    // Get all items
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    // Create a new item with validation
    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setMessage("Validation Failed");
            result.getFieldErrors().forEach(error -> errorMessage
                    .getDetails().put(error.getField(), error.getDefaultMessage()));
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST); // Changed Created to Bad_Request
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED); // Changed Bad_Request -> Created
    }

    // Get an item by its ID
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.FOUND)) // Changed OK -> FOUND
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Changed No_Content -> NOT_FOUND
    }

    // Update an item by its ID
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.ACCEPTED); // Changed Created -> Accepted
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Changed Accepted -> NOT_FOUND
        }
    }

    // Delete an item by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemService.findById(id).isPresent()) { // We need to check if the
            // ID exists in the database
            itemService.deleteById(id); // Delete the item
            return new ResponseEntity<>(HttpStatus.OK); // Return HTTP OK status
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // If ID not found, return NOT_FOUND
    }

    // Process items asynchronously
    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() {
        return itemService.processItemsAsync()
                .thenApply(items -> new ResponseEntity<>(items, HttpStatus.OK));
    }
}
