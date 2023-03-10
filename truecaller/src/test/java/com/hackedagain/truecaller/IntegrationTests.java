package com.hackedagain.truecaller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.hackedagain.truecaller.interfaces.IProfile;
import com.hackedagain.truecaller.interfaces.ITrueCallerProfile;
import com.hackedagain.truecaller.services.MostFrequentNameSelectionStrategy;
import com.hackedagain.truecaller.services.PhoneBookContactServices;
import com.hackedagain.truecaller.services.TrueCallerCallBlockService;
import com.hackedagain.truecaller.services.TrueCallerDirectoryService;
import com.hackedagain.truecaller.services.TrueCallerProfileService;
import com.hackedagain.truecaller.entities.*;
import com.hackedagain.truecaller.enums.Category;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IntegrationTests {

    @Bean
    public TrueCallerProfileService getTrueCallerProfileService() {
        return new TrueCallerProfileService(new MostFrequentNameSelectionStrategy());
    }
    
    @Autowired
    PhoneBookContactServices phoneBookContactServices;

    @Autowired
    TrueCallerProfileService trueCallerProfileService;

    @Autowired
    TrueCallerDirectoryService trueCallerDirectoryService;

    @Autowired
    TrueCallerCallBlockService trueCallerCallBlockService;

    @BeforeEach
    public void setup() {
        trueCallerProfileService.addListener(trueCallerDirectoryService);
    }

    void initializeDataInPhoneBookContactService() {
        IProfile[] profiles = {
			new PhoneBookProfile(new PhoneNumber("+91","123456789"), "Person A", null),
			new PhoneBookProfile(new PhoneNumber("+91","234567891"), "Person B", null),
			new PhoneBookProfile(new PhoneNumber("+91","234567819"), "Person C", null)
		};
        phoneBookContactServices.addContacts(profiles);
    }

    void initializeDataInTrueCallerProfileService() {
        ITrueCallerProfile[] profiles = {
            new TrueCallerProfile(new PhoneNumber("+91", "987654321"), "Person D", null, false),
            new TrueCallerProfile(new PhoneNumber("+91", "987654312"), "EdTech Business", Category.EDUCATION, true),
            new TrueCallerProfile(new PhoneNumber("+91", "987653421"), "Person E", null, true),
            new TrueCallerProfile(new PhoneNumber("+91", "986754321"), "Travel Business", Category.TRAVEL, false),
            new TrueCallerProfile(new PhoneNumber("+91", "987653421"), "Mom", null, true),
        };
        for(ITrueCallerProfile profile : profiles) {
            trueCallerProfileService.registerProfile(profile);
        }
    }

    @Test
    void testTrueCallerProfileRegister() {
        assertTrue(trueCallerProfileService.getAllProfiles().size() == 0);
        initializeDataInTrueCallerProfileService();
        assertTrue(trueCallerProfileService.getAllProfiles().size() == 4);
    }

    @Test
    void testSearchByPhoneNumber() {
        initializeDataInTrueCallerProfileService();
        Set<ITrueCallerProfile> profiles = trueCallerDirectoryService.searchByPhoneNumber(new PhoneNumber("+91", "98765"));
        assertTrue(profiles.size() == 3);

        Set<ITrueCallerProfile> exactProfile = trueCallerDirectoryService.searchByPhoneNumber(new PhoneNumber("+91", "987654312"));
        assertTrue(exactProfile.size() == 1);
    }

    @Test
    void testSearchByName() {
        initializeDataInTrueCallerProfileService();
        Set<ITrueCallerProfile> profiles = trueCallerDirectoryService.searchByName("Person");
        assertSame(2, profiles.size());

        Set<ITrueCallerProfile> exactProfile1 = trueCallerDirectoryService.searchByName("Person E");
        assertSame(1, exactProfile1.size());
        
        Set<ITrueCallerProfile> exactProfile2 = trueCallerDirectoryService.searchByName("Mom");
        assertSame(1, exactProfile2.size());
    }

    @Test
    void testNameSelectionStrategy() {
        MostFrequentNameSelectionStrategy nameSelectionStrategy = new MostFrequentNameSelectionStrategy();
        List<String> wordsToFilter = new ArrayList<>();
        wordsToFilter.add("Mom");
        nameSelectionStrategy.updateIgnoredNames(wordsToFilter);
        trueCallerProfileService = new TrueCallerProfileService(nameSelectionStrategy);
        trueCallerProfileService.addListener(trueCallerDirectoryService);
        initializeDataInTrueCallerProfileService();
        PhoneNumber phoneNumber = new PhoneNumber("+91", "987653421");
        Set<ITrueCallerProfile> result = trueCallerDirectoryService.searchByPhoneNumber(phoneNumber);
        ITrueCallerProfile profile = result.iterator().next();
        assertSame("Person E", profile.getName());
        assertTrue(profile.getRegisteredNames().contains("Mom"));
    }

    @Test
    void testBlockedContact() {
        PhoneNumber phoneNumber = new PhoneNumber("+91", "987653421");
        trueCallerCallBlockService.addNumberToBlockList(phoneNumber);
        assertTrue(trueCallerCallBlockService.isPhoneNumberBlocked(phoneNumber));

        trueCallerCallBlockService.removeNumberFromBlockList(phoneNumber);
        assertFalse(trueCallerCallBlockService.isPhoneNumberBlocked(phoneNumber));
    }

    @Test
    void testContactsImportFromPhoneBook() {
        initializeDataInPhoneBookContactService();
        trueCallerProfileService.importProfileFromContacts(phoneBookContactServices);
        assertSame( phoneBookContactServices.getContacts().length, trueCallerProfileService.getAllProfiles().size());
    }

}
