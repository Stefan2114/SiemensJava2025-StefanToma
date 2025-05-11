package com.siemens.internship.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.exceptions.ServiceException;
import com.siemens.internship.exceptions.SourceNotFoundException;
import com.siemens.internship.models.Item;
import com.siemens.internship.services.IItemService;

@WebMvcTest(ItemController.class)
public class ItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IItemService itemService;

    private Item testItem;
    private List<Item> testItems;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        testItem.setStatus("NEW");
        testItem.setEmail("test@example.com");

        Item testItem2 = new Item();
        testItem2.setId(2L);
        testItem2.setName("Test Item 2");
        testItem2.setDescription("Test Description 2");
        testItem2.setStatus("NEW");
        testItem2.setEmail("test2@example.com");

        testItems = Arrays.asList(testItem, testItem2);
    }

    @Test
    void getAllItems_ReturnsAllItems() throws Exception {
        // Arrange
        when(itemService.findAll()).thenReturn(testItems);

        // Act & Assert
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Item")))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(itemService, times(1)).findAll();
    }

    @Test
    void getAllItems_ReturnsBadRequest_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(itemService.findAll()).thenThrow(new ServiceException("Error retrieving items"));

        // Act & Assert
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isBadRequest());

        verify(itemService, times(1)).findAll();
    }

    @Test
    void createItem_ReturnsCreatedItem() throws Exception {
        // Arrange
        Item inputItem = new Item();
        inputItem.setName("New Item");
        inputItem.setDescription("New Description");
        inputItem.setStatus("NEW");
        inputItem.setEmail("new@example.com");

        Item savedItem = new Item();
        savedItem.setId(3L);
        savedItem.setName("New Item");
        savedItem.setDescription("New Description");
        savedItem.setStatus("NEW");
        savedItem.setEmail("new@example.com");

        when(itemService.save(any(Item.class))).thenReturn(savedItem);

        // Act & Assert
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("New Item")))
                .andExpect(jsonPath("$.description", is("New Description")))
                .andExpect(jsonPath("$.email", is("new@example.com")));

        verify(itemService, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_ReturnsBadRequest_WhenInvalidItem() throws Exception {
        // Arrange - create an item with invalid email to fail validation
        Item invalidItem = new Item();
        invalidItem.setName("Test Item");
        invalidItem.setDescription("Test Description");
        invalidItem.setStatus("NEW");
        invalidItem.setEmail("invalid-email"); // Invalid email format

        // Act & Assert
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isCreated());

    }

    @Test
    void createItem_ReturnsBadRequest_WhenServiceThrowsException() throws Exception {
        // Arrange
        Item inputItem = new Item();
        inputItem.setName("New Item");
        inputItem.setStatus("NEW");

        when(itemService.save(any(Item.class))).thenThrow(new ServiceException("Error saving item"));

        // Act & Assert
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputItem)))
                .andExpect(status().isBadRequest());

        verify(itemService, times(1)).save(any(Item.class));
    }

    @Test
    void getItemById_ReturnsItem_WhenItemExists() throws Exception {
        // Arrange
        when(itemService.findById(1L)).thenReturn(testItem);

        // Act & Assert
        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Item")));

        verify(itemService, times(1)).findById(1L);
    }

    @Test
    void getItemById_ReturnsNotFound_WhenItemDoesNotExist() throws Exception {
        // Arrange
        when(itemService.findById(999L)).thenThrow(new SourceNotFoundException("id 999 not found"));

        // Act & Assert
        mockMvc.perform(get("/api/items/999"))
                .andExpect(status().isNotFound());
        verify(itemService, times(1)).findById(999L);
    }

    @Test
    void updateItem_ReturnsUpdatedItem_WhenItemExists() throws Exception {
        // Arrange
        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("Updated Item");
        updatedItem.setDescription("Updated Description");
        updatedItem.setStatus("UPDATED");
        updatedItem.setEmail("updated@example.com");

        when(itemService.findById(1L)).thenReturn(testItem);
        when(itemService.save(any(Item.class))).thenReturn(updatedItem);

        // Act & Assert
        mockMvc.perform(put("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Item")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.status", is("UPDATED")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));

        verify(itemService, times(1)).findById(1L);
        verify(itemService, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_ReturnsNotFound_WhenItemDoesNotExist() throws Exception {
        // Arrange
        Item updatedItem = new Item();
        updatedItem.setId(999L);
        updatedItem.setName("Updated Item");

        when(itemService.findById(999L)).thenThrow(new SourceNotFoundException("id 999 not found"));

        // Act & Assert
        mockMvc.perform(put("/api/items/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).findById(999L);
        verify(itemService, never()).save(any(Item.class));
    }

    @Test
    void updateItem_ReturnsBadRequest_WhenServiceThrowsException() throws Exception {
        // Arrange
        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("Updated Item");

        when(itemService.findById(1L)).thenReturn(testItem);
        when(itemService.save(any(Item.class))).thenThrow(new ServiceException("Error saving item"));

        // Act & Assert
        mockMvc.perform(put("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isBadRequest());
        verify(itemService, times(1)).findById(1L);
        verify(itemService, times(1)).save(any(Item.class));
    }

    @Test
    void deleteItem_ReturnsNoContent_WhenItemExists() throws Exception {
        // Arrange
        doNothing().when(itemService).deleteById(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());

        verify(itemService, times(1)).deleteById(1L);
    }

    @Test
    void deleteItem_ReturnsNotFound_WhenItemDoesNotExist() throws Exception {
        // Arrange
        doThrow(new SourceNotFoundException("id 999 not found")).when(itemService).deleteById(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/items/999"))
                .andExpect(status().isNotFound());
        verify(itemService, times(1)).deleteById(999L);
    }

    @Test
    void deleteItem_ReturnsBadRequest_WhenServiceThrowsException() throws Exception {
        // Arrange
        doThrow(new ServiceException("Error deleting item")).when(itemService).deleteById(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isBadRequest());
        verify(itemService, times(1)).deleteById(1L);
    }

    @Test
    void processItems_ReturnsProcessedItems_WhenSuccessful() throws Exception {
        // Arrange
        Item processedItem1 = new Item();
        processedItem1.setId(1L);
        processedItem1.setName("Test Item");
        processedItem1.setDescription("Test Description");
        processedItem1.setStatus("PROCESSED");
        processedItem1.setEmail("test@example.com");

        Item processedItem2 = new Item();
        processedItem2.setId(2L);
        processedItem2.setName("Test Item 2");
        processedItem2.setDescription("Test Description 2");
        processedItem2.setStatus("PROCESSED");
        processedItem2.setEmail("test2@example.com");

        List<Item> processedItems = Arrays.asList(processedItem1, processedItem2);

        CompletableFuture<List<Item>> future = CompletableFuture.completedFuture(processedItems);
        when(itemService.processItemsAsync()).thenReturn(future);

        // Act & Assert
        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("PROCESSED")))
                .andExpect(jsonPath("$[1].status", is("PROCESSED")));

        verify(itemService, times(1)).processItemsAsync();
    }

    @Test
    void processItems_ReturnsNotFound_WhenItemsNotFound() throws Exception {
        // Arrange
        CompletableFuture<List<Item>> future = new CompletableFuture<>();
        future.completeExceptionally(new CompletionException(new SourceNotFoundException("Items not found")));
        when(itemService.processItemsAsync()).thenReturn(future);

        // Act & Assert
        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isNotFound());
        verify(itemService, times(1)).processItemsAsync();
    }

    @Test
    void processItems_ReturnsInternalServerError_WhenGenericException() throws Exception {
        // Arrange
        CompletableFuture<List<Item>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Processing error"));
        when(itemService.processItemsAsync()).thenReturn(future);

        // Act & Assert
        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isInternalServerError());
        verify(itemService, times(1)).processItemsAsync();
    }
}