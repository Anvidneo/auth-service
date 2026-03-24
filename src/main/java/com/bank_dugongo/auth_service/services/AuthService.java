package com.bank_dugongo.auth_service.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bank_dugongo.auth_service.dto.AuthResponseDTO;
import com.bank_dugongo.auth_service.dto.LoginRequestDTO;
import com.bank_dugongo.auth_service.dto.PatchUserRequestDTO;
import com.bank_dugongo.auth_service.dto.RegisterRequestDTO;
import com.bank_dugongo.auth_service.dto.UserInfoDTO;
import com.bank_dugongo.auth_service.exceptions.InactiveUserException;
import com.bank_dugongo.auth_service.exceptions.InvalidCredentialsException;
import com.bank_dugongo.auth_service.exceptions.UserAlreadyExistsException;
import com.bank_dugongo.auth_service.exceptions.UserNotFoundException;
import com.bank_dugongo.auth_service.models.Customer;
import com.bank_dugongo.auth_service.models.User;
import com.bank_dugongo.auth_service.repositories.CustomerRepository;
import com.bank_dugongo.auth_service.repositories.UserRepository;
import com.bank_dugongo.auth_service.security.JwtUtil;


@Service
public class AuthService {
    // Mensajes de error como constantes
    private static final String MSG_USERNAME_EXISTS = "Username already exists";
    private static final String MSG_EMAIL_EXISTS = "Email already exists";
    private static final String MSG_DOCUMENT_EXISTS = "Document number already exists";
    private static final String MSG_INVALID_CREDENTIALS = "Invalid credentials";
    private static final String MSG_USER_INACTIVE = "User account is inactive";
    private static final String MSG_USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(
        UserRepository userRepository,
        CustomerRepository customerRepository,
        JwtUtil jwtUtil,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // Registro de usuario y creación de customer
    public AuthResponseDTO register(RegisterRequestDTO request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new UserAlreadyExistsException(MSG_USERNAME_EXISTS);
        }

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(MSG_EMAIL_EXISTS);
        }

        if (customerRepository.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new UserAlreadyExistsException(MSG_DOCUMENT_EXISTS);
        }

        // Crear el Customer
        Customer customer = new Customer();
        customer.setAge(request.getAge());
        customer.setName(request.getName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setDocumentType(request.getDocumentType());
        customer.setDocumentNumber(request.getDocumentNumber());
        customer.setPhone(request.getPhone());
        customer.setRiskProfile(request.getRiskProfile());
        customer.setCreditScore(0);
        customer.setIncomeFrequency("PENDING");
        customer.setMonthlyIncome(BigDecimal.ZERO);

        Customer savedCustomer = customerRepository.save(customer);

        // Crear el User asociado al Customer
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(hashedPassword);
        user.setCustomerId(savedCustomer.getId());
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // Generar el token JWT
        String token = jwtUtil.generateToken(
            savedUser.getUsername(),
            savedUser.getId(),
            savedUser.getCustomerId()
        );

        return new AuthResponseDTO(
            token,
            savedUser.getUsername(),
            jwtUtil.getExpirationTime()
        );
    }

    // Inicio de sesión
    public AuthResponseDTO login(LoginRequestDTO request) {
        // Verificar que el usuario existe
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new InvalidCredentialsException(MSG_INVALID_CREDENTIALS));
        
        // Verificar que la cuenta esté activa y que la contraseña sea correcta
        if(!user.getIsActive()){
            throw new InactiveUserException(MSG_USER_INACTIVE);
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new InvalidCredentialsException(MSG_INVALID_CREDENTIALS);
        }

        // Actualizar la fecha del último inicio de sesión
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generar el token JWT
        String token = jwtUtil.generateToken(
            user.getUsername(),
            user.getId(),
            user.getCustomerId()
        );

        return new AuthResponseDTO(
            token,
            user.getUsername(),
            jwtUtil.getExpirationTime()
        );
    }

    // Validación de token
    public boolean validateToken(String token) {
        String username = jwtUtil.extractUsername(token);

        return jwtUtil.validateToken(token, username);
    }

    // Obtener información del usuario a partir del token
    public UserInfoDTO getUserInfo(String token) {
        // Buscar el usuario en la base de datos
        Integer userId = jwtUtil.extractUserId(token);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(MSG_USER_NOT_FOUND));

        // Mapear la información del usuario a un DTO
        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setCustomerId(user.getCustomerId());
        userInfo.setIsActive(user.getIsActive());
        userInfo.setLastLogin(user.getLastLogin());
        userInfo.setCreatedAt(user.getCreatedAt());

        return userInfo;
    }

    // Método para actualizar la información del usuario
    public UserInfoDTO patchUser(String token, PatchUserRequestDTO request) {
        // Buscar el usuario activo en la base de datos
        Integer userId = jwtUtil.extractUserId(token);

        User user = userRepository.findByIdAndIsActiveTrue(userId)
            .orElseThrow(() -> new UserNotFoundException(MSG_USER_NOT_FOUND));
        
        // Validar y actualizar los campos recibidos en el request
        if (request.getUsername() != null) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new UserAlreadyExistsException(MSG_USERNAME_EXISTS);
            }

            user.setUsername(request.getUsername());
        }

        if (request.getPassword() != null) {
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            user.setPasswordHash(hashedPassword);
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        User updateUser = userRepository.save(user);

        // Mapear la información del usuario a un DTO
        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(updateUser.getId());
        userInfo.setUsername(updateUser.getUsername());
        userInfo.setCustomerId(updateUser.getCustomerId());
        userInfo.setIsActive(updateUser.getIsActive());
        userInfo.setLastLogin(updateUser.getLastLogin());
        userInfo.setCreatedAt(updateUser.getCreatedAt());

        return userInfo;
    }

    public void softDeleteUser(String token){
        // Buscar el usuario activo en la base de datos
        Integer userId = jwtUtil.extractUserId(token);

        User user = userRepository.findByIdAndIsActiveTrue(userId)
            .orElseThrow(() -> new UserNotFoundException(MSG_USER_NOT_FOUND));

        user.setIsActive(false);
        userRepository.save(user);
    }
}
