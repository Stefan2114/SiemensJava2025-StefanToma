package com.siemens.internship.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(nullable = false, length = 256)
    private String description;

    @Column(nullable = false, length = 256)
    private String status;

    // Add email regex validation
    @Column(nullable = false, length = 256)
    private String email;
}