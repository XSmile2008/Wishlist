package com.company.wishlist.component;

import java.util.HashSet;

public class IndexedHashSet<E> extends HashSet<E> {
    public E get(int index){
        for(E e : this){
            if(index-- <= 0){
                return e;
            }
        }
        return null;
    }
}
