package com.pixeltribe.membersys.vo;

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
@Table(name = "log_fail_reason")
public class LogFailReason {
    @Id
    @Column(name = "LOG_FAIL_REASON_NO", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "LOG_FAIL_TYPE", nullable = false, length = 50)
    private String logFailType;

    @OneToMany(mappedBy = "logFailReasonNo")
    private Set<MemberLoginLog> memberLoginLogs = new LinkedHashSet<>();

}