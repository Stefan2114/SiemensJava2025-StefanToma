package com.siemens.internship.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.siemens.internship.exceptions.ServiceException;
import com.siemens.internship.exceptions.SourceNotFoundException;
import com.siemens.internship.models.Item;
import com.siemens.internship.repositories.IItemRepository;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService implements IItemService {

    private final IItemRepository itemRepository;
    private final ExecutorService executorService;

    public ItemService(IItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        this.executorService = Executors.newFixedThreadPool(10); // Create a thread pool with 10 threads
    }

    @Override
    public List<Item> findAll() {
        try {
            return this.itemRepository.findAll();
        } catch (Exception e) {
            throw new ServiceException("Error retrieving items", e);
        }

    }

    @Override
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new SourceNotFoundException("id " + id + " not found"));
    }

    @Override
    public Item save(Item item) {
        try {
            return itemRepository.save(item);
        } catch (Exception e) {
            throw new ServiceException("Error saving item", e);
        }

    }

    @Override
    public void deleteById(Long id) {
        try {
            if (!this.itemRepository.existsById(id)) {
                throw new SourceNotFoundException("id " + id + " not found");
            }
            itemRepository.deleteById(id);
        } catch (Exception e) {
            throw new ServiceException("Error deleting item with id: " + id, e);
        }

    }

    @Override
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        try {
            List<Long> itemIds = itemRepository.findAllIds();

            List<CompletableFuture<Item>> futures = itemIds.stream()
                    .map(id -> CompletableFuture.supplyAsync(() -> {
                        try {
                            Thread.sleep(100);
                            Item item = itemRepository.findById(id)
                                    .orElseThrow(
                                            () -> new SourceNotFoundException("id " + id + " not found"));

                            item.setStatus("PROCESSED");
                            return itemRepository.save(item);
                        } catch (InterruptedException e) {
                            throw new CompletionException("Processing interrupted", e);
                        } catch (Exception e) {
                            throw new CompletionException("Error processing item with id: " + id, e);
                        }
                    }, executorService))
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList()))
                    .exceptionally(ex -> {
                        throw new ServiceException("Error processing items: " + ex.getMessage(), ex);
                    });
        } catch (Exception e) {
            CompletableFuture<List<Item>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new ServiceException("Error initiating async processing"));
            return failedFuture;
        }
    }

}
