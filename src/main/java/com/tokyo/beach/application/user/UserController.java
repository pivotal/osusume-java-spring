package com.tokyo.beach.application.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.tokyo.beach.application.logon.LogonCredentials;
import com.tokyo.beach.application.session.TokenGenerator;
import com.tokyo.beach.application.token.UserSession;

import java.util.Optional;

@RestController
public class UserController {

    private UserRepository userRepository;
    private TokenGenerator tokenGenerator;

    @Autowired
    public UserController(
            UserRepository userRepository,
            TokenGenerator tokenGenerator
    ) {
        this.userRepository = userRepository;
        this.tokenGenerator = tokenGenerator;
    }

    @RequestMapping(value = "/auth/session", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public UserSession login(@RequestBody LogonCredentials credentials) {
        Optional<UserSession> userSessionOptional = userRepository.logon(tokenGenerator, credentials.getEmail(), credentials.getPassword());

        if (userSessionOptional.isPresent()) {
            return userSessionOptional.get();
        }

        return new UserSession(tokenGenerator, "thisshouldbe@anerror.com");
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public DatabaseUser registerUser(@RequestBody LogonCredentials credentials) {
        return userRepository.create(credentials.getEmail(), credentials.getPassword());
    }

}
