package com.app.service;
import com.app.entity.User;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;


import java.util.List;
import java.util.stream.Stream;

public class UserArgumentsProvider implements ArgumentsProvider{
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        User user1=new User();
        user1.setUserName("ok");
        user1.setPassword("ok@1234");
        User user2=new User();
        user2.setUserName("abc");
        user2.setPassword("");
        return Stream.of(
                Arguments.of(new User("john", "john@example.com", "password123", List.of("USER"))),
                Arguments.of(new User("jane", "jane@example.com", "password456", List.of("ADMIN")))
        );
    }
}
