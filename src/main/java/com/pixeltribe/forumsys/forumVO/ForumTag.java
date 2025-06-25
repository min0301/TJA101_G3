package com.pixeltribe.forumsys.forumVO;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "forum_tag")
public class ForumTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FTAG_NO", nullable = false)
    private Integer id;

    @Size(max = 30)
    @NotNull
    @Column(name = "FTAG_NAME", nullable = false, length = 30)
    private String ftagName;

    @Size(max = 255)
    @Column(name = "FTAG_SPEC")
    private String ftagSpec;

    @OneToMany(mappedBy = "ftagNo")
    private Set<ForumPost> forumPosts = new LinkedHashSet<>();

}