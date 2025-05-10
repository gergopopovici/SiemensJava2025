package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ItemController, verifying CRUD operations and async processing
 * using mocked ItemService and repository.
 */

@SpringBootTest
class InternshipApplicationTests {

    @Mock
    private ItemService itemService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private ItemController itemController;

    /**
     * Initializes mocks before each test method.
     */

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    /**
     * Ensures Spring Boot context loads without issues.
     */

    @Test
    void contextLoads() {
    }

    /**
     * Tests that getAllItems() returns all available items.
     */

    @Test
    void getAllItems_shouldReturnAllItems() {
        // Arrange
        List<Item> items = Arrays.asList(new Item(1L, "Test1",
                        "Description1", "On Stock",
                        "email1@test.com"),
                new Item(2L, "Test2",
                        "Description2", "On Stock",
                        "email2@test.com"));
        when(itemService.findAll()).thenReturn(items);

        // Act
        ResponseEntity<List<Item>> response = itemController.getAllItems();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("Test1", response.getBody().get(0).getName());
    }

    /**
     * Tests that getItemById() returns the correct item if it exists.
     */

    @Test
    void getItemById_withExistingId_shouldReturnItem() {
        //Arrange
        Item item = new Item(1L, "Test1",
                "Description1", "On Stock",
                "email1@test.com");
        when(itemService.findById(1L)).thenReturn(Optional.of(item));

        //Act
        ResponseEntity<Item> response = itemController.getItemById(1L);

        //Assert
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("Test1", Objects.requireNonNull(response.getBody()).
                getName());
    }

    /**
     * Tests that getItemById() returns 404 for non-existent ID.
     */

    @Test
    void getItemById_withNonExistingId_shouldReturnNotFound() {
        //Arrange
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        //Act
        ResponseEntity<Item> response = itemController.getItemById(1L);

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    /**
     * Tests that a valid item is created successfully.
     */

    @Test
    void createItem_withValidData_shouldReturnCreatedItem() {
        //Arrange
        Item item = new Item(null, "Test Item", "Description",
                "In Stock", "email@test.com");
        Item savedItem = new Item(1L, "Test Item", "Description",
                "In Stock", "email@test.com");
        when(itemService.save(item)).thenReturn(savedItem);
        when(bindingResult.hasErrors()).thenReturn(false);

        //AcT
        ResponseEntity<Object> response = itemController.createItem(item,
                bindingResult);

        //Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Item responseItem = (Item) response.getBody();
        assert responseItem != null;
        assertEquals(1, responseItem.getId());
    }

    /**
     * Tests that invalid item creation returns validation error.
     */

    @Test
    void createItem_withInvalidData_shouldReturnBadRequest() {

        //Arrange
        Item item = new Item(null, "", "Description",
                "In Stock", "email@test.com");
        List<FieldError> errors = List.of(new FieldError("item",
                "name", "name cannot be empty"));
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(errors);

        //Act
        ResponseEntity<Object> response = itemController.createItem(item,
                bindingResult);

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorMessage errorMessage = (ErrorMessage) response.getBody();
        assert errorMessage != null;
        assertEquals("Validation Failed", errorMessage.getMessage());
        assertEquals("name cannot be empty", errorMessage.getDetails().
                get("name"));
    }

    /**
     * Tests updating an item that exists.
     */

    @Test
    void updateItem_withExistingId_shouldUpdateItem() {

        //Arrange
        Item oldItem = new Item(1L, "Test Item",
                "Description", "In Stock", "email@test.com");
        Item updatedItem = new Item(null, "Test Item2",
                "Description2",
                "In Stock2", "email2@test.com");
        Item savedItem = new Item(1L, "Test Item2",
                "Description2",
                "In Stock2", "email2@test.com");

        when(itemService.findById(1L)).thenReturn(Optional.of(oldItem));
        when(itemService.save(any(Item.class))).thenReturn(savedItem);

        //Act
        ResponseEntity<Item> response = itemController.updateItem(1L,
                updatedItem);

        //Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Test Item2",
                Objects.requireNonNull(response.getBody()).getName());
        assertEquals("Description2", response.getBody().getDescription());
        assertEquals("email2@test.com", response.getBody().getEmail());
        assertEquals("In Stock2", response.getBody().getStatus());
    }

    /**
     * Tests that updating a nonexistent item returns 404.
     */

    @Test
    void updateItem_withNonExistingId_shouldReturnNotFound() {
        //Arrange
        Item updatedItem = new Item(null, "Test Item",
                "Description",
                "In Stock", "email@test.com");
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        //Act
        ResponseEntity<Item> response = itemController.updateItem(1L,
                updatedItem);

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    /**
     * Tests deletion of an existing item.
     */

    @Test
    void deleteItem_withExistingId_shouldDeleteItem() {

        //Arrange
        Item item = new Item(1L, "Test Item", "Description",
                "In Stock", "email@test.com");
        when(itemService.findById(1L)).thenReturn(Optional.of(item));
        doNothing().when(itemService).deleteById(1L);

        //Act
        ResponseEntity<Void> response = itemController.deleteItem(1L);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    /**
     * Tests deletion of a non-existent item returns 404.
     */

    @Test
    void deleteItem_withNonExistingId_shouldReturnNotFound() {
        //Arrange
        doNothing().when(itemService).deleteById(1L);

        //Act
        ResponseEntity<Void> response = itemController.deleteItem(1L);

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    /**
     * Tests asynchronous processing returns processed items correctly.
     */

    @Test
    void processItems_shouldReturnProcessedItems() {
        // Arrange
        List<Item> processedItems = List.of(
                new Item(1L, "Processed Item", "Description",
                        "PROCESSED", "email@test.com")
        );
        CompletableFuture<List<Item>> future =
                CompletableFuture.completedFuture(processedItems);
        when(itemService.processItemsAsync()).thenReturn(future);

        // Act
        CompletableFuture<ResponseEntity<List<Item>>> responseFuture =
                itemController.processItems();

        // Assert
        assertNotNull(responseFuture);
        try {
            ResponseEntity<List<Item>> response = responseFuture.get();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals("PROCESSED", response.getBody().get(0).getStatus());
            assertEquals("Processed Item", response.getBody().get(0).getName());
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    /**
     * Tests that exceptions during async processing are handled.
     */

    @Test
    void processItems_withFailure_shouldLogAndSkip() {
        // Arrange
        when(itemService.processItemsAsync()).thenReturn(
                CompletableFuture.supplyAsync(() -> {
                    throw new RuntimeException("Simulated failure");
                })
        );

        // Act
        CompletableFuture<ResponseEntity<List<Item>>> future =
                itemController.processItems();

        // Assert
        assertThrows(ExecutionException.class, future::get);
    }

    /**
     * Tests that empty result list from async processing is handled correctly.
     */
    @Test
    void processItems_withNoItems_shouldReturnEmptyList() {

        // Arrange

        when(itemService.processItemsAsync()).thenReturn(CompletableFuture.
                completedFuture(Collections.emptyList()));

        // Act
        CompletableFuture<ResponseEntity<List<Item>>> future = itemController.
                processItems();

        // Assert
        try {
            ResponseEntity<List<Item>> response = future.get();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(Objects.requireNonNull(response.getBody()).isEmpty());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }


}
