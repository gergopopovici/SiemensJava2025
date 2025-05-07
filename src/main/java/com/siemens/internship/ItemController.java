package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Changed Created to Bad_Request
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);// Changed Bad_Request->Created
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.FOUND)) //Changed Ok -> Found
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Changed No_Content -> Not_Found
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.ACCEPTED); //Changed Created->Accepted
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Changed Accepted -> Not_Found
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if(itemService.findById(id).isPresent()) { //We need to check if the ID is present in the DataBase
            itemService.deleteById(id); // Deleting the ID
            return new ResponseEntity<>(HttpStatus.OK);// Sending HttpStatus OK
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // IF The ID isn't available we send Not Found
    }

    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        return new ResponseEntity<>(itemService.processItemsAsync(), HttpStatus.OK);
    }
}
