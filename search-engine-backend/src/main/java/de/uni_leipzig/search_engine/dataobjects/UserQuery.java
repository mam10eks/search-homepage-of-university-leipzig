package de.uni_leipzig.search_engine.dataobjects;

public class UserQuery implements Comparable {

    private Integer count;
    private final String search;
    private String normalized;

    public UserQuery(String search) {
            this.count = 1;
            this.search = search;
    }
    
    public UserQuery(String search, Integer count) {
            this.count = count;
            this.search = search;
    }

    public int getCount()                   { return count;                     }
    public String getSearch()               { return search;                    }
    public String getNormalized()           { return normalized;                }
    
    public void setCount(Integer count)     { this.count = count;               }
    public void setNormalized(String s)     { this.normalized = s;              }
    
    @Override
    public int compareTo(Object o) {
	return ((UserQuery)o).getCount() - this.count;
    }
    
}