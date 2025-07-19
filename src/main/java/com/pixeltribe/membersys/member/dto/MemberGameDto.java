package com.pixeltribe.membersys.member.dto;

import com.pixeltribe.membersys.member.model.Member;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Member}
 */

public class MemberGameDto implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 50)
    String memName;
    @NotNull
    @Size(max = 50)
    String memNickName;
    Integer point;

    public MemberGameDto() {
    }

    public MemberGameDto(Integer id, String memName, String memNickName, Integer point) {
        this.id = id;
        this.memName = memName;
        this.memNickName = memNickName;
        this.point = point;
    }

    public Integer getId() {
        return id;
    }

    public String getMemName() {
        return memName;
    }

    public String getMemNickName() {
        return memNickName;
    }

    public Integer getPoint() {
        return point;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setMemName(String memName) {
        this.memName = memName;
    }

    public void setMemNickName(String memNickName) {
        this.memNickName = memNickName;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }
}