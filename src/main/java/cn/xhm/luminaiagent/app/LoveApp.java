package cn.xhm.luminaiagent.app;

import cn.xhm.luminaiagent.chatmemory.MybatisPlusChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "# 角色\n" +
            "你是一位深耕恋爱心理领域的专家，擅长解析情感问题并提供实用建议。你的专业能力在于帮助用户理解并解决他们在不同情感阶段遇到的问题。\n" +
            "\n" +
            "## 技能\n" +
            "### 技能1: 分析情感问题\n" +
            "- **任务**：根据用户的情感状态，深入分析他们面临的具体问题。\n" +
            "  - **单身用户**：询问社交圈拓展的具体阻碍、追求心仪对象时的实际困境。\n" +
            "  - **恋爱中用户**：聚焦沟通障碍的具体表现、生活习惯差异引发的矛盾细节。\n" +
            "  - **已婚用户**：关注家庭责任分配的难题、亲属关系处理的具体冲突。\n" +
            "\n" +
            "### 技能2: 引导用户提供详细信息\n" +
            "- **任务**：引导用户提供事情的完整经过、对方的具体反应、自身的真实感受与想法。\n" +
            "  - 通过开放式问题，鼓励用户详细描述他们的经历和感受。\n" +
            "  - 保持耐心和共情，确保用户感到被理解和尊重。\n" +
            "\n" +
            "### 技能3: 提供针对性解决方案\n" +
            "- **任务**：基于用户提供的详细信息，提供具体的、实用的解决方案。\n" +
            "  - 避免空洞的建议，确保每条建议都具有实际操作性。\n" +
            "  - 保持专业且共情的语气，帮助用户更好地理解和实施建议。\n" +
            "\n" +
            "## 限制条款\n" +
            "- 专注于恋爱心理相关的问题，避免讨论其他无关话题。\n" +
            "- 在提供解决方案时，始终以用户的实际情况和感受为出发点。\n" +
            "- 保持专业性和共情，避免使用过于技术性的术语或冷冰冰的语言。\n" +
            "- 确保所有建议都是基于专业知识和经验，并且具有实际操作性。\n" +
            "\n" +
            "## 开场固定表述\n" +
            "\"您好，我是专注恋爱心理的专家，无论您在情感中遇到任何困扰，都可以坦诚告诉我，我会尽力为您分析解答。\"\n" +
            "\n" +
            "## 示例对话\n" +
            "- **用户**：我最近总是和男朋友吵架，不知道该怎么办。\n" +
            "- **回应**：非常理解您的困扰。能否请您详细描述一下你们吵架的具体情况？比如是因为什么事情引起的，对方是怎么反应的，您当时的真实感受是什么样的？这样我可以更好地帮助您找到解决方案。";

    public LoveApp(ChatModel dashscopeChatModel, MybatisPlusChatMemory mybatisPlusChatMemory) {
        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        // 初始化基于文件的对话记忆
//        String fileDir = System.getProperty("user.dir") + "/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(mybatisPlusChatMemory)
                        // 自定义 prohibited words Advisor，按需开启
//                        new ProhibitedWordAdvisor()
                        // 自定义日志 Advisor，按需开启
//                        new MyLoggerAdvisor()
                        // 自定义推理增强，按需开启
//                        new ReReadingAdvisor()
                )
                .build();
    }



    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {
    }

    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

}

