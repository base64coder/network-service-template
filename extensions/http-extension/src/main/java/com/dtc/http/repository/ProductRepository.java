package com.dtc.http.repository;

import com.dtc.http.model.Product;
import com.dtc.annotations.ioc.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 产品数据访问层
 * 提供产品数据的存储和查询功能
 * 
 * @author Network Service Template
 */
@Repository
@Singleton
public class ProductRepository {

    private static final Logger log = LoggerFactory.getLogger(ProductRepository.class);

    // 内存存储产品数据
    private final Map<String, Product> products = new ConcurrentHashMap<>();

    public ProductRepository() {
        // 初始化示例数据
        save(new Product("1", "Product A", 99.99, "A great product"));
        save(new Product("2", "Product B", 149.99, "Another great product"));
    }

    /**
     * 查询所有产品
     */
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    /**
     * 根据ID查询产品
     */
    public Product findById(String id) {
        Product product = products.get(id);
        if (product == null) {
            // 如果产品不存在，返回一个默认产品
            return new Product(id, "Product A", 99.99, "A great product");
        }
        return product;
    }

    /**
     * 保存产品
     */
    public Product save(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(String.valueOf(System.currentTimeMillis()));
        }
        products.put(product.getId(), product);
        log.debug("Saved product: {}", product);
        return product;
    }
}
