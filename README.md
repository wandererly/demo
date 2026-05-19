# HRM Enterprise Demo

一个面向企业人事管理场景的 HRM 演示系统，覆盖组织架构、员工主数据、考勤请假、绩效、薪酬、统一审批、员工自助、权限控制和报表看板。系统采用 Spring Boot 后端与 Vue/Vite 前端，前端构建产物由后端统一托管。

## 技术栈

- 后端：Spring Boot、Spring Security、JWT、MyBatis
- 前端：Vue 3、Vite、原生 CSS
- 数据库：MySQL 8+
- 权限：RBAC 权限点 + 员工自助数据隔离
- 构建：Maven + npm/Vite

## 核心流程

系统按真实 HR 日常流程组织：

```text
部门/岗位建设 -> 员工入职 -> 考勤/请假/绩效维护 -> 薪酬工资条 -> 统一审批 -> 报表分析
```

主要能力：

- 管理后台：部门管理、岗位与晋升、员工管理、考勤管理、请假管理、绩效管理、薪酬管理、统一审批、报表看板、权限管理、系统设置
- 员工自助：我的档案、我的考勤、上班打卡、下班打卡、补卡申请、加班申请、我的请假、我的绩效、我的工资条、我的通知
- 审批闭环：请假、绩效、转正、调岗、离职、补卡、加班、薪酬和调薪统一进入审批中心
- 考勤闭环：每天自动生成考勤基线，员工自助打卡，异常通过补卡审批修正，月度汇总可用于薪酬
- 薪酬闭环：按考勤、请假、绩效生成工资条，薪酬审批通过后锁定

## 运行环境

- JDK 21 或更高
- Maven 3.8+
- Node.js 18+
- MySQL 8+

## 快速开始

1. 创建数据库并配置连接

```sql
CREATE DATABASE hrm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `src/main/resources/application.yml` 中的 `spring.datasource` 为本机数据库账号密码。

2. 构建前端

前端源码位于 `frontend/`，构建后会输出到 `src/main/resources/static`：

```powershell
cd frontend
npm install
npm run build
cd ..
```

3. 启动后端

可以使用 IDE 直接运行 `com.hrm.HrmApplication`，也可以命令行运行：

```powershell
mvn -s .mvn-settings.xml -DskipTests package
java -jar target/hrm-0.1.0-SNAPSHOT.jar
```

4. 访问系统

- 首页：`http://localhost:8080/`
- 登录页：`http://localhost:8080/login`

## 默认账号

演示数据会在非生产环境启动时自动初始化。

- 管理员：`admin / admin123`
- 员工账号：按员工编号小写生成，例如 `E3001 -> e3001`
- 员工初始密码：`员工编号@123`，例如 `E3001@123`

员工编号按部门延续：部门 ID 为 `3` 时，员工账号为 `e3001`、`e3002`、`e3003`。

> 如果遇到 403 或登录状态异常，请清除浏览器 LocalStorage 中的 `hrm_token` 后重新登录。

## 员工如何考勤

员工登录后进入“员工自助 -> 我的考勤”：

1. 点击“上班打卡”，系统使用服务器当前时间记录签到。
2. 点击“下班打卡”，系统使用服务器当前时间记录签退。
3. 09:00 后签到会标记为迟到，18:00 前签退会标记为早退。
4. 忘记打卡时提交补卡申请，审批通过后自动修正考勤记录。
5. 加班通过“加班申请”提交，审批通过后进入月度考勤汇总。

系统每天会为在职员工自动生成当天考勤基线，员工打卡后由 `ABSENT` 自动修正为正常、迟到或早退。

## 目录结构

```text
src/main/java                  后端源码
src/main/resources/static      Vue/Vite 构建后的前端静态资源
src/main/resources/application.yml  后端配置
frontend/                      Vue 前端源码
docs/                          项目文档
hrm.sql                        数据库结构参考
```

## 常见问题

### 端口 8080 被占用

停止占用 8080 的进程，或在 `application.yml` 中修改 `server.port`。

### GitHub 推送失败

如果出现 `Failed to connect to github.com port 443`、`Connection timed out` 或 `Connection was reset`，通常是网络无法连接 GitHub。切换网络、配置代理，或改用 SSH 远端后重试。

### 接口提示 Request failed

- 确认后端已启动
- 清除浏览器 LocalStorage 中的旧 token
- 检查数据库是否已初始化演示数据

### 前端页面刷新后 404

项目已配置 SPA 路由转发。若仍出现问题，请重新构建前端并重启后端。

## 验收清单

- 管理员可登录并进入管理后台
- 员工账号可登录并进入员工自助
- 员工可上班打卡、下班打卡，重复打卡有中文提示
- 员工可提交补卡和加班申请
- 管理员可在统一审批中心审批补卡/加班
- 审批通过后考勤记录或月度汇总正确变化
- 部门、岗位、员工、请假、绩效、薪酬、报表页面可正常访问

