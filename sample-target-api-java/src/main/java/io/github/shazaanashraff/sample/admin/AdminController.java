package io.github.shazaanashraff.sample.admin;

import io.github.shazaanashraff.sample.user.User;
import io.github.shazaanashraff.sample.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin")
public class AdminController {

  private final UserRepository userRepo;

  public AdminController(UserRepository userRepo) {
    this.userRepo = userRepo;
  }

  // BUG B8: no @PreAuthorize or security check — any request returns user list
  @GetMapping("/users")
  @Operation(summary = "List all users (should require ADMIN role)")
  public List<User> listUsers() {
    return userRepo.findAll();
  }
}
