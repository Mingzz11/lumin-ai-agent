package cn.xhm.luminaiagent.exception;

/**
 * 违禁词异常
 */
public class ProhibitedWordException extends RuntimeException {
    public ProhibitedWordException(String message) {
        super(message);
    }
}
