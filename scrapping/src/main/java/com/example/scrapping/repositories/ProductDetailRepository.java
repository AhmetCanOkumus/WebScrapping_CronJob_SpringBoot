package com.example.scrapping.repositories;


import com.example.scrapping.models.ProductDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailRepository extends MongoRepository<ProductDetail,String> {
}
