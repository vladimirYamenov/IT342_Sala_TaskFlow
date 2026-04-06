package edu.cit.sala.TaskFlow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private List<MemberInfo> members;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberInfo {
        private Long userId;
        private String email;
        private String fullName;
        private String role;
    }
}
