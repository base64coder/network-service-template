package com.dtc.http.service;

import com.dtc.http.model.Product;
import com.dtc.http.repository.ProductRepository;
import com.dtc.annotations.ioc.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * 产品服务
 * 处理产品相关的业务逻辑
 * 
 * @author Network Service Template
 */
@Service
@Singleton
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    @Inject
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 获取所有产品
     */
    public List<Product> getAllProducts() {
        log.debug("Getting all products");
        return productRepository.findAll();
    }

    /**
     * 根据ID获取产品
     */
    public Product getProductById(String id) {
        log.debug("Getting product with id: {}", id);
        return productRepository.findById(id);
    }

    /**
     * 创建产品
     */
    public Product createProduct(Product product) {
        log.info("Creating product: {}", product);
        return productRepository.save(product);
    }
}
