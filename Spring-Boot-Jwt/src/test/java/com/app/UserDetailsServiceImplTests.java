package com.app;

import com.app.repository.UserRepository;
import com.app.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.app.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class UserDetailsServiceImplTests {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void loadUserByUsernameTest(){

        User newUser = new User();
        newUser.setUserName("ram");
        newUser.setEmail("ram@gmail.com");
        newUser.setPassword("ibibbibub");
        newUser.setRoles(new ArrayList<>());
        when(userRepository.findByUserName(ArgumentMatchers.anyString())).thenReturn(newUser);
        UserDetails user = userDetailsService.loadUserByUsername("ram");
        assertNotNull(user);


    }

}
