package querqy.solr.contrib;

import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.solr.common.util.NamedList;

import querqy.rewrite.RewriterFactory;
import querqy.solr.RewriterFactoryAdapter;

import java.io.IOException;

/**
 * FactoryAdapter for {@link ShingleRewriterFactory}
 */
public class ShingleRewriterFactory implements RewriterFactoryAdapter {

    @Override
    public RewriterFactory createRewriterFactory(NamedList<?> args, ResourceLoader resourceLoader) throws IOException {
        Boolean acceptGeneratedTerms = args.getBooleanArg("acceptGeneratedTerms");
        boolean t = (acceptGeneratedTerms == null) ? false : acceptGeneratedTerms;
        return new querqy.rewrite.contrib.ShingleRewriterFactory(t);
    }
}
