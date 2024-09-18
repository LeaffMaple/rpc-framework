package com.rpc.extension;

import com.rpc.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * refer to dubbo spi: https://dubbo.apache.org/zh-cn/docs/source_code_guide/dubbo-spi.html
 * 源码分析: https://cn.dubbo.apache.org/zh-cn/docsv2.7/dev/source/dubbo-spi/
 */
@Slf4j
public final class ExtensionLoader<T> {

    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";
    // 使用ConcurrentHashMap来线程安全地存储扩展加载器
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    // 使用ConcurrentHashMap来线程安全地存储扩展实例
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    // 扩展加载器的类型
    private final Class<?> type;
    // 缓存实例
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    // 缓存类
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    // 私有构造方法，防止外部实例化
    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     * 获取扩展加载器
     *
     * @param type 扩展类型，必须是接口并且有@SPI注解
     * @return 扩展加载器
     */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }
        // 首先从缓存中获取，如果未命中，则创建一个
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * 获取扩展实例
     *
     * @param name 扩展名称，不能为空或空字符串
     * @return 扩展实例
     */
    public T getExtension(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        // 首先从缓存中获取，如果未命中，则创建一个
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // 如果实例不存在，则创建一个单例
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 创建扩展实例
     *
     * @param name 扩展名称
     * @return 扩展实例
     */
    private T createExtension(String name) {
        // 从文件中加载所有扩展类，并通过名称获取特定的一个
        // 从配置文件中加载所有的拓展类，可得到“配置项名称”到“配置类”的映射关系表
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    /**
     * 获取扩展类
     *
     * @return 扩展类映射
     */
    private Map<String, Class<?>> getExtensionClasses() {
        // 从缓存中获取已加载的扩展类
        Map<String, Class<?>> classes = cachedClasses.get();
        // 双重检查锁定
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    // 从我们的扩展目录中加载所有扩展
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 从目录中加载扩展类
     *
     * @param extensionClasses 扩展类映射
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 从资源中加载扩展类
     *
     * @param extensionClasses 扩展类映射
     * @param classLoader 类加载器
     * @param resourceUrl 资源URL
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // 逐行读取
            while ((line = reader.readLine()) != null) {
                // 处理注释
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        // 确保键值对都不为空
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
