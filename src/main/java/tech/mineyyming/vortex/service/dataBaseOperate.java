package tech.mineyyming.vortex.service;

import tech.mineyyming.vortex.model.ProgramInfo;
import tech.mineyyming.vortex.model.ProgramSource;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;

public class dataBaseOperate {
    private static final String DB_URL = "jdbc:sqlite:installed_software.db";

    // 1. 将构造函数设为私有，防止外部直接 new
    private dataBaseOperate() {
        // 在实例被创建时，确保表结构存在
        createTable();
    }

    // 2. 创建一个私有的静态内部类来持有单例实例
    private static class SingletonHolder {
        private static final dataBaseOperate INSTANCE = new dataBaseOperate();
    }

    // 3. 提供一个公共的静态方法来获取这个唯一的实例
    public static dataBaseOperate getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * 创建软件信息表 (如果不存在)
     */
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS software (\n"
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "baseName TEXT NOT NULL,\n"
                + "displayName TEXT,\n"
                + "programName TEXT,\n"
                + "version TEXT,\n"
                + "publisher TEXT,\n"
                + "installLocation TEXT,\n"
                + "source TEXT,\n"
                + "enabled TEXT,\n"
                + "path TEXT\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    /**
     * 增 (Create): 插入一个软件列表，使用事务批量处理
     */
    public void insertSoftwareList(List<ProgramInfo> programInfoList) throws SQLException {
        String sql = "INSERT INTO software(baseName, displayName, version, publisher, installLocation, enabled) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect()) {
            // 关闭自动提交，开启事务
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (ProgramInfo software : programInfoList) {
                    pstmt.setString(1, software.getBaseName());
                    pstmt.setString(2, software.getDisplayName());
                    pstmt.setString(3, software.getVersion());
                    pstmt.setString(4, software.getPublisher());
                    pstmt.setString(5, software.getInstallLocation());
                    pstmt.setString(6, software.getEnabled().toString());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit(); // 提交事务
                System.out.println(programInfoList.size() + " records inserted/replaced successfully.");
            } catch (SQLException e) {
                conn.rollback(); // 如果出错，回滚事务
                System.err.println("Transaction rolled back. Error: " + e.getMessage());
            }
        }
    }

    // 在 dataBaseOperate.java 中

    /**
     * 同步软件列表到数据库，处理新增、更新和删除。
     * 使用 displayName + version 作为唯一标识来查找记录。
     * 任何字段（包括 installLocation）的变化都会触发更新。
     *
     * @param latestProgramList 最新的软件信息列表
     * @throws SQLException 如果发生数据库错误
     */
    public void syncSoftwareList(List<ProgramInfo> latestProgramList) throws SQLException {
        // SQL 语句
        String selectWithVersionSql = "SELECT * FROM software WHERE displayName = ? AND installLocation = ?";
        String selectWithoutVersionSql = "SELECT * FROM software WHERE displayName = ? AND installLocation IS NULL";
        String insertSql = "INSERT INTO software(baseName, displayName, programName, version, publisher, installLocation, source, enabled, path) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // 更新时，我们更新所有可能变化的字段
        String updateSql = "UPDATE software SET baseName = ?, programName = ?,publisher = ?, version = ?, source = ?, enabled = ?, path = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false); // 开启事务

            // 预编译所有需要的语句
            PreparedStatement selectWithVersionStmt = conn.prepareStatement(selectWithVersionSql);
            PreparedStatement selectWithoutVersionStmt = conn.prepareStatement(selectWithoutVersionSql);
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);

            int insertedCount = 0;
            int updatedCount = 0;

            // 1. 处理新增和更新
            for (ProgramInfo newProgram : latestProgramList) {
                // 用“钥匙”查找数据库中的记录
                PreparedStatement currentSelectStmt;
                if (newProgram.getInstallLocation() != null && !newProgram.getInstallLocation().isEmpty()) {
                    // 情况一：版本号存在
                    currentSelectStmt = selectWithVersionStmt;
                    currentSelectStmt.setString(1, newProgram.getDisplayName());
                    currentSelectStmt.setString(2, newProgram.getInstallLocation());
                } else {
                    // 情况二：版本号为 null 或为空字符串
                    currentSelectStmt = selectWithoutVersionStmt;
                    currentSelectStmt.setString(1, newProgram.getDisplayName());
                }
                ResultSet rs = currentSelectStmt.executeQuery();

                if (rs.next()) { // 找到了匹配的记录
                    long dbId = rs.getLong("id");

                    // 从数据库记录创建一个 ProgramInfo 对象，用于比较
                    ProgramInfo dbProgram = new ProgramInfo();
                    dbProgram.setBaseName(rs.getString("baseName"));
                    dbProgram.setDisplayName(rs.getString("displayName"));
                    dbProgram.setProgramName(rs.getString("programName"));
                    dbProgram.setVersion(rs.getString("version"));
                    dbProgram.setPublisher(rs.getString("publisher"));
                    dbProgram.setInstallLocation(rs.getString("installLocation"));
                    dbProgram.setEnabled(Boolean.parseBoolean(rs.getString("enabled")));
                    dbProgram.setPath(Optional.ofNullable((rs.getString("path"))).map(Path::of).orElse(null));


                    if (!newProgram.equals(dbProgram)) {

                        updateStmt.setString(1, newProgram.getBaseName());
                        updateStmt.setString(2, newProgram.getProgramName());
                        updateStmt.setString(3, newProgram.getPublisher());
                        updateStmt.setString(4, newProgram.getVersion());
                        updateStmt.setString(5, newProgram.getSource().toString());
                        updateStmt.setString(6, newProgram.getEnabled().toString());
                        updateStmt.setString(7, Optional.ofNullable(newProgram.getPath()).map(Path::toString).orElse(null));
                        updateStmt.setLong(8, dbId); // 用主键 id 来更新，最精确

                        updateStmt.addBatch();
                        updatedCount++;
                    }


                } else { // 数据库中没有这条记录
                    // 执行插入
                    insertStmt.setString(1, newProgram.getBaseName());
                    insertStmt.setString(2, newProgram.getDisplayName());
                    insertStmt.setString(3, newProgram.getProgramName());
                    insertStmt.setString(4, newProgram.getVersion());
                    insertStmt.setString(5, newProgram.getPublisher());
                    insertStmt.setString(6, newProgram.getInstallLocation());
                    insertStmt.setString(7, newProgram.getSource().toString());
                    insertStmt.setString(8, newProgram.getEnabled().toString());
                    insertStmt.setString(9, Optional.ofNullable(newProgram.getPath()).map(Path::toString).orElse(null));

                    insertStmt.addBatch();
                    insertedCount++;
                }
                rs.close();
            }

            // 执行所有累积的插入和更新操作
            insertStmt.executeBatch();
            updateStmt.executeBatch();

            // 2. 处理删除 (这部分逻辑对于完整同步至关重要)
            //int deletedCount = handleDelete(conn, latestProgramList);

            // 3. 提交事务
            conn.commit();
            System.out.println("同步完成！新增: " + insertedCount + ", 更新: " + updatedCount);

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public ArrayList<ProgramInfo> readProgramInfoList() throws SQLException {
        String readSQL= "SELECT * FROM software";
        ArrayList<ProgramInfo> programInfoList = new ArrayList<>();
        try (Connection conn = connect();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(readSQL)) {
            while (rs.next()) {
                ProgramInfo programInfo = new ProgramInfo();
                programInfo.setBaseName(rs.getString("baseName"));
                programInfo.setDisplayName(rs.getString("displayName"));
                programInfo.setProgramName(rs.getString("programName"));
                programInfo.setVersion(rs.getString("version"));
                programInfo.setPublisher(rs.getString("publisher"));
                programInfo.setInstallLocation(rs.getString("installLocation"));
                programInfo.setSource(ProgramSource.valueOf(rs.getString("source")));
                programInfo.setEnabled(Boolean.parseBoolean(rs.getString("enabled")));
                programInfo.setPath(Optional.ofNullable(rs.getString("path")).map(Path::of).orElse(null));
                programInfo.setEnabled(Boolean.parseBoolean(rs.getString("enabled")));
                programInfo.setId(rs.getString("id"));
                programInfoList.add(programInfo);
            }
        }
        return programInfoList;
    }

    /**
     * 一个辅助方法，用于处理删除逻辑
     */
    private int handleDelete(Connection conn, List<ProgramInfo> latestProgramList) throws SQLException {
        // 创建一个新列表中所有软件的唯一标识符集合
        Set<String> latestKeys = new HashSet<>();
        for (ProgramInfo p : latestProgramList) {
            // 使用和查询时完全相同的“钥匙”
            latestKeys.add(p.getDisplayName() + "::" + p.getVersion());
        }

        List<Long> idsToDelete = new ArrayList<>();
        // 查询数据库中所有的 displayName, version 和 id
        String selectAllSql = "SELECT id, displayName, displayVersion FROM software";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectAllSql)) {

            while (rs.next()) {
                String dbKey = rs.getString("displayName") + "::" + rs.getString("version");
                if (!latestKeys.contains(dbKey)) {
                    // 这个数据库记录在新列表中不存在，准备删除
                    idsToDelete.add(rs.getLong("id"));
                }
            }
        }

        if (idsToDelete.isEmpty()) {
            return 0;
        }

        // 批量删除
        String deleteSql = "DELETE FROM software WHERE id = ?";
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            for (Long id : idsToDelete) {
                deleteStmt.setLong(1, id);
                deleteStmt.addBatch();
            }
            int[] result = deleteStmt.executeBatch();
            return result.length;
        }
    }




}
