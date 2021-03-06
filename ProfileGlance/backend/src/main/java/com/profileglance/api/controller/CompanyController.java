package com.profileglance.api.controller;

import com.profileglance.api.request.CompanyLikePostReq;
import com.profileglance.api.request.CompanyLoginPostReq;
import com.profileglance.api.request.CompanyPostReq;
import com.profileglance.api.response.CompanyInterviewGetRes;
import com.profileglance.api.response.CompanyLikeListGetRes;
import com.profileglance.api.response.CompanyMypageGetRes;
import com.profileglance.api.response.RecruitPostRes;
import com.profileglance.api.service.CompanyService;
import com.profileglance.api.service.UserService;
import com.profileglance.common.response.BaseResponseBody;
import com.profileglance.config.JwtTokenProvider;
import com.profileglance.db.entity.Company;
import com.profileglance.db.repository.CompanyRepository;
import com.profileglance.db.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "기업회원 API", tags = {"Company"})
@RequestMapping("/company")
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins="*")
public class CompanyController {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    @Autowired
    CompanyService companyService;
    @Autowired
    UserService userService;

    // 회원가입
    @PostMapping("/signup")
    @ApiOperation(value = "기업회원 가입", notes = "<strong>아이디와 패스워드</strong>를 통해 회원가입 한다.")
    public ResponseEntity<? extends BaseResponseBody> signUp(@ModelAttribute CompanyPostReq companyPostReq) {

        companyService.createCompany(companyPostReq);

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "Success"));
    }

    // 기업회원 로그인
    @PostMapping("/login")
    @ApiOperation(value = "기업 회원 로그인", notes = "<strong>아이디와 패스워드</strong>를 통해 로그인 한다.")
    public String login(@RequestBody CompanyLoginPostReq companyLoginPostReq) {

        Company company = companyRepository.findByCompanyId(companyLoginPostReq.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 회원 입니다."));
        if (!passwordEncoder.matches(companyLoginPostReq.getCompanyPassword(), company.getCompanyPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }
        return jwtTokenProvider.createToken(company.getCompanyId());
    }

    // 기업회원 아이디 중복 확인
    @GetMapping("/companyidcheck/{companyId}")
    @ApiOperation(value = "기업회원 아이디 중복 확인", notes = "DB에 있으면 202, 없으면 201")
    public ResponseEntity<? extends BaseResponseBody> userNicknameCheck(@PathVariable("companyId") String companyId){
        if (companyRepository.findByCompanyId(companyId).isPresent()){
            return ResponseEntity.status(202).body(BaseResponseBody.of(202, "아이디가 이미 있습니다."));
        }else {
            return ResponseEntity.status(201).body(BaseResponseBody.of(201, "아이디 사용가능"));
        }
    }

    // 좋아요 누른 유저 리스트
    @GetMapping("/userlike/{companyId}")
    @ApiOperation(value = "좋아요 누른 유저 리스트", notes = "userNickname 들을 리스트로 반환")
    public ResponseEntity<List<CompanyLikeListGetRes>> userLike(@PathVariable("companyId") String companyId){
       List<CompanyLikeListGetRes> companyLikeListGetResList = companyService.userLikeListByCompany(companyId);
        return new ResponseEntity<List<CompanyLikeListGetRes>>(companyLikeListGetResList, HttpStatus.OK);
    }

    // 단순히 좋아요를 눌렀는지를 판단.
    @PostMapping("/likecheck")
    @ApiOperation(value = "좋아요를 눌렀는지 안눌렀는지 판단해서 statusCode 반환", notes = "좋아요가 눌러져있는거면 202, 좋아요를 누르지 않은 상태이면 201")
    public ResponseEntity<? extends BaseResponseBody> likeCheck(@RequestBody CompanyLikePostReq companyLikePostReq){
        String userEmail = userRepository.findByUserNickname(companyLikePostReq.getUserNickname()).get().getUserEmail();
        String companyId = companyLikePostReq.getCompanyId();
        if (companyService.isHitLike(userEmail, companyId)){
            return ResponseEntity.status(202).body(BaseResponseBody.of(202, "좋아요가 눌러져 있습니다."));
        }else{
            return ResponseEntity.status(201).body(BaseResponseBody.of(201, "좋아요를 누르지 않았습니다."));
        }
    }

    // 좋아요를 눌렀는지 안눌렀는지 판단해서 userlike 테이블에 추가/삭제를 함
    @PostMapping("/likecheckclick")
    @ApiOperation(value = "좋아요를 눌렀는지 안눌렀는지 판단하고 userlike 테이블 추가 삭제함", notes = "좋아요를 취소했으면 202, 좋아요를 누른거면 201")
    public ResponseEntity<? extends BaseResponseBody> likeCheckAndClick(@RequestBody CompanyLikePostReq companyLikePostReq){
        String userEmail = userRepository.findByUserNickname(companyLikePostReq.getUserNickname()).get().getUserEmail();
        String companyId = companyLikePostReq.getCompanyId();
        if (companyService.isHitLike(userEmail, companyId)){
            // userlike 테이블에 있음
            // userlike 테이블에서 삭제해야함
            companyService.deleteLikeByCompany(userEmail,companyId);
            userService.companyLikeChange(companyLikePostReq.getUserNickname(), false);
            return ResponseEntity.status(202).body(BaseResponseBody.of(202, "좋아요를 취소했습니다."));
        }else {
            // 위에랑 반대
            companyService.addLikeByCompany(userEmail,companyId);
            userService.companyLikeChange(companyLikePostReq.getUserNickname(), true);
            return ResponseEntity.status(201).body(BaseResponseBody.of(201, "좋아요를 눌렀습니다."));
        }
    }

    // 기업회원 아이디로 내 정보 찾기
    @GetMapping("/companyinfo/{companyId}")
    @ApiOperation(value = "기업회원 아이디로 정보 가져오기", notes = "기업 테이블에 있는것들 전부 준다.")
    public ResponseEntity<CompanyMypageGetRes> companyInfoByCompanyId(@PathVariable("companyId") String companyId){
        CompanyMypageGetRes companyMypageGetRes = companyService.companyInfo(companyId);
        return new ResponseEntity<CompanyMypageGetRes>(companyMypageGetRes, HttpStatus.OK);
    }

    // 기업 회원 아이디로 인터뷰 테이블 정보 가져오기
    @GetMapping("/companyinterviewinfo/{companyId}")
    @ApiOperation(value = "기업 회원 아이디로 인터뷰 테이블 정보 가져오기", notes = "유저닉네임, 인터뷰날짜, 인터뷰시간, 세션명 반환")
    public ResponseEntity<List<CompanyInterviewGetRes>> companyInterviewInfoByCompanyId(@PathVariable("companyId") String companyId){
        String csId = companyRepository.findByCompanyId(companyId).get().getSessionId();
        List<CompanyInterviewGetRes> companyInterviewGetResList = companyService.interviewList(csId);
        return new ResponseEntity<List<CompanyInterviewGetRes>>(companyInterviewGetResList, HttpStatus.OK);
    }

    // 기업 회원 아이디로 채용 리스트 가져오기
    @GetMapping("/companyrecruitinfo/{companyId}")
    @ApiOperation(value = "기업 회원 아이디로 채용 리스트 가져오기", notes = "기업 회원 세션명으로 채용 리스트 가져오기")
    public ResponseEntity<List<RecruitPostRes>> companyRecruitInfoByCompanyId(@PathVariable("companyId") String companyId){
        String csId = companyRepository.findByCompanyId(companyId).get().getSessionId();
        List<RecruitPostRes> recruitPostResList = companyService.recruitList(csId);
        return new ResponseEntity<List<RecruitPostRes>>(recruitPostResList, HttpStatus.OK);
    }

}
