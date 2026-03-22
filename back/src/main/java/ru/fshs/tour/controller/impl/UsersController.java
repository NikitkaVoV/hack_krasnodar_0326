package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.fshs.tour.controller.common.AbstractCrudController;
import ru.fshs.tour.domain.user.User;
import ru.fshs.tour.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
public class UsersController extends AbstractCrudController<User, UserRepository> {

    public UsersController(UserRepository repository) {
        super(repository);
    }

    @Override
    @GetMapping
    public List<User> findAll() {
        return repository.findAll();
    }

    @GetMapping(params = "login")
    public User findByLogin(@RequestParam String login) {
        return repository.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
