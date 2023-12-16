package cn.thecoldworld.textfilereader.exceptions;

public class PermissionException extends TranslatableException {
    public PermissionException() {
        super("text.filereader.permission.no");
    }
}
