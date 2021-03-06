package com.tokyo.beach.like;

import com.tokyo.beach.restaurants.like.Like;
import com.tokyo.beach.restaurants.like.LikeController;
import com.tokyo.beach.restaurants.like.LikeDataMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LikeControllerTest {
    private LikeDataMapper likeDataMapper;
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        likeDataMapper = mock(LikeDataMapper.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LikeController(likeDataMapper))
                .build();
    }

    @Test
    public void test_create_returnsCreatedHTTPStatus() throws Exception {
        ResultActions result = mockMvc.perform(post("/restaurants/99/likes")
                .requestAttr("userId", 11L)
        );


        result.andExpect(status().isCreated());
    }

    @Test
    public void test_create_callsCreateOnLikeRepo() throws Exception {
        mockMvc.perform(post("/restaurants/99/likes")
                .requestAttr("userId", 11L)
        );


        verify(likeDataMapper, times(1)).create(11L, 99L);
    }

    @Test
    public void test_create_returnsLikeInResponseJson() throws Exception {
        when(likeDataMapper.create(11L, 99))
                .thenReturn(new Like(99L, 11L));


        ResultActions result = mockMvc.perform(post("/restaurants/99/likes")
                .requestAttr("userId", 11L));


        result.andExpect(jsonPath("$.userId", equalTo(99)));
        result.andExpect(jsonPath("$.restaurantId", equalTo(11)));
    }

    @Test
    public void test_delete_returnsHttpStatusOk() throws Exception {
        ResultActions result = mockMvc.perform(delete("/restaurants/99/likes")
                .requestAttr("userId", 11L)
        );


        result.andExpect(status().isOk());
    }

    @Test
    public void test_delete_callsDeleteMyLikeOnLikeRepo() throws Exception {
        mockMvc.perform(delete("/restaurants/99/likes")
                .requestAttr("userId", 11L)
        );


        verify(likeDataMapper, times(1))
                .delete(11L, 99L);
    }
}
