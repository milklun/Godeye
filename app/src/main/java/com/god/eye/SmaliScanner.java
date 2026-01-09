package com.god.eye;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.MethodReference;

import java.util.ArrayList;
import java.util.List;

/**
 * SmaliScanner
 *
 * 核心职责：
 * - 扫描 dex
 * - 找出“自己包 -> 外来包”的 invoke 调用
 * - 输出 InjectResult
 */
public class SmaliScanner {

    /**
     * 扫描单个 dex
     *
     * @param dexData dex 原始字节
     * @param appPkg  APK 主包名（如 com.xiaomi.vlive）
     */
    public static List<InjectResult> scanDex(byte[] dexData, String appPkg)
            throws Exception {

        List<InjectResult> results = new ArrayList<>();

        String appPrefix = "L" + appPkg.replace('.', '/') + "/";

        DexBackedDexFile dexFile =
                DexBackedDexFile.fromInputStream(
                        Opcodes.getDefault(),
                        new java.io.ByteArrayInputStream(dexData)
                );

        for (DexBackedClassDef cls : dexFile.getClasses()) {

            String callerClass = cls.getType();

            // 只分析自己包内的类
            if (!callerClass.startsWith(appPrefix)) {
                continue;
            }

            for (DexBackedMethod method : cls.getMethods()) {

                if (method.getImplementation() == null) {
                    continue;
                }

                for (Instruction insn : method.getImplementation().getInstructions()) {

                    // 只关心 invoke 指令
                    if (!(insn instanceof ReferenceInstruction)) {
                        continue;
                    }

                    if (!(insn.getOpcode().name().startsWith("INVOKE"))) {
                        continue;
                    }

                    MethodReference mr =
                            (MethodReference) ((ReferenceInstruction) insn).getReference();

                    String targetClass = mr.getDefiningClass();

                    // 过滤系统 / 标准库
                    if (isSystemClass(targetClass)) {
                        continue;
                    }

                    // 过滤自己包内调用
                    if (targetClass.startsWith(appPrefix)) {
                        continue;
                    }

                    // ===== 命中：跨包 invoke =====

                    String smaliLine =
                            insn.getOpcode().name().toLowerCase() +
                            " ... , " +
                            targetClass +
                            "->" +
                            mr.getName() +
                            mr.getParameterTypes().toString();

                    results.add(
                            new InjectResult(
                                    callerClass,
                                    targetClass,
                                    smaliLine
                            )
                    );
                }
            }
        }

        return results;
    }

    /**
     * 系统 / 标准库过滤
     */
    private static boolean isSystemClass(String cls) {
        return cls.startsWith("Landroid/")
                || cls.startsWith("Ljava/")
                || cls.startsWith("Lkotlin/")
                || cls.startsWith("Landroidx/");
    }
}
