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
            return compareStrings(o1.getTitle(), o2.getTitle());
        }
    };

    private static int compareStrings(final String s1, final String s2) {
        if (s1 == null) {
            return s2 == null ? 0 : 1;
        }
        if (s2 == null) {
            return -1;
        }
        return s2.compareTo(s1);
    }
}
