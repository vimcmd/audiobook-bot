package com.github.vimcmd.abookserver.service.audioknigiclub.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStreamReader;
import java.io.Reader;

@Component
@Slf4j
public class LiveStreetSecurityProvider {

    @Value("${app.audioknigi.club.hash.secret}")
    private String secret; // FIXME (24.11.2018): char array

    private ScriptEngine nashorn;

    @PostConstruct
    public void init() throws ScriptException {
        nashorn = new ScriptEngineManager().getEngineByName("nashorn");
        nashorn.eval(read("/static/js/audioknigiclub/livestreet-hash.js"));
        nashorn.eval(read("/static/js/audioknigiclub/http_cdnjs.cloudflare.com_ajax_libs_crypto-js_3.1.2_rollups_aes.js"));
    }

    public Object getHash(String message) {
        return getHash(message, secret);
    }

    private Object getHash(String message, String key) {
        try {
            return ((Invocable) nashorn).invokeFunction("hash", message, key);
        } catch (NoSuchMethodException | ScriptException e) {
            throw new IllegalStateException("invoke function failed", e);
        }
    }



    private Reader read(String path) {
        return new InputStreamReader(getClass().getResourceAsStream(path));
    }

}
