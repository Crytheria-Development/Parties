package com.cjcameron92.crytheria.parties.storage;

import com.cjcameron92.crytheria.core.redis.JedisMapStorage;
import com.cjcameron92.crytheria.core.redis.RedisData;
import com.cjcameron92.crytheria.parties.model.Party;
import gg.supervisor.api.Component;

@Component
public class JedisPartyStorage extends JedisMapStorage<Party> {
    public JedisPartyStorage(RedisData redisData) {
        super(redisData, Party.class, "party");
    }
}
