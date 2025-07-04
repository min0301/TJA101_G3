package com.pixeltribe.membersys.privatechatroom.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.membersys.privatemessage.model.PrivateMessage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "PRIVATE_CHATROOM")
public class PrivateChatroom {
    @Id
    @Column(name = "PRIROOM_NO", nullable = false)
    private Integer id;
    
    @JsonBackReference(value = "member1-privatechatroom")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER1", nullable = false)
    private Member member1;

    @JsonBackReference(value = "member2-privatechatroom")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER2", nullable = false)
    private Member member2;

    @Column(name = "CREATE_TIME")
    private Instant createTime;

    @Column(name = "PRI_STATUS")
    private Character priStatus;
    
    @JsonManagedReference
    @OneToMany(mappedBy = "priroomNo")
    private Set<PrivateMessage> privateMessages = new LinkedHashSet<>();

}