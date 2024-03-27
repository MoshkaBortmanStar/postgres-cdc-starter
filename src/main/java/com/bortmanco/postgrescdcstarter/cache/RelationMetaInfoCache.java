package com.bortmanco.postgrescdcstarter.cache;

import com.bortmanco.postgrescdcstarter.data.RelationMetaInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RelationMetaInfoCache {

    private RelationMetaInfoCache() {}

    private static final ThreadLocal<Map<Integer, RelationMetaInfo>> relationMetaInfoCache = ThreadLocal.withInitial(ConcurrentHashMap::new);

    public static void put(Integer key, RelationMetaInfo value) {
        relationMetaInfoCache.get().put(key, value);
    }

    public static RelationMetaInfo get(Integer key) {
        return relationMetaInfoCache.get().get(key);
    }

    public static void remove(Integer key) {
        relationMetaInfoCache.get().remove(key);
    }

    public static void clear() {
        relationMetaInfoCache.get().clear();
    }


}
