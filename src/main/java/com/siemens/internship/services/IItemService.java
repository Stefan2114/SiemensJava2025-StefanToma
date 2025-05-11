package com.siemens.internship.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.siemens.internship.models.Item;

public interface IItemService {

    List<Item> findAll();

    Item findById(Long id);

    Item save(Item item);

    void deleteById(Long id);

    CompletableFuture<List<Item>> processItemsAsync();

}
