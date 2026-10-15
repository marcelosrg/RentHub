package com.renthub.api.infra.presentation.user;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.core.usecases.user.CreateUserUseCase;
import com.renthub.api.infra.dtos.user.CreateUserRequest;
import com.renthub.api.infra.dtos.user.CreateUserRequest;
import com.renthub.api.infra.dtos.user.UserResponse;
import com.renthub.api.infra.mapper.user.UserDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final UserDtoMapper userDtoMapper;

    public UserController(CreateUserUseCase createUserUseCase,
                          UserDtoMapper userDtoMapper) {
        this.createUserUseCase = createUserUseCase;
        this.userDtoMapper = userDtoMapper;
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
}
