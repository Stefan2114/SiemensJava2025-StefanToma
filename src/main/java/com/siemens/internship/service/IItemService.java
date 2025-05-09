package com.siemens.internship.service;

import java.util.List;
import java.util.Optional;

import com.siemens.internship.model.Item;

public interface IItemService {

    List<Item> findAll();

    Optional<Item> findById(Long id);

    Item save(Item item);

    void deleteById(Long id);

    List<Item> processItemsAsync();

}
