package cn.xhm.luminaiagent.mapper;

import cn.xhm.luminaiagent.domain.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 聊天消息表 Mapper 接口
 * </p>
 *
 * @author xiehm
 * @since 2025-08-13
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

}
