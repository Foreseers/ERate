package com.foreseer.erate.Utils;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Foreseer on 22/03/2017.
 */

public class Utils {
    private Utils() {}

    public static <E> Collection<E> addItemToCollection(Collection<E> collection, E e){
        collection.add(e);
        return collection;
    }
}
