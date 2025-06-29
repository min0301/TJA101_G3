package com.pixeltribe.membersys.friendlist.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.pixeltribe.membersys.member.model.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "friend_list")
public class FriendList {
    @Id
    @Column(name = "FRILIST_NO", nullable = false)
    private Integer id;
    
    @JsonBackReference(value = "member1-friendlist")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEMBER_1", nullable = false)
    private Member member1;
    
    @JsonBackReference(value = "member2-friendlist")
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