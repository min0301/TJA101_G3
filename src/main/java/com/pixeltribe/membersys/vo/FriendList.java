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
@Table(name = "friend_list")
public class FriendList {
    @Id
    @Column(name = "FRILIST_NO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_1", nullable = false)
    private Member member1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_2", nullable = false)
    private Member member2;

    @Size(max = 20)
    @NotNull
    @Column(name = "FRILIST_STATUS", nullable = false, length = 20)
    private String frilistStatus;

    @Column(name = "SEND_TIME")
    private Instant sendTime;

    @Column(name = "RESPOND_TIME")
    private Instant respondTime;

}