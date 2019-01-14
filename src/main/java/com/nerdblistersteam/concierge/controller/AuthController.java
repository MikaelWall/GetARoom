package com.nerdblistersteam.concierge.controller;

import com.nerdblistersteam.concierge.domain.Role;
import com.nerdblistersteam.concierge.domain.User;
import com.nerdblistersteam.concierge.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

@Controller
public class AuthController {

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    private final Role adminRole = new Role("ROLE_ADMIN");
    private final Role userRole = new Role("ROLE_USER");

    @GetMapping("login")
    public String login() {
        return "auth/login";
    }
    @GetMapping("/register")
    public String registrering() {
        return "/auth/registrering";
    }

    @PostMapping("/register")
    public String registerNewUser(@Valid User user, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // show validation errors
            logger.info("Validation errors were found while registering a new user.");
            model.addAttribute("user", user);
            model.addAttribute("validationErrors", bindingResult.getAllErrors());
            System.out.println(bindingResult.getAllErrors());
            return "auth/registrering";
        } else {
            // register new user
            User newUser = userService.register(user);
            redirectAttributes
                    .addAttribute("id", newUser.getId())
                    .addFlashAttribute("success", true);
            return "redirect:/register";
        }
    }

    @PostMapping("/login")
    public String postLogin(@RequestParam String username,
                            @RequestParam String password) throws  Exception {

//        //Nedan är endast test för att kunna se att koppling finns till databasen
//        Connection conn = DriverManager.getConnection("jdbc:h2:mem:concierge", "sa", "");
//        System.out.println("Connected to test db");
//        Statement statement = conn.createStatement();
//        statement.executeUpdate("INSERT INTO user VALUES (2, null, 'woop@woop.se', TRUE, '" + username +"', 'woop', '" + password + "')");
//        ResultSet resultSet = statement.executeQuery("SELECT * FROM USER");
//
//        while(resultSet.next()) {
//            System.out.println(resultSet.getInt("id"));
//            System.out.println(resultSet.getString("email"));
//            System.out.println(resultSet.getString("password"));
//
//        }
//        conn.close();

        System.out.println("Du har loggat in med: " + username + password);
        return "/auth/login";
    }

    @GetMapping("/activate/{email}/{activationCode}")
    public String activateAdmin(@PathVariable String email, @PathVariable String activationCode) {
        Optional<User> user = userService.findByEmailAndActivationCode(email, activationCode);
        if (user.isPresent()) {
            User newUser = user.get();
            newUser.setEnabled(true);
            newUser.addRole(adminRole);
            newUser.setConfirmPassword(newUser.getPassword());
            userService.save(newUser);
            return "auth/activated";
        }
        return "redirect:/";
    }

    @GetMapping("/activate/{addedBy}/{email}/{activationCode}")
    public String activateUser(Model model, @PathVariable String email, @PathVariable String addedBy, @PathVariable String activationCode) {
        Optional<User> user = userService.findByEmailAndActivationCode(email, activationCode);
        if (user.isPresent()) {
            User newUser = user.get();
            model.addAttribute("email", email);
            model.addAttribute("firstName", newUser.getFirstName());
            model.addAttribute("lastName", newUser.getLastName());
            newUser.setEnabled(true);
            newUser.setAddedByFullName(addedBy);
            newUser.addRole(userRole);
//            newUser.setConfirmPassword(newUser.getPassword());
            userService.save(newUser);
            return "auth/register";
        }
        return "redirect:/";
    }
}