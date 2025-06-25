package com.pixeltribe.membersys.vo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "member_login_log")
public class MemberLoginLog {
    @Id
    @Column(name = "LOG_NO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEM_NO", nullable = false)
    private Member memNo;

    @NotNull
    @Column(name = "LOG_TIME", nullable = false)
    private Instant logTime;

    @Size(max = 50)
    @NotNull
    @Column(name = "LOG_IP", nullable = false, length = 50)
    private String logIp;

    @Size(max = 50)
    @NotNull
    @Column(name = "LOG_BROWSER", nullable = false, length = 50)
    private String logBrowser;

    @Size(max = 50)
    @NotNull
    @Column(name = "LOG_EQUIP", nullable = false, length = 50)
    private String logEquip;

    @NotNull
    @Column(name = "LOG_SUCCESS", nullable = false)
    private Boolean logSuccess = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOG_FAIL_REASON_NO")
    private LogFailReason logFailReasonNo;

}