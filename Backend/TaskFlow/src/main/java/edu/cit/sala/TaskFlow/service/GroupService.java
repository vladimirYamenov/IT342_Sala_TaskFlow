package edu.cit.sala.TaskFlow.service;

import edu.cit.sala.TaskFlow.dto.GroupResponse;
import edu.cit.sala.TaskFlow.dto.GroupRequest;
import edu.cit.sala.TaskFlow.entity.Group;
import edu.cit.sala.TaskFlow.entity.GroupMember;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.repository.GroupMemberRepository;
import edu.cit.sala.TaskFlow.repository.GroupRepository;
import edu.cit.sala.TaskFlow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupResponse createGroup(GroupRequest request, User creator) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name is required");
        }

        Group group = Group.builder()
                .name(request.getName())
                .build();
        groupRepository.save(group);

        GroupMember admin = GroupMember.builder()
                .group(group)
                .user(creator)
                .role("ADMIN")
                .build();
        groupMemberRepository.save(admin);

        return toResponse(group);
    }

    public List<GroupResponse> getUserGroups(Long userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(gm -> toResponse(gm.getGroup()))
                .toList();
    }

    public GroupResponse getGroupById(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this group");
        }

        return toResponse(group);
    }

    public GroupResponse addMemberByEmail(Long groupId, String email, Long requestingUserId) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requestingUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this group"));

        if (!"ADMIN".equals(requester.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can add members");
        }

        User targetUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found with email: " + email));

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of this group");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(targetUser)
                .role("MEMBER")
                .build();
        groupMemberRepository.save(member);

        return toResponse(group);
    }

    public GroupResponse addMember(Long groupId, Long targetUserId, Long requestingUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requestingUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this group"));

        if (!"ADMIN".equals(requester.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can add members");
        }

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(targetUser)
                .role("MEMBER")
                .build();
        groupMemberRepository.save(member);

        return toResponse(group);
    }

    public void removeMember(Long groupId, Long targetUserId, Long requestingUserId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        GroupMember requester = groupMemberRepository.findByGroupIdAndUserId(groupId, requestingUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this group"));

        if (!"ADMIN".equals(requester.getRole()) && !targetUserId.equals(requestingUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can remove other members");
        }

        GroupMember target = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found in group"));

        groupMemberRepository.delete(target);
    }

    public boolean isGroupMember(Long groupId, Long userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    private GroupResponse toResponse(Group group) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
        List<GroupResponse.MemberInfo> memberInfos = members.stream()
                .map(gm -> GroupResponse.MemberInfo.builder()
                        .userId(gm.getUser().getId())
                        .email(gm.getUser().getEmail())
                        .fullName(gm.getUser().getFullName())
                        .role(gm.getRole())
                        .build())
                .toList();

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .createdAt(group.getCreatedAt())
                .members(memberInfos)
                .build();
    }
}
