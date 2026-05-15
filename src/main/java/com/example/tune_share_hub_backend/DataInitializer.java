package com.example.tune_share_hub_backend;

import com.example.tune_share_hub_backend.dao.like.LikeDao;
import com.example.tune_share_hub_backend.dao.playlist.CommentDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistDao;
import com.example.tune_share_hub_backend.dao.playlist.PlaylistTrackDao;
import com.example.tune_share_hub_backend.dao.user.UserDao;
import com.example.tune_share_hub_backend.entity.Comment;
import com.example.tune_share_hub_backend.entity.Playlist;
import com.example.tune_share_hub_backend.entity.PlaylistTrack;
import com.example.tune_share_hub_backend.entity.like.Like;
import com.example.tune_share_hub_backend.entity.user.User;
import com.example.tune_share_hub_backend.entity.user.UserRoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserDao userDao;
    private final PlaylistDao playlistDao;
    private final PlaylistTrackDao playlistTrackDao;
    private final CommentDao commentDao;
    private final LikeDao likeDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 더미 사용자 데이터 삽입
        List<User> users = insertDummyUsers();
        List<Playlist> playlists = insertDummyPlaylists(users);
        insertDummyEngagements(users, playlists);
    }

    private List<User> insertDummyUsers() {
        List<UserSeed> userSeeds = userSeeds();

        try {
            LocalDateTime now = LocalDateTime.now();

            for (UserSeed seed : userSeeds) {
                User existingUser = userDao.getUserByEmail(seed.email());
                if (existingUser != null) {
                    log.info("더미 사용자 이미 존재: {}", existingUser.getEmail());
                    continue;
                }

                User user = User.builder()
                        .email(seed.email())
                        .passwordHash(passwordEncoder.encode(seed.password()))
                        .nickname(seed.nickname())
                        .role(UserRoleType.USER)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                userDao.insert(user);
                log.info("더미 사용자 생성: {}", user.getEmail());
            }

            insertDummyAdminUser(now);

            return userSeeds.stream()
                    .map(seed -> userDao.getUserByEmail(seed.email()))
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            log.error("더미 사용자 데이터 삽입 중 오류 발생: ", e);
            return List.of();
        }
    }

    private List<UserSeed> userSeeds() {
        List<UserSeed> seeds = new ArrayList<>(Arrays.asList(
                new UserSeed("test@example.com", "password123", "테스트유저"),
                new UserSeed("demo@example.com", "password123", "데모유저"),
                new UserSeed("jiwon@example.com", "password123", "지원"),
                new UserSeed("minsu@example.com", "password123", "민수")
        ));

        for (int i = 1; i <= 50; i++) {
            seeds.add(new UserSeed(
                    "dummy%02d@example.com".formatted(i),
                    "password123",
                    "더미유저%02d".formatted(i)
            ));
        }

        return seeds;
    }

    private void insertDummyAdminUser(LocalDateTime now) {
        if (userDao.getUserByEmail("admin@example.com") != null) {
            log.info("더미 관리자 이미 존재: admin@example.com");
            return;
        }

        User admin = User.builder()
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .nickname("관리자")
                .role(UserRoleType.ADMIN)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userDao.insert(admin);
        log.info("더미 관리자 생성: {}", admin.getEmail());
    }

    private List<Playlist> insertDummyPlaylists(List<User> users) {
        if (users.isEmpty()) {
            log.info("더미 플레이리스트를 생성할 사용자가 없습니다.");
            return List.of();
        }

        List<PlaylistSeed> playlistSeeds = Arrays.asList(
                new PlaylistSeed("test@example.com", "픽셀 게임 비트", "픽셀 게임 화면처럼 통통 튀는 비트 모음입니다.", "/uploads/images/demo-pixel-quest-beats.png", List.of("게임", "픽셀", "아케이드")),
                new PlaylistSeed("test@example.com", "차고 펑크 록", "거친 기타와 지하 합주실 분위기의 펑크 플레이리스트입니다.", "/uploads/images/demo-garage-punk-riot.png", List.of("펑크", "차고", "록")),
                new PlaylistSeed("test@example.com", "동화 속 자장가", "동화책 숲과 달빛이 떠오르는 잔잔한 플레이리스트입니다.", "/uploads/images/demo-fairy-tale-lullabies.png", List.of("동화", "자장가", "잔잔한")),
                new PlaylistSeed("test@example.com", "비 오는 날 드라마 OST", "비 오는 밤 드라마 장면에 어울리는 감성 OST 무드입니다.", "/uploads/images/demo-drama-ost-rain.png", List.of("드라마", "오에스티", "비오는날")),
                new PlaylistSeed("test@example.com", "시티팝 드라이브", "노을진 해안도로를 달리는 2D 시티팝 감성입니다.", "/uploads/images/demo-2d-city-pop-drive.png", List.of("시티팝", "드라이브", "감성")),

                new PlaylistSeed("demo@example.com", "사이버 운동장", "사이버 경기장처럼 빠르고 강한 운동용 비트입니다.", "/uploads/images/demo-cyber-workout-arena.png", List.of("운동", "사이버", "이디엠")),
                new PlaylistSeed("demo@example.com", "케이인디 옥상", "해질녘 옥상에서 듣는 따뜻한 인디 사운드입니다.", "/uploads/images/demo-k-indie-rooftop.png", List.of("인디", "옥상", "국내음악")),
                new PlaylistSeed("demo@example.com", "로파이 공부 책상", "비 오는 창가 책상에 어울리는 집중용 로파이입니다.", "/uploads/images/demo-lo-fi-study-desk.png", List.of("로파이", "공부", "집중")),
                new PlaylistSeed("demo@example.com", "사막 신스웨이브", "사막 고속도로와 레트로 신스가 만나는 전자음악 무드입니다.", "/uploads/images/demo-desert-synthwave.png", List.of("신스웨이브", "레트로", "전자음악")),
                new PlaylistSeed("demo@example.com", "아침 카페 어쿠스틱", "아침 카페에서 편하게 듣는 어쿠스틱 플레이리스트입니다.", "/uploads/images/demo-cafe-acoustic-morning.png", List.of("어쿠스틱", "카페", "아침")),

                new PlaylistSeed("jiwon@example.com", "보스전 EDM", "보스전처럼 몰아치는 게임 EDM 에너지입니다.", "/uploads/images/demo-boss-battle-edm.png", List.of("게임", "보스전", "이디엠")),
                new PlaylistSeed("jiwon@example.com", "빈티지 재즈 라운지", "빈티지 라운지의 밤공기와 재즈 악기 무드입니다.", "/uploads/images/demo-vintage-jazz-lounge.png", List.of("재즈", "라운지", "빈티지")),
                new PlaylistSeed("jiwon@example.com", "우주 오페라 발라드", "우주 오페라 장면처럼 웅장한 발라드 감성입니다.", "/uploads/images/demo-space-opera-ballads.png", List.of("발라드", "우주", "영화음악")),
                new PlaylistSeed("jiwon@example.com", "케이팝 네온 무대", "네온 무대 조명 아래의 밝은 K-pop 에너지입니다.", "/uploads/images/demo-k-pop-neon-stage.png", List.of("케이팝", "팝", "무대")),
                new PlaylistSeed("jiwon@example.com", "다큐멘터리 포크 여행", "다큐멘터리 여행길처럼 담백한 포크 무드입니다.", "/uploads/images/demo-documentary-folk-road.png", List.of("포크", "여행", "다큐")),

                new PlaylistSeed("minsu@example.com", "새벽 힙합 작업실", "새벽 작업실에서 만드는 비트와 힙합 분위기입니다.", "/uploads/images/demo-midnight-hip-hop-lab.png", List.of("힙합", "비트", "새벽")),
                new PlaylistSeed("minsu@example.com", "던전 메탈 질주", "던전 스테이지처럼 묵직하고 어두운 메탈 사운드입니다.", "/uploads/images/demo-metal-dungeon-run.png", List.of("메탈", "던전", "강렬한")),
                new PlaylistSeed("minsu@example.com", "복고 트로트 야시장", "야시장 조명과 복고 감성이 살아있는 트로트 무드입니다.", "/uploads/images/demo-retro-trot-night-market.png", List.of("트로트", "복고", "야시장")),
                new PlaylistSeed("minsu@example.com", "판타지 오케스트라 정원", "판타지 정원의 달빛과 오케스트라가 어울리는 플레이리스트입니다.", "/uploads/images/demo-fantasy-orchestra-garden.png", List.of("클래식", "오케스트라", "판타지")),
                new PlaylistSeed("minsu@example.com", "해변 레게 석양", "해변 석양 아래 여유롭게 흐르는 레게 리듬입니다.", "/uploads/images/demo-beach-reggae-sunset.png", List.of("레게", "해변", "석양"))
        );

        try {
            for (PlaylistSeed seed : playlistSeeds) {
                User user = userDao.getUserByEmail(seed.email());
                if (user == null) {
                    log.info("더미 플레이리스트 사용자 없음: {}", seed.email());
                    continue;
                }

                Playlist playlist = findExistingPlaylist(user.getUserId(), seed.title());
                if (playlist != null) {
                    log.info("더미 플레이리스트 이미 존재: {}", seed.title());
                } else {
                    playlist = new Playlist();
                    playlist.setUserId(user.getUserId());
                    playlist.setTitle(seed.title());
                    playlist.setDescription(seed.description());
                    playlist.setCoverImageUrl(seed.coverImageUrl());
                    playlist.setPublicYn("Y");
                    playlist.setTags(seed.tags());

                    playlistDao.insert(playlist);
                    log.info("더미 플레이리스트 생성: {} ({})", seed.title(), seed.email());
                }

                insertDummyTracks(playlist);
            }

            return findSeedPlaylists(playlistSeeds);
        } catch (Exception e) {
            log.error("더미 플레이리스트 데이터 삽입 중 오류 발생: ", e);
            return List.of();
        }
    }

    private List<Playlist> findSeedPlaylists(List<PlaylistSeed> playlistSeeds) {
        return playlistSeeds.stream()
                .map(seed -> {
                    User user = userDao.getUserByEmail(seed.email());
                    return user == null ? null : findExistingPlaylist(user.getUserId(), seed.title());
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Playlist findExistingPlaylist(Long userId, String title) {
        return playlistDao.findByUserId(userId).stream()
                .filter(playlist -> title.equals(playlist.getTitle()))
                .findFirst()
                .orElse(null);
    }

    private void insertDummyTracks(Playlist playlist) {
        if (playlist == null || playlist.getPlaylistId() == null) {
            return;
        }

        if (!playlistTrackDao.findByPlaylistId(playlist.getPlaylistId()).isEmpty()) {
            log.info("더미 트랙 이미 존재: {}", playlist.getTitle());
            return;
        }

        List<TrackSeed> trackSeeds = tracksByPlaylistTitle().getOrDefault(playlist.getTitle(), List.of());
        if (trackSeeds.isEmpty()) {
            log.info("더미 트랙 목록 없음: {}", playlist.getTitle());
            return;
        }

        List<PlaylistTrack> playlistTracks = trackSeeds.stream()
                .map(seed -> {
                    int positionNo = trackSeeds.indexOf(seed) + 1;
                    return PlaylistTrack.builder()
                        .playlistId(playlist.getPlaylistId())
                        .spotifyTrackId(seed.spotifyTrackId())
                        .title(seed.title())
                        .artistName(seed.artistName())
                        .albumName(seed.albumName())
                        .albumImageUrl(seed.albumImageUrl())
                        .spotifyUrl(seed.spotifyUrl())
                        .youtubeUrl(seed.youtubeUrl())
                        .durationMs(seed.durationMs())
                        .positionNo(positionNo)
                        .build();
                })
                .toList();

        playlistTrackDao.insertPlaylistTracks(playlistTracks);
        playlistDao.increaseTrackCount(playlist.getPlaylistId(), playlistTracks.size());
        log.info("더미 트랙 생성: {}개 ({})", playlistTracks.size(), playlist.getTitle());
    }

    private void insertDummyEngagements(List<User> users, List<Playlist> playlists) {
        if (users.isEmpty() || playlists.isEmpty()) {
            log.info("더미 좋아요/댓글을 생성할 데이터가 부족합니다.");
            return;
        }

        try {
            for (int playlistIndex = 0; playlistIndex < playlists.size(); playlistIndex++) {
                Playlist playlist = playlists.get(playlistIndex);
                insertDummyLikes(users, playlist, playlistIndex);
                insertDummyComments(users, playlist, playlistIndex);
            }
        } catch (Exception e) {
            log.error("더미 좋아요/댓글 데이터 삽입 중 오류 발생: ", e);
        }
    }

    private void insertDummyLikes(List<User> users, Playlist playlist, int playlistIndex) {
        int targetLikeCount = 12 + (playlistIndex % 9);
        int activeLikeCount = 0;
        int insertedLikeCount = 0;

        for (int i = 0; i < users.size() && activeLikeCount < targetLikeCount; i++) {
            User user = users.get((playlistIndex * 7 + i) % users.size());
            if (user.getUserId().equals(playlist.getUserId())) {
                continue;
            }

            Like existingLike = likeDao.getLikeByUserIdAndPlaylistId(playlist.getPlaylistId(), user.getUserId());
            if (existingLike != null && "Y".equals(existingLike.getStatus())) {
                activeLikeCount++;
                continue;
            }

            if (existingLike == null) {
                likeDao.insertLike(playlist.getPlaylistId(), user.getUserId(), "Y");
                likeDao.incrementLikeCount(playlist.getPlaylistId());
                activeLikeCount++;
                insertedLikeCount++;
                continue;
            }

            if ("N".equals(existingLike.getStatus())) {
                likeDao.updateLikeStatus(playlist.getPlaylistId(), user.getUserId(), "Y");
                likeDao.incrementLikeCount(playlist.getPlaylistId());
                activeLikeCount++;
                insertedLikeCount++;
            }
        }

        log.info("더미 좋아요 생성: {}개 ({})", insertedLikeCount, playlist.getTitle());
    }

    private void insertDummyComments(List<User> users, Playlist playlist, int playlistIndex) {
        if (!commentDao.findByPlaylistId(playlist.getPlaylistId()).isEmpty()) {
            log.info("더미 댓글 이미 존재: {}", playlist.getTitle());
            return;
        }

        List<String> commentContents = commentsForPlaylist(playlist.getTitle());
        for (int i = 0; i < commentContents.size(); i++) {
            User user = users.get((playlistIndex * 5 + i + 3) % users.size());
            Comment comment = Comment.builder()
                    .playlistId(playlist.getPlaylistId())
                    .userId(user.getUserId())
                    .userNickname(user.getNickname())
                    .content(commentContents.get(i))
                    .build();

            commentDao.insertComment(comment);
            playlistDao.increaseCommentCount(playlist.getPlaylistId());
        }

        log.info("더미 댓글 생성: {}개 ({})", commentContents.size(), playlist.getTitle());
    }

    private List<String> commentsForPlaylist(String playlistTitle) {
        if (playlistTitle.contains("케이팝") || playlistTitle.contains("사이버")) {
            return List.of(
                    "발표 때 틀어도 바로 분위기 살아날 것 같아요.",
                    "선곡이 밝고 에너지가 있어서 계속 듣게 됩니다.",
                    "운동할 때 들으면 템포가 딱 맞을 것 같아요."
            );
        }

        if (playlistTitle.contains("카페") || playlistTitle.contains("인디") || playlistTitle.contains("로파이")) {
            return List.of(
                    "작업할 때 틀어두기 좋은 차분한 선곡이에요.",
                    "곡 흐름이 자연스러워서 오래 듣기 좋네요.",
                    "아침이나 공부할 때 쓰기 좋은 플레이리스트입니다."
            );
        }

        if (playlistTitle.contains("트로트")) {
            return List.of(
                    "복고 분위기가 확실해서 발표용 더미 데이터로 잘 보여요.",
                    "부모님도 좋아하실 것 같은 선곡이에요.",
                    "야시장 콘셉트랑 곡들이 잘 맞습니다."
            );
        }

        return List.of(
                "플레이리스트 콘셉트가 명확해서 보기 좋습니다.",
                "커버 이미지랑 곡 분위기가 잘 어울려요.",
                "좋아요 누르고 다시 들으러 올 것 같아요."
        );
    }

    private Map<String, List<TrackSeed>> tracksByPlaylistTitle() {
        return Map.ofEntries(
                Map.entry("픽셀 게임 비트", trackList(
                        track("5P4dYeVXBq1xOrxL4dnnNQ", "Jump Up, Super Star!", "The Super Mario Players", "Super Mario Odyssey", "https://i.scdn.co/image/ab67616d0000b2738c1a4b66cb97d4af7021a3ce", "https://open.spotify.com/track/5P4dYeVXBq1xOrxL4dnnNQ", "https://www.youtube.com/watch?v=e9r5hx47kxM", 244363),
                        track("1J03Vp93ybKIxfzYI4YJtL", "Megalovania", "Toby Fox", "UNDERTALE Soundtrack", "https://i.scdn.co/image/ab67616d0000b27324edb22d068eb245a924b7f2", "https://open.spotify.com/track/1J03Vp93ybKIxfzYI4YJtL", "https://www.youtube.com/watch?v=KK3KXAECte4", 156000),
                        track("2ZbJuTlSK9TYokeza42cmR", "Green Hill Zone", "Masato Nakamura", "Sonic The Hedgehog", "https://i.scdn.co/image/ab67616d0000b2730c9af38beaecd4d7b83ee46f", "https://open.spotify.com/track/2ZbJuTlSK9TYokeza42cmR", "https://www.youtube.com/watch?v=NaAb1s0MCQQ", 156186),
                        track("63eAc8LBe2F595dnsTAzbI", "Korobeiniki", "Tetris", "Tetris Theme", "https://i.scdn.co/image/ab67616d0000b273a8b5a371287177f75d7c1bb2", "https://open.spotify.com/track/63eAc8LBe2F595dnsTAzbI", "https://www.youtube.com/watch?v=NmCCQxVBfyM", 44571),
                        track("0UfPo4BUMSlnjSw0XDIiL7", "Dream Land", "Hirokazu Ando", "Kirby Soundtrack", "https://i.scdn.co/image/ab67616d0000b2736c1bc72dd9325e2dcc586754", "https://open.spotify.com/track/0UfPo4BUMSlnjSw0XDIiL7", "https://www.youtube.com/watch?v=uLrR2aK-Jfg", 182653),
                        track("6vdcizPLYIZhZOZZJUsAtS", "Gerudo Valley", "Koji Kondo", "The Legend of Zelda", "https://i.scdn.co/image/ab67616d0000b2738ad288504f81652595f19794", "https://open.spotify.com/track/6vdcizPLYIZhZOZZJUsAtS", "https://www.youtube.com/watch?v=0hEYvdMoF2g", 234135),
                        track("5MSJVvMCEp8vp1zXR2DUQ7", "Stickerbush Symphony", "David Wise", "Donkey Kong Country 2", "https://i.scdn.co/image/ab67616d0000b2730f51b851afa4fe31ea28f476", "https://open.spotify.com/track/5MSJVvMCEp8vp1zXR2DUQ7", "https://www.youtube.com/watch?v=lndBgOrTWxo", 267284),
                        track("0hIHGsoPG2VdcJf2aHoLM6", "Vampire Killer", "Konami Kukeiha Club", "Castlevania", "https://i.scdn.co/image/ab67616d0000b2733a2443b565415d7f43872703", "https://open.spotify.com/track/0hIHGsoPG2VdcJf2aHoLM6", "https://www.youtube.com/watch?v=9Q_da2FxEPI", 185196),
                        track("76jLguvP9uwjwYpApKUNvW", "Still Alive", "Jonathan Coulton", "Portal", "https://i.scdn.co/image/ab67616d0000b2739479b97f7f4254660f6c95e2", "https://open.spotify.com/track/76jLguvP9uwjwYpApKUNvW", "https://www.youtube.com/watch?v=Y6ljFaKRTrI", 177373),
                        track("716HpbfSkMkU7TwQsJlhHT", "Sweden", "C418", "Minecraft Volume Alpha", "https://i.scdn.co/image/ab67616d0000b273a6f6056c9ded5242ca245c56", "https://open.spotify.com/track/716HpbfSkMkU7TwQsJlhHT", "https://www.youtube.com/watch?v=aBkTkxKDduc", 215500)
                )),
                Map.entry("차고 펑크 록", trackList(
                        track("22ea9e9wspXjaR7qfqfsdS", "Blitzkrieg Bop", "Ramones", "Ramones", "https://i.scdn.co/image/ab67616d0000b273f1a090e4c286822f1eefb29b", "https://open.spotify.com/track/22ea9e9wspXjaR7qfqfsdS", "https://www.youtube.com/watch?v=skdE0KAFCEA", 133440),
                        track("6L89mwZXSOwYl76YXfX13s", "Basket Case", "Green Day", "Dookie", "https://i.scdn.co/image/ab67616d0000b273db89b08034de626ebee6823d", "https://open.spotify.com/track/6L89mwZXSOwYl76YXfX13s", "https://www.youtube.com/watch?v=NUTGr5t3MoY", 181533),
                        track("124Y9LPRCAz3q2OP0iCvcJ", "London Calling", "The Clash", "London Calling", "https://i.scdn.co/image/ab67616d0000b2736e49cf8fd2505d4dc5368403", "https://open.spotify.com/track/124Y9LPRCAz3q2OP0iCvcJ", "https://www.youtube.com/watch?v=LC2WpBcdM_A", 199733),
                        track("6nTiIhLmQ3FWhvrGafw2zj", "American Idiot", "Green Day", "American Idiot", "https://i.scdn.co/image/ab67616d0000b27308a1b1e0674086d3f1995e1b", "https://open.spotify.com/track/6nTiIhLmQ3FWhvrGafw2zj", "https://www.youtube.com/watch?v=kqkGY9jsBOw", 176346),
                        track("39shmbIHICJ2Wxnk1fPSdz", "Should I Stay or Should I Go", "The Clash", "Combat Rock", "https://i.scdn.co/image/ab67616d0000b273280b72ca76b4734debfc190e", "https://open.spotify.com/track/39shmbIHICJ2Wxnk1fPSdz", "https://www.youtube.com/watch?v=BN1WwnEDWAM", 188987),
                        track("2m1hi0nfMR9vdGC8UcrnwU", "All The Small Things", "blink-182", "Enema Of The State", "https://i.scdn.co/image/ab67616d0000b2736da502e35a7a3e48de2b0f74", "https://open.spotify.com/track/2m1hi0nfMR9vdGC8UcrnwU", "https://www.youtube.com/watch?v=9Ht5RZpzPqw", 167066),
                        track("5moTxUGPZXgGmosl4rIELm", "Anarchy In The U.K.", "Sex Pistols", "Never Mind The Bollocks", "https://i.scdn.co/image/ab67616d0000b273b691fa29c123b5fc4bc5d469", "https://open.spotify.com/track/5moTxUGPZXgGmosl4rIELm", "https://www.youtube.com/watch?v=q31WY0Aobro", 212480),
                        track("5vfjUAhefN7IjHbTvVCT4Z", "Holiday", "Green Day", "American Idiot", "https://i.scdn.co/image/ab67616d0000b2731bb1db39abc18755d7ab2114", "https://open.spotify.com/track/5vfjUAhefN7IjHbTvVCT4Z", "https://www.youtube.com/watch?v=Ajxn0PKbv7I", 233026),
                        track("6HvUYS1xDfTCGWoeVrv3XS", "Dammit", "blink-182", "Dude Ranch", "https://i.scdn.co/image/ab67616d0000b2730153faf0def5084faabe2b0f", "https://open.spotify.com/track/6HvUYS1xDfTCGWoeVrv3XS", "https://www.youtube.com/watch?v=xoLE2yEvuPM", 165373),
                        track("5NoQvINZLBV1wMYPdNmReL", "I Wanna Be Sedated", "Ramones", "Road To Ruin", "https://i.scdn.co/image/ab67616d0000b273072ee11a51e23f861c54cef9", "https://open.spotify.com/track/5NoQvINZLBV1wMYPdNmReL", "https://www.youtube.com/watch?v=bm51ihfi1p4", 149786)
                )),
                Map.entry("동화 속 자장가", trackList(
                        track("3s6nfqfT6kyLBT10vs0M4l", "A Dream Is a Wish Your Heart Makes", "Lily James", "Cinderella", "https://i.scdn.co/image/ab67616d0000b27365c6c451e1ed0d70f0774b24", "https://open.spotify.com/track/3s6nfqfT6kyLBT10vs0M4l", "https://www.youtube.com/watch?v=Zye-4GGANzw", 120546),
                        track("3liiZRlJxa8GeLUgWMoIeu", "Once Upon a Dream", "Lana Del Rey", "Maleficent", "https://i.scdn.co/image/ab67616d0000b273fa644aeb3f0226b4f81f45e6", "https://open.spotify.com/track/3liiZRlJxa8GeLUgWMoIeu", "https://www.youtube.com/watch?v=8waJ7W3QcJc", 203080),
                        track("1WrPa4lrIddctGWAIYYfP9", "When You Wish Upon a Star", "Cliff Edwards", "Pinocchio", "https://i.scdn.co/image/ab67616d0000b27350f9a2e8f1102e1f5a76d369", "https://open.spotify.com/track/1WrPa4lrIddctGWAIYYfP9", "https://www.youtube.com/watch?v=wEWowjC2dSk", 195066),
                        track("631kyE9HXj54yvg3mBrM38", "Castle on a Cloud", "Isabelle Allen", "Les Miserables", "https://i.scdn.co/image/ab67616d0000b273170e79548d280867ef12742b", "https://open.spotify.com/track/631kyE9HXj54yvg3mBrM38", "https://www.youtube.com/watch?v=z-X3eHq-KGw", 71600),
                        track("3l4L76cnSaCNoNkkUGNSVR", "Pure Imagination", "Gene Wilder", "Willy Wonka", "https://i.scdn.co/image/ab67616d0000b27379bb3eb5ba95f875ad4a1041", "https://open.spotify.com/track/3l4L76cnSaCNoNkkUGNSVR", "https://www.youtube.com/watch?v=A-DuOmA75lI", 259253),
                        track("568SEFtDjKr7N2PytpA6D5", "Over the Rainbow", "Judy Garland", "The Wizard of Oz", "https://i.scdn.co/image/ab67616d0000b2736893ceb167a556d6a9e068fd", "https://open.spotify.com/track/568SEFtDjKr7N2PytpA6D5", "https://www.youtube.com/watch?v=PSZxmZmBfnU", 165240),
                        track("3PV8uIQnxD4Qiimn3x6PVr", "La Vie En Rose", "Emily Watts", "Dreamy Covers", "https://i.scdn.co/image/ab67616d0000b273cd99b58ac0ddac2f3f2f9cb7", "https://open.spotify.com/track/3PV8uIQnxD4Qiimn3x6PVr", "https://www.youtube.com/watch?v=EloXaKNp2co", 157683),
                        track("2EOo6tJpZIgmVQHmUv1dtg", "Moon River", "Audrey Hepburn", "Breakfast at Tiffany's", "https://i.scdn.co/image/ab67616d0000b2730902aa7dee196ec1f219e351", "https://open.spotify.com/track/2EOo6tJpZIgmVQHmUv1dtg", "https://www.youtube.com/watch?v=0BfUDyvdTSE", 123160),
                        track("2agBDIr9MYDUducQPC1sFU", "River Flows in You", "Yiruma", "First Love", "https://i.scdn.co/image/ab67616d0000b2734bdb66d1335f8571240e755f", "https://open.spotify.com/track/2agBDIr9MYDUducQPC1sFU", "https://www.youtube.com/watch?v=7maJOI3QMu0", 188786),
                        track("7vd1j4IDTU0koES9M8dvBQ", "Kiss the Rain", "Yiruma", "From The Yellow Room", "https://i.scdn.co/image/ab67616d0000b273a9173cdb5fc02e5dafc32a03", "https://open.spotify.com/track/7vd1j4IDTU0koES9M8dvBQ", "https://www.youtube.com/watch?v=imGaOIm5HOk", 260653)
                )),
                Map.entry("비 오는 날 드라마 OST", trackList(
                        track("6mzF8HvHdVrzJNd8M1uFCS", "Beautiful", "Crush", "Guardian OST", "https://i.scdn.co/image/ab67616d0000b273bc051de00f5d0631b8df4bcd", "https://open.spotify.com/track/6mzF8HvHdVrzJNd8M1uFCS", "https://www.youtube.com/watch?v=hAjiKVEWZSk", 227583),
                        track("1HYzRuWjmS9LXCkdVHi25K", "Stay With Me", "Chanyeol, Punch", "Guardian OST", "https://i.scdn.co/image/ab67616d0000b2730f5c597bba60a1e0c5364baa", "https://open.spotify.com/track/1HYzRuWjmS9LXCkdVHi25K", "https://www.youtube.com/watch?v=pcKR0LPwoYs", 192441),
                        track("1D6lK8qADoErTTLkY4YBfM", "Everytime", "Chen, Punch", "Descendants of the Sun OST", "https://i.scdn.co/image/ab67616d0000b27352b4a39a68f595936012712e", "https://open.spotify.com/track/1D6lK8qADoErTTLkY4YBfM", "https://www.youtube.com/watch?v=fTc5tuEn6_U", 189355),
                        track("4s80CRYk3rRPZE56NvmFi7", "You Are My Everything", "Gummy", "Descendants of the Sun OST", "https://i.scdn.co/image/ab67616d0000b273f0a6aae96d2d73108b16092e", "https://open.spotify.com/track/4s80CRYk3rRPZE56NvmFi7", "https://www.youtube.com/watch?v=8ykKs4gUuMg", 240590),
                        track("1pEavn8UFAeij6afPwWfvM", "All With You", "Taeyeon", "Moon Lovers OST", "https://i.scdn.co/image/ab67616d0000b2733faf7c8704b859da0c2e07b9", "https://open.spotify.com/track/1pEavn8UFAeij6afPwWfvM", "https://www.youtube.com/watch?v=LFC9WdtbitE", 233939),
                        track("07qN0WAQ099t8KZeDVMLSs", "Can You Hear My Heart", "Epik High, Lee Hi", "Moon Lovers OST", "https://i.scdn.co/image/ab67616d0000b273abdb1b890060b4e80df9ba84", "https://open.spotify.com/track/07qN0WAQ099t8KZeDVMLSs", "https://www.youtube.com/watch?v=F_9pZv0wn9Q", 248143),
                        track("1uZ5Ulb2qfle3HbqB12vNQ", "Here I Am Again", "Yerin Baek", "Crash Landing on You OST", "https://i.scdn.co/image/ab67616d0000b2736413fc7cd84907d255024c01", "https://open.spotify.com/track/1uZ5Ulb2qfle3HbqB12vNQ", "https://www.youtube.com/watch?v=FFmdTU4Cpr8", 234840),
                        track("6dGsBRuavumBs5BghcXF3D", "Give You My Heart", "IU", "Crash Landing on You OST", "https://i.scdn.co/image/ab67616d0000b273f71c919ad0b87d2726b14778", "https://open.spotify.com/track/6dGsBRuavumBs5BghcXF3D", "https://www.youtube.com/watch?v=euI-C1YONaU", 280360),
                        track("2X45nVBeYzmDlrXji9Av0Q", "Love, Maybe", "MeloMance", "Business Proposal OST", "https://i.scdn.co/image/ab67616d0000b27347d4fcf597d9aee2d5a34e8e", "https://open.spotify.com/track/2X45nVBeYzmDlrXji9Av0Q", "https://www.youtube.com/watch?v=2v0InJbwc9E", 185000),
                        track("186NCtNk1tUYS7c2DxgJ7O", "Christmas Tree", "V", "Our Beloved Summer OST", "https://i.scdn.co/image/ab67616d0000b2738b03ae2b1d78de7c1e4dc3a2", "https://open.spotify.com/track/186NCtNk1tUYS7c2DxgJ7O", "https://www.youtube.com/watch?v=lj8TV9q59P4", 209946)
                )),
                Map.entry("시티팝 드라이브", trackList(
                        track("7rU6Iebxzlvqy5t857bKFq", "Plastic Love", "Mariya Takeuchi", "VARIETY", "https://i.scdn.co/image/ab67616d0000b273ba6b3322182afc4136521d2c", "https://open.spotify.com/track/7rU6Iebxzlvqy5t857bKFq", "https://www.youtube.com/watch?v=T_lC2O1oIew", 294493),
                        track("7xmkcZJJsT1san7GjuksE6", "Stay With Me", "Miki Matsubara", "Pocket Park", "https://i.scdn.co/image/ab67616d0000b2730d7925a25c0aae26d9fc4d06", "https://open.spotify.com/track/7xmkcZJJsT1san7GjuksE6", "https://www.youtube.com/watch?v=DhATYhwoFVk", 308803),
                        track("1qUo7d5lAOclNVbTUY0A2R", "Remember Summer Days", "Anri", "Timely!!", "https://i.scdn.co/image/ab67616d0000b273cfd93d36fe2365f9436587d1", "https://open.spotify.com/track/1qUo7d5lAOclNVbTUY0A2R", "https://www.youtube.com/watch?v=yHKb38-nl3U", 295666),
                        track("3pgainA2dH9c7e8JVYGRCN", "Ride On Time", "Tatsuro Yamashita", "Ride On Time", "https://i.scdn.co/image/ab67616d0000b273ae03a889dd876866982b309a", "https://open.spotify.com/track/3pgainA2dH9c7e8JVYGRCN", "https://www.youtube.com/watch?v=_FEZPkX0sv8", 272147),
                        track("6gI8gTacAsGPEy70QK4a3i", "Sparkle", "Tatsuro Yamashita", "For You", "https://i.scdn.co/image/ab67616d0000b273b410256f0c9158425ee88463", "https://open.spotify.com/track/6gI8gTacAsGPEy70QK4a3i", "https://www.youtube.com/watch?v=pqobRu9aR3M", 257932),
                        track("0JUWF44gfMszGNhjCF7Ufs", "Midnight Pretenders", "Tomoko Aran", "Fuyu-Kukan", "https://i.scdn.co/image/ab67616d0000b2737d2d24d8a6bf7578a140db55", "https://open.spotify.com/track/0JUWF44gfMszGNhjCF7Ufs", "https://www.youtube.com/watch?v=7pPb5fmumNo", 345266),
                        track("0zoGVO4bQXG8U6ChKwNgeg", "4:00A.M.", "Taeko Onuki", "Mignonne", "https://i.scdn.co/image/ab67616d0000b273af12a920f8a4256f4fe32d35", "https://open.spotify.com/track/0zoGVO4bQXG8U6ChKwNgeg", "https://www.youtube.com/watch?v=_sOKkON_UnQ", 336960),
                        track("3FYDqY5BRtx3IVSaiQZSze", "I Can't Stop The Loneliness", "Anri", "Timely!!", "https://i.scdn.co/image/ab67616d0000b273cfd93d36fe2365f9436587d1", "https://open.spotify.com/track/3FYDqY5BRtx3IVSaiQZSze", "https://www.youtube.com/watch?v=6bALJxjL8jw", 263706),
                        track("5hwWC08lhYLiZH41IksW2r", "Telephone Number", "Junko Ohashi", "Magical", "https://i.scdn.co/image/ab67616d0000b27348f4bb994184d96101fc8efa", "https://open.spotify.com/track/5hwWC08lhYLiZH41IksW2r", "https://www.youtube.com/watch?v=XJWqHmY-g9U", 265305),
                        track("1Myuv6YUjLyOownmZZWnxy", "Fantasy", "Meiko Nakahara", "Friday Magic", "https://i.scdn.co/image/ab67616d0000b273a4120249788258d45bc99a79", "https://open.spotify.com/track/1Myuv6YUjLyOownmZZWnxy", "https://www.youtube.com/watch?v=toVErfLS8kk", 263826)
                )),
                Map.entry("사이버 운동장", trackList(
                        track("11jDqolNZVgXlwnVn9WYiB", "손오공", "SEVENTEEN", "FML", "https://i.scdn.co/image/ab67616d0000b273e82d8ab40d624fa15693d26a", "https://open.spotify.com/track/11jDqolNZVgXlwnVn9WYiB", "https://www.youtube.com/watch?v=-GQg25oP0S4", 197564),
                        track("6I2tqFhk8tq69iursYxuxd", "HOT", "SEVENTEEN", "Face the Sun", "https://i.scdn.co/image/ab67616d0000b273decd839dd4fef3faf64c5fd5", "https://open.spotify.com/track/6I2tqFhk8tq69iursYxuxd", "https://www.youtube.com/watch?v=GqKewrvEtI8", 197586),
                        track("0ObcVIFoDdBBClvABfJvdz", "MANIAC", "Stray Kids", "ODDINARY", "https://i.scdn.co/image/ab67616d0000b2738c759dea19e40f9225e447e8", "https://open.spotify.com/track/0ObcVIFoDdBBClvABfJvdz", "https://www.youtube.com/watch?v=OvioeS1ZZ7o", 182757),
                        track("54zRGA28tVRKRmFCpywWko", "특", "Stray Kids", "5-STAR", "https://i.scdn.co/image/ab67616d0000b273f50dce3e166b3b248e1ba983", "https://open.spotify.com/track/54zRGA28tVRKRmFCpywWko", "https://www.youtube.com/watch?v=EzgCa_m8pOY", 195688),
                        track("5GK7eDDTVz7CTge1wJrDQW", "Kill This Love", "BLACKPINK", "KILL THIS LOVE", "https://i.scdn.co/image/ab67616d0000b2732d602ab2d4acff0c2cf57683", "https://open.spotify.com/track/5GK7eDDTVz7CTge1wJrDQW", "https://www.youtube.com/watch?v=2S24-y0Ij3Y", 189052),
                        track("7EyhPjrJzjx0fk2i7vUJCS", "Pink Venom", "BLACKPINK", "BORN PINK", "https://i.scdn.co/image/ab67616d0000b273d8c2bf84c41ec28dc6fb8926", "https://open.spotify.com/track/7EyhPjrJzjx0fk2i7vUJCS", "https://www.youtube.com/watch?v=3or3dp3qNQU", 186964),
                        track("7KavHYqoVFNB3IUXfs5gvP", "I AM", "IVE", "I've IVE", "https://i.scdn.co/image/ab67616d0000b2734156626a16155286856ea868", "https://open.spotify.com/track/7KavHYqoVFNB3IUXfs5gvP", "https://www.youtube.com/watch?v=6ZUIwj3FgUY", 183853),
                        track("0XrDEv1Xe0ZQsS6AmPn04c", "Baddie", "IVE", "I'VE MINE", "https://i.scdn.co/image/ab67616d0000b2733bbc6a71db1759c3b0053135", "https://open.spotify.com/track/0XrDEv1Xe0ZQsS6AmPn04c", "https://www.youtube.com/watch?v=MXIKzekRAj8", 154360),
                        track("2zrhoHlFKxFTRF5aMyxMoQ", "Next Level", "aespa", "Next Level", "https://i.scdn.co/image/ab67616d0000b2737a393b04e8ced571618223e8", "https://open.spotify.com/track/2zrhoHlFKxFTRF5aMyxMoQ", "https://www.youtube.com/watch?v=ZNZj64oIBqc", 221573),
                        track("0SE1qlvZVknGGKTwuXBPKz", "Drama", "aespa", "Drama", "https://i.scdn.co/image/ab67616d0000b273ae59604190fd4dcd891d5c69", "https://open.spotify.com/track/0SE1qlvZVknGGKTwuXBPKz", "https://www.youtube.com/watch?v=rmUxs2DM3zA", 214973)
                )),
                Map.entry("케이인디 옥상", trackList(
                        track("6DA7kCWYMggJjqPM84V2Ng", "Tomboy", "HYUKOH", "23", "https://i.scdn.co/image/ab67616d0000b2736b0b72f94905f2321564a20f", "https://open.spotify.com/track/6DA7kCWYMggJjqPM84V2Ng", "https://www.youtube.com/watch?v=pC6tPEaAiYU", 241692),
                        track("66UcQu5LBo2A7AC0A5r0lI", "Wi Ing Wi Ing", "HYUKOH", "20", "https://i.scdn.co/image/ab67616d0000b2731ca37e9bcd0cc377a82aec48", "https://open.spotify.com/track/66UcQu5LBo2A7AC0A5r0lI", "https://www.youtube.com/watch?v=D6dqkvR4F9g", 194866),
                        track("72cq3rZCIEYaq1TM8y5LBQ", "Your Dog Loves You", "Colde, Crush", "Wave", "https://i.scdn.co/image/ab67616d0000b273b8b1020d37e55d5db307a0a7", "https://open.spotify.com/track/72cq3rZCIEYaq1TM8y5LBQ", "https://www.youtube.com/watch?v=WybG8FesMc8", 273000),
                        track("6z1kLsntE7FuzKZHZWrXYN", "instagram", "DEAN", "instagram", "https://i.scdn.co/image/ab67616d0000b2734ee9dc60013d5648d0f23bcc", "https://open.spotify.com/track/6z1kLsntE7FuzKZHZWrXYN", "https://www.youtube.com/watch?v=wKyMIrBClYw", 255728),
                        track("2cTK5kS6aPYBNdhUQCzFN7", "Square", "Yerin Baek", "Every letter I sent you.", "https://i.scdn.co/image/ab67616d0000b273fca7f5aebfb6010c6da60e00", "https://open.spotify.com/track/2cTK5kS6aPYBNdhUQCzFN7", "https://www.youtube.com/watch?v=4iFP_wd6QU8", 261762),
                        track("2bAomTBntkwBb0xXUIxTK9", "Maybe It's Not Our Fault", "Yerin Baek", "Our love is great", "https://i.scdn.co/image/ab67616d0000b273336c8631246485947d6c5b70", "https://open.spotify.com/track/2bAomTBntkwBb0xXUIxTK9", "https://www.youtube.com/watch?v=yWP--1gsr20", 232568),
                        track("3jsYQw78lrxJA2ysnmOIf9", "Some", "BOL4", "Red Diary Page.1", "https://i.scdn.co/image/ab67616d0000b2732cfb66b63fe85ccfff4109b1", "https://open.spotify.com/track/3jsYQw78lrxJA2ysnmOIf9", "https://www.youtube.com/watch?v=Je-AOvGZVso", 181373),
                        track("15c7KZTrsCUxCQcOdUVELc", "Galaxy", "BOL4", "Full Album RED PLANET", "https://i.scdn.co/image/ab67616d0000b273cbde8680a3f8d3976fa9cfab", "https://open.spotify.com/track/15c7KZTrsCUxCQcOdUVELc", "https://www.youtube.com/watch?v=8DeV-Qax6as", 213853),
                        track("0hqj5JBnFt1BHEz2UCFwrl", "How Can I Love The Heartbreak", "AKMU", "SAILING", "https://i.scdn.co/image/ab67616d0000b273d41cdd1f3e033a0ea1642112", "https://open.spotify.com/track/0hqj5JBnFt1BHEz2UCFwrl", "https://www.youtube.com/watch?v=BydX3CHVPp8", 290095),
                        track("7sNPQxJLeBNOQY1pEDZl1K", "Falling Leaves Are Beautiful", "Heize", "Late Autumn", "https://i.scdn.co/image/ab67616d0000b2739314c6ddad027167f1932026", "https://open.spotify.com/track/7sNPQxJLeBNOQY1pEDZl1K", "https://www.youtube.com/watch?v=zF3l_BQbcgI", 202933)
                )),
                Map.entry("로파이 공부 책상", trackList(
                        track("4sUTagdmyuyAxd7RvbygpQ", "Aruarian Dance", "Nujabes", "Samurai Champloo", "https://i.scdn.co/image/ab67616d0000b27359ef99ed996b4e537ed2e6a3", "https://open.spotify.com/track/4sUTagdmyuyAxd7RvbygpQ", "https://www.youtube.com/watch?v=qYcoJpqCha4", 250433),
                        track("4aK4LNijbD7kkCg54UoIij", "Feather", "Nujabes, Cise Starr", "Modal Soul", "https://i.scdn.co/image/ab67616d0000b273421d647a4f604d79943f4dad", "https://open.spotify.com/track/4aK4LNijbD7kkCg54UoIij", "https://www.youtube.com/watch?v=hQ5x8pHoIPA", 175693),
                        track("09LMmJBZmxK9sVVDIjXbFA", "Luv(sic) pt3", "Nujabes, Shing02", "Modal Soul", "https://i.scdn.co/image/ab67616d0000b273421d647a4f604d79943f4dad", "https://open.spotify.com/track/09LMmJBZmxK9sVVDIjXbFA", "https://www.youtube.com/watch?v=Fwv2gnCFDOc", 337880),
                        track("6zxUG5IWr0yjjHvh7vGGWd", "Blessing It", "Nujabes", "Metaphorical Music", "https://i.scdn.co/image/ab67616d0000b273340621140003e6e8dcba27ca", "https://open.spotify.com/track/6zxUG5IWr0yjjHvh7vGGWd", "https://www.youtube.com/watch?v=e1p2S3HxwUo", 163186),
                        track("6jfdBc7Vma7hZ6ytt2lZF8", "Reflection Eternal", "Nujabes", "Modal Soul", "https://i.scdn.co/image/ab67616d0000b273421d647a4f604d79943f4dad", "https://open.spotify.com/track/6jfdBc7Vma7hZ6ytt2lZF8", "https://www.youtube.com/watch?v=2wK27xW4OFI", 260253),
                        track("4SUqvLTprZ3ohbDZLKIpbN", "Sea of Cloud", "Nujabes", "Modal Soul", "https://i.scdn.co/image/ab67616d0000b273421d647a4f604d79943f4dad", "https://open.spotify.com/track/4SUqvLTprZ3ohbDZLKIpbN", "https://www.youtube.com/watch?v=FLk_nvHZW2Y", 181773),
                        track("3d9GBklH58z0TKCv0oryxU", "Counting Stars", "Nujabes", "Metaphorical Music", "https://i.scdn.co/image/ab67616d0000b27391d1fcb64e6f615f76213c48", "https://open.spotify.com/track/3d9GBklH58z0TKCv0oryxU", "https://www.youtube.com/watch?v=IXa0kLOKfwQ", 248000),
                        track("2jHjD4TByFyRpouBrCbqzN", "World's End Rhapsody", "Nujabes", "Modal Soul", "https://i.scdn.co/image/ab67616d0000b273421d647a4f604d79943f4dad", "https://open.spotify.com/track/2jHjD4TByFyRpouBrCbqzN", "https://www.youtube.com/watch?v=0XJFSTYryv4", 341200),
                        track("6TkPrGpY43wOi1rdMWHKe9", "Thank You", "Nujabes", "Modal Soul Classics", "https://i.scdn.co/image/ab67616d0000b273421d647a4f604d79943f4dad", "https://open.spotify.com/track/6TkPrGpY43wOi1rdMWHKe9", "https://www.youtube.com/watch?v=NC8fZXqUAsU", 249373),
                        track("11nuN9xWUoKJNohMtyCoJg", "Lady Brown", "Nujabes, Cise Starr", "Metaphorical Music", "https://i.scdn.co/image/ab67616d0000b27310c73c3476e60630df210053", "https://open.spotify.com/track/11nuN9xWUoKJNohMtyCoJg", "https://www.youtube.com/watch?v=R5dphZnPb9A", 199160)
                )),
                Map.entry("사막 신스웨이브", trackList(
                        track("0U0ldCRmgCqhVvD6ksG63j", "Nightcall", "Kavinsky", "OutRun", "https://i.scdn.co/image/ab67616d0000b273d6d8c2eaa1f9031b62f7a3f7", "https://open.spotify.com/track/0U0ldCRmgCqhVvD6ksG63j", "https://www.youtube.com/watch?v=MV_3Dpw-BRY", 258413),
                        track("3Q6mwJseOFYBJ10d5CXp4o", "Odd Look", "Kavinsky", "OutRun", "https://i.scdn.co/image/ab67616d0000b2738cc5da5394cb8ccd3646179a", "https://open.spotify.com/track/3Q6mwJseOFYBJ10d5CXp4o", "https://www.youtube.com/watch?v=vJ1oyOazhEk", 253546),
                        track("1aJrVsHwcQMZ8TZ6mlvT5J", "A Real Hero", "College, Electric Youth", "Drive", "https://i.scdn.co/image/ab67616d0000b27316fd333484d62242764455f0", "https://open.spotify.com/track/1aJrVsHwcQMZ8TZ6mlvT5J", "https://www.youtube.com/watch?v=BHgYtKkSEDA", 267859),
                        track("10qbHF920zH5K8C8IcE5AL", "Turbo Killer", "Carpenter Brut", "Trilogy", "https://i.scdn.co/image/ab67616d0000b2731b1d6c550aaaae5acf220e84", "https://open.spotify.com/track/10qbHF920zH5K8C8IcE5AL", "https://www.youtube.com/watch?v=wy9r2qeouiQ", 208972),
                        track("7oxnK2wg8qFv8EXyyxKDJ4", "Roller Mobster", "Carpenter Brut", "Trilogy", "https://i.scdn.co/image/ab67616d0000b2731b1d6c550aaaae5acf220e84", "https://open.spotify.com/track/7oxnK2wg8qFv8EXyyxKDJ4", "https://www.youtube.com/watch?v=qFfybn_W8Ak", 214351),
                        track("0MabrxpL9vrCJeOjGMnGgM", "Venger", "Perturbator", "The Uncanny Valley", "https://i.scdn.co/image/ab67616d0000b2737bd7c7c0b0b0e4e38d2feafc", "https://open.spotify.com/track/0MabrxpL9vrCJeOjGMnGgM", "https://www.youtube.com/watch?v=abETp8Q6H8U", 308197),
                        track("20ztml2STRF7Sq1UaBB6ox", "Future Club", "Perturbator", "Dangerous Days", "https://i.scdn.co/image/ab67616d0000b2737be0bdb1f8731f9af3e95812", "https://open.spotify.com/track/20ztml2STRF7Sq1UaBB6ox", "https://www.youtube.com/watch?v=RY66fdMt4vc", 289186),
                        track("6mB9A9YLbY4jxpKX5EYAnT", "Sunset", "The Midnight", "Endless Summer", "https://i.scdn.co/image/ab67616d0000b27366f27b0ef0b92099a1927721", "https://open.spotify.com/track/6mB9A9YLbY4jxpKX5EYAnT", "https://www.youtube.com/watch?v=URma_gu1aNE", 326520),
                        track("5MPLeS9KdZlA04OOCUb5Bt", "Los Angeles", "The Midnight", "Days of Thunder", "https://i.scdn.co/image/ab67616d0000b273dffe8e5a1e7c4eed82fbbfd5", "https://open.spotify.com/track/5MPLeS9KdZlA04OOCUb5Bt", "https://www.youtube.com/watch?v=z_b4tucWzSw", 389094),
                        track("28uitw2AXQQIUHvSJW9qy6", "Days of Thunder", "The Midnight", "Days of Thunder", "https://i.scdn.co/image/ab67616d0000b273dffe8e5a1e7c4eed82fbbfd5", "https://open.spotify.com/track/28uitw2AXQQIUHvSJW9qy6", "https://www.youtube.com/watch?v=UiSB2Fbw9gs", 324018)
                )),
                Map.entry("아침 카페 어쿠스틱", trackList(
                        track("1OnBsmZjRUj32yJ7zhmWUP", "밤편지", "IU", "Palette", "https://i.scdn.co/image/ab67616d0000b27364904880572aebd94cc1eb00", "https://open.spotify.com/track/1OnBsmZjRUj32yJ7zhmWUP", "https://www.youtube.com/watch?v=6744glqD6lk", 262060),
                        track("3PViMZ3kY1Nz2yw4BDpSEN", "가을 아침", "IU", "꽃갈피 둘", "https://i.scdn.co/image/ab67616d0000b2734606d2d5183beabfefe3a516", "https://open.spotify.com/track/3PViMZ3kY1Nz2yw4BDpSEN", "https://www.youtube.com/watch?v=ZDoH5dQ58ps", 227552),
                        track("5Sh2pJT3VWTvkQOXPAaRiC", "폰서트", "10CM", "4.0", "https://i.scdn.co/image/ab67616d0000b273b6b49f0d0e5d33b707c041ef", "https://open.spotify.com/track/5Sh2pJT3VWTvkQOXPAaRiC", "https://www.youtube.com/watch?v=qJQA2ZIFKig", 197503),
                        track("5rXkWmQz7HKaNjbvcruAUe", "스토커", "10CM", "3.0", "https://i.scdn.co/image/ab67616d0000b273080466ac55997bcb313b523d", "https://open.spotify.com/track/5rXkWmQz7HKaNjbvcruAUe", "https://www.youtube.com/watch?v=tEJVcyB7sug", 259571),
                        track("3U05TkFX1Tzlu064IDai5I", "우주를 줄게", "BOL4", "Full Album RED PLANET", "https://i.scdn.co/image/ab67616d0000b273ceb392d41618ad82b2e0492c", "https://open.spotify.com/track/3U05TkFX1Tzlu064IDai5I", "https://www.youtube.com/watch?v=8DeV-Qax6as", 215066),
                        track("5xuVcWfj8QTeRLRSKyQOyi", "좋다고 말해", "BOL4", "Full Album RED PLANET", "https://i.scdn.co/image/ab67616d0000b27304ea6586dbf5e29b4f197895", "https://open.spotify.com/track/5xuVcWfj8QTeRLRSKyQOyi", "https://www.youtube.com/watch?v=2GbImQQVAY0", 201772),
                        track("3Ml2s37uS9jqRM2R3bfDiB", "모든 날, 모든 순간", "Paul Kim", "Should We Kiss First OST", "https://i.scdn.co/image/ab67616d0000b27390a98780fb70dde769f9e61a", "https://open.spotify.com/track/3Ml2s37uS9jqRM2R3bfDiB", "https://www.youtube.com/watch?v=64uidOIH2vY", 210493),
                        track("1qeZGmP3hvURwkImj8jzCr", "너를 만나", "Paul Kim", "너를 만나", "https://i.scdn.co/image/ab67616d0000b2732abab4b867353a66de2aa520", "https://open.spotify.com/track/1qeZGmP3hvURwkImj8jzCr", "https://www.youtube.com/watch?v=L5phdoBLhtE", 282566),
                        track("49jhaFKylisSzgaReEP2Jt", "흔들리는 꽃들 속에서 네 샴푸향이 느껴진거야", "Jang Beom June", "Melo Is My Nature OST", "https://i.scdn.co/image/ab67616d0000b27351c2dca2df814c291f0b7c6a", "https://open.spotify.com/track/49jhaFKylisSzgaReEP2Jt", "https://www.youtube.com/watch?v=YBEUXfT7_48", 168492),
                        track("3MgWFkrUFnoxMcxxtTn7WN", "사랑은 은하수 다방에서", "10CM", "1.0", "https://i.scdn.co/image/ab67616d0000b273e5194f7212791b8c96492862", "https://open.spotify.com/track/3MgWFkrUFnoxMcxxtTn7WN", "https://www.youtube.com/watch?v=f06dSoQPX2U", 175190)
                )),
                Map.entry("보스전 EDM", trackList(
                        track("6VRhkROS2SZHGlp0pxndbJ", "Bangarang", "Skrillex", "Bangarang", "https://i.scdn.co/image/ab67616d0000b2736081278cb62df2757d55633b", "https://open.spotify.com/track/6VRhkROS2SZHGlp0pxndbJ", "https://www.youtube.com/watch?v=cR2XilcGYOo", 215253),
                        track("5q8oybjZelukF4h0CzSUN9", "Scary Monsters and Nice Sprites", "Skrillex", "Scary Monsters", "https://i.scdn.co/image/ab67616d0000b2730d9a6a2a68b4010c5b21a38c", "https://open.spotify.com/track/5q8oybjZelukF4h0CzSUN9", "https://www.youtube.com/watch?v=WSeNSzJ2-Jw", 243852),
                        track("0u2AIKDVafHwCFQ9LDnqxH", "Centipede", "Knife Party", "Rage Valley", "https://i.scdn.co/image/ab67616d0000b273303ae66195b6feba7022698b", "https://open.spotify.com/track/0u2AIKDVafHwCFQ9LDnqxH", "https://www.youtube.com/watch?v=kXp6wOg6pxM", 246770),
                        track("0QIYINh2AwmOmdu8CRYvlw", "Bonfire", "Knife Party", "Rage Valley", "https://i.scdn.co/image/ab67616d0000b273303ae66195b6feba7022698b", "https://open.spotify.com/track/0QIYINh2AwmOmdu8CRYvlw", "https://www.youtube.com/watch?v=zc6CIU5t9qg", 272077),
                        track("5qFL2uwfnGU8FccwLMgPNQ", "Internet Friends", "Knife Party", "100% No Modern Talking", "https://i.scdn.co/image/ab67616d0000b273ed2a0b49e0be66b06fcc63b5", "https://open.spotify.com/track/5qFL2uwfnGU8FccwLMgPNQ", "https://www.youtube.com/watch?v=luJJBeCFeM0", 301122),
                        track("4ua0IepBEISCWwF8dTJvcU", "Ghosts 'n' Stuff", "deadmau5", "For Lack of a Better Name", "https://i.scdn.co/image/ab67616d0000b273d49d976721f4dc6b3c6225ad", "https://open.spotify.com/track/4ua0IepBEISCWwF8dTJvcU", "https://www.youtube.com/watch?v=pb-EwykPTv8", 328253),
                        track("0F539Pcn7LDa2MbBhDe3Tw", "Raise Your Weapon", "deadmau5", "4x4=12", "https://i.scdn.co/image/ab67616d0000b27326f7709399913201ebe40eee", "https://open.spotify.com/track/0F539Pcn7LDa2MbBhDe3Tw", "https://www.youtube.com/watch?v=YnwfTHpnGLY", 502909),
                        track("60wwxj6Dd9NJlirf84wr2c", "Clarity", "Zedd, Foxes", "Clarity", "https://i.scdn.co/image/ab67616d0000b273941dd3b3343d9cb9329d37bf", "https://open.spotify.com/track/60wwxj6Dd9NJlirf84wr2c", "https://www.youtube.com/watch?v=l9Q7GISatW0", 271426),
                        track("1dFkD1JfRMzwO6hwUsE8aS", "Spectrum", "Zedd, Matthew Koma", "Clarity", "https://i.scdn.co/image/ab67616d0000b273941dd3b3343d9cb9329d37bf", "https://open.spotify.com/track/1dFkD1JfRMzwO6hwUsE8aS", "https://www.youtube.com/watch?v=wEp9MCQlAa4", 243413),
                        track("3NRDLYyqIXja0UElvdzjkB", "Language", "Porter Robinson", "Language", "https://i.scdn.co/image/ab67616d0000b2736046328819f2994a34214b7f", "https://open.spotify.com/track/3NRDLYyqIXja0UElvdzjkB", "https://www.youtube.com/watch?v=Vsy1URDYK88", 368475)
                )),
                Map.entry("빈티지 재즈 라운지", trackList(
                        track("1YQWosTIljIvxAgHWTp7KP", "Take Five", "The Dave Brubeck Quartet", "Time Out", "https://i.scdn.co/image/ab67616d0000b273b6bd44cf06bf8f4d5ce1e080", "https://open.spotify.com/track/1YQWosTIljIvxAgHWTp7KP", "https://www.youtube.com/watch?v=-DHuW1h1wHw", 328831),
                        track("6J18r0R7rQFuZ4hiRW4P4W", "So What", "Miles Davis", "Kind of Blue", "https://i.scdn.co/image/ab67616d0000b273415146787899e44956a3b512", "https://open.spotify.com/track/6J18r0R7rQFuZ4hiRW4P4W", "https://www.youtube.com/watch?v=ylXk1LBvIqU", 561065),
                        track("5o6PkOAAwHvdkkOBGPxi20", "Blue in Green", "Miles Davis", "Kind of Blue", "https://i.scdn.co/image/ab67616d0000b27353916cd700eef7318d795479", "https://open.spotify.com/track/5o6PkOAAwHvdkkOBGPxi20", "https://www.youtube.com/watch?v=TLDflhhdPCg", 324160),
                        track("3ZikLQCnH3SIswlGENBcKe", "My Favorite Things", "John Coltrane", "My Favorite Things", "https://i.scdn.co/image/ab67616d0000b273c00913832628c23c7952ae7b", "https://open.spotify.com/track/3ZikLQCnH3SIswlGENBcKe", "https://www.youtube.com/watch?v=JQvc-Gkwhow", 824133),
                        track("0X5DcGkbxCXSadgj01ZXd7", "Autumn Leaves", "Cannonball Adderley", "Somethin' Else", "https://i.scdn.co/image/ab67616d0000b2730f81ed1bf84b3585a8492d3d", "https://open.spotify.com/track/0X5DcGkbxCXSadgj01ZXd7", "https://www.youtube.com/watch?v=CpB7-8SGlJ0", 659520),
                        track("6Rqn2GFlmvmV4w9Ala0I1e", "Feeling Good", "Nina Simone", "I Put a Spell on You", "https://i.scdn.co/image/ab67616d0000b273892abb1ade35b4863b29e051", "https://open.spotify.com/track/6Rqn2GFlmvmV4w9Ala0I1e", "https://www.youtube.com/watch?v=Yc5jwR9KUvg", 174000),
                        track("2dR5WkrpwylTuT3jRWNufa", "Fly Me To The Moon", "Frank Sinatra", "It Might As Well Be Swing", "https://i.scdn.co/image/ab67616d0000b273b19cb81319fbfd9ed54baeae", "https://open.spotify.com/track/2dR5WkrpwylTuT3jRWNufa", "https://www.youtube.com/watch?v=JYuyWrkwpok", 147146),
                        track("33jt3kYWjQzqn3xyYQ5ZEh", "Cheek to Cheek", "Ella Fitzgerald, Louis Armstrong", "Ella and Louis", "https://i.scdn.co/image/ab67616d0000b273a0b669c5273f36ffefcf1ebc", "https://open.spotify.com/track/33jt3kYWjQzqn3xyYQ5ZEh", "https://www.youtube.com/watch?v=20iOlPwz0J0", 354533),
                        track("29U7stRjqHU6rMiS8BfaI9", "What A Wonderful World", "Louis Armstrong", "What A Wonderful World", "https://i.scdn.co/image/ab67616d0000b273845a5660b804e5f3e821fbed", "https://open.spotify.com/track/29U7stRjqHU6rMiS8BfaI9", "https://www.youtube.com/watch?v=rBrd_3VMC3c", 139226),
                        track("75KRlncgTKRWd9CjqPZgcx", "Misty", "Erroll Garner", "Contrasts", "https://i.scdn.co/image/ab67616d0000b2737848871b11c831e866c1cee1", "https://open.spotify.com/track/75KRlncgTKRWd9CjqPZgcx", "https://www.youtube.com/watch?v=UOLEEkYO4MA", 169373)
                )),
                Map.entry("우주 오페라 발라드", trackList(
                        track("33LC84JgLvK2KuW43MfaNq", "My Heart Will Go On", "Celine Dion", "Let's Talk About Love", "https://i.scdn.co/image/ab67616d0000b273def7146ca744f3b1bf838404", "https://open.spotify.com/track/33LC84JgLvK2KuW43MfaNq", "https://www.youtube.com/watch?v=mNsm2P0l_7Y", 280000),
                        track("225xvV8r1yKMHErSWivnow", "I Don't Want to Miss a Thing", "Aerosmith", "Armageddon", "https://i.scdn.co/image/ab67616d0000b273da8d92affd796f7e20af7375", "https://open.spotify.com/track/225xvV8r1yKMHErSWivnow", "https://www.youtube.com/watch?v=WC6lEMYmxR8", 298760),
                        track("0gsl92EMIScPGV1AU35nuD", "All By Myself", "Celine Dion", "Falling Into You", "https://i.scdn.co/image/ab67616d0000b273c6aebd89b2dcda3348649633", "https://open.spotify.com/track/0gsl92EMIScPGV1AU35nuD", "https://www.youtube.com/watch?v=3p-rsl10t9I", 312306),
                        track("5kK1Iru9ogP3Iy1zsANU1n", "The Power of Love", "Celine Dion", "The Colour of My Love", "https://i.scdn.co/image/ab67616d0000b27363e07c2dbc7450974a146e96", "https://open.spotify.com/track/5kK1Iru9ogP3Iy1zsANU1n", "https://www.youtube.com/watch?v=8noF9djRNzI", 342400),
                        track("7wuJGgpTNzbUyn26IOY6rj", "Total Eclipse of the Heart", "Bonnie Tyler", "Faster Than the Speed of Night", "https://i.scdn.co/image/ab67616d0000b2738cefe8e2f2cfd63ce073fa96", "https://open.spotify.com/track/7wuJGgpTNzbUyn26IOY6rj", "https://www.youtube.com/watch?v=LSYGIljR6sQ", 267360),
                        track("0vB4Vd6PtkJSEnWsmqATnZ", "Nothing's Gonna Change My Love for You", "George Benson", "20/20", "https://i.scdn.co/image/ab67616d0000b273e71efa8c6cfc1f8b39b0bdab", "https://open.spotify.com/track/0vB4Vd6PtkJSEnWsmqATnZ", "https://www.youtube.com/watch?v=4tf0ZjN1UPE", 242678),
                        track("4LFwNJWoj74Yd71fIr1W8x", "Right Here Waiting", "Richard Marx", "Repeat Offender", "https://i.scdn.co/image/ab67616d0000b273edc27582d8f5c3a7c56893bf", "https://open.spotify.com/track/4LFwNJWoj74Yd71fIr1W8x", "https://www.youtube.com/watch?v=yW_vyiPWwq4", 264333),
                        track("63CHa6rmamv9OsehkRD8oz", "Against All Odds", "Phil Collins", "Against All Odds", "https://i.scdn.co/image/ab67616d0000b2736731eabe4c268971eeed3c06", "https://open.spotify.com/track/63CHa6rmamv9OsehkRD8oz", "https://www.youtube.com/watch?v=LYlJbc4brTk", 206360),
                        track("3xZMPZQYETEn4hjor3TR1A", "Angel", "Sarah McLachlan", "Surfacing", "https://i.scdn.co/image/ab67616d0000b2739469c3ab051994ec802574a6", "https://open.spotify.com/track/3xZMPZQYETEn4hjor3TR1A", "https://www.youtube.com/watch?v=Jd9irPMuxho", 270400),
                        track("6lanRgr6wXibZr8KgzXxBl", "A Thousand Years", "Christina Perri", "The Twilight Saga", "https://i.scdn.co/image/ab67616d0000b2733dea4a2ccd58ad1f8e4dbb03", "https://open.spotify.com/track/6lanRgr6wXibZr8KgzXxBl", "https://www.youtube.com/watch?v=rtOvBOTyX00", 285120)
                )),
                Map.entry("케이팝 네온 무대", trackList(
                        track("3r8RuvgbX9s7ammBn07D3W", "Ditto", "NewJeans", "OMG", "https://i.scdn.co/image/ab67616d0000b273edf5b257be1d6593e81bb45f", "https://open.spotify.com/track/3r8RuvgbX9s7ammBn07D3W", "https://www.youtube.com/watch?v=5emU4TSPxc8", 185506),
                        track("0a4MMyCrzT0En247IhqZbD", "Hype Boy", "NewJeans", "New Jeans", "https://i.scdn.co/image/ab67616d0000b2739d28fd01859073a3ae6ea209", "https://open.spotify.com/track/0a4MMyCrzT0En247IhqZbD", "https://www.youtube.com/watch?v=T--6HBX2K4g", 179026),
                        track("5sdQOyqq2IDhvmx2lHOpwd", "Super Shy", "NewJeans", "Get Up", "https://i.scdn.co/image/ab67616d0000b2733d98a0ae7c78a3a9babaf8af", "https://open.spotify.com/track/5sdQOyqq2IDhvmx2lHOpwd", "https://www.youtube.com/watch?v=n7ePZLn9_lQ", 154666),
                        track("56v8WEnGzLByGsDAXDiv4d", "ETA", "NewJeans", "Get Up", "https://i.scdn.co/image/ab67616d0000b2730744690248ef3ba7b776ea7b", "https://open.spotify.com/track/56v8WEnGzLByGsDAXDiv4d", "https://www.youtube.com/watch?v=2u_kncwzJ5Y", 151373),
                        track("18nZWRpJIHzgb1SQr4ncwb", "Supernova", "aespa", "Armageddon", "https://i.scdn.co/image/ab67616d0000b273115d1e2cfde4e387f0a13ce2", "https://open.spotify.com/track/18nZWRpJIHzgb1SQr4ncwb", "https://www.youtube.com/watch?v=vQkdt5txAcM", 178880),
                        track("0SE1qlvZVknGGKTwuXBPKz", "Drama", "aespa", "Drama", "https://i.scdn.co/image/ab67616d0000b273ae59604190fd4dcd891d5c69", "https://open.spotify.com/track/0SE1qlvZVknGGKTwuXBPKz", "https://www.youtube.com/watch?v=rmUxs2DM3zA", 214973),
                        track("0Q5VnK2DYzRyfqQRJuUtvi", "LOVE DIVE", "IVE", "LOVE DIVE", "https://i.scdn.co/image/ab67616d0000b2739016f58cc49e6473e1207093", "https://open.spotify.com/track/0Q5VnK2DYzRyfqQRJuUtvi", "https://www.youtube.com/watch?v=l-jZOXa7gQY", 177186),
                        track("6CV6j2xz54thzlrWML3kAW", "After LIKE", "IVE", "After LIKE", "https://i.scdn.co/image/ab67616d0000b2731abe22e6883e5a8b3f4726e2", "https://open.spotify.com/track/6CV6j2xz54thzlrWML3kAW", "https://www.youtube.com/watch?v=CVxTT38_J4c", 176973),
                        track("4fsQ0K37TOXa3hEQfjEic1", "ANTIFRAGILE", "LE SSERAFIM", "ANTIFRAGILE", "https://i.scdn.co/image/ab67616d0000b2739cfed1dd2d368a0855d7b1a4", "https://open.spotify.com/track/4fsQ0K37TOXa3hEQfjEic1", "https://www.youtube.com/watch?v=vsgdqdbQuc8", 184444),
                        track("68r87x3VZdAMhv8nBVuynz", "Queencard", "(G)I-DLE", "I feel", "https://i.scdn.co/image/ab67616d0000b27357de3da10da259d0a19a81b4", "https://open.spotify.com/track/68r87x3VZdAMhv8nBVuynz", "https://www.youtube.com/watch?v=JuJBr9v6BGA", 161240)
                )),
                Map.entry("다큐멘터리 포크 여행", trackList(
                        track("52vA3CYKZqZVdQnzRrdZt6", "The Times They Are A-Changin'", "Bob Dylan", "The Times They Are A-Changin'", "https://i.scdn.co/image/ab67616d0000b273b75cedd9435250e77b60bfbe", "https://open.spotify.com/track/52vA3CYKZqZVdQnzRrdZt6", "https://www.youtube.com/watch?v=90WD_ats6eE", 197020),
                        track("18GiV1BaXzPVYpp9rmOg0E", "Blowin' in the Wind", "Bob Dylan", "The Freewheelin' Bob Dylan", "https://i.scdn.co/image/ab67616d0000b273c7f7596cd80cbd6436086f80", "https://open.spotify.com/track/18GiV1BaXzPVYpp9rmOg0E", "https://www.youtube.com/watch?v=MMFj8uDubsE", 168756),
                        track("5ibNaVO251Pb2c0Wdl00gG", "Heartbeats", "Jose Gonzalez", "Veneer", "https://i.scdn.co/image/ab67616d0000b27376557bf2d3926bf5a607cd92", "https://open.spotify.com/track/5ibNaVO251Pb2c0Wdl00gG", "https://www.youtube.com/watch?v=ik_BQYbbZ5U", 160240),
                        track("3Kj2EWpIBnvETsYq4cq0IH", "Big Black Car", "Gregory Alan Isakov", "This Empty Northern Hemisphere", "https://i.scdn.co/image/ab67616d0000b27393a1519118102a6d78164713", "https://open.spotify.com/track/3Kj2EWpIBnvETsYq4cq0IH", "https://www.youtube.com/watch?v=JgumMOMHpns", 217466),
                        track("4X6aeSW2bhlbm7YyEstj6Y", "Rivers and Roads", "The Head and the Heart", "The Head and the Heart", "https://i.scdn.co/image/ab67616d0000b273eb1755811f99b211e44baa15", "https://open.spotify.com/track/4X6aeSW2bhlbm7YyEstj6Y", "https://www.youtube.com/watch?v=mL5eKMUKWaw", 284000),
                        track("0DwClY2t9YAWHBROMIgrXb", "Ho Hey", "The Lumineers", "The Lumineers", "https://i.scdn.co/image/ab67616d0000b273f350c13195ce048876b5fea4", "https://open.spotify.com/track/0DwClY2t9YAWHBROMIgrXb", "https://www.youtube.com/watch?v=zvCBSSwgtg4", 163133),
                        track("14AyWf6y7KlWWLfAjdKMKI", "Ophelia", "The Lumineers", "Cleopatra", "https://i.scdn.co/image/ab67616d0000b27321b550b66cf1391c6642088c", "https://open.spotify.com/track/14AyWf6y7KlWWLfAjdKMKI", "https://www.youtube.com/watch?v=pTOC_q0NLTk", 160097),
                        track("0eBryM7ePQH3Klt3jz8xZd", "First Day of My Life", "Bright Eyes", "I'm Wide Awake, It's Morning", "https://i.scdn.co/image/ab67616d0000b2738eded59eb143ee6000a77c62", "https://open.spotify.com/track/0eBryM7ePQH3Klt3jz8xZd", "https://www.youtube.com/watch?v=pDm7xWsf3VM", 188877),
                        track("3B3eOgLJSqPEA0RfboIQVM", "Skinny Love", "Bon Iver", "For Emma, Forever Ago", "https://i.scdn.co/image/ab67616d0000b273bf7c317a63c4f128b8823406", "https://open.spotify.com/track/3B3eOgLJSqPEA0RfboIQVM", "https://www.youtube.com/watch?v=95FyXUHv8hk", 238520),
                        track("35KiiILklye1JRRctaLUb4", "Holocene", "Bon Iver", "Bon Iver", "https://i.scdn.co/image/ab67616d0000b2734b6b1547455bbecb9f6bba64", "https://open.spotify.com/track/35KiiILklye1JRRctaLUb4", "https://www.youtube.com/watch?v=MjxA25Tj1Ks", 336613)
                )),
                Map.entry("새벽 힙합 작업실", trackList(
                        track("2E7oJceSDSnQ63jD7xEPHA", "회전목마", "sokodomo", "쇼미더머니 10 Episode 2", "https://i.scdn.co/image/ab67616d0000b273663a62907b2ec965fa359d14", "https://open.spotify.com/track/2E7oJceSDSnQ63jD7xEPHA", "https://www.youtube.com/watch?v=3laLavGghc0", 250070),
                        track("2Zr0bYRHwWXi7eM2wuE6Aj", "VVS", "Mirani, Munchman, Khundi Panda, MUSHVENOM", "쇼미더머니 9 Episode 1", "https://i.scdn.co/image/ab67616d0000b2734fc43bf4163c96eca2c640d8", "https://open.spotify.com/track/2Zr0bYRHwWXi7eM2wuE6Aj", "https://www.youtube.com/watch?v=hq9hcJIzB6w", 335253),
                        track("4g6XOg9rvB55GCTJcYchOG", "METEOR", "CHANGMO", "Boyhood", "https://i.scdn.co/image/ab67616d0000b273e0891e322bc2773b1e4389c2", "https://open.spotify.com/track/4g6XOg9rvB55GCTJcYchOG", "https://www.youtube.com/watch?v=vjl_uRTeOfU", 197096),
                        track("7GqAKBYPHaiLleRPu8z2op", "BAND", "CHANGMO, Hash Swan, ASH ISLAND, Keem Hyo-Eun", "BAND", "https://i.scdn.co/image/ab67616d0000b273674c883dd862de92cdaae074", "https://open.spotify.com/track/7GqAKBYPHaiLleRPu8z2op", "https://www.youtube.com/watch?v=bsgBUM2Mnsw", 245874),
                        track("1R6qeTvAvaxB7hydIYVIY8", "Okey Dokey", "MINO, ZICO", "쇼미더머니 4 Episode 6", "https://i.scdn.co/image/ab67616d0000b27395768aaeb6607d53450a527b", "https://open.spotify.com/track/1R6qeTvAvaxB7hydIYVIY8", "https://www.youtube.com/watch?v=CJnKrgTdKZY", 248202),
                        track("2SMq0lOqCTHayWa9juoI0d", "We Are", "Woo, Loco, GRAY", "쇼미더머니 6 Episode 1", "https://i.scdn.co/image/ab67616d0000b27380af1bc4fa047ba0e2f17c04", "https://open.spotify.com/track/2SMq0lOqCTHayWa9juoI0d", "https://www.youtube.com/watch?v=RsHq6Q-7NsU", 196020),
                        track("3i6vV5Z2P9uMlxf8wdm2WK", "Day Day", "BewhY, Jay Park", "쇼미더머니 5 Episode 4", "https://i.scdn.co/image/ab67616d0000b27302cab4a45074b259fc48c565", "https://open.spotify.com/track/3i6vV5Z2P9uMlxf8wdm2WK", "https://www.youtube.com/watch?v=AMWOLv4Y_0Y", 209019),
                        track("5EpnSlOddutjRCIqo77JBG", "Bermuda Triangle", "ZICO, Crush, DEAN", "Bermuda Triangle", "https://i.scdn.co/image/ab67616d0000b273cafdd0c927186e1212551e6e", "https://open.spotify.com/track/5EpnSlOddutjRCIqo77JBG", "https://www.youtube.com/watch?v=MPU2luc_lzQ", 207026),
                        track("3b80IqPWhMgFA4RXFQLpFz", "Achoo", "Mirani, pH-1, HAON", "Achoo", "https://i.scdn.co/image/ab67616d0000b2732713fe4f19a87fd306054664", "https://open.spotify.com/track/3b80IqPWhMgFA4RXFQLpFz", "https://www.youtube.com/watch?v=zt1zcLJbftw", 236106),
                        track("5oxmx6B0kWTuCKgBzv8NpH", "IndiGO", "JUSTHIS, Kid Milli, NO:EL, Young B", "IM", "https://i.scdn.co/image/ab67616d0000b273eeb3f8447e06c79cbc7a381c", "https://open.spotify.com/track/5oxmx6B0kWTuCKgBzv8NpH", "https://www.youtube.com/watch?v=jd2DxTR0znU", 240502)
                )),
                Map.entry("던전 메탈 질주", trackList(
                        track("2MuWTIM3b0YEAskbeeFE1i", "Master of Puppets", "Metallica", "Master of Puppets", "https://i.scdn.co/image/ab67616d0000b273668e3aca3167e6e569a9aa20", "https://open.spotify.com/track/2MuWTIM3b0YEAskbeeFE1i", "https://www.youtube.com/watch?v=E0ozmU9cJDg", 515386),
                        track("3DwQ7AH3xGD9h65ezslm6q", "Enter Sandman", "Metallica", "Metallica", "https://i.scdn.co/image/ab67616d0000b273c1a13209dfe146aef3296e34", "https://open.spotify.com/track/3DwQ7AH3xGD9h65ezslm6q", "https://www.youtube.com/watch?v=XZuM4zFg-60", 331560),
                        track("3hwuHzRicnu6Ji9i1JLzor", "Paranoid", "Black Sabbath", "Paranoid", "https://i.scdn.co/image/ab67616d0000b273174f71bdda81ecb2eddba01b", "https://open.spotify.com/track/3hwuHzRicnu6Ji9i1JLzor", "https://www.youtube.com/watch?v=BOTIIw76qiE", 168626),
                        track("74fURW8XNuLYHOvKmjA2XT", "Iron Man", "Black Sabbath", "Paranoid", "https://i.scdn.co/image/ab67616d0000b273174f71bdda81ecb2eddba01b", "https://open.spotify.com/track/74fURW8XNuLYHOvKmjA2XT", "https://www.youtube.com/watch?v=b3-QqGVt-tM", 355306),
                        track("0L7zm6afBEtrNKo6C6Gj08", "Painkiller", "Judas Priest", "Painkiller", "https://i.scdn.co/image/ab67616d0000b27360db4ca924d17bc6754e89aa", "https://open.spotify.com/track/0L7zm6afBEtrNKo6C6Gj08", "https://www.youtube.com/watch?v=oSetzjSKb8U", 365826),
                        track("3CIOopLwvyMvXk97ZEksKO", "Ace of Spades", "Motorhead", "Ace of Spades", "https://i.scdn.co/image/ab67616d0000b273cb92df4d1fdbafa4a6e18855", "https://open.spotify.com/track/3CIOopLwvyMvXk97ZEksKO", "https://www.youtube.com/watch?v=86Iwytfa6ms", 168320),
                        track("4Zc7TCHzuNwL0AFBlyLdyr", "Run to the Hills", "Iron Maiden", "The Number of the Beast", "https://i.scdn.co/image/ab67616d0000b2735c29a88ba5341ca428f0c322", "https://open.spotify.com/track/4Zc7TCHzuNwL0AFBlyLdyr", "https://www.youtube.com/watch?v=tyhr0n3v5EU", 233506),
                        track("3JlZD0amss9ftlE836VKx5", "The Trooper", "Iron Maiden", "Piece of Mind", "https://i.scdn.co/image/ab67616d0000b27368fe324f22bf71d65a16aac6", "https://open.spotify.com/track/3JlZD0amss9ftlE836VKx5", "https://www.youtube.com/watch?v=FjFzSfN6W6w", 251946),
                        track("2DlHlPMa4M17kufBvI2lEN", "Chop Suey!", "System Of A Down", "Toxicity", "https://i.scdn.co/image/ab67616d0000b27307bc7d2a745636c356b4d0aa", "https://open.spotify.com/track/2DlHlPMa4M17kufBvI2lEN", "https://www.youtube.com/watch?v=GK-DkHHAMIU", 210240),
                        track("61mWefnWQOLf90gepjOCb3", "Duality", "Slipknot", "Vol. 3", "https://i.scdn.co/image/ab67616d0000b2736b3463e7160d333ada4b175a", "https://open.spotify.com/track/61mWefnWQOLf90gepjOCb3", "https://www.youtube.com/watch?v=3GWwohI5h1w", 252613)
                )),
                Map.entry("복고 트로트 야시장", trackList(
                        track("2RPtul5DOEFkmkcnfauxZF", "찐이야", "영탁", "내일은 미스터트롯", "https://i.scdn.co/image/ab67616d0000b273057f2bba936216cae1ccc2cf", "https://open.spotify.com/track/2RPtul5DOEFkmkcnfauxZF", "https://www.youtube.com/watch?v=SiQzf2MyHl0", 190563),
                        track("4zCcKPm03kHARVAiyzlDX8", "이제 나만 믿어요", "임영웅", "내일은 미스터트롯", "https://i.scdn.co/image/ab67616d0000b273e76a3b26a23718f8772bb6fa", "https://open.spotify.com/track/4zCcKPm03kHARVAiyzlDX8", "https://www.youtube.com/watch?v=7qki5-JzF18", 242786),
                        track("4Ok2SrteG9PvK6x3Sye8GY", "막걸리 한잔", "영탁", "내일은 미스터트롯", "https://i.scdn.co/image/ab67616d0000b273e3971fe4745dff30ab50a32e", "https://open.spotify.com/track/4Ok2SrteG9PvK6x3Sye8GY", "https://www.youtube.com/watch?v=QOlIpphN_XQ", 234333),
                        track("2vtMPWxIaEDOKcHmLPkP1b", "보릿고개", "진성", "보릿고개", "https://i.scdn.co/image/ab67616d0000b273e72f2425871f77dbe56c66a6", "https://open.spotify.com/track/2vtMPWxIaEDOKcHmLPkP1b", "https://www.youtube.com/watch?v=7Xyjjl6Vf6g", 208053),
                        track("2l8cP9ccodfkRwS6Oz5Tlb", "안동역에서", "진성", "안동역에서", "https://i.scdn.co/image/ab67616d0000b273831e502abff91f6b32d6381d", "https://open.spotify.com/track/2l8cP9ccodfkRwS6Oz5Tlb", "https://www.youtube.com/watch?v=-kpve_FSzc4", 215524),
                        track("77U7ZuVAuhGIrCA8UPZ2kg", "어머나", "장윤정", "어머나", "https://i.scdn.co/image/ab67616d0000b273f64eafa59f0c81d0228407e0", "https://open.spotify.com/track/77U7ZuVAuhGIrCA8UPZ2kg", "https://www.youtube.com/watch?v=w5bH9buaoFY", 192511),
                        track("6eJBDAZcQDVvxkxqulNY4V", "초혼", "장윤정", "초혼", "https://i.scdn.co/image/ab67616d0000b27314789bce1d29aee1f5770046", "https://open.spotify.com/track/6eJBDAZcQDVvxkxqulNY4V", "https://www.youtube.com/watch?v=7c1h5D_eoJ8", 227683),
                        track("6z8NrCMOM2Y6nJrCfAyuhK", "사랑의 배터리", "홍진영", "사랑의 배터리", "https://i.scdn.co/image/ab67616d0000b273ad6c78bf75c63c2fdba1093d", "https://open.spotify.com/track/6z8NrCMOM2Y6nJrCfAyuhK", "https://www.youtube.com/watch?v=0-1rdQXHadw", 242234),
                        track("3hof21ImLz7igyLGgGfAZK", "무조건", "박상철", "무조건", "https://i.scdn.co/image/ab67616d0000b27300f9adef393d009f10e67583", "https://open.spotify.com/track/3hof21ImLz7igyLGgGfAZK", "https://www.youtube.com/watch?v=G_KNxk_q8PU", 216506),
                        track("41nfWl180LzwBDHb3iQIIa", "곤드레 만드레", "박현빈", "곤드레 만드레", "https://i.scdn.co/image/ab67616d0000b27368fbf8e2e1d42fe96099057f", "https://open.spotify.com/track/41nfWl180LzwBDHb3iQIIa", "https://www.youtube.com/watch?v=tyLqhRR41pw", 253257)
                )),
                Map.entry("판타지 오케스트라 정원", trackList(
                        track("1ghlpxVfPbFH2jenrv9vVw", "Duel of the Fates", "John Williams", "Star Wars", "https://i.scdn.co/image/ab67616d0000b2738b344822c35025ba9439f004", "https://open.spotify.com/track/1ghlpxVfPbFH2jenrv9vVw", "https://www.youtube.com/watch?v=nIgZS5tieeE", 254346),
                        track("1n8NKQRg8LVHy7oUhUgbFF", "Hedwig's Theme", "John Williams", "Harry Potter", "https://i.scdn.co/image/ab67616d0000b27313f982aa5c43146c3d2c1964", "https://open.spotify.com/track/1n8NKQRg8LVHy7oUhUgbFF", "https://www.youtube.com/watch?v=wtHra9tFISY", 309093),
                        track("644es5aYPJghtZLjM1rmSP", "Concerning Hobbits", "Howard Shore", "The Lord of the Rings", "https://i.scdn.co/image/ab67616d0000b273128ca6b63d83d47c909a43ce", "https://open.spotify.com/track/644es5aYPJghtZLjM1rmSP", "https://www.youtube.com/watch?v=CL_3mlOPnGI", 175040),
                        track("1ykbtFnlIjmIFnZ8j6wg6i", "The Breaking of the Fellowship", "Howard Shore", "The Lord of the Rings", "https://i.scdn.co/image/ab67616d0000b273128ca6b63d83d47c909a43ce", "https://open.spotify.com/track/1ykbtFnlIjmIFnZ8j6wg6i", "https://www.youtube.com/watch?v=fowHzOH9rqk", 440800),
                        track("6ZFbXIJkuI1dVNWvzJzown", "Time", "Hans Zimmer", "Inception", "https://i.scdn.co/image/ab67616d0000b2735327620df3029a04646914c1", "https://open.spotify.com/track/6ZFbXIJkuI1dVNWvzJzown", "https://www.youtube.com/watch?v=c56t7upa8Bk", 275556),
                        track("6pWgRkpqVfxnj3WuIcJ7WP", "Cornfield Chase", "Hans Zimmer", "Interstellar", "https://i.scdn.co/image/ab67616d0000b2730310d2e254062d98b2a92b2d", "https://open.spotify.com/track/6pWgRkpqVfxnj3WuIcJ7WP", "https://www.youtube.com/watch?v=JuSsvM8B4Jc", 126959),
                        track("1elGwF4VwkwglV4nCBPJtv", "Now We Are Free", "Hans Zimmer, Lisa Gerrard", "Gladiator", "https://i.scdn.co/image/ab67616d0000b273f3a2d7f692fcad25284c5f1e", "https://open.spotify.com/track/1elGwF4VwkwglV4nCBPJtv", "https://www.youtube.com/watch?v=ghxzLw2wRis", 254293),
                        track("08QaHlMPWuO5PUxjl61bXn", "He's a Pirate", "Klaus Badelt", "Pirates of the Caribbean", "https://i.scdn.co/image/ab67616d0000b27338786c7492ac252797bb2648", "https://open.spotify.com/track/08QaHlMPWuO5PUxjl61bXn", "https://www.youtube.com/watch?v=BuYf0taXoNw", 90426),
                        track("6PrKZUXJPmBiobMN44yR8Y", "The Ecstasy of Gold", "Ennio Morricone", "The Good, the Bad and the Ugly", "https://i.scdn.co/image/ab67616d0000b27388eb37784eee4c3651807406", "https://open.spotify.com/track/6PrKZUXJPmBiobMN44yR8Y", "https://www.youtube.com/watch?v=cCEwUFtjAr4", 203226),
                        track("5lR6LEIhZ4iKK8t0DC8mge", "Main Theme", "Ramin Djawadi", "Game of Thrones", "https://i.scdn.co/image/ab67616d0000b2739d53f064a179aede404118db", "https://open.spotify.com/track/5lR6LEIhZ4iKK8t0DC8mge", "https://www.youtube.com/watch?v=uXZd_W5B7N0", 104453)
                )),
                Map.entry("해변 레게 석양", trackList(
                        track("7vggqxNKwd6xdRoYS0pQtM", "Three Little Birds", "Bob Marley & The Wailers", "Exodus", "https://i.scdn.co/image/ab67616d0000b273413a6c2c7b296d98171e5e21", "https://open.spotify.com/track/7vggqxNKwd6xdRoYS0pQtM", "https://www.youtube.com/watch?v=NOyRsPDPfMM", 180440),
                        track("5O4erNlJ74PIF6kGol1ZrC", "Could You Be Loved", "Bob Marley & The Wailers", "Uprising", "https://i.scdn.co/image/ab67616d0000b2731c40418d1c37d727e8e91b04", "https://open.spotify.com/track/5O4erNlJ74PIF6kGol1ZrC", "https://www.youtube.com/watch?v=uf9DjrIEEwc", 237000),
                        track("6JRLFiX9NJSoRRKxowlBYr", "Is This Love", "Bob Marley & The Wailers", "Kaya", "https://i.scdn.co/image/ab67616d0000b273387799441ba867649dfbb702", "https://open.spotify.com/track/6JRLFiX9NJSoRRKxowlBYr", "https://www.youtube.com/watch?v=co2FK0WbXX0", 232200),
                        track("5ktLiFRny1waEnVkZNUa03", "One Love", "Bob Marley & The Wailers", "Exodus", "https://i.scdn.co/image/ab67616d0000b273ca5561a4b47e91dfb358eca3", "https://open.spotify.com/track/5ktLiFRny1waEnVkZNUa03", "https://www.youtube.com/watch?v=IN0KkGeEURw", 202160),
                        track("4uOKFydzAejjSFqYbv1XPt", "Red Red Wine", "UB40", "Labour of Love", "https://i.scdn.co/image/ab67616d0000b273f1dd69d7399290cc25324706", "https://open.spotify.com/track/4uOKFydzAejjSFqYbv1XPt", "https://www.youtube.com/watch?v=UidFA-dP8GQ", 183733),
                        track("1YnVYHXoak0syv4xBRHQRb", "Kingston Town", "UB40", "Labour of Love II", "https://i.scdn.co/image/ab67616d0000b27328fc1c4ca395c4a2ffa31a47", "https://open.spotify.com/track/1YnVYHXoak0syv4xBRHQRb", "https://www.youtube.com/watch?v=2UOY1JeFvAw", 231733),
                        track("1nXTM2CVdaoCySSD5kgt3J", "Sweat", "Inner Circle", "Bad Boys", "https://i.scdn.co/image/ab67616d0000b2739d17f31f7b76ad8ec88a4417", "https://open.spotify.com/track/1nXTM2CVdaoCySSD5kgt3J", "https://www.youtube.com/watch?v=P-i4oRmNbb0", 229000),
                        track("1NojrDCqDLh4dWRZ7F589Q", "Bad Boys", "Inner Circle", "Bad Boys", "https://i.scdn.co/image/ab67616d0000b273585c316e217aca06ec83f3b6", "https://open.spotify.com/track/1NojrDCqDLh4dWRZ7F589Q", "https://www.youtube.com/watch?v=2bOUjNKjZ4s", 229374),
                        track("1ti85OealKe0Yq23ws9n0e", "Here I Am", "UB40", "Labour of Love II", "https://i.scdn.co/image/ab67616d0000b273c773360a7cefe9c8dd6db0b5", "https://open.spotify.com/track/1ti85OealKe0Yq23ws9n0e", "https://www.youtube.com/watch?v=fE_1k2Gw8pw", 242040),
                        track("3PQLYVskjUeRmRIfECsL0X", "No Woman, No Cry", "Bob Marley & The Wailers", "Natty Dread", "https://i.scdn.co/image/ab67616d0000b273b5a0ee94e2741374ce5c71a2", "https://open.spotify.com/track/3PQLYVskjUeRmRIfECsL0X", "https://www.youtube.com/watch?v=IT8XvzIfi4U", 226240)
                ))
        );
    }

    private List<TrackSeed> trackList(TrackSeed... tracks) {
        return Arrays.asList(tracks);
    }

    private TrackSeed track(String spotifyTrackId, String title, String artistName, String albumName, long durationMs) {
        String query = encode(title + " " + artistName);
        return track(
                "demo-" + spotifyTrackId,
                title,
                artistName,
                albumName,
                "https://picsum.photos/seed/" + spotifyTrackId + "/640/640",
                "https://open.spotify.com/search/" + query,
                "https://www.youtube.com/results?search_query=" + query,
                durationMs
        );
    }

    private TrackSeed track(String spotifyTrackId, String title, String artistName, String albumName,
                            String albumImageUrl, String spotifyUrl, String youtubeUrl, long durationMs) {
        return new TrackSeed(
                spotifyTrackId,
                title,
                artistName,
                albumName,
                albumImageUrl,
                spotifyUrl,
                youtubeUrl,
                durationMs
        );
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record UserSeed(String email, String password, String nickname) {
    }

    private record PlaylistSeed(String email, String title, String description, String coverImageUrl, List<String> tags) {
    }

    private record TrackSeed(String spotifyTrackId, String title, String artistName, String albumName,
                             String albumImageUrl, String spotifyUrl, String youtubeUrl, Long durationMs) {
    }
}
