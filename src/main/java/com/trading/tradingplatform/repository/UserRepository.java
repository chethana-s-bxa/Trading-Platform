package com.trading.tradingplatform.repository;

import com.trading.tradingplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    /**
     *  Spring Data JPA automatically generates a query to check
     *  whether a user with the given username already exists.
     * @param username name of user used to search
     * @return boolean
     */
    public boolean existsByUsername(String username);

    /**
     * Spring Data JPA automatically generates a query to check
     * whether a user with the given email already exists.
     * @param email email address used to search
     * @return boolean
     */
    public boolean existsByEmail(String email);

    /**
     * Fetches a user from the database using the provided email address.
     *
     * @param email the email address used to search for the user
     * @return Optional containing the User if found, otherwise an empty Optional
     */
    public Optional<User> findByEmail(String email);
}
