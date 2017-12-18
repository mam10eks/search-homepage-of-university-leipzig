package de.uni_leipzig.custom_index_cleaning;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

@Data
public class IndexCleaner
{
	@NonNull
	private final SolrClient solrClient;

	@SneakyThrows
	List<String> distinctDigests()
	{
		SolrQuery query = new SolrQuery("*:*");
		query.setRequestHandler("select");
		query.set("stats", "on");
		query.set("stats.field", "digest");
		query.set("rows", "0");
		query.set("stats.calcdistinct", "true");
		
		QueryResponse response = solrClient.query(query);
		
		return response.getFieldStatsInfo().get("digest").getDistinctValues().stream()
				.map(i -> (String)i)
				.collect(Collectors.toList());
	}

	@SneakyThrows
	public void cleanIndex()
	{
		int digestsWithDuplicates = 0;
		int removedDuplicates = 0;
		
		for(String digest: distinctDigests())
		{
			SolrQuery query = new SolrQuery("digest: \""+ digest +"\"");
			query.setRows(Integer.MAX_VALUE);
			
			QueryResponse response = solrClient.query(query);
			
			if(response.getResults().size() == 1)
			{
				continue;
			}
			
			digestsWithDuplicates++;
			Set<String> duplicateUrls = new HashSet<>();

			response.getResults().sort((a,b) -> ((String)b.get("content")).length() - ((String)a.get("content")).length()); 

			for(int i=1; i<response.getResults().size(); i++)
			{
				SolrDocument doc = response.getResults().get(i);
				duplicateUrls.add((String) doc.get("url"));
				
				removedDuplicates++;
				solrClient.deleteById((String) doc.get("id"));
			}
			
			duplicateUrls.stream().forEach(a -> {if(a == null) throw new RuntimeException("Handle this..");});
			duplicateUrls.remove((String) response.getResults().get(0).get("url"));
			
			if(duplicateUrls.isEmpty())
			{
				solrClient.commit();
				System.out.println("Skip "+ response.getResults().size() +" docs as full duplicates...");
				continue;
			}
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", response.getResults().get(0).get("id"));
			Map<String, Collection<String>> partialUpdate = new HashMap<>();
			partialUpdate.put("set", duplicateUrls);
			doc.addField("duplicatesUrl", partialUpdate);
			solrClient.add(doc);
			
			solrClient.commit();
		}
		
		System.out.println("Finished the cleaning with "+ digestsWithDuplicates +" digests with updates and removed "+
				removedDuplicates +" duplicates at all.");
	}

}
