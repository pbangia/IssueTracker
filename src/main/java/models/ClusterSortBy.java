package models;

import java.util.Comparator;

/**
 * Created by g.tiongco on 23/10/17.
 */
public enum ClusterSortBy implements Comparator<Cluster> {
    NUMPOSTS {
        @Override
        public int compare(Cluster o1, Cluster o2) {
            if (o1.getNumPosts() == o2.getNumPosts())
                return 0;
            return o1.getNumPosts() < o2.getNumPosts() ? 1 : -1;
        }
    },
    NUM_AFFECTED_USERS {
        @Override
        public int compare(Cluster o1, Cluster o2) {
            if (o1.getNumAffectedUsers() == o2.getNumAffectedUsers())
                return 0;
            return o1.getNumAffectedUsers() < o2.getNumAffectedUsers() ? 1 : -1;
        }
    },
    TITLE {
        @Override
        public final int compare(Cluster o1, Cluster o2) {
            return 0;
        }
    }
//    ,
//    ENGLISH {
//        @Override
//        public final int compare(final Item o1, final Item o2) {
//            return compareStrings(o1.getEnglish(), o2.getEnglish());
//        }
//    },
//    NORWEGIAN {
//        @Override
//        public final int compare(final Item o1, final Item o2) {
//            return compareStrings(o1.getNorwegian(), o2.getNorwegian());
//        }
//    };
//
//    private static int compareStrings(final String s1, final String s2) {
//        if (s1 == null) {
//            return s2 == null ? 0 : -1;
//        }
//        if (s2 == null) {
//            return 1;
//        }
//        return s1.compareTo(s2);
//    }
}
