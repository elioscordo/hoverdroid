package com.core;

import java.util.Collection;
import java.util.Random;

public class NearbyUtils {
    public static final String EVENT_NEARBY_START_ADVERTISING = "EVENT_NEARBY_START_ADVERTISING";

    @SuppressWarnings("unchecked")
    public static <T> T pickRandomElem(Collection<T> collection) {
        return (T) collection.toArray()[new Random().nextInt(collection.size())];
    }

}
