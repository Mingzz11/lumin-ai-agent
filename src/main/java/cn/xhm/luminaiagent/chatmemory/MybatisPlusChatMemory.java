package cn.xhm.luminaiagent.chatmemory;

import cn.xhm.luminaiagent.domain.ChatMessage;
import cn.xhm.luminaiagent.service.IChatMessageService;
import cn.xhm.luminaiagent.util.MessageConverter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MybatisPlusChatMemory implements ChatMemory {

    private final IChatMessageService chatMessageService;

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<ChatMessage> chatMessages  = messages.stream()
                .map(message -> MessageConverter.toChatMessage(message, conversationId))
                .toList();
        chatMessageService.saveBatch(chatMessages, chatMessages.size());
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        LambdaQueryWrapper<ChatMessage> queryWrapper =
                Wrappers.lambdaQuery(ChatMessage.class)
                        .eq(ChatMessage::getConversationId, conversationId)
                        .orderByDesc(ChatMessage::getCreateTime)
                        .last(lastN > 0, "LIMIT " + lastN);

        List<ChatMessage> chatMessages = chatMessageService.list(queryWrapper);

        // 按照时间顺序返回
        if(!chatMessages.isEmpty()) {
            Collections.reverse(chatMessages);
        }

        return chatMessages.stream()
                .map(MessageConverter::toMessage)
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = Wrappers.lambdaQuery(ChatMessage.class)
                .eq(ChatMessage::getConversationId, conversationId);
        chatMessageService.remove(queryWrapper);
    }
}
