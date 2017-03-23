package com.foreseer.erate.Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

    public static <E> ArrayList<E> convertSetToArraylist(Set<E> set){
        ArrayList<E> arrayList = new ArrayList<E>();
        arrayList.addAll(set);
        return arrayList;
    }

    public static <E> ArrayList<E> addElementToArrayList(ArrayList<E> list, E element){
        list.add(element);
        return list;
    }

    public static  Map<String, Double> addCurrenciesWithBase(Map<String, Double> rates, String baseCurrency){
        Map<String, Double> resultMap = new HashMap<String, Double>();
        for (Map.Entry<String, Double> entry : rates.entrySet()){
            resultMap.put(baseCurrency + entry.getKey(), entry.getValue());
        }
        return resultMap;
    }
}
