package io.github.shazaanashraff.sample.auth;

import io.github.shazaanashraff.sample.user.User;
import io.github.shazaanashraff.sample.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

  private final UserRepository userRepo;

  public AuthController(UserRepository userRepo) {
    this.userRepo = userRepo;
  }

  @PostMapping("/login")
  @Operation(summary = "Authenticate and receive a token")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    return userRepo.findByUsername(req.username())
        .map(user -> {
          String token = Base64.getEncoder().encodeToString(
              ("sample:" + user.getUsername() + ":" + user.getRole()).getBytes());
          return ResponseEntity.ok(Map.of("token", token, "role", user.getRole()));
        })
        .orElse(ResponseEntity.status(401).build());
  }

  public record LoginRequest(String username, String password) {}
}
