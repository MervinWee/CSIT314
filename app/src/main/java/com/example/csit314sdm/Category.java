package com.example.csit314sdm;

import com.google.firebase.firestore.Exclude;
import java.util.Objects;

// BCE: This is the ENTITY class.
public class Category {
    private String id;
    private String name;
    private String description;

    // A no-arg constructor is required for calls to doc.toObject(Category.class)
    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }


    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id) &&
               Objects.equals(name, category.name) &&
               Objects.equals(description, category.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }
}
