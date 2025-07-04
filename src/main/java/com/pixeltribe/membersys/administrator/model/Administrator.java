package com.pixeltribe.membersys.administrator.model;

import com.pixeltribe.newssys.news.model.News;
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
@Table(name = "ADMINISTRATOR")
public class Administrator {
    @Id
    @Column(name = "ADM_NO", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "ADM_ACCOUNT", nullable = false, length = 50)
    private String admAccount;

    @Size(max = 50)
    @NotNull
    @Column(name = "ADM_NAME", nullable = false, length = 50)
    private String admName;

    @Size(max = 50)
    @NotNull
    @Column(name = "ADM_PASSWORD", nullable = false, length = 50)
    private String admPassword;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATE_TIME")
    private Instant createTime;

    @Column(name = "ADM_PROFILE")
    private byte[] admProfile;

    @Size(max = 20)
    @ColumnDefault("'ROLE_ADMIN'")
    @Column(name = "ROLE", length = 20)
    private String role;

    @OneToMany(mappedBy = "adminNo")
    private Set<News> news = new LinkedHashSet<>();

}