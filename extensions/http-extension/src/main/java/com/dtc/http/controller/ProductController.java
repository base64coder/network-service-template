package com.dtc.http.controller;

import com.dtc.http.model.Product;
import com.dtc.http.service.ProductService;
import com.dtc.annotations.web.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * 产品控制器
 * 提供产品相关的 REST API 接口，包括创建、查询产品等功能
 * 
 * @author Network Service Template
 */
@RestController
@RequestMapping("/api/products")
@Singleton
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    @Inject
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 获取所有产品
     * 返回系统中所有产品的列表，以 JSON 格式返回
     */
    @GetMapping
    public Map<String, List<Product>> getProducts() {
        List<Product> products = productService.getAllProducts();
        return Map.of("products", products);
    }

    /**
     * 创建产品
     */
    @PostMapping
    public Map<String, Object> createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        return Map.of(
                "message", "Product created successfully",
                "id", created.getId());
    }

    /**
     * 根据ID获取产品
     */
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable("id") String id) {
        return productService.getProductById(id);
    }
}
