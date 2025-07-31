package tech.minediamond.vortex.model;

/**
 * 枚举，用于表示向 Everything 请求的数据字段。
 * 每个枚举项对应 Everything SDK 中的一个请求标志。
 */
public enum RequestFlag {
    FILE_NAME(0x00000001),
    PATH(0x00000002),
    FULL_PATH_AND_FILE_NAME(0x00000004),
    EXTENSION(0x00000008),
    SIZE(0x00000010),
    DATE_CREATED(0x00000020),
    DATE_MODIFIED(0x00000040),
    DATE_ACCESSED(0x00000080),
    ATTRIBUTES(0x00000100);

    private final int value;

    RequestFlag(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
