package com.pixeltribe.membersys.privatemessage.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.membersys.privatechatroom.model.PrivateChatroom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "PRIVATE_MESSAGE")
public class PrivateMessage {
	
    @Id
    @Column(name = "PRIVATE_MES_NO", nullable = false)
    private Integer id;
    
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRIROOM_NO")
    private PrivateChatroom priroomNo;
    
    @JsonBackReference
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