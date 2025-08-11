package cn.xhm.luminaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SpringAiInvoke implements CommandLineRunner
{
    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("springai调用:" + dashscopeChatModel.call(new Prompt("你好，我是小明")).getResult().getOutput().getText());
    }
}
