package com.example.ngoun.controller;

import com.example.ngoun.dto.VoteConfirmRequest;
import com.example.ngoun.dto.VoteConfirmResponse;
import com.example.ngoun.dto.VoteOtpRequest;
import com.example.ngoun.dto.VoteOtpResponse;
import com.example.ngoun.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/api/votes/request-otp")
    public VoteOtpResponse requestOtp(@RequestBody VoteOtpRequest req) {
        return voteService.requestOtp(req.getVoteProfileId(), req.getEmail());
    }

    @PostMapping("/api/votes/confirm-otp")
    public VoteConfirmResponse confirmOtp(@RequestBody VoteConfirmRequest req) {
        return voteService.confirmOtp(req.getEmail(), req.getOtp());
    }
}
