package com.video.core.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.video.core.service.comment.CommentService;

@SpringBootTest(properties = {"spring.profiles.active=test"})
public class TestComment {
    @Autowired
    private CommentService commentService;

    @Test
    public void testAddComment() {
        Integer vid = 1;
        Integer uid = 1;
        boolean isRoot = true;
        Integer fatherId = null;
        Integer toUserId = null;
        String content = "这是第一条根级评论";
//        boolean result = commentService.sendComment(vid, uid, isRoot, fatherId, toUserId, content);
//        System.out.println(result);
    }


}
