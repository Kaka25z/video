package com.video.core.service.comment;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.video.core.entity.Comment;
import com.video.core.entity.CommentTree;
import com.video.core.entity.CustomResponse;

public interface CommentService {
    List<CommentTree> getCommentTreeByVid(Integer vid, Long offset, Integer type);

    CommentTree sendComment(Integer vid, Integer uid, Integer rootId, Integer parentId, Integer toUserId, String content);

    CustomResponse deleteComment(Integer id, Integer uid, boolean isAdmin);

    List<Comment> getChildCommentsByRootId(Integer rootId, Integer vid, Long start, Long stop);

    List<Comment> getRootCommentsByVid(Integer vid, Long offset, Integer type);

    CommentTree getMoreCommentsById(Integer id);

    /*
    评论点赞点踩相关
     */
    void updateLikeAndDisLike(Integer id, boolean addLike);

    void updateComment(Integer id, String column, boolean incr, Integer count);

    List<Map<String, Object>> getAllComments(Integer page, Integer quantity);

    List<Map<String, Object>> getCommentsByUid(Integer uid, Integer page, Integer quantity);
}
