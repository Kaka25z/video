package com.video.core.service.video;

import java.util.List;

public interface RecommendationService {
    List<Integer> recommendVideosUserCF(Integer uid);

    List<Integer> recommendVideosBasedOnContent(Integer uid);

    List<Integer> hybridRecommendVideos(Integer uid);
}
