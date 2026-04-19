package com.guatai.yuntukubackend.manger.websocket.disruptor;

import com.guatai.yuntukubackend.manger.websocket.model.PictureEditRequestMessage;
import com.guatai.yuntukubackend.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * ClassName: a
 * Package: com.guatai.yuntukubackend.manger.websocket.disruptor
 * Description:
 *处理消息所需的上下文容器
 */
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片 id
     */
    private Long pictureId;

}

