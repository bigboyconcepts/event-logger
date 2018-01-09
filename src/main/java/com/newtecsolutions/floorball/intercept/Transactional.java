package com.newtecsolutions.floorball.intercept;

import com.newtecsolutions.floorball.model.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Michal Gajdos
 */
@Target({ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Transactional
{
    Permission[] requiredPermissions() default {};
    boolean requiresAuth() default true;
}