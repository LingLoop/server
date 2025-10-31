package song.lingloop.server.auth.service;


import com.nimbusds.openid.connect.sdk.claims.Gender;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import song.lingloop.server.auth.dto.JoinDto;
import song.lingloop.server.auth.dto.MailDto;
import song.lingloop.server.common.error.exception.BusinessException;
import song.lingloop.server.core.user.domain.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class AuthService implements UserDetailsService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MailRepository mailRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final CrewUserRepository crewUserRepository;
    private final S3FileService s3FileService;

    @Value("${spring.mail.username}")
    private String configEmail;
    @Value("${spring.image.profile.user}")
    private String configProfile;

    //TODO : 나중에 지우기 마스터 계정 생성용임
    @PostConstruct
    public void INIT(){
        if(userRepository.existsByEmail("aaaa@ssafy.com")) return;
        userRepository.save(User.builder()
                        .seq(1L)
                        .email("aaaa@ssafy.com")
                        .password(bCryptPasswordEncoder.encode("1111"))
                        .nickname("king")
                        .name("master")
                        .gender(Gender.ETC)
                        .birth(LocalDate.now())
                        .position(Position.BASE)
                        .genre(Genre.BALAD)
                        .region(Region.BS)
                        .content("Hi everyone")
                        .profileImage(configProfile)
                .build());
    }

    public CommonResponse join(JoinDto joinDto, MultipartFile file) {
        String email = joinDto.getEmail();

        if(userRepository.existsByEmail(email)) throw new BusinessException(USER_EXIST);
        else if(!mailRepository.isEmailValid(email)) throw new BusinessException(EMAIL_EXPIRED);


        String image_url = file==null ?  configProfile : s3FileService.uploadFile(file);
        joinDto.setProfileImage(image_url);
        log.info("profile : {}", joinDto.getProfileImage());

        // 회원가입
        User user = User.of(joinDto);
        user.modifyPassword(bCryptPasswordEncoder.encode(joinDto.getPassword()));
        userRepository.save(user);
        return new CommonResponse("회원가입 완료");
    }
    public CommonResponse sendEmail(MailDto mailDto) {
        String email = mailDto.getEmail();
        if(userRepository.existsByEmail(email)) throw new BusinessException(USER_EXIST);

        String code = generateRandomMailAuthenticationCode();
        String content = getEmailAuthContent(code);

        mailRepository.createEmailCode(email, code);
        return sendEmailToRequestUser(configEmail, email, USER_AUTH_MAIL_TITLE, content)
                .map(sendResult -> new CommonResponse("ok"))
                .orElseThrow(() -> new BusinessException(EMAIL_NOT_SENT));
    }

    public CommonResponse verifyEmailCode(MailDto mailDto) {
        if(!mailRepository.isEmailCodeValid(mailDto.getEmail(), mailDto.getCode())) throw new BusinessException(EMAIL_INVALID);
        mailRepository.createEmailSuccess(mailDto.getEmail());

        return new CommonResponse("이메일 인증 완료");
    }
    public Optional<Integer> sendEmailToRequestUser(String configEmail, String email, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
            helper.setFrom(new InternetAddress(configEmail));
            helper.setTo(new InternetAddress(email));
            helper.setSubject(title);
            helper.setText(content,true);
            mailSender.send(message);
        } catch (MessagingException e) {
            return Optional.empty();
        }

        return Optional.of(1);
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(IllegalArgumentException::new);
        List<Long> crews = crewUserRepository.findPk_CrewSeqByPk_userSeq(user.getSeq());
        return CustomUserDetails.builder()
                .seq(user.getSeq())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .email(user.getEmail())
                .genre(user.getGenre())
                .year(String.valueOf(user.getBirth().getYear()))
                .position(user.getPosition())
                .region(user.getRegion())
                .gender(user.getGender())
                .crews(crews)
                .role(null)
                .build();

    }
}



