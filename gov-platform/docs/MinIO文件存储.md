# MinIO 文件存储

> 记录时间：2026-11-26
> 项目用 MinIO 做文件上传存储（材料文件、图片等）

---

## MinIO 是什么

MinIO 是一个开源的对象存储服务，类似阿里云 OSS，但可以自己部署在本地。文件以"对象"的形式存在 bucket（存储桶）里，通过 URL 访问。

项目里用它来存：申请材料的文件（身份证照片、证明材料等）。上传后拿到一个 URL，把 URL 存到数据库的 `fileUrl` 字段。

---

## 下载安装

MinIO 是一个单独的 exe 文件，放在 `D:\Environment\minIO\` 目录下：

```
D:\Environment\minIO\
├── minio.exe        # 服务端
├── mc.exe           # 客户端命令行工具
├── data/            # 数据存储目录
└── minio运行.bat    # 启动脚本
```

---

## 启动

```bash
cd D:\Environment\minIO
./minio.exe server ./data --console-address ":9001"
```

启动后：
- **API 地址**：`http://localhost:9000`（后端代码连这个）
- **控制台**：`http://localhost:9001`（浏览器打开管理文件）
- **默认账号**：`minioadmin`
- **默认密码**：`minioadmin`

---

## 创建 Bucket

浏览器打开 `http://localhost:9001`：

1. 左侧菜单 → **Buckets**
2. 点 **Create Bucket**
3. 名字填 `gov-files` → 点 **Create**

> 也可以不手动建，后端服务启动时会自动创建（`MinioUtils.init()` 里有检查逻辑）。

---

## 后端怎么配的

### 1. 依赖

MinIO 的 Java SDK 加在 `gov-common-core/pom.xml` 里，所有服务都能用：

```xml
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>
```

### 2. 工具类

`gov-common-core/src/main/java/com/haikou/government/common/core/utils/MinioUtils.java`

核心方法：
- `upload(MultipartFile file)` → 上传文件，返回访问 URL（7天有效）
- `delete(String objectName)` → 删除文件

加了 `@ConditionalOnProperty(name = "minio.endpoint")`，只有配了 `minio.endpoint` 的服务才会加载这个 Bean，其他服务不受影响。

### 3. 配置

在需要用文件存储的服务的 `application.yml` 里加：

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: gov-files
```

### 4. 上传接口

`gov-approval` 里加了通用的上传接口：

```
POST /approval/file/upload
参数：file（multipart/form-data）
返回：{ url: "http://localhost:9000/gov-files/uploads/...", name: "xx.jpg", size: "12345" }
```

---

## 前端怎么调

```js
import { uploadFile } from '@/api/approval'

const formData = new FormData()
formData.append('file', file.raw)
const res = await uploadFile(file.raw)
console.log(res.data.url)  // 拿到 URL，存到业务表的 fileUrl 字段
```

注意：`uploadFile` 里**不要手动设 `Content-Type: multipart/form-data`**，让浏览器自动加 boundary，否则文件上传会失败。

---

## 文件存储路径

上传的文件在 MinIO 里按日期分目录：

```
gov-files/
└── uploads/
    └── 2026/
        └── 11/
            └── 26/
                ├── a1b2c3d4e5.jpg
                └── f6g7h8i9j0.pdf
```

文件名用 UUID 生成，不会重复。

---

## 常见问题

**Q：上传报"文件上传失败"？**
A：先检查 MinIO 有没有启动（`http://localhost:9001` 能不能打开）。再看 gov-approval 的日志，通常会显示具体错误。

**Q：图片在浏览器里打不开？**
A：MinIO 返回的是带签名的临时 URL（7天有效），过期了就打不开。正式项目应该配置 bucket 策略为公开读，或者每次访问都重新获取 URL。

**Q：其他服务也要用文件存储怎么办？**
A：引 `gov-common-core` 依赖（本来就有），然后在 `application.yml` 加 `minio.*` 配置，注入 `MinioUtils` 就能用。

**Q：默认密码安全吗？**
A：本地开发无所谓。上线的话必须改，启动时加环境变量：
```bash
set MINIO_ROOT_USER=你的用户名
set MINIO_ROOT_PASSWORD=你的密码
./minio.exe server ./data --console-address ":9001"
```

**Q：怎么查看已上传的文件？**
A：浏览器打开 `http://localhost:9001`，进 `gov-files` bucket 就能看到 `uploads/` 目录下的文件。
