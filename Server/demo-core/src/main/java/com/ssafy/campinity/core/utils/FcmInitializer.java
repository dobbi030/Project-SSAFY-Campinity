package com.ssafy.campinity.core.utils;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class FcmInitializer {

    private final String GOOGLE_APPLICATION_CREDENTIALS = "firebase/campinity-5ff94-firebase-adminsdk-a0uem-64c6576e75.json";;
    private final String FIREBASE_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    @PostConstruct
    public void initialize() throws IOException {
        ClassPathResource resource = new ClassPathResource(GOOGLE_APPLICATION_CREDENTIALS);

        try (InputStream is = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials
                            .fromStream(is)
                            .createScoped(List.of(FIREBASE_SCOPE)))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
//                log.info("FirebaseApp initialization complete");
            }
        } catch (IOException e){
//            log.error(e.getMessage());
            // spring 뜰때 알림 서버가 잘 동작하지 않는 것이므로 바로 죽임
            throw new RuntimeException(e.getMessage());
        }
    }
}
