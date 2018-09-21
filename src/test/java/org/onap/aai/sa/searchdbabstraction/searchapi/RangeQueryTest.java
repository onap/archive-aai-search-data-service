/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017-2018 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.sa.searchdbabstraction.searchapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.onap.aai.sa.rest.TestUtils;

public class RangeQueryTest {

    @Test(expected=IllegalArgumentException.class)
    public void testSetGt(){

        RangeQuery rq = new RangeQuery();
        rq.setLt(new String("2x"));
        Assert.assertEquals("2x", rq.getLt());
        Assert.assertNotNull(rq.toElasticSearch());
        Assert.assertNotNull(rq.toString());
        rq.setGt(new Integer(1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetGte(){

        RangeQuery rq = new RangeQuery();
        rq.setGt(new Integer(1));
        Assert.assertNotNull(rq.toElasticSearch());
        Assert.assertNotNull(rq.toString());
        rq.setGte(new Integer(1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetLt(){

        RangeQuery rq = new RangeQuery();
        rq.setLt(new Integer(1));
        rq.setFormat("format-1");
        Assert.assertEquals(rq.getFormat(), "format-1");
        Assert.assertNotNull(rq.toElasticSearch());
        Assert.assertNotNull(rq.toString());

        rq.setGt(new Integer(1));
        Assert.assertNotNull(rq.toElasticSearch());
        Assert.assertNotNull(rq.toString());
        rq.setLt(new String("10"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetLte(){
        RangeQuery rq = new RangeQuery();
        rq.setGt(new Integer(1));
        rq.setTimeZone("CT");
        Assert.assertEquals(rq.getTimeZone(), "CT");
        Assert.assertNotNull(rq.toElasticSearch());
        Assert.assertNotNull(rq.toString());

        rq.setLte(new String("10"));
    }

    @Test
    public void testSearchStatementAggregations() throws IOException {
        File queryWithSubrangeFile = new File("src/test/resources/json/queries/query-with-subrange.json");
        String queryWithSubrangeStr = TestUtils.readFileToString(queryWithSubrangeFile);

        ObjectMapper mapper = new ObjectMapper();
        SearchStatement ss = mapper.readValue(queryWithSubrangeStr, SearchStatement.class);

        Aggregation a1 = getAggregationObject();
        Aggregation a2 = getAggregationObject();
        Aggregation[] aggs= new Aggregation[] {a1, a2};
        ss.setAggregations(aggs);
        Assert.assertNotNull(ss.toString());
    }

    private Aggregation getAggregationObject(){
        Aggregation a = new Aggregation();

        AggregationStatement as = new AggregationStatement();
        DateHistogramAggregation dha = new DateHistogramAggregation();
        dha.setField("field-1");
        dha.setInterval("interval-1");
        Assert.assertEquals(dha.getInterval(), "interval-1");
        dha.setTimeZone("CT");
        Assert.assertEquals(dha.getTimeZone(), "CT");
        dha.setFormat("format-1");
        Assert.assertEquals(dha.getFormat(), "format-1");
        dha.setSize(10);
        dha.setMinThreshold(1);
        Assert.assertNotNull(dha.toElasticSearch());
        Assert.assertNotNull(dha.toString());
        as.setDateHist(dha);
        as.toString();

        as.getNestedPath();

        DateRangeAggregation dra = new DateRangeAggregation();
        dra.setField("field-1");
        dra.setMinThreshold(1);
        dra.setFormat("format-1");
        Assert.assertEquals(dra.getFormat(), "format-1");
        DateRange dr = new DateRange();
        dr.setFromDate("01-12-2017");
        Assert.assertEquals(dr.getFromDate(), "01-12-2017");
        dr.setToDate("21-12-2017");
        Assert.assertEquals(dr.getToDate(), "21-12-2017");
        DateRange[] drs = {dr};
        dra.setDateRanges(drs);
        Assert.assertTrue(dra.getDateRanges().length==1);
        Assert.assertNotNull(dra.toElasticSearch());
        Assert.assertNotNull(dra.toString());
        as.setDateRange(dra);
        as.toString();

        as.getNestedPath();

        GroupByAggregation gba = new GroupByAggregation();
        gba.setField("field-1");
        gba.setMinThreshold(1);
        Assert.assertNotNull(gba.toElasticSearch());
        Assert.assertNotNull(gba.toString());
        as.setGroupBy(gba);
        Assert.assertNotNull(as.toString());

        a.setStatement(as);
        Assert.assertNotNull(a.toString());
        return a;
    }
}
