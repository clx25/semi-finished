# SEMI-FINISHED

是一个封装了常用操作的项目，更多的功能还在开发中。

semi-finished可以使用一个查询接口，通过传递不同的json参数，实现基础的查询功能，并且可以通过扩展接口实现查询规则与查询结果处理的自定义。

# 数据源配置

semi-finished支持多数据源，如下配置中，`master`与`test`是数据源的名称，配置数据源的同时可配置[表名字段名映射](#表名，字段名映射，排除字段配置)，master数据源必须存在，如不存在则会提示异常。

```yml
spring:
  datasource:
    master:
      driver-class-name: com.mysql.cj.jdbc.Driver
      jdbc-url: xxxx
      username: root
      password: 123456
    test:
      driver-class-name: com.mysql.cj.jdbc.Driver
      jdbc-url: xxxxx
      username: root
      password: 123456
```

# API文档

## 一些查询规则符号的灵感来源与释义

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
> `\\`:排序二叉树中右边为从小到大
> 
> `^`:树结构
> 
> `$`:在一些语言中使用`${字段}`的方式表示引用字段的值

## 查询规则

**接口地址**:`/enhance`

**请求方式**:`POST`

**完整的查询示例**:

```js
{
    "@tb":"user",
    "@":"name,gender,age",
    "gender:":{
        "@tb":"user_gender",
        "@on":"id",
        "@":"name:genderName",
        "@row":1
    }
}
```

**返回结果**：

```js
{
    "msg": "操作成功",
    "code": 200,
    "result": [
        {
            "name": "xx",
            "gender": "xxx",
            "age":26,
            "genderName":"xxx"
        },
        {
            "name": "aaa",
            "gender": "aaa",
            "age":23,
            "genderName":"aa"
        }
    ]
}
```

### 指定数据源

默认数据源为`master`，该名称可在yml配置文件中修改。如未指定数据源，则使用默认数据源。暂不支持多数据源混合查询。

`@db`为固定写法，后面的`master`是数据源名称，关于数据源名称可查看[数据源配置](#数据源配置)

```js
{
    "@db":"master"
}
```

### 指定表名

**所有请求都需要指定表名**

`@tb`为固定写法，表示指定表名。查询该表的所有未[排除字段](表名，字段名映射，排除字段配置)，这样可能导致一次性返回过多数据，可以在[yaml配置](yaml配置)中配置没有分页参数的情况下最大返回行数。

```js
{
    "@tb":"表名"
}
```

### 指定查询字段

当没有`@`参数时，默认查询指定表的所有未[排除字段](表名，字段名映射，排除字段配置)

```js
{
    "@":"字段1,字段2,..."
}
```

可在指定字段的同时设置别名，格式为 `字段:别名`

```js
{
    "@":"字段1:别名1,字段2:别名2,..."
}
```

### 排除字段

在SQL层面排除这个查询字段

```js
{
    "~":"字段1，字段2,..."
}
```

### 别名

如果指定字段，排除，别名三个规则有重复，优先级：排除>别名>指定字段

如果别名是特殊字符（非英文字母开头，后续为英文字母，数字，下划线），那么会先使用系统生成的别名查询，再通过修改查询结果字段的方式实现别名

```js
{
    ":":"字段1:别名1,字段2:别名2,..."
}
```

### 相等

生成 `字段=内容` 查询

```js
{
    "字段":"内容"
}
```

### 模糊查询

在字段的前后加上`%`符号表示查询内容的对应位置添加`%`符号

```js
{
    "%字段":"内容",//生成 【%内容】 查询
    "字段%":"内容",//生成 【内容%】 查询
    "%字段%":"内容"//生成 【%内容%】 查询
}
```

### IN

使用`[]`包裹字段，生成 `字段 in (1,2,3)`查询

```js
{
    "[字段]":"1,2,3"
}
```

可以使用数组的方式

```js
{
    "[字段]":[1,2,3]
}
```

多个字段的in查询，同样可以使用数组的方式

```js
{
    "[col1,col2]":"(value1,value2),(value3,value4)",
    "[col1,col2]":["value1,value2","value3,value4"]
}
```

### 不等于

在字段前添加`!`符号，生成 `字段!=内容`查询

```js
{
    "!字段":"内容"
}
```

### 范围

在字段前后添加`<`或`>`符号，表示在查询中在对应位置添加对应符号，当冲突时抛出异常。

```js
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

```js
{
    "/":"字段名",//根据字段名内容从大到小排序
    "\\":"字段名"//根据字段名内容从小到大排序
}
```

### 括号、或

使用`|`作为前缀就表示或查询，可以在任何查询条件中添加，一般与括号查询一起使用。字段后使用`{}`表示与该字段括号在一起查询，`{}`内的`value`参数表示外层字段的查询内容，`value`字段可以[配置](yaml配置)

如下查询解析结果为: where （ 字段1=内容1 or 字段2=内容2）and 字段3=内容3

```js
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

```js
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

使用`&`符号表示join查询，`&`在字段左方时表示`left join`，在右方时表示`inner join`
。如下方的查询参数，`id`是`user`表的关联字段，`@on`指定的`user_id`表示`order`表的关联字段，解析结果为`user inner join order on user.id=order.user_id`。支持深度`join`，就是`oder`表的查询也可以使用`join`规则

```js
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

暂不支持复杂情况下的group查询

```js
{
    "@tb":"表名",
    "@":"字段1,count(*),max(字段3),字段4"
    "@group":"字段1,字段2"
}
```

### 分页

只要参数中存在pageNum或pageSize其中一个，那么返回值会携带分页信息，缺少的`pageNum`默认为1，缺少的`pageSize`默认为10，分页参数字段可以在yaml文件中[配置](yaml配置)

```js
{
    "pageNum":1,//第几页
    "pageSize":10 //每页行数
}
```

### 插值规则

插值规则是查询规则中第一个执行的规则，用于对参数值进行替换。在key的末尾添加`$`符号表示这是一个插值规则

如现在想创建一个规则，某个字段等于一个随机数，那么可以这样设计一个规则

```js
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
    public boolean match(String key, JsonNode interpolatedKey) {
        return "random".equals(interpolatedKey.asText());
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
    public JsonNode value(String table, String key, JsonNode interpolatedKey, SqlDefinition sqlDefinition) {
        return JsonNodeFactory.instance.numberNode(new Random().nextInt());
    }
}
```

如果`value`方法返回的随机数是5，那么在该规则执行后请求的参数会被替换为

```js
{
    "id":"5"
}
```

### 表字典查询

与join规则基本相同，只是把`&`改为`:`，表示把一个字段映射为其他字段的值。如用户表的`id`字段与订单表的`user_id`对应，那么可以使用以下查询，`@on`也与`join`规则相同，是两个表的关联关系`user.id=order.user_id`。

该查询会先查询`user`表，获取`id`后用`in`查询去查`order`
表，最后合并。由于id名称重复，所以使用别名规则修改`order.id`的返回字段名。暂不支持深度映射和一个字段对应多个表映射。

```js
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

```js
{
    "@row":"1,5"
}
```

## 结果替换规则

对查询结果指定字段的值进行替换或者格式化，前缀`#`表示是替换规则，后面的字符是规则内容，由具体实现类自定义。

### 数字格式化

`num0.00`是规则内容，由不同的实现类自定义。`num`表示这是一个数字的替换规则，`0.00`是格式化规则，该规则由`DecimalFormat`实现，所以可以使用`DecimalFormat`支持的所有规则。如果`DecimalFormat`无法解析该规则，那么会抛出异常。

```js
{
    "#num0.00":"字段1,字段2..."//把指定字段的数字格式化为四舍五入保留两位小数
}
```

### 转json

把指定字段的值转为json格式，如果无法转为json，则抛出异常

```js
{
    "#json":"字段1,字段2..."
}
```

### 默认

`def`表示默认规则，后面的字符表示当指定字段结果为`null`时，替换的字符。

```js
{
    "#def123":"字段1,字段2..."//表示指定字段如果为null，那么返回123
}
```

### 日期时间格式化

`time`表示时间规则，后面的字符串表示指定字段的时间格式化规则。该规则使用`DateTimeFormatter`实现，所以`time`后面能使用`DateTimeFormatter`支持的所有规则。

对于数据的日期格式，目前只支持`yyyy-MM-dd HH:mm:ss`与`yyyy-MM-dd`两种。

```js
{
    "#timeyyyy-MM":"字段1,字段2..."//把指定字段的日期字符串转为yyyy-MM格式
}
```

### Booean格式

空字符串(去除空格)，字符串"false"返回`false`，其他返回`true`，如果是集合，则会深度获取数据再转换。

```js
{
    "#boolean":"字段1,字段2..."
}
```

### 脱敏规则

脱敏规则也是结果替换规则的一部分，脱敏规则需要通过接口配置

```java
/**
 * 扩展配置
 */
public interface CoreConfigurer {


    /**
     * 添加脱敏规则，如果添加了自定义脱敏器，那么就不需要设置left，right
     */
    default void addDesensitize(List<Desensitization> desensitize) {
    }


}
```

脱敏规则有多种配置方式

1. 配置左右保留字符串的数量，如左边保留3个字符，右边保留4个字符
   
   ```java
   @Component
   public class TestCoreConfigurer implements CoreConfigurer {
   
       @Override
       public void addDesensitize(List<Desensitization> desensitize) {
           Desensitization build = Desensitization.builder().table("info")
                   .column("title")
                   .left(3)
                   .right(4)
                   .build();
           desensitize.add(build);
       }
   }
   
   ```

2. 配置左右保留字符的百分比，如左边保留20%，右边保留30%
   
   ```java
    Desensitization build = Desensitization.builder().table("info")
                   .column("title")
                   .left(0.2)
                   .right(0.3)
                   .build();
           desensitize.add(build);
   ```

3. 使用自定义方法自行处理
   
   ```java
    Desensitization build = Desensitization.builder().table("info")
                   .column("title")
                   .desensitize((title) -> "a" + title + "c")
                   .build(););
       }
   }
   ```

    2. 配置左右保留字符的百分比

# 自定义查询规则

有两种方式可以实现自定义查询规则，`semi-finished`提供了`ParamsParser`和`KeyValueParamsParser`两个接口，可供不同情况下选择使用。这两个接口的功能都是对前端传入的参数进行解析，并把解析结果存入`SqlDefinition`中。这两个接口都继承了`Ordered`接口，所以需要指定解析类的顺序。

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

对结果进行处理，一对多的group查询，表字典查询，格式化数据，脱敏数据等功能就是使用查询规则与增强接口共同实现。

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

结果替换规则由增强类`ValueReplaceEnhance`实现，这也是一个增强规则，在这个增强类中定义了`#`符号作为替换规则标识和使用`ValueReplace`接口作为替换规则的实现方式。

#### 实现ValueReplace接口

replace方法第一个参数是前端参数解析后的SQL定义信息

第二个参数是规则去掉#后的字符串，如前端参数传递了一个数字格式化规则

```js
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
/**
 * 扩展配置
 */
public interface CoreConfigurer {


    /**
     * 添加脱敏规则，如果添加了自定义脱敏器，那么就不需要设置left，right
     */
    default void addDesensitize(List<Desensitization> desensitize) {
    }


}

```

# yaml配置

```yml
semi-finished:
  core:
    page-size-key: pageSize  # 分页参数每页多少行的key
    page-num-key: pageNUm    # 分页参数第几页的key
    data-source: master      # 默认数据源名称
    max-page-size: 200       # 当没有分页参数时的最大获取行数
    page-normalized: true    # 分页参数合理化
    brackets-key: value      # 括号规则中值的key
    datacenter-id: 1         # 数据中心id,用于雪花算法
    machine-id: 1            # 机器标识,用于雪花算法
```

# 表名，字段名映射，排除字段配置

为了避免前端直接使用真实的表名与字段名，可以在[配置数据源](#数据源配置)的同时，配置表名，字段名映射。

当配置了映射以后，前端指定表名和字段必须使用映射后的字段。

配置排除之后该字段会从SQL排除，前端无法查询该字段。

```yaml
spring:
  datasource:
    master:
      driver-class-name: com.mysql.cj.jdbc.Driver
      jdbc-url: xxxx
      username: root
      password: 123456
      mapping:
        enable: true     # 启动映射
        table:           # 表名映射
          user: person   # 把user表映射为person 
        column:            # 字段映射
          user:            # 表名
            id: uid        # 字段名，把user表id字段映射为uid
      excludes:        # 排除字段
        user:          # 表名
          - tel        # 把user表的tel字段排除
          - password   # 把user表的password字段排除 
```

```js
{
    "@tb":"person",
    "@":"password,age,name",
    "uid":1
}
```

上方的请求参数会无视`password`查询字段，把`uid`字段解析为`id`，并把表名`person`解析为`user`表，最终结果为查询`user`表id=1的age,name字段
