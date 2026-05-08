package com.example.tune_share_hub_backend;

import com.example.tune_share_hub_backend.dao.playlist.PlaylistMapperDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistTrackDao;
import com.example.tune_share_hub_backend.dao.user.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TuneShareHubBackendApplicationTests {

	@MockBean
	private UserDao userDao;

	@MockBean
	private PlaylistMapperDao playlistMapperDao;

	@MockBean
	private PlaylistTrackDao playlistTrackDao;

	@Test
	void contextLoads() {
	}

}
