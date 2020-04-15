package com.hk.HKConnector.Controller;

import lombok.extern.slf4j.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;

import java.security.Principal;
import java.util.logging.*;

@Slf4j
@Controller
@RequestMapping({"/", "/loginPage"})
public class AuthorizationController {

    @RequestMapping(value ="" , method = RequestMethod.GET)
    public ModelAndView loginPage(
            @RequestParam(value = "error",required = false) String error,
            @RequestParam(value = "logout",	required = false) String logout,
            Principal principal
            ) {

        ModelAndView model = new ModelAndView();
        if(principal == null ) {
            if (error != null) {
                model.addObject("error", "Invalid Credentials provided.");
            }

            if (logout != null) {
                model.addObject("message", "Logged out successfully.");
            }

            model.setViewName("login");
        }else{
            model.setViewName("dashboard");
        }
        return model;
    }

}
