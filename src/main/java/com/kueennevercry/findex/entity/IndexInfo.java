package com.kueennevercry.findex.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Table(name = "index_info")
public class IndexInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder
    public IndexInfo(Long id) {
        this.id = id;
    }
}
