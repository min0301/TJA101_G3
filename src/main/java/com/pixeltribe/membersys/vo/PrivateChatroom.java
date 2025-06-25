package com.pixeltribe.membersys.vo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "private_chatroom")
public class PrivateChatroom {
    @Id
    @Column(name = "PRIROOM_NO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER1", nullable = false)
    private Member member1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER2", nullable = false)
    private Member member2;

    @Column(name = "CREATE_TIME")
    private Instant createTime;

    @Column(name = "PRI_STATUS")
    private Character priStatus;

    @OneToMany(mappedBy = "priroomNo")
    private Set<PrivateMessage> privateMessages = new LinkedHashSet<>();

}