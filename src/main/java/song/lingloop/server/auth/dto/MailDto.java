package song.lingloop.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MailDto {
    @NotBlank(message = "이메일은 공백이 될 수 없습니다.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.", regexp = EMAIL_EXP)
    private String email;
    private String code;

    public void modifyCode(String code){
        this.code = code;
    }
}