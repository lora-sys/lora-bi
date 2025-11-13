package com.lora.bi.controller;

import com.lora.bi.common.BaseResponse;
import com.lora.bi.common.ErrorCode;
import com.lora.bi.common.ResultUtils;
import com.lora.bi.exception.BusinessException;
import com.lora.bi.model.dto.postthumb.PostThumbAddRequest;
import com.lora.bi.model.entity.User;
import com.lora.bi.service.PostThumbService;
import com.lora.bi.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子点赞接口
 *
 * @param postThumbAddRequest
 * @param request
 * @author lora
 * <p>
 * <p>
 * /**
 * 点赞 / 取消点赞
 * @return resultNum 本次点赞变化数
 */
@RestController
@RequestMapping("/post_thumb")
@Slf4j
public class PostThumbController {
    @Resource
    private PostThumbService postThumbService;
    @Resource
    private UserService userService;
    @PostMapping("/")
    public BaseResponse<Integer> doThumb(@RequestBody PostThumbAddRequest postThumbAddRequest,
                                         HttpServletRequest request) {
        if (postThumbAddRequest == null || postThumbAddRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long postId = postThumbAddRequest.getPostId();
        int result = postThumbService.doPostThumb(postId, loginUser);
        return ResultUtils.success(result);
    }

}
