package querqy.solr;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.ExtendedQuery;
import org.apache.solr.search.SyntaxError;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import querqy.lucene.LuceneQueries;
import querqy.lucene.LuceneSearchEngineRequestAdapter;
import querqy.model.ExpandedQuery;
import querqy.model.MatchAllQuery;
import querqy.parser.QuerqyParser;
import querqy.rewrite.RewriteChain;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class QuerqyDismaxQParserTest {

    @Mock
    SolrQueryRequest request;

    @Mock
    QuerqyParser querqyParser;

    @Mock
    RewriteChain rewriteChain;

    @Mock
    IndexSchema schema;


    @Test
    public void testThatEmptyQueryStringTriggersBadRequestException() {

        try {
            new QuerqyDismaxQParser(" ", new ModifiableSolrParams(), new ModifiableSolrParams(), request, querqyParser,
                    rewriteChain, null);
            Assert.fail();
        } catch (final SolrException e) {
            Assert.assertEquals(400, e.code());
        }

        try {
            new QuerqyDismaxQParser("", new ModifiableSolrParams(), new ModifiableSolrParams(), request, querqyParser,
                    rewriteChain, null);
            Assert.fail();
        } catch (final SolrException e) {
            Assert.assertEquals(400, e.code());
        }
    }

    @Test
    public void testSuppressCaching() throws Exception {

        when(request.getSchema()).thenReturn(schema);
        when(schema.getQueryAnalyzer()).thenReturn(new StandardAnalyzer());
        when(rewriteChain.rewrite(any(), any())).thenReturn(new ExpandedQuery(new MatchAllQuery()));

        final ModifiableSolrParams solrParams = new ModifiableSolrParams();
        solrParams.add("qf", "f1");

        final ModifiableSolrParams localParams = new ModifiableSolrParams();
        localParams.add(CommonParams.CACHE, CommonParams.FALSE);
        final QuerqyDismaxQParser parser = new QuerqyDismaxQParser("*:*", localParams, solrParams, request,
                querqyParser, rewriteChain, null);
        final Query query = parser.getQuery();
        Assert.assertTrue(query instanceof ExtendedQuery);
        final ExtendedQuery extendedQuery = (ExtendedQuery) query;
        Assert.assertFalse(extendedQuery.getCache());

    }

    @Test
    public void testForceCaching() throws Exception {

        when(request.getSchema()).thenReturn(schema);
        when(schema.getQueryAnalyzer()).thenReturn(new StandardAnalyzer());
        when(rewriteChain.rewrite(any(), any())).thenReturn(new ExpandedQuery(new MatchAllQuery()));

        final ModifiableSolrParams solrParams = new ModifiableSolrParams();
        solrParams.add("qf", "f1");

        final ModifiableSolrParams localParams = new ModifiableSolrParams();
        localParams.add(CommonParams.CACHE, CommonParams.TRUE);
        final QuerqyDismaxQParser parser = new QuerqyDismaxQParser("*:*", localParams, solrParams, request,
                querqyParser, rewriteChain, null);
        final Query query = parser.getQuery();
        Assert.assertTrue(query instanceof ExtendedQuery);
        final ExtendedQuery extendedQuery = (ExtendedQuery) query;
        Assert.assertTrue(extendedQuery.getCache());

    }

    @Test
    public void testThatInterdependentLuceneQueriesWillNotBeCachedSeparately() throws Exception {

        when(request.getSchema()).thenReturn(schema);
        when(schema.getQueryAnalyzer()).thenReturn(new StandardAnalyzer());
        when(rewriteChain.rewrite(any(), any())).thenReturn(new ExpandedQuery(new MatchAllQuery()));

        final ModifiableSolrParams solrParams = new ModifiableSolrParams();
        solrParams.add("qf", "f1");

        final ModifiableSolrParams localParams = new ModifiableSolrParams();
        localParams.add(CommonParams.CACHE, "sep");

        final QuerqyDismaxQParser parser = new QuerqyDismaxQParser("*:*", localParams, solrParams, request,
                querqyParser, rewriteChain, null);
        parser.luceneQueries = new LuceneQueries(new MatchAllDocsQuery(), Collections.emptyList(),
                Collections.singletonList(new MatchAllDocsQuery()), new MatchAllDocsQuery(), true);
        parser.applyLocalParams();

        final Query query = parser.getQuery();
        Assert.assertFalse(query instanceof ExtendedQuery);
    }

    @Test
    public void testThatNonInterdependentLuceneQueriesCanBeCachedSeparately() throws Exception {

        when(request.getSchema()).thenReturn(schema);
        when(schema.getQueryAnalyzer()).thenReturn(new StandardAnalyzer());
        when(rewriteChain.rewrite(any(), any())).thenReturn(new ExpandedQuery(new MatchAllQuery()));

        final ModifiableSolrParams solrParams = new ModifiableSolrParams();
        solrParams.add("qf", "f1");

        final ModifiableSolrParams localParams = new ModifiableSolrParams();
        localParams.add(CommonParams.CACHE, "sep");

        final QuerqyDismaxQParser parser = new QuerqyDismaxQParser("*:*", localParams, solrParams, request,
                querqyParser, rewriteChain, null);
        parser.luceneQueries = new LuceneQueries(new MatchAllDocsQuery(), Collections.emptyList(),
                Collections.singletonList(new MatchAllDocsQuery()), new MatchAllDocsQuery(), false);
        parser.applyLocalParams();

        final Query query = parser.getQuery();
        Assert.assertTrue(query instanceof ExtendedQuery);

        final ExtendedQuery extendedQuery = (ExtendedQuery) query;
        Assert.assertTrue(extendedQuery.getCache());
        Assert.assertTrue(extendedQuery.getCacheSep());

    }

}
