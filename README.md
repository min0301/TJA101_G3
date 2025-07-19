# Pixel Tribe 論壇商城系統

> 為遊戲玩家打造的整合式討論交流與虛擬商品交易平台

## 專案簡介

**Pixel Tribe** 是專為遊戲玩家打造的社群平台，整合論壇與商城功能，提供遊戲資訊交流與虛擬商品交易服務。

本專案源自於對現有遊戲平台功能分散、操作複雜的觀察，致力於打造一站式服務，涵蓋發文留言、聊天室互動、商品瀏覽購買與後台管理等核心功能，讓使用者享有一致且流暢的體驗。

我們採用現代後端技術如 JWT 與 Redis 強化效能與安全，並透過 RESTful API 簡化前後端整合。角色權限控管清晰，支援會員與管理員的操作需求。

期望 Pixel Tribe 成為玩家首選的遊戲社群與虛擬商品平台，兼顧內容、交易與互動。

## 核心功能特色

- **👤 會員系統** - 提供用戶註冊／登入（支援 JWT 驗證）、個人資料管理、登入日誌與黑名單（可停權）等功能。
- **📰 遊戲新聞** - 定期發佈電玩遊戲相關新聞，讓會員瀏覽最新資訊並參與留言評論、按讚／倒讚與檢舉不當內容，增進社群互動與風氣維護。
- **📝 論壇討論** - 玩家可在各遊戲論討板發表文章、留言回覆，具備分類、標籤、置頂、即時聊天室與留言樹狀巢狀結構。
- **🛒 商城模組** - 商品展示支援圖片輪播，購物車與訂單模組與訂單流程狀態追蹤。
- **🛡️ 後台管理** - 包含會員控管（封鎖/停權）、商品與文章管理、留言檢舉審核處理與統計圖表報表展示等功能。

## 技術架構

### 後端技術

- **語言**：Java 17
- **框架**：Spring Boot 3.4.x
- **核心模組**：Spring Web (MVC), Spring Data JPA (Hibernate), Spring Security (JWT 驗證)
- **資料庫**：MySQL 8.0
- **快取工具**：Redis 8+
- **建置工具**：Maven 3.8
- **其他**：Lombok（減少樣板程式碼）、Logback 日誌配置、Spring Validation 資料驗證、Swagger(OpenAPI)（API 文件生成）

### 前端技術

- **UI 框架**：Bootstrap 5
- **腳本語言**：HTML5, CSS3, JavaScript (ES6), jQuery
- **AJAX 技術**：透過原生 JS / jQuery 與 REST API 串接實現動態資料互動

## 系統架構

- **分層架構**：Controller → Service → Repository → Entity → DTO/VO
- **模組拆分**：
    - `membersys/`：會員註冊、管理
    - `forumsys/`：論壇、發文、留言、公共聊天室
    - `shopsys/`：商品、訂單、評價、購物車
    - `newssys/`：新聞發布與評論管理
    - `common/`：共用工具類與例外處理邏輯
    - `config/`：安全性設定、JWT 過濾器、CORS 配置等
    - `util/`：工具函式庫（如圖片壓縮、格式轉換、base64 編碼等）

## **版本控制 (Git Flow)**

本專案採用 Git 分組-個人 分支模型進行版本管理：

```
master      # 穩定發布版本

TommyHCY    # 組長的分支

諸如此類...
```

## 快速開始

### 環境需求

- Java 17
- MySQL 8.0+
- Redis 8+
- Maven 3.8+

### 安裝步驟

```bash
git clone <repository-url>
cd PixelTribe
```

建立資料庫：

```sql
CREATE
DATABASE pixeltribe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

設定 application-dev.properties：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pixeltribe
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.data.redis.host=localhost
spring.data.redis.port=6379
jwt.secret=${JWT_SECRET}
```

啟動應用程式：

```bash
mvn spring-boot:run
```

**存取應用程:**

```
開啟瀏覽器前往: `http://localhost:8080`
```

## 專案結構

```
src/main/java/com/pixeltribe/
├── common/
├── config/
├── forumsys/
├── membersys/
├── newssys/
├── shopsys/
├── util/
├── PixeltribeApplication
└── ServletInitializer
```

## 組員資訊

- **組長**：黃健祐（負責新聞模組及環境建制）
- **副組長**：許政逵（負責會員模組）
- **組員**：
    - 魏君珉、林玉祥（負責討論區模組）
    - 高于雯、林薰妤（負責購物車模組）

## 授權條款

本專案採用 MIT 授權條款 - 詳見 [LICENSE](LICENSE)

