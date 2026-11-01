package com.dsabg.customroom.controller;

import com.dsabg.customroom.dto.request.UpdateRoomSettingsRequest;
import com.dsabg.customroom.dto.response.RoomSettingsResponse;
import com.dsabg.customroom.entity.enums.Difficulty;
import com.dsabg.customroom.entity.enums.Topic;
import com.dsabg.customroom.exception.GlobalExceptionHandler;
import com.dsabg.customroom.service.RoomSettingsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class RoomSettingsControllerTest {

    private static final long ROOM_CODE = 41633442L;
    private static final String USER_ID = "room-host";

    private final RoomSettingsService roomSettingsService = mock(RoomSettingsService.class);
    private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        validator.afterPropertiesSet();
        mockMvc = standaloneSetup(new RoomSettingsController(roomSettingsService))
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void validSettingsUpdateAcceptsRequiredProblemCount() throws Exception {
        UpdateRoomSettingsRequest request = new UpdateRoomSettingsRequest(
                Difficulty.MEDIUM,
                Set.of(Topic.ARRAYS, Topic.GRAPHS),
                5
        );
        when(roomSettingsService.updateSettings(ROOM_CODE, request, USER_ID))
                .thenReturn(new RoomSettingsResponse(
                        ROOM_CODE,
                        Difficulty.MEDIUM,
                        request.topics(),
                        5
                ));

        mockMvc.perform(put("/room/{roomCode}/settings/update-settings", ROOM_CODE)
                        .header("X-User-Id", USER_ID)
                        .contentType("application/json")
                        .content("""
                                {
                                  "difficulty": "MEDIUM",
                                  "topics": ["ARRAYS", "GRAPHS"],
                                  "numberOfProblems": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfProblems").value(5));

        verify(roomSettingsService).updateSettings(ROOM_CODE, request, USER_ID);
    }

    @Test
    void getSettingsReturnsSavedProblemCount() throws Exception {
        when(roomSettingsService.getSettings(ROOM_CODE, USER_ID))
                .thenReturn(new RoomSettingsResponse(
                        ROOM_CODE,
                        Difficulty.MEDIUM,
                        Set.of(Topic.ARRAYS),
                        7
                ));

        mockMvc.perform(get("/room/{roomCode}/settings/get-settings", ROOM_CODE)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfProblems").value(7));

        verify(roomSettingsService).getSettings(ROOM_CODE, USER_ID);
    }

    @Test
    void missingProblemCountReturnsFieldValidationMessage() throws Exception {
        mockMvc.perform(put("/room/{roomCode}/settings/update-settings", ROOM_CODE)
                        .header("X-User-Id", USER_ID)
                        .contentType("application/json")
                        .content("""
                                {
                                  "difficulty": "MEDIUM",
                                  "topics": ["ARRAYS", "GRAPHS"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("numberOfProblems")));
    }

    @Test
    void unsupportedProblemCountReturnsValidationMessage() throws Exception {
        mockMvc.perform(put("/room/{roomCode}/settings/update-settings", ROOM_CODE)
                        .header("X-User-Id", USER_ID)
                        .contentType("application/json")
                        .content("""
                                {
                                  "difficulty": "MEDIUM",
                                  "topics": ["ARRAYS"],
                                  "numberOfProblems": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Problem count must be one of")));
    }

    @Test
    void invalidEnumReturnsReadableRequestMessage() throws Exception {
        mockMvc.perform(put("/room/{roomCode}/settings/update-settings", ROOM_CODE)
                        .header("X-User-Id", USER_ID)
                        .contentType("application/json")
                        .content("""
                                {
                                  "difficulty": "EXTREME",
                                  "topics": ["ARRAYS"],
                                  "numberOfProblems": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("unsupported values")));
    }
}
