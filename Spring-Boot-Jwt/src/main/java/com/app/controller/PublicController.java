package com.app.controller;

import com.app.dto.UserDTO;
import com.app.entity.User;
import com.app.service.UserDetailsServiceImpl;
import com.app.service.UserService;
import com.app.utilis.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/public")
public class PublicController {

    private static final Logger log = LoggerFactory.getLogger(PublicController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public void signup(@RequestBody UserDTO user){
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setUserName(user.getUserName());
        newUser.setPassword(user.getPassword());
        userService.saveNewUser(newUser);

    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user){
        try {
            authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(user.getUserName(),user.getPassword()));
            userDetailsService.loadUserByUsername(user.getUserName());
            String jwt=jwtUtil.generateToken(user.getUserName());
            return new  ResponseEntity<>(jwt, HttpStatus.OK);
        }catch (Exception e){
            log.error("Exception Occur while createAuthenticationToken");
            return new ResponseEntity<>("Incorrect username or password",HttpStatus.BAD_REQUEST);

        }
    }
}
