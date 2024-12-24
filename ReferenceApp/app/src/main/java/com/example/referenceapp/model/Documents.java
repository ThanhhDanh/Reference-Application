package com.example.referenceapp.model;

import java.io.Serializable;
import java.util.Date;

public class Documents implements Serializable {

    private int CategoryId;
    private int SchoolId;
    private int UserId;
    private String Description;
    private int Id;
    private double Price;
    private String ImagePath;

    private double Star;
    private String CreatedAt;
    private String UpdatedAt;

    private String Title;
    private int numberInCart;

    public Documents() {
    }

    public int getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(int categoryId) {
        CategoryId = categoryId;
    }

    public int getSchoolId() {
        return SchoolId;
    }

    public void setSchoolId(int schoolId) {
        SchoolId = schoolId;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public double getStar() {
        return Star;
    }

    public void setStar(double star) {
        Star = star;
    }

    public String getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }

    public String getUpdatedAt() {
        return UpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        UpdatedAt = updatedAt;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getNumberInCart() {
        return numberInCart;
    }

    public void setNumberInCart(int numberInCart) {
        this.numberInCart = numberInCart;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Documents document = (Documents) obj;

        return Id == document.Id &&
                Title.equals(document.Title) &&
                Description.equals(document.Description) &&
                (ImagePath != null ? ImagePath.equals(document.ImagePath) : document.ImagePath == null);
    }

    @Override
    public int hashCode() {
        int result = Id;
        result = 31 * result + (Title != null ? Title.hashCode() : 0);
        result = 31 * result + (Description != null ? Description.hashCode() : 0);
        result = 31 * result + (ImagePath != null ? ImagePath.hashCode() : 0);
        return result;
    }

}
