package com.teamtask.repository;

import com.teamtask.entity.Project;
import com.teamtask.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwner(User owner);

    @Query("SELECT p FROM Project p WHERE p.owner = :user OR :user MEMBER OF p.members")
    List<Project> findByOwnerOrMember(@Param("user") User user);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m = :user")
    List<Project> findByMember(@Param("user") User user);
}
