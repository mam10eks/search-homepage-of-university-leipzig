package de.uni_leipzig.search_engine.dataobjects;

import java.util.Comparator;

/**
 *
 * @author Danilo
 */
public class UserQueryComparator implements Comparator<UserQuery> {
    
    @Override
    public int compare(UserQuery u1, UserQuery u2) {
        if(u1.getCount() == u2.getCount()) return 0;
        return u1.getCount() < u2.getCount() ? 1 : -1;
    }
    
}
