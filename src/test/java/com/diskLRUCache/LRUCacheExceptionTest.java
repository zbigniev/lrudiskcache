package com.diskLRUCache;

import org.testng.annotations.*;

import java.security.NoSuchAlgorithmException;

public final class LRUCacheExceptionTest {

    @Test(expectedExceptions = NoSuchAlgorithmException.class)
    public void noSuchAlgoExceptionTest() throws NoSuchAlgorithmException {
        LRUCache cacheObject = LRUCache.getInstance("cacheDir", 10, 100, "abc");
    }

}
