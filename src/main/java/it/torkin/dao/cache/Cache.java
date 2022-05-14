package it.torkin.dao.cache;

import java.util.HashMap;
import java.util.Map;

public class Cache {

    private final Map<String, Object> cached = new HashMap<>();

    public Map<String, Object> getCached() {
        return cached;
    }

}
