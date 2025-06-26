package com.pixeltribe.forumsys.forumcategory.model;

import com.pixeltribe.forumsys.forum.model.Forum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "forum_category")
public class ForumCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CAT_NO", nullable = false)
    private Integer id;

    @Size(max = 30)
    @NotNull
    @Column(name = "CAT_NAME", nullable = false, length = 30)
    private String catName;

    @Size(max = 255)
    @Column(name = "CAT_DES")
    private String catDes;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CAT_DATE")
    private Instant catDate;

    @OneToMany(mappedBy = "catNo")
    private Set<Forum> forums = new LinkedHashSet<>();

}