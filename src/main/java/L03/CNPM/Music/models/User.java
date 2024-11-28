package L03.CNPM.Music.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String profileImage;

    private String publicImageId;

    private String country;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(nullable = false)
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_role_id"))
    private Role role;

    @Column(name = "created_at", updatable = false)
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));

        return authorityList;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        if (email != null && !email.isEmpty())
            return email;
        else if (username != null && !username.isEmpty())
            return username;
        return "";
    }

    public String GetUserNameInWeb() {
        return this.username;
    }

    public boolean isActive() {
        return isActive;
    }
}
