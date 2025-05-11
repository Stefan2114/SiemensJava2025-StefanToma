package com.siemens.internship.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.siemens.internship.exceptions.ServiceException;
import com.siemens.internship.exceptions.SourceNotFoundException;
import com.siemens.internship.models.Item;
import com.siemens.internship.services.IItemService;

import java.util.List;
import java.util.concurrent.CompletionException;

@Validated
@RestController
@RequestMapping("/api/items")
public class ItemController implements IItemController {

    private final IItemService itemService;

    public ItemController(IItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        try {
            List<Item> items = this.itemService.findAll();
            return ResponseEntity.ok(items);
        } catch (ServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }

    }

    @Override
    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation errors: " + result.getAllErrors());
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(this.itemService.save(item));
        } catch (ServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);

        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {

        try {
            return ResponseEntity.ok(itemService.findById(id));
        } catch (SourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found", e);
        }
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @Valid @RequestBody Item item) {
        try {
            itemService.findById(id);
            item.setId(id);
            return ResponseEntity.ok(this.itemService.save(item));
        } catch (SourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error finding item", e);

        } catch (ServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error updating item", e);
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        try {
            itemService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (SourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found", e);
        } catch (ServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error deleting item", e);
        }
    }

    @Override
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        try {
            List<Item> processedItems = itemService.processItemsAsync().join();
            return ResponseEntity.ok(processedItems);
        } catch (CompletionException e) {
            Throwable cause = e.getCause();

            if (cause instanceof SourceNotFoundException) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error finding items", e);
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing items", e);

            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing items", e);
        }
    }
}
