/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.plugins.index.solr.index;

import static org.junit.Assert.assertTrue;

import javax.security.auth.Subject;

import org.apache.jackrabbit.oak.api.Root;
import org.apache.jackrabbit.oak.core.RootImpl;
import org.apache.jackrabbit.oak.plugins.index.solr.SolrBaseTest;
import org.apache.jackrabbit.oak.spi.commit.PostCommitHook;
import org.apache.jackrabbit.oak.spi.query.CompositeQueryIndexProvider;
import org.apache.jackrabbit.oak.spi.security.OpenSecurityProvider;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

/**
 * Integration test for {@link org.apache.jackrabbit.oak.plugins.index.solr.index.SolrCommitHook}
 */
public class SolrCommitHookIT extends SolrBaseTest {

    @Test
    public void testAddSomeNodes() throws Exception {
        Root r = createRootImpl();
        r.getTree("/").addChild("a").addChild("b").addChild("doc1").
                setProperty("text", "hit that hot hat tattoo");
        r.getTree("/").getChild("a").addChild("c").addChild("doc2").
                setProperty("text", "it hits hot hats");
        r.getTree("/").getChild("a").getChild("b").addChild("doc3").
                setProperty("text", "tattoos hate hot hits");
        r.getTree("/").getChild("a").getChild("b").addChild("doc4").
                setProperty("text", "hats tattoos hit hot");
        r.commit();

        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        QueryResponse queryResponse = server.query(query);
        assertTrue("no documents were indexed", queryResponse.getResults().size() > 0);
    }

    @Test
    public void testRemoveNode() throws Exception {
        Root r = createRootImpl();

        // Add a node
        r.getTree("/").addChild("testRemoveNode").setProperty("foo", "bar");
        r.commit();

        // check the node is not in Solr anymore
        SolrQuery query = new SolrQuery();
        query.setQuery("path_exact:\\/testRemoveNode\\/");
        assertTrue("item with id:testRemoveNode was not found in the index",
                server.query(query).getResults().size() > 0);

        // remove the node in oak
        r.getTree("/").getChild("testRemoveNode").remove();
        r.commit();

        // check the node is not in Solr anymore
        assertTrue("item with id:testRemoveNode was found in the index",
                server.query(query).getResults().size() == 0);
    }
}