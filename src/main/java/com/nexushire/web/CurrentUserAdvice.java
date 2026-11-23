package com.nexushire.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.nexushire.model.User;

import jakarta.servlet.http.HttpSession;

/** Puts the logged-in user into every page's model as "currentUser" (used by the navbar). */
@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }
}
