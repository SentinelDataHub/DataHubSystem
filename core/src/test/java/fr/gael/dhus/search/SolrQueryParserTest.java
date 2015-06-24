package fr.gael.dhus.search;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.gael.dhus.search.SolrQueryParser;

public class SolrQueryParserTest
{
   @Test
   public void testSolrQueryPerser()
   {
      String q="envisat";
      List <String[]>elements = SolrQueryParser.parse(q);
      Assert.assertEquals(elements.size(), 1, "No element parsed in query \"" + q + "\".");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_VALUE], q,
         "Query not properly parsed.");
      
      q="*";
      elements = SolrQueryParser.parse(q);
      Assert.assertEquals(elements.size(), 1, "No element parsed in query \"" + q + "\".");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_VALUE], "*", "Query not properly parsed");

      q="12-TRE_1_RER.AZS";
      elements = SolrQueryParser.parse(q);
      Assert.assertEquals(elements.size(), 1, "No element parsed in query \"" + q + "\".");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_VALUE], "12-TRE_1_RER.AZS", "Query not properly parsed");

      
      q="platformName:envisat";
      elements = SolrQueryParser.parse(q);
      Assert.assertEquals(elements.size(), 1, "No element parsed in query \"" + q + "\".");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_VALUE], "envisat", "Query not properly parsed");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_FIELD], "platformName", "Query not properly parsed");

      q="*:*";
      elements = SolrQueryParser.parse(q);
      Assert.assertEquals(elements.size(), 1, "No element parsed in query \"" + q + "\".");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_VALUE], "*", "Query not properly parsed");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_FIELD], "*", "Query not properly parsed");

      q="a:12-TRE_1_RER.AZS";
      elements = SolrQueryParser.parse(q);
      Assert.assertEquals(elements.size(), 1, "No element parsed in query \"" + q + "\".");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_VALUE], "12-TRE_1_RER.AZS", "Query not properly parsed");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_FIELD], "a", "Query not properly parsed");
      
      q="cloudCoverage:[0 TO 100]";
      elements = SolrQueryParser.parse(q);
      Assert.assertEquals(elements.size(), 1, "No element parsed in query \"" + q + "\".");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_VALUE], "[0 TO 100]", "Query not properly parsed");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_FIELD], "cloudCoverage", "Query not properly parsed");

      q="ca:A AND cb:B";
      elements = SolrQueryParser.parse(q);
      Assert.assertEquals(elements.size(), 3, "Not enough element parsed in query \"" + q + "\".");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_VALUE], "A", "Query not properly parsed");
      Assert.assertEquals (elements.get(0)[SolrQueryParser.INDEX_FIELD], "ca", "Query not properly parsed");
      Assert.assertEquals (elements.get(1)[SolrQueryParser.INDEX_VALUE], "AND", "Query not properly parsed");
      Assert.assertEquals (elements.get(2)[SolrQueryParser.INDEX_VALUE], "B", "Query not properly parsed");
      Assert.assertEquals (elements.get(2)[SolrQueryParser.INDEX_FIELD], "cb", "Query not properly parsed");
   }
}
