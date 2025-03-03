package com.video.core.service.user;

import java.util.Map;

public interface UserSimilarityService {
    
    Map<Integer, Map<Integer, Double>> calculateSimilarity();
}
