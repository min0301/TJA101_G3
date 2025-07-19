package com.pixeltribe.membersys.member.model;

import com.pixeltribe.membersys.member.dto.MemberGameDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemRepository extends JpaRepository<Member, Integer> {

	Member findByMemAccount(String memAccount);

	Member findByMemPassword(String memPassword);

	Member findByMemEmail(String memEmail);

	boolean existsByMemEmail(String memEmail);

	@Query("SELECT m FROM Member m WHERE " + "CAST(m.id AS string) LIKE %:keyword% OR "
			+ "LOWER(m.memNickName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(m.memAccount) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(m.memEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(m.memAddr) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	Page<Member> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

	@Query("""
            SELECT new com.pixeltribe.membersys.member.dto.MemberGameDto(
            			m.id, m.memName, m.memNickName, m.point)
            			FROM Member m
                        ORDER BY m.point DESC""")
    List<MemberGameDto> findMemberGameScores();
}
