package cn.thecoldworld.textfilereader.networking;

import com.google.gson.JsonObject;

import java.util.Random;

public class NetWorkPackage {

    public final String ID;
    public final JsonObject Body;

    public NetWorkPackage(JsonObject body) {
        Body = body;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOQPRSTUVWXYZ_".toCharArray();
        while (sb.length() < 24) {
            sb.append(chars[random.nextInt(0, chars.length - 1)]);
        }
        ID = sb.toString();
    }
}
