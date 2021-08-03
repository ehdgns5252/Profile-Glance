package com.profileglance.db.repository;

import com.profileglance.db.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {

    List<UserLike> findAllByCompany_CompanyId(String companyId);

    Optional<UserLike> findByUser_UserEmailAndCompany_CompanyId(String userEmail, String companyId);

    Long countByUser_UserEmail(String userEmail);

}
