# semifinished

是一个封装了常用操作的项目，也可以作为一个依赖包引入项目

# 源码编译

```shell
git clone https://gitee.com/clxin/semifinished-core.git
cd semifinished-core
mvn install
```

在项目`pom.xml`中引入

```xml
<dependency>
    <groupId>com.semifinished</groupId>
    <artifactId>semifinished-core</artifactId>
    <version>0.2.0-Alpha</version>
</dependency>
```

# API文档

## 登录

>**接口地址**:`/login`
>
>**请求方式**:`POST`
>
>**请求参数**: 
>
>```json
>{
>    "username":"xxx",
>    "password":"xxx"
>}
>```
>
>**返回结果**：
>
>```json
>{
>    "msg": "登录成功",
>    "code": 200,
>}
>```

## 获取验证码

>**接口地址**:`/captcha`
>
>**请求方式**:`GET`
>
>**请求参数**: 无
>
>**返回结果**：
>
>```json
>{
>    "msg": "操作成功",
>    "code": 200,
>    "result": {
>        "img": "data:image/jpeg;base64,xxxxxxx",
>        "key": "719904028766638080"
>    }
>}
>```

## 注册

> **接口地址**:`/signup`
>
> **请求方式**:`POST`
>
> **请求参数**: 
>
> ```json
> {
>     "username":"xxx",
>     "password":"xxx",
>     "captcha":"xxx",//验证码 可选，需要配置semi-finished.signup-captcha=false
>     "key":"xxx"		//验证码接口返回的key 可选
> }
> ```
>
> **返回结果**：
>
> ```json
> {
>     "msg": "操作成功",
>     "code": 200
> }
> ```

## 图片上传

> 该接口会判断文件的真实类型是否图片，也可以配置限制的图片类型，如果不是图片类型，或不是指定类型其中一个，那么返回异常信息，具体的配置查看`application.yaml`配置目录
>
> **接口地址**:`/upload/image`
>
> **请求方式**:`POST`
>
> **Content-Type**: multipart/form-data
>
> **请求参数**: 
>
> ```
> file:file
> ```
>
> **返回结果**：
>
> ```json
> {
>     "msg": "操作成功",
>     "code": 200,
>     "result": "366c018d246e4d36c8b674dadb9242b8.png"//后台保存的文件名
> }
> ```

## 文件上传

> 该接口不会对文件类型做处理，直接以原始文件名的后缀作为类型保存。
>
> **接口地址**:`/upload`
>
> **请求方式**:`POST`
>
> **Content-Type**: multipart/form-data
>
> **请求参数**: 
>
> ```
> file:file
> ```
>
> **返回结果**：
>
> ```json
> {
>     "msg": "操作成功",
>     "code": 200,
>     "result": "366c018d246e4d36c8b674dadb9242b8.png"//后台保存的文件名
> }
> ```

## 新增

> **接口地址**:`common/{table}`, `common/{table}/{semiId}`
>
> `table`:表名，`semiId`:该请求的标识，具体使用查看`自定义增强规则`目录
>
> **请求方式**:`POST`
>
> **请求参数**: 
>
> ```json
> {
>     "&":"字段1",//可选
>     "字段1":"内容1",
>     "字段2":"内容2"
> }
> ```
>
> **参数规则**:
>
> `&`:表示字段1如果不存与内容1相同的数据则添加，如果存在则抛出异常。如果`&`符号指定了一个此次请求不存在的字段如字段3，那么会忽略该规则，视为一个普通的新增请求。
>
> **返回结果**：
>
> ```json
> {
>     "msg": "操作成功",
>     "code": 200
> }
> ```

## 修改

> **接口地址**:`common/{table}`, `common/{table}/{semiId}`
>
> `table`:表名，`semiId`:该请求的标识，具体使用查看`自定义增强规则`目录
>
> **请求方式**:`PUT`
>
> **请求参数**: 
>
> ```json
> {
>     "id":"1",//必选
>     "字段1":"内容",
>     "字段2":"内容"
> }
> ```

## 删除单个数据

> **接口地址**:`common/{table}/{id}`
>
> `table`:表名，`id`被删除的数据id
>
> **请求方式**:`DELETE`
>
> **返回结果**：
>
> ```json
> {
>     "msg": "操作成功",
>     "code": 200
> }
> ```

## 删除多个数据

> **接口地址**:`common/{table}`
>
> **请求方式**:`DELETE`
>
> **请求参数(必选)**: ?ids=id1,id2,id3
>
> **返回结果**：
>
> ```json
> {
>     "msg": "操作成功",
>     "code": 200
> }
> ```

## 查询规则

**接口地址**:`enhance/{table}`,`enhance/{table}/{semiId}`

`table`:表名，`semiId`:该请求的标识，具体使用查看`自定义增强规则`目录

**请求方式**:`POST`

**返回结果**：

```json
{
    "msg": "操作成功",
    "code": 200,
    "result": [
        {
            "xx": "xx",
            "xxx": "xxx"
        },
        {
            "xx": "xx",
            "xxx": "xxx"
        }
}
```

### 一些符号的灵感来源与释义

> `@`：论坛或聊天工具中代表指定
>
> `~`:位运算中的“取反”操作
>
> `:`:表示key-value的对应关系，映射
>
> `%`:sql中的模糊查询匹配符
>
> `!`:在判断式中代表“非”
>
> `&`:位运算中的“与”操作
>
> `#`:代表规则
>
> `/`:二叉树中左边为从大到小
>
> `\\`:二叉树中从小到大
>
> `^`:树结构
>
> `$`:在一些语言中使用`${字段}`的方式表示引用字段的值

### 查询全部/单条

不需要传值或者传`{}`，默认查询前500条数据，且不会携带总行数，条数限制可在`application.yaml`中配置

查询多条订单数据：`enhance/orders`

```json
{
    "msg": "操作成功",
    "code": 200,
    "result": []//未查询到数据
}
```

查询单条订单数据：`enhance/{orders}`

该规则在查询出多条的情况下取第一条

```json
{
    "msg": "操作成功",
    "code": 200,
    "result": {}//未查询到数据
}
```

### 相等

```json
{
    "字段":"内容"
}
```

### 指定字段

```json
{
    "@":"字段1,字段2:别名,..."//可在指定字段的同时设置别名
}
```

### 排除字段

```json
{
    "~":"字段1，字段2,..."
}
```

### 别名

如果指定，排除，别名三个规则有重复或冲突，优先级：排除>别名>指定

```json
{
    ":":"字段1:别名1,字段2:别名2,..."
}
```

### 模糊查询

```json
{
    "%字段":"内容",//生成 【%内容】 查询
    "字段%":"内容",//生成 【内容%】 查询
    "%字段%":"内容"//生成 【%内容%】 查询
}
```

### IN

```json
{
    "[字段]":"1,2,3"//生成 【字段 in (1,2,3)】 查询
}
```

### 不等于

```json
{
    "!字段":"内容" //生成 【字段 != 内容】 查询
}
```

### 范围

```json
{
    "<字段":"内容",// 内容<字段
    ">字段":"内容",// 内容>字段
    "字段<":"内容",// 字段<内容
    "字段>":"内容",// 字段>内容
    "<字段<":"内容1,内容2",// 内容1<字段<内容2
    ">字段>":"内容1,内容2",// 内容1>字段>内容2
}
```

### 等于登录用户信息

key与上方的规则相同，

```json
{
    "$字段":"用户字段"//如角色表有user_id字段，那么"user_id=":"id"表示生成【user_id=当前登录用户id】查询
}
```

### JOIN

> 需要配置表的关联关系，具体配置方式查看`扩展配置`目录
>
> 并且需要手动处理重复的字段
>
> ```json
> {
>     "&表名":{// 生成 table left join 表名
>         //支持上方的所有规则，包含嵌套join规则
>     },
>     "表名&":{// 生成 table inner join 表名
>         
>     }
> }
> ```
>
> 示例：
>
> 请求: `enhance/test1`
>
> ```json
> {
>     "@":"a",//指定查询test1的a字段
>     "&test2":{//test1 left join test2
>         "-":"id",//排除test2中的id字段
>         "test3&":{//test2 inner join test3
>             ":":"id:id3"//设置 test3.id别名id3
>         }
>     },
>     "test4&":{ //test1 inner join test4
>         "d":"1" // test4.d=1
>     }
> }
> ```
>
> ### 

### 分页

只要参数中存在pageNum或pageSize，那么返回值会携带总行数

```json
{
    "pageNum":1,//第几页
    "pageSize":10 //每页行数
}
```

## 增强规则

### 总和

用以计算一些字段的和

```json
{
   "+":"字段1,字段2,..."
}
```

### 格式化

转换数字或日期格式，如保留两位小数，转百分数，转换日期显示格式

```json
{
    "#n0.00":"字段1,字段2,...",//#n表示格式化数字类型
    "#dyyyy-MM-dd":"字段1,字段2,..."//#d表示格式化日期类型
}
```

### 表字典

需要配置表的关联关系，具体配置方式查看`扩展配置`目录

该规则会根据关联关系去指定的表中查询指定的字段，并根据关联关系进行数据合并。

```json
{
    ":表名":"字段1:别名1，字段2,..."
}
```

示例：

请求: `enhance/test1`

关联关系为 `test1.id=test2.test1_id`

```json
{
    "pageSize":10,
    ":test2":"a,b:c"
}
```

程序会先查询`test1`的数据集合，获取`id`集合，通过`in`查询`test2.test1_id` 的`a`字段，`b`字段数据,并设置`b`字段的别名为`c`，返回数据并拼接。

# 自定义查询规则

## 实现Params2SqlParser接口

该接口适用于确定的key，如指定字段规则的`@`符号，或需要同时获得多个参数才能确定的规则，如分页参数`pageNum`,`pageSize`。解析后的参数填入`sqlDefinition`中。

```java
/**
 * 前端参数的解析器接口
 * 适用于匹配一个确定的key
 * 如果需要匹配一个需要解析的key,可以使用{@link Value2SqlParser}
 */
public interface Params2SqlParser {

    /**
     * 如果存在匹配的key调用的方法
     *
     * @param table         表名
     * @param params        前端参数
     * @param sqlDefinition sql的定义类，所有解析出来的数据保存到里面，根据这些数据生成sql
     */
    void parse(String table, ObjectNode params, SqlDefinition sqlDefinition);
}
```

## 实现Value2SqlParser接口

`Value2SqlParser`接口由`Params2SqlParser`的实现类`Value2SqlParserExecutor`调用，传入的参数为遍历后拆分出的key，value。

`Value2SqlParser`接口适用于需要对key进行解析的规则，如模糊查询的`%字段%`规则。

```java

public interface Value2SqlParser {
    /**
     * 对前端传过来的json参数进行解析
     * 具体示例可查看实现类，如{@link LikeParser}
     *
     * @param table         表名，在调用之前对table进行过校验，可以保证能从semicache中获取字段列表
     * @param key           前端json参数key
     * @param value         前端json参数value
     * @param sqlDefinition sql的定义类，所有解析出来的数据保存到里面，根据这些数据生成sql
     * @return 是否使用了该解析器，如果为true,则该查询条件不再进行下一次解析
     */
    boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition);

}
```

# 自定义增强规则

实现对应的接口并添加到spring容器，程序会调用对应的实现类进行增强。

请求中的`semiId`参数会传入`match`方法，用于判断是否执行该增强

## 新增数据增强接口

```java
public interface InsertEnhance {
    /**
     * 判断是否执行该增强
     *
     * @param table  表名
     * @param semiId 请求标识
     * @param params 请求参数
     * @return true执行，false不执行
     */
    boolean match(String table, String semiId, Map<String, String> params);

    /**
     * 新增之前执行
     *
     * @param table  表名
     * @param params 请求参数
     */
    void beforeInsert(String table, Map<String, String> params);

    /**
     * 新增之后执行
     *
     * @param table  表名
     * @param id     新增数据的id
     * @param params 请求参数
     */
    void afterInsert(String table, int id, Map<String, String> params);
}
```

## 更新数据增强接口

```java
public interface UpdateEnhance {
    /**
     * 判断是否执行该增强
     *
     * @param table  表名
     * @param semiId 请求标识
     * @param params 请求参数
     * @return true执行，false不执行
     */
    boolean match(String table, String semiId, Map<String, String> params);

    /**
     * 修改之前执行
     *
     * @param table  表名
     * @param params 请求参数
     */
    void beforeUpdate(String table, Map<String, String> params);

    /**
     * 修改之后执行
     *
     * @param table  表名
     * @param params 请求参数
     */
    void afterUpdate(String table, Map<String, String> params);
}
```

## 登录增强接口

```java
public interface LoginEnhance {


    /**
     * 登录之前执行
     *
     * @param principal 包含登录信息
     */
    void beforeLogin(Principal principal);

    /**
     * 登录成功之后执行
     *
     * @param principal 包含登录信息
     */
    void afterLogin(Principal principal);
}
```

## 注册增强接口

```java
public interface SignupEnhance {

    /**
     * 注册之前执行
     *
     * @param principal 包含注册信息
     */
    void beforeSignup(Principal principal);

    /**
     * 注册成功之后执行
     *
     * @param principal 包含注册信息
     */
    void afterSignup(Principal principal);
}
```

## 查询增强接口

```java
public interface SelectEnhance {

    /**
     * 判断是否执行该增强
     *
     * @param table  表名
     * @param semiId 请求的id,可以用来标识一个请求
     * @param params 请求参数
     * @return true要执行，false不执行
     */
    boolean match(String table, String semiId, ObjectNode params);

    /**
     * 在解析参数之前执行
     *
     * @param table  表名
     * @param params 前端参数，该对象会传入解析器
     */
    void before(String table, ObjectNode params);


    /**
     * 参数解析完成后执行，可以直接对解析完成后的内容进行修改，并直接影响最终的sql
     *
     * @param table         表名
     * @param params        前端参数
     * @param sqlDefinition 解析完成后的sqlDefinition
     */
    void afterParse(String table, ObjectNode params, SqlDefinition sqlDefinition);

    /**
     * 非分页查询，在获取到数据之后执行
     *
     * @param table  表名
     * @param params 前端参数，这个参数是before方法执行之前深拷贝的参数
     * @param list   查询返回的数据列表
     */
    void afterQuery(String table, ObjectNode params, List<ObjectNode> list);

    /**
     * 分页查询之后执行
     *
     * @param table  表名
     * @param params 前端参数，这个参数是before方法执行之前深拷贝的参数
     * @param page   分页信息，包含查询返回的数据列表
     */
    void pageAfter(String table, ObjectNode params, Page page);
}
```

# 扩展配置

实现`ExtendConfigurer`接口

```java
@Component
public class Demo implements ExtendConfigurer {
    @Override
    public void setTableRelations(List<TableRelation> tableRelations) {
        //配置表的关联关系，有两种方式，如test1.id=test2.test1_id
        tableRelations.add(new TableRelation("test1.id","test2.test1_id"));
        tableRelations.add(new TableRelation("test1","id","test2","test1_id"));
    }


    @Override
    public void setExcludeColumns(List<Table> excludeColumns) {
        //配置排除的字段，使该字段无法被前端查询。如果前端指定了该字段，那么会抛出异常
        excludeColumns.add(new Table("user","password"));
    }


    @Override
    public void setSkipAuth(List<ApiMatch> apiMatches) {
        //配置跳过认证的接口，使用ant风格，可以匹配一种或多种请求方式，*表示匹配所有请求方式
        apiMatches.add(new ApiMatch("/test/**","get,post"));
    }
}
```

# properties配置

```java
@ConfigurationProperties("semi-finished")
public class ConfigProperties {
    /**
     * 上传文件保持路径，如果没有配置，默认使用程序运行所在目录
     */
    private String filePath;

    /**
     * 在没有指定分页参数时的最大获取行数
     */
    private int maxPageSize = 500;
    /**
     * 图片上传支持的文件类型，如果不配置，则是所有图片类型
     */
    private List<String> imageType;

    /**
     * token的有效期，单位秒
     */
    private long tokenDuration = 7 * 24 * 60 * 60;

    /**
     * 是否开启认证
     */
    private boolean authEnable = true;

    /**
     * 是否开启登录的验证码
     */
    private boolean loginCaptcha;

    /**
     * 是否开启注册的验证码
     */
    private boolean signupCaptcha;

    /**
     * token在header中的key
     */
    private String tokenKey = "token";

}
```

