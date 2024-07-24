认证模块使用JWT作为用户认证和授权的机制，该模块需要导入指定的SQL表

# 登录

**接口地址**:`/semi_login`

**请求方式**:`POST`

**请求参数**:

```json
{
    "username":"",
    "password":""
}
```

登录成功后会创建一个token放入响应头并返回，下次请求需要在请求头中携带此token

## 修改登录接口

内置登录接口json配置如下，被查询出的所有字段会存入JWT的payload中，并可以在[登录用户插值规则](#登录用户插值规则)
中使用，所以不要在登录json配置中查询敏感数据。

```js
  "postQ": {
    "/login": {
      "summary": "登录",
      "version": 0,
      "params": {
        "@tb": "semi_user",
        "@row": 1,
        "@bean": "captchaEnhance,loginEnhance,afterLoginEnhance",
        "username$$": "username",
        "@": "username,id,password", //查询出username，id
        "id:": [
          {
            "@tb": "semi_user_role",
            "@on": "user_id",
            "@": "role_id:roleId",//查询出roleId
          },
          {
            "@tb": "semi_user_dept",
            "@on": "user_id",
            "@": "dept_id:deptId", //查询出deptId
            "#str": "dept_id"
          }
        ]
      },
      "ruler": {
        "username": {
          "text": "username不能为空"
        },
        "password": {
          "text": "密码不能为空",
          "len>=6": "密码长度应该大于6"
        }
      }
    }
  }
```

# 登录用户插值规则

从内置登录接口查询出的username，id，roleId，deptId字段可用于插值规则

```json
{
  "@tb": "semi_role_access",
  "[role_id]$": "roleId" //$插值规则，roleId登录查询出的字段
}
```

如果用户的角色id是，2，3

那么请求参数会被替换为

```json
{  
  "@tb": "semi_role_access",
  "[role_id]$": "2,3"
}
```

# yaml配置

```yml
semi-finished:
  auth:
    captcha: true # 是否开启登录和认证的验证码校验
    token-duration: 604800 # token过期时间，单位秒
    token-key: token # token在header中的key
    admin-code: admin # 管理员的角色编码
    auth-enable: true # 是否开启登录认证
    skip:     # 跳过登录验证的路径
      /userInfo: get # 请求路径 : 请求方式，多个请求方式用逗号分隔，*号表示全部
```
