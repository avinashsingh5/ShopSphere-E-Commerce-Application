package com.shopsphere.catalog.service;

import com.shopsphere.catalog.exception.ResourceNotFoundException;
import com.shopsphere.catalog.model.Category;
import com.shopsphere.catalog.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Cacheable(value = "categories")
    public List<Category> getAllCategories() {
        System.out.println("Fetching categories from DB...");
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @CacheEvict(value = {"categories", "products", "featuredProducts", "product"}, allEntries = true)
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new ResourceNotFoundException("Category already exists: " + category.getName());
        }
        return categoryRepository.save(category);
    }

    @CacheEvict(value = {"categories", "products", "featuredProducts", "product"}, allEntries = true)
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        return categoryRepository.save(category);
    }

    @CacheEvict(value = {"categories", "products", "featuredProducts", "product"}, allEntries = true)
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
