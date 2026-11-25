package com.example.ngoun.controller;

import com.example.ngoun.dto.DeviceVoteStatus;
import com.example.ngoun.dto.VoteConfirmRequest;
import com.example.ngoun.dto.VoteConfirmResponse;
import com.example.ngoun.dto.VoteOtpRequest;
import com.example.ngoun.dto.VoteOtpResponse;
import com.example.ngoun.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/api/votes/request-otp")
    public VoteOtpResponse requestOtp(@RequestBody VoteOtpRequest req) {
        return voteService.requestOtp(req.getVoteProfileId(), req.getEmail(), req.getDeviceId());
    }

    @PostMapping("/api/votes/confirm-otp")
    public VoteConfirmResponse confirmOtp(@RequestBody VoteConfirmRequest req) {
        return voteService.confirmOtp(req.getEmail(), req.getOtp(), req.getDeviceId(), req.getVoteProfileId());
    }

    @GetMapping("/api/votes/device-status")
    public DeviceVoteStatus deviceStatus(@RequestParam(required = false) String deviceId) {
        return voteService.deviceStatus(deviceId);
    }

    // Two confirmations from one device committing at the same moment: the
    // database's one-verified-vote-per-device index rejects the second.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public VoteConfirmResponse duplicateDeviceVote() {
        return VoteConfirmResponse.fail("Un vote a déjà été enregistré depuis cet appareil dans cette catégorie.");
    }
}
