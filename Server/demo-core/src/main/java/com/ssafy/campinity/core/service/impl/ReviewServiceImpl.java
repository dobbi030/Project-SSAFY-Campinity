package com.ssafy.campinity.core.service.impl;

import com.ssafy.campinity.core.dto.ReviewReqDTO;
import com.ssafy.campinity.core.entity.campsite.Campsite;
import com.ssafy.campinity.core.entity.member.Member;
import com.ssafy.campinity.core.entity.review.Review;
import com.ssafy.campinity.core.repository.campsite.CampsiteRepository;
import com.ssafy.campinity.core.repository.member.MemberRepository;
import com.ssafy.campinity.core.repository.review.ReviewRepository;
import com.ssafy.campinity.core.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CampsiteRepository campsiteRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Override
    public void createReview(ReviewReqDTO reviewReqDTO) {
        Member member = memberRepository.findMemberByUuidAndExpiredIsFalse(reviewReqDTO.getUserId())
                .orElseThrow(IllegalArgumentException::new);
        Campsite campsite = campsiteRepository.findByUuid(reviewReqDTO.getCampsiteId())
                .orElseThrow(IllegalArgumentException::new);

        Review review = Review.builder().uuid(UUID.randomUUID()).
                content(reviewReqDTO.getContent()).rate(reviewReqDTO.getRate()).campsite(campsite).
                member(member).build();

        reviewRepository.save(review);

        member.addUserReview(review);
        campsite.addCampsiteReview(review);
    }

    @Override
    public void deleteReview(UUID reviewId) {
        Review review = reviewRepository.findByUuid(reviewId).orElseThrow(IllegalArgumentException::new);

        reviewRepository.delete(review);
    }
}