package cn.thecoldworld.textfilereader;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class funcitons {
    public static boolean CreateFile(File file,String DefaultInput) throws Exception{
        if( file.exists() && file.isFile()) throw new Exception("File exist");
        try {
           if(! file.createNewFile()) return false;
           FileWriter fr = new FileWriter(file);
           fr.write(DefaultInput);
           fr.flush();fr.close();
           return true;
        } catch (IOException e) {
            variables.Log.error("",e);
            return false;
        }
    }
    public static boolean CreateDir(Path path) throws Exception
    {
        Path _path = path.toAbsolutePath().normalize();
        if(_path.toFile().exists() && _path.toFile().isDirectory()) throw new Exception("Dir exist");
        try
        {
            if(!_path.getParent().toFile().exists() || !_path.getParent().toFile().isDirectory()) CreateDir(_path.getParent());
            Files.createDirectory(path);
            return true;
        }
        catch (IOException e)
        {
            variables.Log.error("",e);
            return false;
        }
    }
    public static String GetFilePerfix(@NotNull File fp)
    {
        if(fp.exists() && fp.isFile()) return fp.getName().substring(fp.getName().lastIndexOf(".")+1);
        return "";
    }
}
