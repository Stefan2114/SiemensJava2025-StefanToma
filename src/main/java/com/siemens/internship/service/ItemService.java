package com.siemens.internship.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.siemens.internship.Exceptions.ServiceException;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.IItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ItemService implements IItemService {

    private final IItemRepository itemRepository;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;

    public ItemService(IItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public List<Item> findAll() {
        try {
            return this.itemRepository.findAll();
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }

    }

    @Override
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ServiceException("error finding the Item"));
    }

    @Override
    public Item save(Item item) {
        try {
            return itemRepository.save(item);
        } catch (Exception e) {
            throw new ServiceException("null");
        }

    }

    @Override
    public void deleteById(Long id) {
        try {
            itemRepository.deleteById(id);
        } catch (Exception e) {
            throw new ServiceException("null");
        }

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
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async
     * operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */

    @Override
    @Async
    public List<Item> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        for (Long id : itemIds) {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(100);

                    Item item = itemRepository.findById(id).orElse(null);
                    if (item == null) {
                        return;
                    }

                    processedCount++;

                    item.setStatus("PROCESSED");
                    itemRepository.save(item);
                    processedItems.add(item);

                } catch (InterruptedException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }, EXECUTOR);
        }

        return processedItems;
    }

}
