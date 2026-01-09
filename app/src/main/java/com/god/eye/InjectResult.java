package com.god.eye;

/**
 * InjectResult
 *
 * UI 层数据模型
 * 表示一条“可疑注入调用”
 */
public class InjectResult {

    /** 调用方类（smali 形式） */
    public final String callerClass;

    /** 被调用方类（smali 形式） */
    public final String targetClass;

    /** 原始 smali invoke 行 */
    public final String smali;

    public InjectResult(String callerClass,
                        String targetClass,
                        String smali) {
        this.callerClass = callerClass;
        this.targetClass = targetClass;
        this.smali = smali;
    }
}
