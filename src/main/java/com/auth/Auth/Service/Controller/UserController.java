package com.auth.Auth.Service.Controller;

import com.auth.Auth.Service.DTO.Auth.UserInfoDTO;
import com.auth.Auth.Service.DTO.UpdateEmailRequest;
import com.auth.Auth.Service.DTO.UpdateUsernameRequest;
import com.auth.Auth.Service.DTO.UserRequest;
import com.auth.Auth.Service.DTO.UserResponse;
import com.auth.Auth.Service.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public void getUser(){

    }

    @PutMapping("/me")
    public UserResponse updateUser(@RequestBody UserRequest request) {
        return userService.updateUser(request);
    }

    @PatchMapping("/me/email")
    public ResponseEntity<String> updateEmail(@RequestBody UpdateEmailRequest request) {
        UserInfoDTO response = userService.updateEmail(request);
        return new ResponseEntity<>("Hi "+response.getName()+" \n\n your email is updated and a Email verification link is sent to you email "+response.getEmail(), HttpStatus.OK);

    }

    @PatchMapping("/me/username")
    public ResponseEntity<String> updateUsername(@RequestBody UpdateUsernameRequest request) {
        UserInfoDTO response = userService.updateUsername(request);
        return ResponseEntity.ok("username updated");

    }

    @DeleteMapping("/me")
    public String deleteUser() {
        userService.deleteUser();
        return "User deleted successfully";
    }

}
