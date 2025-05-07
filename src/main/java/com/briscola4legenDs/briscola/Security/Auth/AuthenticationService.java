package com.briscola4legenDs.briscola.Security.Auth;

import com.briscola4legenDs.briscola.Security.Config.JwtService;
import com.briscola4legenDs.briscola.User.Role;
import com.briscola4legenDs.briscola.User.User;
import com.briscola4legenDs.briscola.User.UserException;
import com.briscola4legenDs.briscola.User.REST.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        Optional<User> userByUsername = userRepository.findByUsername(request.getUsername());
        Optional<User> userByEmail = userRepository.findByEmail(request.getEmail());

        if (userByUsername.isPresent())
            throw new UserException("Username already in use", UserException.Type.UsernameAlreadyTaken);
        if (userByEmail.isPresent())
            throw new UserException("Email address already in use", UserException.Type.EmailAlreadyTaken);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
//                .password(request.getPassword())
                .imageUrl(request.getImgUrl())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username : " + request.getUsername() + " not found"));

        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
