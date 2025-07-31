package tech.minediamond.vortex.model;

/**
 * 定义 Everything 的搜索模式。
 */
public enum SearchMode {
    /**
     * 搜索文件和文件夹 (默认)。
     */
    ALL(""),

    /**
     * 仅搜索文件。
     * 这会在查询前添加 "file:" 修饰符。
     */
    FILES_ONLY("file:"),

    /**
     * 仅搜索文件夹。
     * 这会在查询前添加 "folder:" 修饰符。
     */
    FOLDERS_ONLY("folder:");

    private final String prefix;

    SearchMode(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 获取要添加到搜索查询中的前缀。
     * @return a search prefix string, for example "file: ".
     */
    public String getQueryPrefix() {
        // 如果前缀不为空，则在后面加一个空格，以分隔实际的查询内容
        return prefix.isEmpty() ? "" : prefix + " ";
    }
}

