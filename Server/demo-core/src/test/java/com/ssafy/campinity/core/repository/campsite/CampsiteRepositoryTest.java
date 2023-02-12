package com.ssafy.campinity.core.repository.campsite;

import com.ssafy.campinity.core.entity.campsite.Campsite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class CampsiteRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    CampsiteRepository campsiteRepository;

    @Test
    @DisplayName("캠핑장 엔터티 리스너 테스트")
    void campsiteListenerTest(){

        Campsite campsite = new Campsite();
        campsite.setCampName("test1");
        campsite.setUuid(UUID.randomUUID());
        Campsite campsite1 = campsiteRepository.save(campsite);

        em.flush();
        assertNotNull(campsite1.getCreatedAt());
        assertNotNull(campsite1.getUpdatedAt());

        campsite1.setCampName("updated1");
        Campsite campsite2 = campsiteRepository.save(campsite1);
        em.flush();

        assertNotSame(campsite2.getCreatedAt(), campsite2.getUpdatedAt());

    }
}