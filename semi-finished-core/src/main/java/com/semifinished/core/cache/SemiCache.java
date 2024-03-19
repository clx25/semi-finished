package com.semifinished.core.cache;


import java.util.Map;

/**
 * 缓存接口
 */
public interface SemiCache {

    /**
     * 根据key获取数据
     * 使用setValue保存的数据用改方法获取
     *
     * @param key 缓存的键
     * @param <T> 数据类型
     * @return 根据key获取的数据
     */
    <T> T getValue(String key);

    /**
     * 根据key和hashKey获取数据
     * 通过addHashValue添加的数据用该方法获取
     *
     * @param key     缓存的键
     * @param hashKey 缓存的哈希键
     * @param <T>     数据类型
     * @return 根据key和hashKey获取的数据
     */
    <T> T getHashValue(String key, String hashKey);


    /**
     * 保存数据
     *
     * @param key   缓存的键
     * @param value 缓存的值
     */
    void setValue(String key, Object value);


    /**
     * 根据key和hashKey添加数据
     *
     * @param key     缓存的键
     * @param hashKey 缓存的哈希键
     * @param value   添加的数据
     */
    <T> void addHashValue(String key, String hashKey, T value);


    /**
     * 初始化数据
     *
     * @param key   缓存的键
     * @param value 添加的数据
     */
    void initValue(String key, Object value);

    /**
     * 初始化哈希数据
     *
     * @param key   缓存的键
     * @param value 添加的数据
     */
    void initHashValue(String key, Map<String, ?> value);

    /**
     * 根据key删除数据
     *
     * @param key 缓存的键
     */
    void removeValue(String key);


    void removeHashValue(String key, String hashKey);

    /**
     * 获取原始的缓存对象
     *
     * @param <S> 缓存对象类型
     * @return 缓存对象
     */
    <S> S getSource();

}
