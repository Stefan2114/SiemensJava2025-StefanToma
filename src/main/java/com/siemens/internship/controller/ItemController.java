package com.siemens.internship.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.siemens.internship.Exceptions.ServiceException;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.IItemService;

import java.util.List;

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
            return ResponseEntity.badRequest().build();
        }

    }

    @Override
    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.CREATED);
        }
        try {
            return ResponseEntity.ok(this.itemService.save(item));
        } catch (ServiceException e) {
            return ResponseEntity.badRequest().build();

        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {

        try {
            return ResponseEntity.ok(itemService.findById(id));
        } catch (ServiceException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        try {
            itemService.findById(id);
            return ResponseEntity.ok(this.itemService.save(item));
        } catch (ServiceException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        try {
            itemService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ServiceException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        try {
            return ResponseEntity.ok(itemService.processItemsAsync());
        } catch (ServiceException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
