package com.renthub.api.infra.presentation.user;
import com.renthub.api.core.domain.user.User;
import com.renthub.api.core.usecases.user.CreateUserUseCase;
import com.renthub.api.core.usecases.user.GetUserByIdUseCase;
import com.renthub.api.infra.dtos.user.CreateUserRequest;
import com.renthub.api.infra.dtos.user.UserResponse;
import com.renthub.api.infra.mapper.user.UserDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UserDtoMapper userDtoMapper;

    public UserController(CreateUserUseCase createUserUseCase,
                          UserDtoMapper userDtoMapper,
                          GetUserByIdUseCase getUserByIdUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.userDtoMapper = userDtoMapper;
        this.getUserByIdUseCase = getUserByIdUseCase;
    }

    @PostMapping()
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest userDto) {
        User create = createUserUseCase.execute(userDtoMapper.toDomain(userDto));
        UserResponse createUserDto = userDtoMapper.toDto(create);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createUserDto.id())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        User user = getUserByIdUseCase.execute(UUID.fromString(jwt.getSubject()));
        UserResponse userDto = userDtoMapper.toDto(user);

        return ResponseEntity.ok(userDto);

    }
}
