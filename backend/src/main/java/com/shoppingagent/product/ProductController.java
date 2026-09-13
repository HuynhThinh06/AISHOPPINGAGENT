package com.shoppingagent.product;

import com.shoppingagent.product.dto.ProductDetailDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Thông tin chi tiết sản phẩm")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết sản phẩm theo ID")
    public ResponseEntity<ProductDetailDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }
}
