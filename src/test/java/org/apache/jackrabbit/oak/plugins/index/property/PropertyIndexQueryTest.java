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
package org.apache.jackrabbit.oak.plugins.index.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.api.ContentRepository;
import org.apache.jackrabbit.oak.api.PropertyValue;
import org.apache.jackrabbit.oak.api.ResultRow;
import org.apache.jackrabbit.oak.plugins.index.property.PropertyIndexEditorProvider;
import org.apache.jackrabbit.oak.plugins.index.property.PropertyIndexProvider;
import org.apache.jackrabbit.oak.plugins.nodetype.write.InitialContent;
import org.apache.jackrabbit.oak.query.AbstractQueryTest;
import org.apache.jackrabbit.oak.query.JsopUtil;
import org.apache.jackrabbit.oak.spi.query.PropertyValues;
import org.apache.jackrabbit.oak.spi.security.OpenSecurityProvider;
import org.junit.Test;

/**
 * Tests the query engine using the default index implementation: the
 * {@link PropertyIndexProvider}
 */
public class PropertyIndexQueryTest extends AbstractQueryTest {

    @Override
    protected ContentRepository createRepository() {
        return new Oak().with(new InitialContent())
                .with(new OpenSecurityProvider())
                .with(new PropertyIndexProvider())
                .with(new PropertyIndexEditorProvider())
                .createContentRepository();
    }

    @Test
    public void xpath() throws Exception {
        test("xpath.txt");
    }

    @Test
    public void bindVariableTest() throws Exception {
        JsopUtil.apply(
                root,
                "/ + \"test\": { \"hello\": {\"id\": \"1\"}, \"world\": {\"id\": \"2\"}}");
        root.commit();

        Map<String, PropertyValue> sv = new HashMap<String, PropertyValue>();
        sv.put("id", PropertyValues.newString("1"));
        Iterator<? extends ResultRow> result;
        result = executeQuery("select * from [nt:base] where id = $id",
                SQL2, sv).getRows().iterator();
        assertTrue(result.hasNext());
        assertEquals("/test/hello", result.next().getPath());

        sv.put("id", PropertyValues.newString("2"));
        result = executeQuery("select * from [nt:base] where id = $id",
                SQL2, sv).getRows().iterator();
        assertTrue(result.hasNext());
        assertEquals("/test/world", result.next().getPath());
    }

    @Test
    public void sql2Index() throws Exception {
        test("sql2_index.txt");
    }

    @Test
    public void sql2Measure() throws Exception {
        test("sql2_measure.txt");
    }

    @Test
    public void sql1() throws Exception {
        test("sql1.txt");
    }

    @Test
    public void sql2() throws Exception {
        test("sql2.txt");
    }

}