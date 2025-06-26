package com.pixeltribe.membersys.administrator.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "administrator")
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

}