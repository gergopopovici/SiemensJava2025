package com.siemens.internship;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing items.
 * Provides methods for CRUD operations (create, read, update, delete) and
 * asynchronous processing of items.
 */

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     * <p>
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple
     * async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */

    /**
     * What was wrong before:
     * Wrong return type (List instead of CompletableFuture) was returning empty
     * list too early
     * Did not wait for background tasks to finish
     * ArrayList wasn't thread-safe for multiple background threads
     * Bad error handling just println, no proper logging
     * Was using a shared counter variable without synchronization
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        // Get all IDs to process
        List<Long> itemIds = itemRepository.findAllIds();

        // Using thread-safe list
        List<Item> processedItems = Collections.synchronizedList(new ArrayList<>());

        // Keeping track of all futures so we can wait for everything to finish
        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                            try {
                                Thread.sleep(100);
                                return itemRepository.findById(id).orElse(null);
                            } catch (InterruptedException e) {
                                // Need to set interrupt flag when catching
                                // InterruptedException
                                Thread.currentThread().interrupt();
                                throw new CompletionException("Processing interrupted", e);
                            }
                        }, executor)

                        // Processing the item if found
                        .thenApply(item -> {
                            if (item == null) {
                                return null; // Skip items not found
                            }
                            item.setStatus("PROCESSED");
                            return item;
                        })

                        // Save to DB
                        .thenCompose(item -> {
                            if (item == null) {
                                return CompletableFuture.completedFuture(null);
                            }

                            return CompletableFuture.supplyAsync(() -> itemRepository.save(item), executor);
                        })

                        // Add to our result list
                        .thenAccept(savedItem -> {
                            if (savedItem != null) {
                                processedItems.add(savedItem);
                            }
                        })

                        // Handle errors properly
                        .exceptionally(ex -> {
                            logger.error("Error processing item: " + ex.getMessage(), ex);
                            return null;
                        }))
                .collect(Collectors.toList());

        // wait for ALL futures to complete before returning
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> processedItems);
    }
}

