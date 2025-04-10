package com.briscola4legenDs.briscola.Security.Auth;

import com.briscola4legenDs.briscola.Assets.RESTInfo;
import com.briscola4legenDs.briscola.User.User;
import com.briscola4legenDs.briscola.User.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    // TODO: capire come passare comunque il token
    @PostMapping(path = "register")
    public RedirectView registerUser(@ModelAttribute RegisterRequest request) {
        try {
            authenticationService.register(request);
        } catch (UserException e) {
            return new RedirectView("/register?" + e.getType());
        }
        return new RedirectView("/register?success");
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/info")
    public RESTInfo[] info() {
        return new RESTInfo[] {
                new RESTInfo(
                        "api/auth/register",
                        "POST",
                        "registerUser(RegisterRequest): redirectView",
                        "RegisterRequest: [ModelAttribute ~(form)~] -> { username: String, password: String, email: String }",
                        "Register the passed user"
                ),
                new RESTInfo(
                        "api/auth/authenticate",
                        "POST",
                        "registerUser(AuthenticationRequest): ResponseEntity<AuthenticationResponse>",
                        "AuthenticationRequest: [RequestBody ~(json)~] -> { username: String, password: String }",
                        "Authenticate the user the passed user and return a json with the jwt token"
                )
        };
    }
}
