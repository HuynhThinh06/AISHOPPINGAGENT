package com.shoppingagent.shared.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ranking_weights")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RankingWeight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** price / rating / spec_match */
    @Column(nullable = false, length = 50)
    private String criteria;

    /** Giá trị 0.00 – 1.00 */
    @Column(nullable = false, columnDefinition = "numeric(4,2)")
    private Double weight;
}
