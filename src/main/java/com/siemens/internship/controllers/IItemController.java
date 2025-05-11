package com.siemens.internship.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import com.siemens.internship.models.Item;

public interface IItemController {

    ResponseEntity<List<Item>> getAllItems();

    ResponseEntity<Item> createItem(Item item, BindingResult result);

    ResponseEntity<Item> getItemById(Long id);

    ResponseEntity<Item> updateItem(Long id, Item item);

    ResponseEntity<Void> deleteItem(Long id);

    ResponseEntity<List<Item>> processItems();

}