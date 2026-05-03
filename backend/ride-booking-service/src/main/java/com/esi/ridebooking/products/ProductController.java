package com.esi.ridebooking.products;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {


/*     private  List<Product> products =  new ArrayList<>(Arrays.asList(
        new Product("01", "Heavy vehicle", "Can be used for heavy work", BigDecimal.valueOf(1200)),
        new Product("02", "Medium vehicle", "Can be used for medium work", BigDecimal.valueOf(1800)),
        new Product("03", "Light vehicle", "Can be used for light work", BigDecimal.valueOf(2200)) 
    ));  
 */

@Autowired
private ProductService productService;

// @CrossOrigin(origins = "http://localhost:8081")
@GetMapping("/products")
    public List<Product> getAllProducts(){
        //return products;
        return productService.getAllProducts();
    }
// For simple description of Java Lambda expressions  https://www.w3schools.com/java/java_lambda.asp

@GetMapping("/products/{id}")
        public Optional<Product> getProduct(@PathVariable String id){
        //return products.stream().filter(p->p.getId().equals(id)).findFirst().get();
        return productService.getProduct(id);
    }

// Here we use products, an ArrayList, as a source for a stream, and then perform a filter-map on the stream to obtain the first element that has the same passed, then, returns it.



@PostMapping("/products")
    public void addProduct(@RequestBody Product product){
    //products.add(product);
    productService.addProduct(product);
    }

@PutMapping("/products/{id}")
    public void updateProduct(@RequestBody Product product, @PathVariable String id){
    /*          
            for (int i = 0; i < products.size(); i++){
            Product p = products.get(i);
            if (p.getId().equals(id)){
            products.set(i, product);
            return;}} 
    */
    productService.updateProduct(id, product);
    }

//size() is a method implemented by all members of Collection (lists, sets, stacks,...). It returns the number of elements the collection contains.

@DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable String id){
        //products.removeIf(p->p.getId().equals(id));
        productService.deleteProduct(id);
    }
}
