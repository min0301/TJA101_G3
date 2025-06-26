package com.pixeltribe.membersys.privatemessage.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.membersys.privatechatroom.model.PrivateChatroom;

@Getter
@Setter
@Entity
@Table(name = "private_message")
public class PrivateMessage {
    @Id
    @Column(name = "PRIVATE_MES_NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRIROOM_NO")
    private PrivateChatroom priroomNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SENDER_NO")
    private Member senderNo;

    @Size(max = 5000)
    @Column(name = "CONTENT", length = 5000)
    private String content;

    @Column(name = "SEND_AT")
    private Instant sendAt;

    @Column(name = "IS_DEL")
    private Boolean isDel;

}