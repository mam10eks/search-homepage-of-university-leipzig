package de.uni_leipzig.custom_index_cleaning;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	SolrClient solrClient = new HttpSolrClient.Builder()
    			.withBaseSolrUrl("http://localhost:8983/solr/uni_leipzig_core")
    			.build();
    			
        IndexCleaner indexCleaner = new IndexCleaner(solrClient);
        indexCleaner.cleanIndex();
    }
}
