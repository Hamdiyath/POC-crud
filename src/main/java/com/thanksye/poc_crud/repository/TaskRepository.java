package com.thanksye.poc_crud.repository;
import com.thanksye.poc_crud.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {





}
