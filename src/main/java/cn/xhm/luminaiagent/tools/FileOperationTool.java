package cn.xhm.luminaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.xhm.luminaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具类
 */
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    /**
     * 读取文件内容
     *
     * @param filePath 文件路径
     * @return 文件内容
     */
    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String filePath) {
        try {
            return FileUtil.readUtf8String(FILE_DIR + "/" + filePath);
        } catch (IORuntimeException e) {
            // 读取文件失败
            return "Error reading file: " + e.getMessage();
        }
    }

    /**
     * 写入文件内容
     *
     * @param fileName 文件路径
     * @param content  文件内容
     * @return 文件写入结果
     */
    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of a file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to: " + filePath;
        } catch (IORuntimeException e) {
            // 写入文件失败
            return "Error writing to file: " + e.getMessage();
        }
    }

}
