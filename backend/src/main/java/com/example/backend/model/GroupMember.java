package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "group_members")
@IdClass(GroupMemberId.class) // เพิ่ม IdClass
@Getter
@Setter
public class GroupMember implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}