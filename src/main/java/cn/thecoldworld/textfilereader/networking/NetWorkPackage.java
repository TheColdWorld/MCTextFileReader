package cn.thecoldworld.textfilereader.networking;

import com.google.gson.JsonObject;

import java.util.Random;

public abstract class NetWorkPackage {

    public final String ID;
    public final JsonObject Body;
    public final String Identifier;

    public NetWorkPackage(JsonObject body, String identifier) {
        Body = body;
        Identifier = identifier;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOQPRSTUVWXYZ_".toCharArray();
        while (sb.length() < 30) {
            sb.append(chars[random.nextInt(0, chars.length - 1)]);
        }
        ID = sb.toString();
    }
}
