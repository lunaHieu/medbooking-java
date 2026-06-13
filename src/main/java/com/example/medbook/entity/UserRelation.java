package com.example.medbook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_relations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"UserID", "RelativeUserID"})
})
@Getter
@Setter
public class UserRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RelativeUserID", nullable = false)
    private User relativeUser;

    @Column(name = "RelationType")
    private String relationType;
}
