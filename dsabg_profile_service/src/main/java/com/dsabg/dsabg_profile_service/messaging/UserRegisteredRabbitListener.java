package com.dsabg.dsabg_profile_service.messaging;

import com.dsabg.dsabg_profile_service.entity.Profile;
import com.dsabg.dsabg_profile_service.event.UserRegisteredEvent;
import com.dsabg.dsabg_profile_service.repository.ProfileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredRabbitListener {

    private final ProfileRepository profileRepository;

    public UserRegisteredRabbitListener(
            ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @RabbitListener(queues = "dsabg.user.registered.queue")
    public void handleUserRegistered(UserRegisteredEvent event) {

        if (profileRepository.existsByUserId(event.userId())) {
            return;
        }

        Profile profile = new Profile();
        profile.setUserId(event.userId());
        profile.setDisplayName(event.username());

        profileRepository.save(profile);
    }
}