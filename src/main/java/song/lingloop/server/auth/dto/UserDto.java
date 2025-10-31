package song.lingloop.server.auth.dto;

import com.e106.reco.domain.artist.entity.Genre;
import com.e106.reco.domain.artist.entity.Position;
import com.e106.reco.domain.artist.entity.Region;
import com.e106.reco.domain.artist.user.entity.Gender;
import com.e106.reco.domain.artist.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserDto {
    private Long seq;

    private String email;
    private String password;
    private String name;


    private LocalDate birth;
    private String nickname;



    private String content;
    private String profileImage;


}