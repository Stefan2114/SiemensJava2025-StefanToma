package com.siemens.internship.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.siemens.internship.exceptions.ServiceException;
import com.siemens.internship.exceptions.SourceNotFoundException;
import com.siemens.internship.models.Item;
import com.siemens.internship.repositories.IItemRepository;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTests {

    @Mock
    private IItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

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
    void findAll_ReturnsAllItems() {
        // Arrange
        when(itemRepository.findAll()).thenReturn(testItems);

        // Act
        List<Item> result = itemService.findAll();

        // Assert
        assertEquals(testItems, result);
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void findAll_ThrowsServiceException_WhenRepositoryThrowsException() {
        // Arrange
        when(itemRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            itemService.findAll();
        });

        assertEquals("Service error: Error retrieving items", exception.getMessage());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void findById_ReturnsItem_WhenItemExists() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // Act
        Item result = itemService.findById(1L);

        // Assert
        assertEquals(testItem, result);
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void findById_ThrowsSourceNotFoundException_WhenItemDoesNotExist() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        SourceNotFoundException exception = assertThrows(SourceNotFoundException.class, () -> {
            itemService.findById(1L);
        });

        assertEquals("Source not found: id 1 not found", exception.getMessage());
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void save_ReturnsItem_WhenItemIsSaved() {
        // Arrange
        when(itemRepository.save(testItem)).thenReturn(testItem);

        // Act
        Item result = itemService.save(testItem);

        // Assert
        assertEquals(testItem, result);
        verify(itemRepository, times(1)).save(testItem);
    }

    @Test
    void save_ThrowsServiceException_WhenRepositoryThrowsException() {
        // Arrange
        when(itemRepository.save(testItem)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            itemService.save(testItem);
        });

        assertEquals("Service error: Error saving item", exception.getMessage());
        verify(itemRepository, times(1)).save(testItem);
    }

    @Test
    void deleteById_DeletesItem_WhenItemExists() {
        // Arrange
        when(itemRepository.existsById(1L)).thenReturn(true);
        doNothing().when(itemRepository).deleteById(1L);

        // Act
        itemService.deleteById(1L);

        // Assert
        verify(itemRepository, times(1)).existsById(1L);
        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_ThrowsSourceNotFoundException_WhenItemDoesNotExist() {
        // Arrange
        when(itemRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            itemService.deleteById(1L);
        });

        assertEquals("Service error: Error deleting item with id: 1", exception.getMessage());
        verify(itemRepository, times(1)).existsById(1L);
        verify(itemRepository, never()).deleteById(1L);
    }

    @Test
    void deleteById_ThrowsServiceException_WhenRepositoryThrowsException() {
        // Arrange
        when(itemRepository.existsById(1L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            itemService.deleteById(1L);
        });

        assertEquals("Service error: Error deleting item with id: 1", exception.getMessage());
        verify(itemRepository, times(1)).existsById(1L);
    }

    @Test
    void processItemsAsync_ProcessesAllItems_WhenSuccessful() throws Exception {
        // Arrange
        List<Long> itemIds = Arrays.asList(1L, 2L);
        when(itemRepository.findAllIds()).thenReturn(itemIds);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(testItems.get(1)));

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

        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    Item item = invocation.getArgument(0);
                    return item;
                });

        // Act
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> result = future.get(); // Wait for the future to complete

        // Assert
        assertEquals(2, result.size());
        assertEquals("PROCESSED", result.get(0).getStatus());
        assertEquals("PROCESSED", result.get(1).getStatus());
        verify(itemRepository, times(1)).findAllIds();
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(2L);
        verify(itemRepository, times(2)).save(any(Item.class));
    }

    @Test
    void processItemsAsync_ThrowsException_WhenItemNotFound() throws Exception {
        // Arrange
        List<Long> itemIds = Arrays.asList(1L);
        when(itemRepository.findAllIds()).thenReturn(itemIds);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();

        // Assert
        CompletionException exception = assertThrows(CompletionException.class, () -> {
            future.join();
        });

        assertTrue(exception.getCause() instanceof ServiceException);
        verify(itemRepository, times(1)).findAllIds();
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void processItemsAsync_ThrowsException_WhenRepositoryFindAllIdsThrowsException() {
        // Arrange
        when(itemRepository.findAllIds()).thenThrow(new RuntimeException("Database error"));

        // Act
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();

        // Assert
        assertTrue(future.isCompletedExceptionally());
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            try {
                future.join();
            } catch (CompletionException e) {
                throw e.getCause();
            }
        });

        assertEquals("Service error: Error initiating async processing", exception.getMessage());
        verify(itemRepository, times(1)).findAllIds();
    }
}