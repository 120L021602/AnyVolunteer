package com.hkucs.groupproject.config;

import com.volcengine.ark.runtime.service.ArkService;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;

public class Config {
    private static final String API_KEY = "b31ae4b0-203f-406b-b616-f3bf4fafd9cb";
    private static final String BASE_URL = "https://ark.cn-beijing.volces.com/api/v3";
    private static final ConnectionPool CONNECTION_POOL = new ConnectionPool(5, 1, TimeUnit.SECONDS);
    private static final Dispatcher dispatcher = new Dispatcher();
    public static ArkService service = ArkService.builder().dispatcher(dispatcher).connectionPool(CONNECTION_POOL).baseUrl(BASE_URL).apiKey(API_KEY).build();

}
