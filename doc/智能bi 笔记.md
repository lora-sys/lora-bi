<h1 id="ddbe7"> 初始化前端项目</h1>
<h2 id="rocSs">使用 npm</h2>
```python
npm i @ant-design/pro-cli -g
pro create myapp
```

```python
? 🐂 使用 umi@4 还是 umi@3 ? (Use arrow keys)
❯ umi@4
  umi@3

? 🚀 要全量的还是一个简单的脚手架? (Use arrow keys)
❯ simple
  complete
$ cd myapp && tyarn
// 或
$ cd myapp && npm install
```

<h3 id="Vpri0"> 删除目录中i18n 国际化</h3>
<h4 id="D2YCy"> 通过package.json 中 i18n 脚本删除</h4>
<h2 id="a4H4V">通[@UmiJS](undefined/umijs)/openapi 生成器生成前端请求代码 swager</h2>
[https://www.npmjs.com/package/@umijs/openapi](https://www.npmjs.com/package/@umijs/openapi)

```plain
npm i --save-dev @umijs/openapi
# or
pnpm add -D @umijs/openapi
# or
yarn add -D @umijs/openapi
```

<h4 id="HGZze"> 创建配置文件`<font style="color:rgb(32, 32, 32);">openapi2ts.config.ts</font>`<font style="color:rgb(32, 32, 32);"> or </font>`<font style="color:rgb(32, 32, 32);">.openapi2tsrc.ts</font>`</h4>
```plain
export default [
  {
    schemaPath: 'http://app.swagger.io/v2/swagger.json',
    serversPath: './servers/app',
  },
  {
    schemaPath: 'http://auth.swagger.io/v2/swagger.json',
    serversPath: './servers/auth',
  }
]
```

<h4 id="QP8L1">在配置package.json 写一个脚本</h4>
```plain
{
  "scripts": {
    "openapi2ts": "openapi2ts"
  }
}
```

启动运行生成对应接口代码，此时后端springboot项目应该启动，后src目录下会生成对应api目录

<h2 id="v7p6V"> 使用 ant design pro 自带的 openapi 工具，根据后端的 swagger 接口文档数据自动生成对应 的请求 service 代码。</h2>
<h3 id="vqWt7"> 注意：前端须更改对应的请求地址为你的后端地址，方法：在 app.tsx 里修改 request.baseURL  </h3>
<h3 id="MQyxx"></h3>
<h1 id="KdwA2">后端项目初始化</h1>
<h2 id="aAIXr">修改项目结构配置java版本为11之间</h2>
<h2 id="B1Vh6"> 创建数据源配置数据库来连接</h2>
<h2 id="VNSYP">在application.yaml配置数据库账号密码，启动运行，访问</h2>
```plain
# 数据库初始化
# @author lora

-- 创建库
create database if not exists lora_bi ;

-- 切换库
use lora_bi;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 帖子表
create table if not exists post
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '帖子' collate = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子点赞';

-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子收藏';

-- 图表表
create table if not exists chart
(
    id bigint auto_increment comment 'id' primary key,
    goal text null comment '分析目标',
    chartData text null comment '图表数据',
    chartType varchar(128) null comment '图表类型',
    genChart text null comment '生成的图表数据',
    genResult text null comment '生成的分析结论',
    userId bigint null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete tinyint default 0 not null comment '是否删除'
    ) comment '图表信息表' collate = utf8mb4_unicode_ci;
```

