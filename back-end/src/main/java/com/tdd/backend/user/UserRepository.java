package com.tdd.backend.user;

import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
	@Query("SELECT * FROM users u WHERE u.email = :email")
	Optional<User> findByEmail(@Param("email") String email);

	Long countByEmail(String email);
}
