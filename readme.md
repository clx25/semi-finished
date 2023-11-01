# semi-finished

是一个封装了常用操作的项目，也可以作为一个依赖包引入项目

# API文档

## 查询规则

**接口地址**:`/enhance`

**请求方式**:`POST`
**完整的查询示例**:

```json
{
    "@tb":"user",
    "@":"name,sex,age",
    "sex:":{
        "@tb":"user_sex",
        "@on":"id",
        "@":"sex",
        "@row":1
    }
}
```

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

    ]
}
```

### 一些符号的灵感来源与释义

> `@`:论坛或聊天工具中代表指定
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
> `/`:排序二叉树中左边为从大到小
> 
> `\\`:排序二叉树中从小到大
> 
> `^`:树结构
> 
> `$`:在一些语言中使用`${字段}`的方式表示引用字段的值

### 查询全部

查询该表的所有未排除字段，如果没有指定分页参数，默认查询前200条，该限制和未排除字段可配置

```json
{
    "@tb":"表名"
}
```

### 指定查询字段

```json
{
    "@":"字段1,字段2,..."
}
```

可在指定字段的同时设置别名

```json
{
    "@":"字段1:别名1,字段2:别名2,..."
}
```

### 排除字段

这种排除方式会在SQL层面排除这个查询字段

```json
{
    "~":"字段1，字段2,..."
}
```

### 别名

如果指定字段，排除，别名三个规则有重复或冲突，优先级：排除>别名>指定字段

如果别名是特殊字符（非英文字母开头，后续为英文字母，数字，下划线），那么会先使用系统生成的别名查询，再通过修改查询结果字段的方式实现别名

```json
{
    ":":"字段1:别名1,字段2:别名2,..."
}
```

### 相等

生成 `字段=内容` 查询

```json
{
    "字段":"内容"
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

生成 `字段 in (1,2,3)`查询

```json
{
    "[字段]":"1,2,3"
}
```

### 不等于

生成 `字段!=内容`查询

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

### 排序

```json
{
    "/":"字段名",//根据字段名内容从大到小排序
    "\\":"字段名"//根据字段名内容从小到大排序
}
```

### 括号、或查询

使用`|`作为前缀就表示或查询，可以在任何查询条件中添加，一般与括号查询一起使用。字段后使用`{}`表示与该字段括号在一起查询，`{}`内的`value`参数表示外层字段的查询内容，`value`字段可以配置

如下查询解析结果为: where （ 字段1=字段2 or 字段2=字段3）and 字段3=内容3

```json
{
    "@tb":"表名",
    "字段1":{
        "value":"内容1",
        "|字段2":"内容2"
    },
    "字段3":"内容3"
}
```

### 子表查询

把查询结果作为一张表再进行查询

```json
{
    "@tb":{
        "@tb":"表名",
        "@":"字段1:别名1,字段2:别名2"
        //可以使用所有查询规则
    },
    "@":"别名1"//使用子表查询，如果子表使用了别名，那么外部表也只能使用对应别名
}
```

### 表字典查询

`:`表示把一个字段映射为其他字段的值。如用户表的`id`字段与订单表的`user_id`对应，那么可以使用以下查询，`@on`就是两个表的关联关系`user.id=order.user_id`。该查询会先查询`user`表，获取`id`后用`in`查询去查`order`表，最后合并。由于id名称重复，所以使用别名规则修改`order.id`的返回字段名。暂不支持深度映射和一个字段对应多个表映射。

```json
{
    "@tb":"user",
    "id:":{
        "@tb":"order",
        "@on":"user_id",
        ":":"id:orderId"
    }
}
```

### JOIN查询

与表字典查询类似，只是把`:`改为了`&`，`&`在字段右方时表示`left join`，在右方时表示`inner join`。该查询解析结果为`user inner join order on user.id=order.user_id`。支持深度`join`，就是`oder`表的查询也可以使用`join`规则

```json
{
    "@tb":"user",
    "id&":{
        "@tb":"order",
        "@on":"user_id",
        ":":"id:orderId",
        //"id&":{}这里也可以使用join规则
    }
```

### 分组查询、聚合函数

目前聚合函数只支持`min,max,avg,sum,count`

`@group`就是指定` group by`字段

如果`@group`的字段没有覆盖指定的查询字段，那么会先把没有覆盖的字段排除进行第一次查询，再查询未覆盖的字段，最后进行合并。

```json
{
    "@tb":"表名",
    "@":"字段1,count(*),max(字段3),字段4"
    "@group":"字段1,字段2"
}
```

### 分页

只要参数中存在pageNum或pageSize，那么返回值会携带分页信息，分页参数字段可以

```json
{
    "pageNum":1,//第几页
    "pageSize":10 //每页行数
}
```

### 指定返回的行

**不能与分页参数同时使用**

返回查询结果中指定范围的行，行号从1开始，包含指定行。如果只有一个参数，那么表示返回指定的那一行，并且返回格式是一个对象。

```json
{
    "@row":"1,5"
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
