package com.esi.ridebooking.products;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

@Autowired
private ProductRepository productRepository;

        public   List<Product> getAllProducts(){
            List<Product> products =  new ArrayList<>();
            productRepository.findAll().forEach(products::add);
            return products;
        }    
    
        public   Optional<Product>  getProduct(String id){
            return productRepository.findById(id);
        }

        public void addProduct(Product product) {
            productRepository.save(product);
        }

        public void updateProduct(String id, Product product) {
            productRepository.save(product);
        }

        public void deleteProduct(String id) {
            productRepository.deleteById(id);
        }
            
        };

