package com.briscola4legenDs.briscola.Assets;

import java.util.HashMap;

public class PayloadBuilder {
    public class Payload {
        HashMap<String, String> String;
        HashMap<String, String[]> Strings;
        HashMap<String, Long> Long;
        HashMap<String, Long[]> Longs;
        HashMap<String, Integer> Integer;
        HashMap<String, Integer[]> Integers;

        private Payload() {
            Strings = new HashMap<>();
            Longs = new HashMap<>();
            Integers = new HashMap<>();

            String = new HashMap<>();
            Long = new HashMap<>();
            Integer = new HashMap<>();
        }

        public void addStrings(String key, String[] value) {
            Strings.put(key, value);
        }

        public void addString(String key, String value) {
            String.put(key, value);
        }

        public void addLongs(String key, Long[] value) {
            Longs.put(key, value);
        }

        public void addLong(String key, Long value) {
            Long.put(key, value);
        }

        public void addIntegers(String key, Integer[] value) {
            Integers.put(key, value);
        }

        public void addInteger(String key, Integer value) {
            Integer.put(key, value);
        }

        public HashMap<String, String[]> getStrings() {
            return Strings;
        }

        public HashMap<java.lang.String, java.lang.String> getString() {
            return String;
        }

        public HashMap<String, Long[]> getLongs() {
            return Longs;
        }

        public HashMap<java.lang.String, java.lang.Long> getLong() {
            return Long;
        }

        public HashMap<String, Integer[]> getIntegers() {
            return Integers;
        }

        public HashMap<java.lang.String, java.lang.Integer> getInteger() {
            return Integer;
        }
    }

    private final Payload payload;

    public PayloadBuilder() {
        payload = new Payload();
    }

    public PayloadBuilder addStrings(String key, String[] value) {
        payload.addStrings(key, value);
        return this;
    }

    public PayloadBuilder addString(String key, String value) {
        payload.addString(key, value);
        return this;
    }

    public PayloadBuilder addLongs(String key, Long[] value) {
        payload.addLongs(key, value);
        return this;
    }

    public PayloadBuilder addLong(String key, Long value) {
        payload.addLong(key, value);
        return this;
    }

    public PayloadBuilder addIntegers(String key, Integer[] value) {
        payload.addIntegers(key, value);
        return this;
    }

    public PayloadBuilder addInteger(String key, Integer value) {
        payload.addInteger(key, value);
        return this;
    }

    public Payload build() {
        return payload;
    }
}
