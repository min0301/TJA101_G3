import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * ImageUpload
 * ------------------------------------------------------------
 * 純 Java SE 批次工具：將位於 src/main/resources/imgseed 底下的
 *  ‑ adm_img         → ADMINISTRATOR.ADM_PROFILE
 *  ‑ game_cover_img  → PRODUCT.PRO_COVER
 *  ‑ game_screen_img → PRODUCT_IMAGE.PRO_IMG_DATA
 *  ‑ member_head_img → MEMBER.MEM_ICON_DATA（含 defaultmem.png）
 *  ‑ news_img        → NEWS_IMAGE.IMG_DATA
 *  ‑ forum_img       → FORUM.FOR_IMG      ← CHANGED!
 *
 * 檔名規則（數字 = 關聯主鍵/序號）：
 *  ‑ admX.png                    ⇒ ADM_NO   = X
 *  ‑ X.png                       ⇒ PRO_NO   = X  (產品封面)
 *  ‑ X‑Y.png / X_Y.png           ⇒ PRO_NO   = X  (產品截圖)
 *  ‑ memX.png                    ⇒ MEM_NO   = X
 *  ‑ defaultmem.png              ⇒ 所有無頭像會員
 *  ‑ newX‑Y.png / newX_Y.png     ⇒ NEWS_NO  = X
 *  ‑ 0N.jpg  (e.g. 01.jpg)       ⇒ FOR_NO   = N   (討論區封面)
 *
 * 依照目錄與檔名前綴解析主鍵後，以 JDBC BLOB 方式寫入對應資料表。
 */

import java.io.FileInputStream;              // 讀取圖片為二進位串流
import java.io.IOException;                 // 例外處理類別
import java.nio.file.Files;                  // NIO 工具，協助讀取檔案 MIME Type
import java.nio.file.Path;                   // 表示檔案路徑
import java.nio.file.Paths;                  // 建立 Path 物件
import java.sql.Connection;                  // JDBC 連線介面
import java.sql.DriverManager;               // 取得 JDBC Connection
import java.sql.PreparedStatement;           // 預備敘述
import java.sql.ResultSet;                   // 查詢結果
import java.sql.SQLException;               // JDBC 例外
import java.util.regex.Matcher;              // 正規表示式比對器
import java.util.regex.Pattern;              // 正規表示式

public class ImageUpload {

    // ====== JDBC 連線設定（依實際環境調整） ===============================
    private static final String URL = "jdbc:mysql://localhost:3306/pixel_tribe?serverTimezone=Asia/Taipei";
    private static final String USER = "root";
    private static final String PASS = "123456";

    // ====== 圖片根目錄（固定在 class‑path） ===============================
    private static final String ROOT = "src/main/resources/imgseed";
    private static final String PUBLIC_PREFIX = "/images/news_img";


    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            seedAdminImg(conn);
            seedProductCover(conn);
            seedProductScreen(conn);
            seedMemberHead(conn);
            seedNewsImg(conn);
//            seedForumCover(conn); // ← 更新討論區封面匯入 (FORUM.FOR_IMG)
            System.out.println("[ImageUpload] All seed tasks finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ---------------------------------------------------------------------
     * 1) 管理員頭像  adm_img/admX.png  → ADMINISTRATOR.ADM_PROFILE
     * -------------------------------------------------------------------*/
    private static void seedAdminImg(Connection conn) throws IOException, SQLException {
        Path dir = Paths.get(ROOT, "adm_img");
        Pattern p = Pattern.compile("adm(\\d+)\\.png");
        String sql = "UPDATE ADMINISTRATOR SET ADM_PROFILE = ? WHERE ADM_NO = ?";
        uploadByPattern(conn, dir, p, sql, 1);
    }

    /* ---------------------------------------------------------------------
     * 2) 產品封面  game_cover_img/X.png → PRODUCT.PRO_COVER
     * -------------------------------------------------------------------*/
    private static void seedProductCover(Connection conn) throws IOException, SQLException {
        Path dir = Paths.get(ROOT, "game_cover_img");
        Pattern p = Pattern.compile("(\\d+)\\.png");
        String sql = "UPDATE PRODUCT SET PRO_COVER = ? WHERE PRO_NO = ?";
        uploadByPattern(conn, dir, p, sql, 1);
    }

    /* ---------------------------------------------------------------------
     * 3) 產品截圖  game_screen_img/X‑Y.png → PRODUCT_IMAGE
     * -------------------------------------------------------------------*/
//    private static void seedProductScreen(Connection conn) throws IOException, SQLException {
//        Path dir = Paths.get(ROOT, "game_screen_img");
//        Pattern p = Pattern.compile("(\\d+)[-_](\\d+)\\.png"); // X‑Y
//        String check = "SELECT COUNT(*) FROM PRODUCT_IMAGE WHERE PRO_NO = ? AND IMG_ORDER = ?";
//        String insert = "INSERT INTO PRODUCT_IMAGE (PRO_NO, IMG_ORDER, PRO_IMG_DATA, IMG_TYPE) VALUES (?,?,?,?)";
//
//        Files.list(dir).filter(Files::isRegularFile).forEach(path -> {
//            Matcher m = p.matcher(path.getFileName().toString());
//            if (m.matches()) {
//                int proNo = Integer.parseInt(m.group(1));
//                int order = Integer.parseInt(m.group(2));
//                try (PreparedStatement psChk = conn.prepareStatement(check);
//                     PreparedStatement psIns = conn.prepareStatement(insert);
//                     FileInputStream fis = new FileInputStream(path.toFile())) {
//
//                    psChk.setInt(1, proNo);
//                    psChk.setInt(2, order);
//                    try (ResultSet rs = psChk.executeQuery()) {
//                        rs.next();
//                        if (rs.getInt(1) == 0) {                         // 若尚未存在才插入
//                            psIns.setInt(1, proNo);
//                            psIns.setInt(2, order);
//                            psIns.setBinaryStream(3, fis, (int) path.toFile().length());
//                            psIns.setString(4, Files.probeContentType(path));
//                            psIns.executeUpdate();
//                            System.out.printf("[product_screen] pro=%d order=%d -> inserted\n", proNo, order);
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
    private static void seedProductScreen(Connection conn) throws IOException, SQLException {
        Path dir = Paths.get(ROOT, "game_screen_img");
        if (!Files.isDirectory(dir)) return;

        Pattern p = Pattern.compile("(\\d+)-(\\d+)\\.JPG"); // 產品‑序號.png

        // ❗ 依實際欄位名稱微調，下方假設有 PRO_IMG_ORDER
        final String check  = "SELECT COUNT(*) FROM PRODUCT_IMAGE WHERE PRO_NO = ? ";
        final String insert = "INSERT INTO PRODUCT_IMAGE (PRO_NO, PRO_IMG_DATA, PRO_IMG_TYPE) VALUES (?,?,?)";

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.JPG")) {
            for (Path path : stream) {
                Matcher m = p.matcher(path.getFileName().toString());
                if (!m.matches()) {
                    System.out.printf("[product_screen] skip file: %s (命名不符)%n", path.getFileName());
                    continue;
                }
                int proNo = Integer.parseInt(m.group(1));
//                int order = Integer.parseInt(m.group(2));

                // 已存在？
//                try (PreparedStatement psChk = conn.prepareStatement(check)) {
//                    psChk.setInt(1, proNo);
////                    psChk.setInt(2, order);
//                    try (ResultSet rs = psChk.executeQuery()) {
//                        rs.next();
//                        if (rs.getInt(1) > 5) {
//                            System.out.printf("[product_screen] pro=%d  already exists, skip%n", proNo);
//                            continue;
//                        }
//                    }
//                }

                // 寫入 DB
                try (PreparedStatement psIns = conn.prepareStatement(insert);
                     FileInputStream   fis   = new FileInputStream(path.toFile())) {
                    psIns.setInt(1, proNo);
//                    psIns.setInt(2, order);
                    psIns.setBinaryStream(2, fis, (int) Files.size(path));
                    String mime = Files.probeContentType(path);
                    psIns.setString(3, mime != null ? mime : "image/jpg");
                    psIns.executeUpdate();
                    System.out.printf("[product_screen] pro=%d  -> inserted (%s)%n", proNo,  path.getFileName());
                }
            }
        }
    }


    /* ---------------------------------------------------------------------
     * 4) 會員頭像  member_head_img/memX.png  + defaultmem.png
     * -------------------------------------------------------------------*/
    private static void seedMemberHead(Connection conn) throws IOException, SQLException {
//        Path dir = Paths.get(ROOT, "member_head_img");
//        Pattern p = Pattern.compile("mem(\\d+)\\.png");
//        String sql = "UPDATE MEMBER SET MEM_ICON_DATA = ? WHERE MEM_NO = ?";
//        uploadByPattern(conn, dir, p, sql, 1);
//        // defaultmem.png → 沒頭像的會員
//        Path def = dir.resolve("defaultmem.png");
//        if (Files.exists(def)) {
//            String q = "SELECT MEM_NO FROM MEMBER WHERE MEM_ICON_DATA IS NULL";
//            try (PreparedStatement psQ = conn.prepareStatement(q);
//                 ResultSet rs = psQ.executeQuery();
//                 PreparedStatement psU = conn.prepareStatement("UPDATE MEMBER SET MEM_ICON_DATA = ? WHERE MEM_NO = ?");
//                 FileInputStream fis = new FileInputStream(def.toFile())) {
//                while (rs.next()) {
//                    psU.setBinaryStream(1, fis);
//                    psU.setInt(2, rs.getInt(1));
//                    psU.addBatch();
//                }
//                psU.executeBatch();
//                System.out.println("[member_head] defaultmem.png applied to members without avatar");
//            }
//        }
    }

    /* ---------------------------------------------------------------------
     * 5) 新聞圖片  news_img/newX‑Y.png → NEWS_IMAGE
     * -------------------------------------------------------------------*/
    private static void seedNewsImg(Connection conn) throws IOException, SQLException {
        Path dir = Paths.get(ROOT, "news_img");
        Pattern p = Pattern.compile("new(\\d+)[-_](\\d+)\\.png", Pattern.CASE_INSENSITIVE);
        String insert = "INSERT INTO NEWS_IMAGE (NEWS_NO, IMG_URL, IMG_TYPE) VALUES (?,?,?)";
        uploadImgUrlList(conn, dir, p, insert);
    }

    /* ---------------------------------------------------------------------
     * 6) 討論區封面  forum_img/0N.jpg → FORUM.FOR_IMG
     * -------------------------------------------------------------------*/
//    private static void seedForumCover(Connection conn) throws IOException, SQLException {
//        Path dir = Paths.get(ROOT, "forum_img");
//        Pattern p = Pattern.compile("0?(\\d+)\\.jpg"); // 01.jpg → FOR_NO = 1
//        String sql = "UPDATE FORUM SET FOR_IMG = ? WHERE FOR_NO = ?";
//        uploadByPattern(conn, dir, p, sql, 1);
//    }
    /* ──────────────────────────────────────────────────────────
     * 通用：依 Pattern 擷取兩段整數 → 僅寫入「圖片路徑」
     * ─────────────────────────────────────────────────────────*/
    private static void uploadImgUrlList(Connection conn, Path dir, Pattern p,
                                         String insertSql)
            throws IOException, SQLException {

        Files.list(dir).filter(Files::isRegularFile).forEach(path -> {
            Matcher m = p.matcher(path.getFileName().toString());
            if (m.matches()) {
                int newsNo = Integer.parseInt(m.group(1));           // NEWS_NO
                String url = PUBLIC_PREFIX + "/" + path.getFileName(); // /img/news_img/new1-1.png
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {

                    ps.setInt   (1, newsNo);
                    ps.setString(2, url);
                    ps.setString(3, "image/png");
                    ps.executeUpdate();

                    System.out.printf("[news_img] newsNo=%d file=%s -> inserted%n",
                            newsNo, path.getFileName());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });}

    /* ---------------------------------------------------------------------
     * 通用：依 Pattern 擷取一組整數主鍵，更新單欄位 BLOB
     * @param keyGroupIndex 正規表示式中哪個()是主鍵數字
     * -------------------------------------------------------------------*/
    private static void uploadByPattern(Connection conn, Path dir, Pattern p, String sql, int keyGroupIndex)
            throws IOException, SQLException {
        Files.list(dir).filter(Files::isRegularFile).forEach(path -> {
            Matcher m = p.matcher(path.getFileName().toString());
            if (m.matches()) {
                int key = Integer.parseInt(m.group(keyGroupIndex));
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     FileInputStream fis = new FileInputStream(path.toFile())) {
                    ps.setBinaryStream(1, fis, (int) path.toFile().length());
                    ps.setInt(2, key);
                    int rows = ps.executeUpdate();
                    System.out.printf("[%s] key=%d -> %d row(s)\n", dir.getFileName(), key, rows);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    /* ---------------------------------------------------------------------
     * 通用：依 Pattern 擷取兩段整數（主鍵 + 流水號）插入多筆圖片
     * -------------------------------------------------------------------*/
    private static void uploadImageList(Connection conn, Path dir, Pattern p, String insertSql)
            throws IOException, SQLException {
        Files.list(dir).filter(Files::isRegularFile).forEach(path -> {
            Matcher m = p.matcher(path.getFileName().toString());
            if (m.matches()) {
                int key1 = Integer.parseInt(m.group(1)); // 主鍵
                try (PreparedStatement ps = conn.prepareStatement(insertSql);
                     FileInputStream fis = new FileInputStream(path.toFile())) {
                    ps.setInt(1, key1);
                    ps.setBinaryStream(2, fis, (int) path.toFile().length());
                    ps.setString(3, Files.probeContentType(path));
                    ps.executeUpdate();
                    System.out.printf("[%s] key=%d file=%s -> inserted\n", dir.getFileName(), key1, path.getFileName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
