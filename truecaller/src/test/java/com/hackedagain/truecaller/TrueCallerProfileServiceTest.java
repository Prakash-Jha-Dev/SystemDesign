package com.hackedagain.truecaller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.hackedagain.truecaller.entities.PhoneNumber;
import com.hackedagain.truecaller.entities.TrueCallerProfile;
import com.hackedagain.truecaller.enums.Category;
import com.hackedagain.truecaller.interfaces.ITrueCallerProfile;
import com.hackedagain.truecaller.services.MostFrequentNameSelectionStrategy;
import com.hackedagain.truecaller.services.TrueCallerProfileService;

@SpringBootTest
public class TrueCallerProfileServiceTest {

    @Mock
    MostFrequentNameSelectionStrategy mostFrequentNameSelectionStrategy;

    @Captor
    ArgumentCaptor<Set<String>> registeredNamesCaptor;

    TrueCallerProfileService trueCallerProfileService;

    @BeforeEach
    public void setup() {
        trueCallerProfileService = new TrueCallerProfileService(mostFrequentNameSelectionStrategy);
    }

    @Test
    public void testProfileUpdate() {
        when(mostFrequentNameSelectionStrategy.getName(registeredNamesCaptor.capture())).thenReturn("Selected Name");
        PhoneNumber phoneNumber = new PhoneNumber("+91", "987653421");
        ITrueCallerProfile profile = new TrueCallerProfile(phoneNumber, "Travel Business", Category.TRAVEL, true);
        trueCallerProfileService.registerProfile(profile);

        ITrueCallerProfile updatedProfile = new TrueCallerProfile(phoneNumber, "EdTech Business", Category.EDUCATION, true);
        trueCallerProfileService.registerProfile(updatedProfile);

        List<ITrueCallerProfile> matchingProfiles = trueCallerProfileService.getAllProfiles().stream().filter(entry -> entry.getPhoneNumber().equals(phoneNumber)).collect(Collectors.toList());
        assertSame(1, matchingProfiles.size());
        assertTrue(matchingProfiles.get(0).getRegisteredNames().contains("Travel Business") && matchingProfiles.get(0).getRegisteredNames().contains("EdTech Business"));
        assertSame(2, registeredNamesCaptor.getValue().size());
    }

    @Test
    public void testProfileSpamCheck() {
        PhoneNumber phoneNumber = new PhoneNumber("+91", "987653421");
        ITrueCallerProfile profile = new TrueCallerProfile(phoneNumber, "Travel Business", Category.TRAVEL, true);
        trueCallerProfileService.registerProfile(profile);

        assertFalse(trueCallerProfileService.getAllProfiles().get(0).isSpam());
        updateSpam(phoneNumber, true);
        assertTrue(trueCallerProfileService.getAllProfiles().stream().filter(x -> x.getPhoneNumber().equals(phoneNumber)).collect(Collectors.toList()).get(0).isSpam());
        updateSpam(phoneNumber, false);
        assertFalse(trueCallerProfileService.getAllProfiles().stream().filter(x -> x.getPhoneNumber().equals(phoneNumber)).collect(Collectors.toList()).get(0).isSpam());
    }

    private void updateSpam(PhoneNumber phoneNumber, boolean markAsSpam) {
        for (int i=101; i<=200; i++) {
            PhoneNumber newPhoneNumber = new PhoneNumber("+"+String.valueOf(i), "987653421");
            ITrueCallerProfile newProfile = new TrueCallerProfile(newPhoneNumber, "Person " + String.valueOf(i), null, true);
            trueCallerProfileService.registerProfile(newProfile);
            trueCallerProfileService.updateProfileSpamData(phoneNumber, newProfile, markAsSpam);
        }
    }
}
