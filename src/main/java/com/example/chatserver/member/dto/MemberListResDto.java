package com.example.chatserver.member.dto;

import com.example.chatserver.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberListResDto {
    private Long id;
    private String name;
    private String email;

    public static MemberListResDto fromEntity(Member member) {
        return new MemberListResDto(member.getId(), member.getName(), member.getEmail());
    }
}
