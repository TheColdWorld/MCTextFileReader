package cn.thecoldworld.textfilereader.api;

import cn.thecoldworld.textfilereader.variables;

public record SubMod(String ModID, String Version) {

    public static void RegisterSubMod(SubMod subMod) {
        variables.subMods.add(subMod);
    }
}
