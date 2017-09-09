package com.webstar.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.webstar.models.UserSubmissions;

@Repository
public interface SubmissionRepository extends JpaRepository<UserSubmissions, Long>
{
    @Query(
        value = "select * from User_Submissions u where u.is_active_post=1 order by u.submittied_date desc limit ?1 offset ?2",
        nativeQuery = true )
    Optional<List<UserSubmissions>> fetchRecentPostsDesc(int limit, int offset);
  
    @Query(
        value = "select * from User_Submissions u where u.is_active_post=1 and u.category = ?1 order by u.submittied_date desc limit ?2 offset ?3",
        nativeQuery = true )
    Optional<List<UserSubmissions>> findByCategoryOrderDesc(String category, int limit, int offset);
    
    @Modifying
    @Transactional
    @Query(value= "update User_Submissions u set u.total_comments = ?1  where u.id = ?2",nativeQuery = true )
    int updateTotalCommentsCount(int totalComments, Long postid);
}
