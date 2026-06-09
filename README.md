# Code Wiki - 图书管理系统 (Library Manager System)

> 本文档基于项目源码自动生成，涵盖项目整体架构、模块职责、关键类与函数、依赖关系及运行方式等关键信息。

---

## 一、项目概览

| 属性                       | 说明                              |
| -------------------------- | --------------------------------- |
| **项目名称**         | springboot-library_manager_system |
| **GroupId**          | com.zbw                           |
| **ArtifactId**       | demo                              |
| **版本**             | 0.0.1-SNAPSHOT                    |
| **打包方式**         | JAR                               |
| **JDK 版本**         | Java 17                           |
| **Spring Boot 版本** | 3.3.2                             |
| **数据库**           | MySQL 5.7+                        |

### 1.1 技术栈

| 层级                 | 技术                                        |
| -------------------- | ------------------------------------------- |
| **前端**       | Thymeleaf、Layui、jQuery、Ajax              |
| **后端框架**   | Spring Boot 3.3.2、Spring MVC               |
| **ORM 框架**   | MyBatis-Plus 3.5.7                          |
| **安全加密**   | Spring Security Crypto (BCrypt)             |
| **参数校验**   | Spring Boot Validation (Jakarta @Valid)     |
| **数据库驱动** | mysql-connector-j                           |
| **工具库**     | Lombok、Apache POI (Excel 导入)             |
| **构建工具**   | Maven                                       |
| **热部署**     | Spring Boot DevTools                        |

---

## 二、项目整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (View)                         │
│   Thymeleaf HTML 模板 + Layui + jQuery + Ajax               │
├─────────────────────────────────────────────────────────────┤
│                      控制层 (Controller)                     │
│   AdminController / BookController /                         │
│   BorrowingController / UserController                       │
├─────────────────────────────────────────────────────────────┤
│                      业务层 (Service)                        │
│   IAdminService / IBookService / IBookCategoryService        │
│   IBorrowingBooksRecordService / IUserService                │
├─────────────────────────────────────────────────────────────┤
│                       数据层 (Mapper/DAO)                    │
│   MyBatis-Plus BaseMapper 接口（零 XML 配置）                 │
├─────────────────────────────────────────────────────────────┤
│                        数据库层                              │
│   MySQL 5.7+ (library-manager-system)                        │
└─────────────────────────────────────────────────────────────┘
```

### 2.1 目录结构

```
library_manager_system-master/
├── pom.xml                          # Maven 构建配置
├── src/
│   ├── main/
│   │   ├── java/com/zbw/
│   │   │   ├── DemoApplication.java          # 项目入口
│   │   │   ├── config/
│   │   │   │   ├── MyBatisPlusConfig.java    # MyBatis-Plus 分页配置
│   │   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   │   ├── controller/                   # 控制层
│   │   │   ├── domain/                       # 实体类 / VO
│   │   │   ├── mapper/                       # 数据访问层
│   │   │   ├── service/                      # 业务层接口 + 实现
│   │   │   └── utils/                        # 工具类
│   │   └── resources/
│   │       ├── application.yml               # 主配置文件
│   │       ├── db/
│   │       │   ├── library-manager-system.sql # 数据库初始化脚本
│   │       │   └── migrate-password-bcrypt.sql # BCrypt 密码迁移脚本
│   │       ├── static/                       # 静态资源 (CSS/JS/图片)
│   │       └── templates/                    # Thymeleaf 页面模板
│   └── test/                                 # 单元测试
├── assets/                          # 项目截图与文档图片
└── README.md                        # 项目说明文档
```

---

## 三、数据库设计

### 3.1 表结构概览

| 表名               | 说明       | 主要字段                                                                                    |
| ------------------ | ---------- | ------------------------------------------------------------------------------------------- |
| `admin`          | 管理员表   | admin_id, admin_name, admin_pwd(200), admin_email                                           |
| `user`           | 用户表     | user_id, user_name, user_pwd(200), user_email                                               |
| `book`           | 图书表     | book_id, book_name, book_author, book_publish, book_category, book_price, book_introduction |
| `book_category`  | 图书类别表 | category_id, category_name                                                                  |
| `borrowingbooks` | 借阅记录表 | id, user_id, book_id, date                                                                  |
| `dept`           | 部门表     | dept_id, dept_name                                                                          |

> **注意**：`user_pwd` 和 `admin_pwd` 列已扩展为 `varchar(200)` 以存储 BCrypt 哈希值（固定 60 字符）。

### 3.2 表关系

- `book.book_category` → `book_category.category_id` (外键)
- `borrowingbooks.book_id` → `book.book_id` (外键)
- `borrowingbooks.user_id` → `user.user_id` (外键)

### 3.3 默认测试账号

| 账号  | 密码   | 角色     | 登录方式       |
| ----- | ------ | -------- | -------------- |
| admin | 123456 | 管理员   | 输入用户名登录 |
| 1     | 123456 | 普通用户 | 输入用户ID登录 |
| 2     | 123456 | 普通用户 | 输入用户ID登录 |

---

## 四、主要模块职责

### 4.1 配置模块 (`config`)

#### MyBatisPlusConfig

- **路径**: `com.zbw.config.MyBatisPlusConfig`
- **职责**: 注册 MyBatis-Plus 分页拦截器，支持 MySQL 物理分页
- **关键 Bean**: `MybatisPlusInterceptor` → `PaginationInnerInterceptor(DbType.MYSQL)`

#### GlobalExceptionHandler

- **路径**: `com.zbw.config.GlobalExceptionHandler`
- **职责**: 全局异常捕获，统一返回 JSON 格式错误信息
- **处理异常**: `MethodArgumentNotValidException`（后端 `@Valid` 校验失败）

### 4.2 控制层模块 (`controller`)

#### AdminController

- **路径**: `com.zbw.controller.AdminController`
- **职责**: 处理管理员相关的页面跳转与接口请求
- **主要功能**:
  - 管理员登录 (`/adminLogin`)
  - 检查管理员是否存在 (`/isAdminExist`)
  - 页面跳转：添加图书、添加类别、查看用户、查看图书、导入等
  - 更新管理员信息 (`/updateAdmin`)，密码自动 BCrypt 加密
  - 退出登录 (`/adminLogOut`)

#### BookController

- **路径**: `com.zbw.controller.BookController`
- **职责**: 处理图书与图书类别的增删查及 Excel 批量导入
- **主要功能**:
  - 录入新书 (`/addBook`)，带 `@Valid` 后端校验
  - 按类别/关键字分页查询图书（管理员+用户）
  - 查询所有图书类别 (`/findAllBookCategory`)
  - 新建/删除图书类别 (`/addBookCategory`, `/deleteCategory`)
  - 删除类别前检查关联图书 (`/findBooksByCategoryId`)
  - 检查图书借阅状态 (`/checkBookStatus`)
  - 删除图书 (`/deleteBook`，借阅中则拒绝，需二次确认)
  - Excel 批量导入图书 (`/importBooksByExcel`)

#### BorrowingController

- **路径**: `com.zbw.controller.BorrowingController`
- **职责**: 处理借阅记录的管理员视角操作
- **主要功能**:
  - 分页查询所有借阅记录 (`/allBorrowBooksRecordPage`)
  - 管理员删除借阅记录 (`/deleteBorrowingRecord`)

#### UserController

- **路径**: `com.zbw.controller.UserController`
- **职责**: 处理普通用户相关的登录、借还书、个人信息及用户管理
- **主要功能**:
  - **用户ID登录** (`/userLogin`)，管理员使用用户名登录不变
  - 用户借书 (`/userBorrowingBook`)
  - 用户还书 (`/userReturnBook`)
  - 查看借书记录 (`/userBorrowBookRecord`)
  - 更新用户信息 (`/updateUser`)，密码自动 BCrypt 加密
  - 添加/删除用户 (`/addUser`, `/deleteUser`)
  - Excel 批量导入用户 (`/importUsersByExcel`)
  - 获取部门列表 (`/getDepts`)

### 4.3 实体/VO 模块 (`domain`)

| 类名                 | 说明                                               | 校验注解                              |
| -------------------- | -------------------------------------------------- | ------------------------------------- |
| `Admin`            | 管理员实体                                         | `@NotBlank`, `@Size`, `@Email`      |
| `User`             | 用户实体                                           | `@NotBlank`, `@Size`, `@Email`      |
| `Book`             | 图书实体                                           | `@NotBlank`, `@Size`, `@DecimalMin` |
| `BookCategory`     | 图书类别实体                                       | `@NotBlank`, `@Size`                |
| `BorrowingBooks`   | 借阅记录实体                                       | —                                     |
| `Department`       | 部门实体                                           | —                                     |
| `BookVo`           | 图书视图对象（含是否可借状态）                     | —                                     |
| `BorrowingBooksVo` | 借阅记录视图对象（含 User、Book 对象及格式化日期） | —                                     |

### 4.4 数据层模块 (`mapper`)

所有 Mapper 均继承 `BaseMapper<T>`，采用 MyBatis-Plus 提供的通用 CRUD，**无自定义 XML**。

| Mapper                   | 对应实体       |
| ------------------------ | -------------- |
| `AdminMapper`          | Admin          |
| `UserMapper`           | User           |
| `BookMapper`           | Book           |
| `BookCategoryMapper`   | BookCategory   |
| `BorrowingBooksMapper` | BorrowingBooks |
| `DepartmentMapper`     | Department     |

### 4.5 业务层模块 (`service`)

#### 接口定义

| 接口                             | 职责                                               |
| -------------------------------- | -------------------------------------------------- |
| `IAdminService`                | 管理员登录验证、图书/类别增删、批量导入图书        |
| `IBookService`                 | 图书关键字/类别查询、借阅状态检查                  |
| `IBookCategoryService`         | 图书类别分页查询、删除类别                         |
| `IBorrowingBooksRecordService` | 借阅记录分页查询（管理员/用户）、删除记录          |
| `IUserService`                 | 用户ID登录/增删/分页、借还书、部门查询、批量导入用户 |

#### 实现类关键逻辑

| 实现类                              | 关键逻辑说明                                                                                      |
| ----------------------------------- | ------------------------------------------------------------------------------------------------- |
| `AdminServiceImpl`                | BCrypt 密码验证；登录时自动将旧明文密码升级为 BCrypt；更新管理员后刷新 Session                     |
| `BookServiceImpl`                 | 查询图书时关联 `borrowingBooksMapper` 判断 `isExist`（可借/不可借）；分页使用 MP 分页插件        |
| `BookCategoryServiceImpl`         | 分页封装到自定义 `Page<T>`                                                                       |
| `BorrowingBooksRecordServiceImpl` | 组装 `BorrowingBooksVo`：查询关联的 User 和 Book，计算应还日期（借书日期 + 2个月）                 |
| `UserServiceImpl`                 | BCrypt 密码验证+自动升级；新增/批量导入时密码自动加密；借书时检查是否已被借阅；还书按 userId+bookId 删除 |

### 4.6 工具类模块 (`utils`)

#### Page`<T>`

- **路径**: `com.zbw.utils.page.Page`
- **职责**: 通用分页封装对象
- **字段**: `list`, `pageNum`, `pageSize`, `pageCount`

#### PasswordUtil

- **路径**: `com.zbw.utils.PasswordUtil`
- **职责**: BCrypt 密码加密与验证，兼容旧明文密码平滑升级
- **主要方法**:
  - `encode(rawPassword)` — 将明文加密为 BCrypt 密文
  - `matches(rawPassword, storedPassword)` — 验证密码（自动识别 BCrypt/明文）
  - `needsUpgrade(storedPassword)` — 检查是否需要从明文升级
  - `isBcryptHash(str)` — 判断字符串是否为 BCrypt 哈希

#### ExcelImportUtil

- **路径**: `com.zbw.utils.ExcelImportUtil`
- **职责**: 基于 Apache POI 解析 Excel 文件，支持 `.xlsx` 和 `.xls`
- **主要方法**:
  - `parseBooksFromExcel(MultipartFile)` → `List<Book>`
  - `parseUsersFromExcel(MultipartFile)` → `List<User>`
- **Excel 图书表头**: 书名 | 作者 | 出版社 | 类别ID | 价格 | 简介
- **Excel 用户表头**: 用户名 | 密码 | 邮箱

---

## 五、关键类与函数详细说明

### 5.1 入口类

#### DemoApplication

- **路径**: `com.zbw.DemoApplication`
- **注解**:
  - `@SpringBootApplication` — Spring Boot 自动配置
  - `@MapperScan("com.zbw.mapper")` — 扫描 Mapper 接口
  - `@ServletComponentScan` — 扫描 Servlet 组件

### 5.2 核心 Service 函数

```java
// BCrypt 密码验证（UserServiceImpl / AdminServiceImpl）
User userLogin(int userId, String password)
// 逻辑：1) 根据 userId 查询用户；2) PasswordUtil.matches() 验证密码
//       3) 若为旧明文密码，自动升级为 BCrypt 并更新数据库

// 新增用户（UserServiceImpl）
int insertUser(User user)
// 逻辑：密码使用 PasswordUtil.encode() 加密后存储

// 判断图书是否被借阅（BookServiceImpl）
boolean isBookBorrowed(int bookId)
// 实现：查询 borrowingbooks 表中是否存在该 book_id 的记录

// 用户借书（UserServiceImpl）
boolean userBorrowingBook(int bookId, HttpServletRequest request)
// 逻辑：1) 检查图书是否已被借阅；2) 插入 borrowingbooks 记录，date = new Date()

// 用户还书（UserServiceImpl）
boolean userReturnBook(int bookId, HttpServletRequest request)
// 逻辑：根据当前 session 中的 userId + bookId 删除 borrowingbooks 记录

// 组装借阅记录 VO（BorrowingBooksRecordServiceImpl）
Page<BorrowingBooksVo> selectAllByPage(int pageNum)
// 逻辑：分页查询 borrowingbooks → 查询关联 User/Book → 计算 dateOfReturn = date + 2个月
```

### 5.3 核心 Controller 接口

| 接口路径                      | 请求方式 | 所属 Controller     | 功能                                        |
| ----------------------------- | -------- | ------------------- | ------------------------------------------- |
| `/adminLogin`               | POST     | AdminController     | 管理员登录（用户名），Session 存储 admin     |
| `/userLogin`                | POST     | UserController      | 用户登录（用户ID），Session 存储 user       |
| `/addBook`                  | 任意     | BookController      | 录入新书，带 `@Valid` 校验                  |
| `/deleteBook`               | 任意     | BookController      | 删除图书（借阅中则拒绝，需二次确认）        |
| `/checkBookStatus`          | 任意     | BookController      | 检查图书借阅状态                            |
| `/findBooksByCategoryId`    | 任意     | BookController      | 查询类别下所有图书（删除类别前检查用）      |
| `/importBooksByExcel`       | 任意     | BookController      | Excel 批量导入图书                          |
| `/importUsersByExcel`       | 任意     | UserController      | Excel 批量导入用户                          |
| `/addUser`                  | 任意     | UserController      | 添加用户，带 `@Valid` 校验                  |
| `/userBorrowingBook`        | 任意     | UserController      | 用户借书                                    |
| `/userReturnBook`           | 任意     | UserController      | 用户还书                                    |
| `/userShowBooksByCategory`  | 任意     | BookController      | 用户端按类别分页查询图书                    |
| `/userFindBooksByKeyword`   | 任意     | BookController      | 用户端按关键字分页查询图书                  |
| `/allBorrowBooksRecordPage` | 任意     | BorrowingController | 管理员查看所有借阅记录                      |
| `/userBorrowBookRecord`     | 任意     | UserController      | 用户查看个人借阅记录                        |

---

## 六、依赖关系

### 6.1 Maven 依赖图（核心）

```
demo (0.0.1-SNAPSHOT)
│
├── spring-boot-starter-web (3.3.2)
│   └── 内置 Tomcat + Spring MVC
├── spring-boot-starter-thymeleaf (3.3.2)
│   └── Thymeleaf 模板引擎
├── spring-boot-starter-validation (3.3.2)
│   └── Jakarta Bean Validation（@Valid, @NotBlank 等）
├── spring-security-crypto
│   └── BCryptPasswordEncoder（密码加密）
├── mybatis-plus-spring-boot3-starter (3.5.7)
│   └── MyBatis-Plus ORM 框架
├── mysql-connector-j (runtime)
│   └── MySQL JDBC 驱动
├── poi-ooxml (5.2.5)
│   └── Apache POI (Excel 读写)
├── lombok (1.18.38)
│   └── 编译时代码生成
├── spring-boot-devtools (optional)
│   └── 热部署
└── spring-boot-starter-test (test)
    └── JUnit 5 + Spring Test
```

### 6.2 模块间依赖关系

```
Controller
    ├── Service (IAdminService / IBookService / IBookCategoryService / IBorrowingBooksRecordService / IUserService)
    │       └── Mapper (AdminMapper / BookMapper / BookCategoryMapper / BorrowingBooksMapper / DepartmentMapper / UserMapper)
    │               └── MyBatis-Plus BaseMapper → MySQL
    └── HttpServletRequest (Session 管理登录状态)
```

---

## 七、配置文件详解

### 7.1 application.yml

```yaml
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml       # Mapper XML 路径（本项目无 XML）
  type-aliases-package: com.zbw.domain           # 实体类别名包
  configuration:
    map-underscore-to-camel-case: true           # 下划线转驼峰
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 打印 SQL
  global-config:
    db-config:
      id-type: auto                              # 自增 ID

server:
  port: 8080                                     # 服务端口

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/library-manager-system?...
    username: root
    password: 123456
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  mvc:
    view:
      prefix: classpath:/templates/
      suffix: .html

  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB
```

---

## 八、项目运行方式

### 8.1 环境准备

- JDK 17+
- MySQL 5.7+
- Maven 3.6+

### 8.2 数据库初始化

```sql
CREATE DATABASE IF NOT EXISTS library-manager-system
DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
```

**全新安装**：导入 `src/main/resources/db/library-manager-system.sql`

**已有数据库升级**（支持 BCrypt 密码加密）：执行 `src/main/resources/db/migrate-password-bcrypt.sql`

```sql
ALTER TABLE `user` MODIFY COLUMN `user_pwd` varchar(200) DEFAULT NULL;
ALTER TABLE `admin` MODIFY COLUMN `admin_pwd` varchar(200) DEFAULT NULL;
```

### 8.3 修改数据库配置

编辑 `src/main/resources/application.yml`，修改 `spring.datasource.url`、`username`、`password` 为你本地的数据库连接信息。

### 8.4 启动项目

**方式一：使用 Maven 命令**

```bash
mvn spring-boot:run
```

**方式二：直接运行入口类**

在 IDE 中运行 `com.zbw.DemoApplication` 的 `main` 方法。

**方式三：打包后运行**

```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### 8.5 访问地址

- 首页: http://localhost:8080/
- 管理员登录: 选择"管理员"，输入用户名 `admin`，密码 `123456`
- 用户登录: 选择"学生"，输入用户ID `1`，密码 `123456`

---

## 九、前端页面结构

### 9.1 模板目录 (`templates`)

| 目录        | 页面                             | 说明                            |
| ----------- | -------------------------------- | ------------------------------- |
| `admin/`  | `index.html`                   | 管理员首页                      |
|             | `addBook.html`                 | 添加图书                        |
|             | `addCategory.html`             | 管理图书类别（删除需二次确认）  |
|             | `showBooks.html`               | 查询图书（按类别+分页，可删除） |
|             | `showUsers.html`               | 用户管理（分页+删除）           |
|             | `addUser.html`                 | 添加用户                        |
|             | `importBooks.html`             | 批量导入图书（Excel）           |
|             | `importUsers.html`             | 批量导入用户（Excel）           |
|             | `allBorrowingBooksRecord.html` | 所有借阅记录                    |
|             | `adminInfo.html`               | 管理员信息修改                  |
| `user/`   | `index.html`                   | 用户首页                        |
|             | `findBook.html`                | 查找图书（按类别+按书名+分页）  |
|             | `borrowingBooks.html`          | 借书页面                        |
|             | `returnBooks.html`             | 还书页面                        |
|             | `borrowingBooksRecord.html`    | 个人借书记录                    |
|             | `userMessage.html`             | 个人信息                        |
| `common/` | `admin_header.html`            | 管理员公共头部                  |
|             | `user_header.html`             | 用户公共头部                    |
|             | `footer.html`                  | 公共底部                        |
| 根目录      | `index.html`                   | 登录首页（角色切换动态表单）    |

### 9.2 静态资源 (`static`)

- `css/` — 自定义样式 + jQuery UI 样式
- `scripts/admin/` — 管理员各页面业务 JS（增删改查、二次确认、Excel 导入）
- `scripts/user/` — 用户各页面业务 JS
- `layui/` — Layui 前端框架完整文件
- `images/` — 背景图、用户默认头像等

---

## 十、安全设计

### 10.1 密码加密

- 使用 **BCrypt** 单向哈希算法，Spring Security Crypto 实现
- 密码存储为 60 字符 BCrypt 密文（如 `$2a$10$N9qo8uLOickgx2ZMRZoMye...`）
- **平滑升级**：旧明文密码首次登录成功后自动升级为 BCrypt，无需手动迁移
- 密码输入限制 4-24 个字符

### 10.2 参数校验

- 实体类添加 Jakarta Validation 注解（`@NotBlank`, `@Size`, `@Email`, `@DecimalMin`）
- 新增操作使用 `@Valid` 触发后端校验
- `GlobalExceptionHandler` 统一返回中文错误信息

### 10.3 删除保护

- **删除类别**：先查关联图书列表，弹窗展示后需二次确认
- **删除图书**：检查借阅状态，借阅中拒绝删除；可借则二次确认
- 后端双重校验，前端通过不意味着后端放行

### 10.4 认证

- 项目使用传统 Session 方式管理登录状态
- 管理员：用户名 + 密码登录
- 用户：用户ID + 密码登录
- 登录页面角色切换时动态修改表单字段名

---

## 十一、测试模块

### 11.1 测试类列表

| 测试类                       | 路径                                 | 说明                 |
| ---------------------------- | ------------------------------------ | -------------------- |
| `DemoApplicationTests`     | `com.zbw.DemoApplicationTests`     | 基础上下文加载测试   |
| `BookCategoryMapperTest`   | `com.zbw.BookCategoryMapperTest`   | 图书类别 Mapper 测试 |
| `BookServiceTest`          | `com.zbw.BookServiceTest`          | 图书 Service 测试    |
| `BorrowingBooksMapperTest` | `com.zbw.BorrowingBooksMapperTest` | 借阅记录 Mapper 测试 |
| `TestController`           | `com.zbw.TestController`           | 测试用 Controller    |
| `UserTest`                 | `com.zbw.UserTest`                 | 用户相关测试         |

---

## 十二、设计亮点与注意事项

### 12.1 设计亮点

1. **BCrypt 密码加密**：密码密文存储，支持旧明文平滑升级，登录时自动迁移。
2. **后端参数校验**：实体类 Jakarta Validation + `@Valid` + 全局异常处理，不依赖前端校验。
3. **二次确认删除**：删除类别/图书前检查关联数据，弹窗展示详情，确认后才执行。
4. **借阅安全检查**：删除图书前检查是否被借阅，防止数据不一致。
5. **零 XML 配置**：全部使用 MyBatis-Plus 注解 + `LambdaQueryWrapper`，代码简洁。
6. **统一分页封装**：自定义 `Page<T>` 对象，与 MP 内部分页对象解耦。
7. **VO 视图对象**：`BookVo`、`BorrowingBooksVo` 将实体与展示逻辑分离。
8. **Excel 批量导入**：基于 Apache POI 封装通用工具，支持图书和用户批量导入。
9. **动态登录表单**：角色切换时实时变更输入框 name 属性，学生用ID、管理员用用户名。

### 12.2 注意事项

1. **数据库迁移**：从旧版升级需执行 `migrate-password-bcrypt.sql` 扩宽密码列。
2. **Session 认证**：项目使用传统 Session 方式，未引入 Spring Security 完整框架。
3. **默认密码**：初始化脚本中的密码仍为明文，首次登录后会自动加密升级。
