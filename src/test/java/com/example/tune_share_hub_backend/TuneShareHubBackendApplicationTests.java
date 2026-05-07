package com.example.tune_share_hub_backend;

import com.example.tune_share_hub_backend.dao.playlist.PlaylistDao;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TuneShareHubBackendApplicationTests {

	@MockitoBean
	private PlaylistDao playlistDao;

	@Test
	void contextLoads() {
	}

}
