package com.semifinished.core.cache;


import java.util.function.Supplier;

/**
 * 缓存接口
 */
public interface SemiCache {

    /**
     * 根据key获取数据
     *
     * @param key 缓存的键
     * @param <T> 数据类型
     * @return 根据key获取的数据
     */
    <T> T getValue(String key);

    /**
     * 根据key和hashKey获取数据
     *
     * @param key     缓存的键
     * @param hashKey 缓存的哈希键
     * @param <T>     数据类型
     * @return 根据key和hashKey获取的数据
     */
    <T> T getValue(String key, String hashKey);


    /**
     * 从缓存中获取key对应的值,如果没有数据，那么从supplier中获取
     *
     * @param key 缓存的键
     * @param <T> 数据类型
     * @return 根据key获取的数据或supplier提供的数据
     */
    <T> T getValue(String key, Supplier<T> supplier);


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
    <T> void addValue(String key, String hashKey, T value);


    /**
     * 根据key删除数据
     *
     * @param key 缓存的键
     */
    void removeValue(String key);


    void removeValue(String key, String hashKey);

    /**
     * 获取原始的缓存对象
     *
     * @param <S> 缓存对象类型
     * @return 缓存对象
     */
    <S> S getSource();

}
