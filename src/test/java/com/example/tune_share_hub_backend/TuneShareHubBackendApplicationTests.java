package com.example.tune_share_hub_backend;

import com.example.tune_share_hub_backend.dao.like.LikeDao;
import com.example.tune_share_hub_backend.dao.playlist.CommentDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistMapperDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistTrackDao;
import com.example.tune_share_hub_backend.dao.refresh.RefreshTokenDao;
import com.example.tune_share_hub_backend.dao.searchhistory.SearchHistoryDao;
import com.example.tune_share_hub_backend.dao.user.UserDao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TuneShareHubBackendApplicationTests {

	@MockitoBean
	private UserDao userDao;

	@MockitoBean
	private PlaylistMapperDao playlistMapperDao;

	@MockitoBean
	private PlaylistTrackDao playlistTrackDao;

	@MockitoBean
	private RefreshTokenDao refreshTokenDao;

    @MockitoBean
    private SearchHistoryDao searchHistoryDao;

    @MockitoBean
    private LikeDao likeDao;
  
	@MockitoBean
	private CommentDao commentDao;

	@Test
	void contextLoads() {
	}

}
