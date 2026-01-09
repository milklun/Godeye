package com.god.eye;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * DexAnalyzer
 *
 * 职责：
 * - 从 APK 中提取所有 dex 文件
 * - 不做任何分析、不做任何判断
 */
public class DexAnalyzer {

    private final File apkFile;

    public DexAnalyzer(File apkFile) {
        this.apkFile = apkFile;
    }

    /**
     * 读取 APK 中的所有 dex
     */
    public List<DexItem> loadAllDex() throws Exception {
        List<DexItem> list = new ArrayList<>();

        ZipFile zipFile = new ZipFile(apkFile);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();

            if (name != null && name.startsWith("classes") && name.endsWith(".dex")) {
                byte[] data = readZipEntry(zipFile, entry);
                list.add(new DexItem(name, data));
            }
        }

        zipFile.close();
        return list;
    }

    private byte[] readZipEntry(ZipFile zipFile, ZipEntry entry) throws Exception {
        InputStream is = zipFile.getInputStream(entry);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) != -1) {
            bos.write(buf, 0, len);
        }

        is.close();
        return bos.toByteArray();
    }

    /**
     * dex 数据结构
     */
    public static class DexItem {
        public final String name;   // classes.dex / classes2.dex
        public final byte[] data;   // 原始 dex 字节

        public DexItem(String name, byte[] data) {
            this.name = name;
            this.data = data;
        }
    }
}
