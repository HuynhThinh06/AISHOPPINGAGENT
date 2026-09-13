package com.shoppingagent.product;

import com.shoppingagent.product.dto.ProductDetailDTO;
import com.shoppingagent.shared.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDetailDTO getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toDetailDTO(p);
    }

    private ProductDetailDTO toDetailDTO(Product p) {
        return ProductDetailDTO.builder()
                .id(p.getId())
                .categoryCode(p.getCategory().getCode())
                .sku(p.getSku())
                .name(p.getName())
                .brand(p.getBrand())
                .price(p.getPrice())
                .productUrl(p.getProductUrl())
                .avgRating(p.getAvgRating())
                .reviewCount(p.getReviewCount())
                .specs(p.getSpecs())
                .build();
    }
}
