package com.diskLRUCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class Node {
    private String fileName;
    private long dataSize;
    
    public Node(String fileName, long dataSize) {
        this.fileName = fileName;
        this.dataSize = dataSize;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setDataSize(long dataSize) {
        this.dataSize = dataSize;
    }

    public long getDataSize() {
        return this.dataSize;
    }
}

public class LRUCache extends LinkedHashMap {
    private static volatile LRUCache instance = null;
    private static String directory;
    private static int valueCount;
    private static long maxSize;
    private static long storedSize;
    private final Map lruEntries;
    MessageDigest digest;


    private LRUCache(String directory, int valueCount, long maxSize, String hashAlgo) throws NoSuchAlgorithmException {
        this.directory = directory;
        this.valueCount = valueCount;
        this.maxSize = maxSize;
        this.digest = MessageDigest.getInstance(hashAlgo);
        this.lruEntries = Collections.synchronizedMap(new LinkedHashMap<String, Node>(0, 0.75f, true));
    }

    public synchronized byte[] get(String key) throws IOException {
        byte[] data = null;
        Node node = (Node)lruEntries.get(key);
        if (node != null) {
            data = Files.readAllBytes(Paths.get(directory, node.getFileName()));
        }
        return data;
    }

    private String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    private void writeToFile(String fileName, byte[] value) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(directory, fileName));
        fos.write(value);
        fos.close();
    }

    private void deleteFile(String fileName) throws IOException {
        Files.delete(Paths.get(directory, fileName));
    }

    public synchronized void put(String key, byte[] value) throws IOException {
        digest.update(value);
        byte[] hashedBytes = digest.digest();
        String newFileName = convertByteArrayToHexString(hashedBytes);
        Node node = (Node)lruEntries.get(key);
        if (node != null) {
            String oldFileName = node.getFileName();
            if (!newFileName.equals(oldFileName)) {
                writeToFile(newFileName, value);
                node.setFileName(newFileName);
                storedSize -= node.getDataSize();
                node.setDataSize(value.length);
                storedSize += value.length;
                lruEntries.replace(key, node);
                deleteFile(oldFileName);
            }
        } else {
            node = new Node(newFileName, value.length);
            writeToFile(newFileName, value);
            storedSize += value.length;
            lruEntries.put(key, node);
        }
        Object[] lruKeys = lruEntries.keySet().toArray();
        int index = 0;
        while (storedSize >= maxSize) {
            node = (Node)lruEntries.get(lruKeys[index]);
            deleteFile(node.getFileName());
            storedSize -= node.getDataSize();
            lruEntries.remove(lruKeys[index]);
            index += 1;
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        if (size() > valueCount) {
            Node node = (Node)eldest.getValue();
            try {
                deleteFile(node.getFileName());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public static LRUCache getInstance(String directory, int valueCount, long maxSize, String hashAlgo) throws Exception {
        if (instance == null) {
            synchronized (LRUCache.class) {
                if (instance == null) {
                    instance = new LRUCache(directory, valueCount, maxSize, hashAlgo);
                }
            }
        }
        return instance;
    }
}
