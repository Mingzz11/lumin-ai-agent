package cn.xhm.luminaiagent.service.impl;

import cn.xhm.luminaiagent.domain.ChatMessage;
import cn.xhm.luminaiagent.mapper.ChatMessageMapper;
import cn.xhm.luminaiagent.service.IChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天消息表 服务实现类
 * </p>
 *
 * @author xiehm
 * @since 2025-08-13
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

}
