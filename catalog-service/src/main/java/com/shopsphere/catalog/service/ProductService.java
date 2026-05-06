package com.shopsphere.catalog.service;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.shopsphere.catalog.dto.ProductRequest;
import com.shopsphere.catalog.dto.ProductResponse;
import com.shopsphere.catalog.exception.InsufficientStockException;
import com.shopsphere.catalog.exception.InvalidRequestException;
import com.shopsphere.catalog.exception.ResourceNotFoundException;
import com.shopsphere.catalog.model.Category;
import com.shopsphere.catalog.model.Product;
import com.shopsphere.catalog.repository.CategoryRepository;
import com.shopsphere.catalog.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.shopsphere.catalog.dto.ProductPageResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Cacheable(
            value = "products",
            key = "'keyword:' + #keyword + ':category:' + #categoryId + ':page:' + #page + ':size:' + #size + ':sortBy:' + #sortBy + ':sortDir:' + #sortDir"
    )
    public ProductPageResponse getProducts(String keyword, Long categoryId,
                                           int page, int size, String sortBy, String sortDir) {

        System.out.println("Fetching products from DB...");

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.searchProducts(keyword, categoryId, pageable);

        List<ProductResponse> content = products.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ProductPageResponse.builder()
                .content(content)
                .page(products.getNumber())
                .size(products.getSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .first(products.isFirst())
                .last(products.isLast())
                .numberOfElements(products.getNumberOfElements())
                .build();
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Cacheable(value = "featuredProducts")
    public List<ProductResponse> getFeaturedProducts() {

        System.out.println("🔍 REDIS KEYS BEFORE DB CALL:");
        redisTemplate.keys("*").forEach(k -> System.out.println("KEY -> " + k));

        System.out.println("🔥 DB HIT: Fetching featured products from DATABASE");

        List<ProductResponse> result = productRepository.findByFeaturedTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();

        System.out.println("📦 RESULT SIZE: " + result.size());

        System.out.println("🔍 REDIS KEYS AFTER DB CALL:");
        redisTemplate.keys("*").forEach(k -> System.out.println("KEY -> " + k));

        return result;
    }

    @Cacheable(value = "product", key = "#id")
    public ProductResponse getProductById(Long id) {
        System.out.println("Fetching product by id from DB...");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @CacheEvict(value = {"products", "featuredProducts", "product"}, allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        Product product = mapToEntity(request);
        product = productRepository.save(product);
        return mapToResponse(product);
    }

    @CacheEvict(value = {"products", "featuredProducts", "product"}, allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setFeatured(request.isFeatured());

        if (request.getCategoryId() == null) {
            throw new InvalidRequestException("Category ID is required");
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        product.setCategory(category);

        product = productRepository.save(product);
        return mapToResponse(product);
    }

    @CacheEvict(value = {"products", "featuredProducts", "product"}, allEntries = true)
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @CacheEvict(value = {"products", "featuredProducts", "product"}, allEntries = true)
    @Transactional
    public ProductResponse reduceStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product '" + product.getName()
                            + "'. Available: " + product.getStockQuantity()
                            + ", Requested: " + quantity);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        product = productRepository.save(product);
        return mapToResponse(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .featured(product.isFeatured())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .build();
    }

    private Product mapToEntity(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .featured(request.isFeatured())
                .build();

        if (request.getCategoryId() == null) {
            throw new InvalidRequestException("Category ID is required");
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        product.setCategory(category);

        return product;
    }
}
