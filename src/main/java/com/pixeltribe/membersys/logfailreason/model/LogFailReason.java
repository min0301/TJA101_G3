package com.pixeltribe.membersys.logfailreason.model;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pixeltribe.membersys.memberloginlog.model.MemberLoginLog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "LOG_FAIL_REASON")
public class LogFailReason {
    @Id
    @Column(name = "LOG_FAIL_REASON_NO", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "LOG_FAIL_TYPE", nullable = false, length = 50)
    private String logFailType;
    
    @JsonManagedReference
    @OneToMany(mappedBy = "logFailReasonNo")
    private Set<MemberLoginLog> memberLoginLogs = new LinkedHashSet<>();
    
    
}