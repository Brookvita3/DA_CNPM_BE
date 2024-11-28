package L03.CNPM.Music.services.users;

import L03.CNPM.Music.DTOS.ResetPasswordDTO;
import L03.CNPM.Music.DTOS.UserDTO;
import L03.CNPM.Music.DTOS.UserLoginDTO;
import L03.CNPM.Music.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    Page<User> findAll(String keyword, Pageable pageable);
    User createUser(UserDTO userDTO) throws Exception;
    String loginGetToken(UserLoginDTO userLoginDTO) throws Exception;
    User getUserDetailsByExtractingToken(String token) throws Exception;
    User resetPassword(Long userId, ResetPasswordDTO resetPasswordDTO) throws Exception;
    void blockOrEnable(Long userId, Boolean active) throws Exception;
    String updateUserImageProfile(Long userId, MultipartFile file) throws Exception;
}