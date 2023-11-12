# semi-finished

是一个封装了常用操作的项目，也可以作为一个依赖包引入项目

# API文档

## 一些符号的灵感来源与释义

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

## 查询规则（在SQL中生效的规则）

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

### 指定表名

**所有请求都需要指定表名**

查询该表的所有未排除字段，如果没有指定分页参数，默认查询前200条，该限制和未排除字段可配置

```json
{
    "@tb":"表名"
}
```

### 指定查询字段

当没有`@`参数时，默认查询指定表的所有未排除字段

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

在字段的前后加上`%`符号表示查询内容的对应位置添加`%`符号

```json
{
    "%字段":"内容",//生成 【%内容】 查询
    "字段%":"内容",//生成 【内容%】 查询
    "%字段%":"内容"//生成 【%内容%】 查询
}
```

### IN

使用`[]`包裹字段，生成 `字段 in (1,2,3)`查询

```json
{
    "[字段]":"1,2,3"
}
```

### 不等于

在字段前添加`!`符号，生成 `字段!=内容`查询

```json
{
    "!字段":"内容" //生成 【字段 != 内容】 查询
}
```

### 范围

在字段前后添加`<`或`>`符号，表示在查询中在对应位置添加对应符号，当冲突时抛出异常。

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

由于json中的`\`表示转义符，所以使用`\\`表示从小到大排序

```json
{
    "/":"字段名",//根据字段名内容从大到小排序
    "\\":"字段名"//根据字段名内容从小到大排序
}
```

### 括号、或

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

如果`@group`的字段覆盖了所有查询字段，那么会直接在SQL中查询

如果没有完全覆盖，那么主要逻辑会在增强中执行，默认只会查询已覆盖的字段，然后在增强中根据返回字段的值通过`in`查询未覆盖的字段，再合并结果。

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

### 插值规则

插值规则是查询规则中第一个执行的规则，用于对参数值进行替换。在key的末尾添加`$`符号表示这是一个插值规则

如现在想创建一个规则，某个值等于一个随机数，那么可以这样设计

```json
{
    "id$":"random"
}
```

此处的`random`不是实际的值，而是一个需要去替换的`key`

然后去实现`Interpolation`接口

```java
@Component
public class RandomInterpolation implements Interpolation {

    /**
     * 匹配请求参数，判断是否执行该插值规则
     *
     * @param key             请求参数的key，已经去除末尾的$符号
     * @param interpolatedKey 插值key
     * @return true表示使用该类获取实际值，false表示不使用
     */
    @Override
    public boolean match(String key, String interpolatedKey) {
        return "random".equals(interpolatedKey);
    }

    /**
     * 获取变量对应的实际值
     *
     * @param table           表名
     * @param interpolatedKey 插值key
     * @param key             请求参数的key，已经去除末尾的$符号
     * @param sqlDefinition   SQL定义信息
     * @return 变量对应的实际值
     */
    @Override
    public JsonNode value(String table, String key, String interpolatedKey, SqlDefinition sqlDefinition) {
        return JsonNodeFactory.instance.numberNode(new Random().nextInt());
    }
}
```

如果`value`方法返回的随机数是5，那么在该规则执行后请求的参数会被替换为

```json
{
    "id":"5"
}
```

## 增强规则（查询后对结果进行处理的规则）

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

### 指定返回的行

**不能与分页参数同时使用**

返回查询结果中指定范围的行，行号从1开始，包含指定行。如果只有一个参数，那么表示返回指定的那一行，并且返回格式是一个对象。

```json
{
    "@row":"1,5"
}
```

### 总和

用以计算一些字段的和，并添加到最后一行

```json
{
   "+":"字段1,字段2,..."
}
```

## 结果替换规则

对查询结果指定字段的值进行替换或者格式化，前缀`#`表示是替换规则，后面的字符是规则内容，由具体实现类自定义。

### 数字格式化

`num0.00`是规则内容，由不同的实现类自定义。`num`表示这是一个数字的替换规则，`0.00`是格式化规则，该规则由`DecimalFormat`实现，所以可以使用`DecimalFormat`的所有规则。如果`DecimalFormat`无法解析该规则，那么会抛出异常。

```json
{
    "#num0.00":"字段1,字段2..."//把指定字段的数字格式化为四舍五入保留两位小数
}
```

### 转json

把指定字段的值转为json格式，如果无法转为json，则抛出异常

```json
{
    "#json":"字段1,字段2..."
}
```

### 默认

`def`表示默认规则，后面的字符表示当指定字段结果为`null`时，替换的字符。

```json
{
    "#def123":"字段1,字段2..."//表示指定字段如果为null，那么返回123
}
```

### 日期时间格式化

`time`表示时间规则，后面的字符串表示指定字段的时间格式化规则。该规则使用`DateTimeFormatter`实现，所以`time`后面能使用`DateTimeFormatter`支持的所有规则。

对于数据的日期格式，目前只支持`yyyy-MM-dd HH:mm:ss`与`yyyy-MM-dd`两种。

```json
{
    "#timeyyyy-MM":"字段1,字段2..."//把指定字段的日期字符串转为yyyy-MM格式
}
```

### Booean格式

空集合，空字符串(去除空格)，字符串"false"返回`false`，其他返回`true`

```json
{
    "#boolean":"字段1,字段2..."
}
```

# 自定义查询规则

有两种方式可以实现自定义查询规则，`semi-finished`提供了`ParamsParser`和`KeyValueParamsParser`两个接口，可供不同情况下选择使用。这两个接口的本质都是在实现类中对前端传入的参数进行解析，并把解析结果存入`SqlDefinition`中。这两个接口都继承了`Ordered`接口，所以需要指定解析类的顺序。

## 实现ParamsParser接口

该接口适用于需要多个参数配合的规则，如分页规则。

如果在该接口中解析的规则不希望在之后`KeyValueParamsParser`中再解析的话，应该使用`remove`方法删除。

```java
public interface ParamsParser extends Ordered {
    /**
     * 解析参数接口方法
     * 为了避免该方法解析过的参数在{@link KeyValueParamsParser}中再解析一次，
     * 所以需要把解析过的参数删掉
     *
     * @param params        请求参数
     * @param sqlDefinition SQL定义信息
     */
    void parser(ObjectNode params, SqlDefinition sqlDefinition);
}
```

## 实现KeyValueParamsParser接口

`KeyValueParamsParser`接口适用于需要对key进行解析的规则，如模糊查询规则，范围查询规则等。该方法把请求参数进行了遍历，将每一项的key和value传入进行解析。

`KeyValueParamsParser`接口的执行类也是`ParamsParser`接口的实现类，所以如果想相对于`ParamsParser`接口的某个实现类排序，那么只能实现`ParamsParser`接口。

```java
public interface KeyValueParamsParser extends Ordered {
    /**
     * 对前端传过来的json参数进行解析
     * 具体示例可查看实现类，如{@link LikeParamsParser}
     *
     * @param table         表名，在调用之前对table进行过校验，可以保证能从semicache中获取字段列表
     * @param key           前端json参数key
     * @param value         前端json参数value
     * @param sqlDefinition SQL定义信息
     * @return 是否使用了该解析器，如果为true,则该查询条件不再进行下一次解析
     */
    boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition);
}
```

# 自定义增强规则

## 查询增强接口

对结果进行处理，一对多的group查询，表字典查询，格式化数据，脱敏数据等功能就是使用增强接口实现。

```java
public interface ServiceEnhance {


    /**
     * 判断请求是否使用该增强,默认使用
     *
     * @param sqlDefinition SQL定义信息
     * @return true使用增强 ，false不使用增强
     */
    default boolean support(SqlDefinition sqlDefinition) {
        return true;
    }


    /**
     * 在参数解析参数之前执行
     *
     * @param sqlDefinition SQL定义信息
     */
    default void beforeParse(SqlDefinition sqlDefinition) {

    }


    /**
     * 参数解析完成后执行，可以直接对解析完成后的内容进行修改，并直接影响最终的SQL
     *
     * @param sqlDefinition SQL定义信息
     */
    default void afterParse(SqlDefinition sqlDefinition) {

    }


}
```

```java
public interface AfterQueryEnhance extends ServiceEnhance {


    /**
     * 查询之后执行，无论是否有分页规则，都会包装到page中
     * 可以在此处获取查询后的数据，进行处理
     *
     * @param page          分页信息，包含查询返回的数据列表
     * @param sqlDefinition SQL定义信息
     */
    default void afterQuery(Page page, SqlDefinition sqlDefinition) {

    }
}
```

# 自定义结果替换规则

结果替换规则由增强类`ValueReplaceEnhance`实现，这也是一个增强规则，在这个增强类中定义了`#`规则和使用`ValueReplace`接口作为替换规则的实现方式。

#### 实现ValueReplace接口

replace方法第一个参数是前端参数解析后的SQL定义信息

第二个参数是规则去掉#后的字符串，如前端参数传递了一个数字格式化规则

```json
{
    "#num0.00":"score"
}
```

那么`pattern`的值就是`num0.00`。
第三个参数是返回结果中`score`对应的值

```java
public interface ValueReplace {

    /**
     * 替换原始的值
     *
     * @param sqlDefinition SQL定义信息
     * @param pattern       替换规则
     * @param value         返回数据的值
     * @return 用这个返回的值替换原始的值
     */
    JsonNode replace(SqlDefinition sqlDefinition, String pattern, JsonNode value);

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
