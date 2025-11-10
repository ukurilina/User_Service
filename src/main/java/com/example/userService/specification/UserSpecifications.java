package com.example.userService.specification;

import com.example.userService.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) ->
                firstName == null ? null : criteriaBuilder.like(root.get("name"), "%" + firstName + "%");
    }

    public static Specification<User> hasSurname(String surname) {
        return (root, query, criteriaBuilder) ->
                surname == null ? null : criteriaBuilder.like(root.get("surname"), "%" + surname + "%");
    }
}