package com.parag.ecom.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class Config {
    // Reads .env from the project root; a missing file is fine (CI uses real env vars)
    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    private Config() {
    }

    // A real environment variable wins over the value in .env
    public static String get(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = DOTENV.get(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing config value: " + key + " (set it in .env or as an env var)");
        }
        return value;
    }

    public static String baseUri() {
        return get("BASE_URI");
    }

    public static String email() {
        return get("RSA_EMAIL");
    }

    public static String password() {
        return get("RSA_PASSWORD");
    }
}
