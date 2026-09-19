package com.estradahermanos.shoestock.repository.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shoe")
public class Shoe
{
    @Id
    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "type", length = 20, nullable = false)
    private String type;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "supplier", nullable = false)
    private Integer supplier;

    @Column(name = "created_at")
    private LocalDate createdAt;
}
