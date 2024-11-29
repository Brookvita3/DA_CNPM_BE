package L03.CNPM.Music.services.users;

import L03.CNPM.Music.DTOS.ResetPasswordDTO;
import L03.CNPM.Music.DTOS.UserDTO;
import L03.CNPM.Music.DTOS.UserLoginDTO;
import L03.CNPM.Music.components.JwtTokenUtils;
import L03.CNPM.Music.components.LocalizationUtils;
import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.exceptions.ExpiredTokenException;
import L03.CNPM.Music.exceptions.PermissionDenyException;
import L03.CNPM.Music.exceptions.UploadCloudinaryException;
import L03.CNPM.Music.models.Role;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.repositories.UserRepository;
import L03.CNPM.Music.repositories.RoleRepository;
import L03.CNPM.Music.utils.DateUtils;
import L03.CNPM.Music.utils.MessageKeys;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements IUserService {
    private final LocalizationUtils localizationUtils;
    private final JwtTokenUtils jwtTokenUtil;

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final Cloudinary cloudinary;
    private final DateUtils dateUtils;

    @Override
    public Page<User> findAll(String keyword, Pageable pageable) {
        return userRepository.findAll(keyword, pageable);
    }

    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws Exception {
        if (!userDTO.getEmail().isBlank() && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DataIntegrityViolationException(
                    localizationUtils.getLocalizedMessage(MessageKeys.EMAIL_EXISTED));
        }

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException(
                        localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS)));
        if (role.getName().equalsIgnoreCase(Role.ADMIN)) {
            throw new PermissionDenyException("Registering admin accounts is not allowed");
        }

        String password = userDTO.getPassword();
        String encodedPassword = passwordEncoder.encode(password);

        // convert from userDTO => user
        User newUser = User.builder()
                .email(userDTO.getEmail())
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(encodedPassword)
                .country(userDTO.getCountry())
                .dateOfBirth(userDTO.getDateOfBirth())
                .createdAt(dateUtils.getCurrentDate())
                .updatedAt(dateUtils.getCurrentDate())
                .isActive(true)
                .role(role)
                .build();

        return userRepository.save(newUser);
    }

    @Override
    public String loginGetToken(UserLoginDTO userLoginDTO) throws Exception {
        Optional<User> optionalUser = Optional.empty();
        String subject = null;

        if (userLoginDTO.getEmail() != null) {
            optionalUser = userRepository.findByEmail(userLoginDTO.getEmail());
            subject = userLoginDTO.getEmail();
        }
        if (optionalUser.isEmpty() && userLoginDTO.getUsername() != null) {
            optionalUser = userRepository.findByUsername(userLoginDTO.getUsername());
            subject = userLoginDTO.getUsername();
        }
        // If user is not found, throw an exception
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.WRONG_EMAIL));
        }

        User existingUser = optionalUser.get();
        // Check if the user account is active
        if (!existingUser.getIsActive()) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
        }

        // Create authentication token using the found subject and granted authorities
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                subject,
                userLoginDTO.isPasswordBlank() ? "" : userLoginDTO.getPassword(),
                existingUser.getAuthorities());

        // authenticate with Java Spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }

    @Override
    public User getUserDetailsByExtractingToken(String token) throws Exception {
        if (jwtTokenUtil.isTokenExpired(token)) {
            throw new ExpiredTokenException(localizationUtils.getLocalizedMessage(MessageKeys.TOKEN_EXPIRED));
        }

        String subject = jwtTokenUtil.getSubject(token);
        Optional<User> user;
        user = userRepository.findByEmail(subject);
        if (user.isEmpty()) {
            user = userRepository.findByUsername(subject);
        }
        return user.orElseThrow(() -> new Exception(localizationUtils.getLocalizedMessage(MessageKeys.USER_EXISTED)));
    }

    @Override
    public User resetPassword(Long userId, ResetPasswordDTO resetPasswordDTO) throws Exception {
        // Find the existing user by userId
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        // Update the password if it is provided in the DTO
        if (resetPasswordDTO.getPassword() != null
                && !resetPasswordDTO.getPassword().isEmpty()) {
            if (!resetPasswordDTO.getPassword().equals(resetPasswordDTO.getRetypePassword())) {
                throw new DataNotFoundException("Password and retype password not the same");
            }
            String newPassword = resetPasswordDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(newPassword);
            existingUser.setPassword(encodedPassword);
        }
        // existingUser.setRole(updatedRole);
        // Save the updated user
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void blockOrEnable(Long userId, Boolean active) throws Exception {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        existingUser.setIsActive(active);
        userRepository.save(existingUser);
    }

    @Override
    public String updateUserImageProfile(Long userId, MultipartFile file) throws Exception {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_EXISTED));
        }

        User existingUser = optionalUser.get();
        String oldFileId = existingUser.getPublicImageId();

        @SuppressWarnings("unchecked")
        Map<String, Object> response = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                "resource_type", "image",
                "folder", "users"));
        if (response.isEmpty())
            throw new UploadCloudinaryException(
                    localizationUtils.getLocalizedMessage(MessageKeys.CLOUDINARY_UPLOAD_FAIL));

        // Cập nhật ảnh mới và public_id cho người dùng
        existingUser.setProfileImage((String) response.get("secure_url"));
        existingUser.setPublicImageId((String) response.get("public_id"));
        userRepository.save(existingUser);

        if (oldFileId != null && !oldFileId.isEmpty())
            cloudinary.uploader().destroy(oldFileId, ObjectUtils.asMap(
                    "resource_type", "image"));

        return (String) response.get("secure_url");
    }
}
