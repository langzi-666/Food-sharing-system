package com.foodsharing.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tags", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tags_name", columnNames = {"name"})
})
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
