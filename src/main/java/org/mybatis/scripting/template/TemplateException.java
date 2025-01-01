package org.mybatis.scripting.template;

/**
 * 模板异常类
 */
public class TemplateException extends RuntimeException {

    public TemplateException(Throwable throwable) {
        super(throwable);
    }

    public TemplateException(String message) {
        super(message);
    }

    public static TemplateException wrap(Throwable throwable) {
        if (throwable instanceof TemplateException) {
            return (TemplateException) throwable;
        }
        return new TemplateException(throwable);
    }
}
