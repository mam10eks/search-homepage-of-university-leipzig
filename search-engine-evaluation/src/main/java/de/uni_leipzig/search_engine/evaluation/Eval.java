package de.uni_leipzig.search_engine.evaluation;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import lombok.SneakyThrows;

public class Eval
{
	@SneakyThrows
	public static void main(String[] args)
	{
		File f = new File("/home/maik/workspace/search-homepage-of-university-leipzig/search-engine-evaluation/dummyStuff1717386767474214073");
		
		for(File c : f.listFiles())
		{
			System.out.println("\n\n---> "+ c.getName());
			
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(c.toPath()));
			
			for(int i=0; i< indexReader.numDocs(); i++)
			{
				Document doc = indexReader.document(i);

				System.out.println(doc.get("url") +"\t\t"+ doc.get("rj"));
			}
		}
	}
}
