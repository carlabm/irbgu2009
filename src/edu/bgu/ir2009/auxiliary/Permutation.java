package edu.bgu.ir2009.auxiliary;

import java.util.*;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 21/02/2010
 * Time: 21:48:35
 */
public class Permutation {
    private final String[] terms;

    public Permutation(Set<String> terms) {
        this.terms = new String[terms.size()];
        terms.toArray(this.terms);
    }

    public List<Set<String>> getPermutation(int size) {
        if (size <= 0) {
            return new LinkedList<Set<String>>();
        }
        int[] ints = new int[size];
        for (int i = 0; i < size; i++) {
            ints[i] = i;
        }
        List<Set<String>> res = new LinkedList<Set<String>>();
        while (ints[0] != terms.length - size) {
            Set<String> strings = new HashSet<String>();
            for (int j = 0; j < size; j++) {
                strings.add(terms[ints[j]]);
            }
            res.add(strings);
            for (int k = size - 1; k > -1; k--) {
                if (ints[k] != terms.length - size + k) {
                    ints[k]++;
                    for (int l = k + 1; l < size; l++) {
                        ints[l] = ints[k] + l - k;
                    }
                    break;
                }
            }
        }
        Set<String> strings = new HashSet<String>();
        for (int j = 0; j < size; j++) {
            strings.add(terms[ints[j]]);
        }
        res.add(strings);
        return res;
    }

    public static void main(String[] args) {
        Set<String> stringSet = new TreeSet<String>();
        stringSet.add("1");
        stringSet.add("2");
        stringSet.add("3");
        stringSet.add("4");
        stringSet.add("5");
        Permutation permutation = new Permutation(stringSet);
        List<Set<String>> sets = permutation.getPermutation(3);
        for (Set<String> l : sets) {
            for (String ll : l) {
                System.out.print(ll + ", ");
            }
            System.out.println();
        }
    }
}
