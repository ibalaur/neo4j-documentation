/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.plugin.cypher;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.impl.annotations.Documented;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.server.rest.DocsGenerator;
import org.neo4j.test.GraphDescription;
import org.neo4j.test.GraphDescription.Graph;
import org.neo4j.test.GraphHolder;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.neo4j.test.TestData;
import org.neo4j.test.TestData.Title;

public class CypherPluginFunctionalTest implements GraphHolder
{
    private static final String ENDPOINT = "http://localhost:7474/db/data/ext/CypherPlugin/graphdb/execute_query";
    private static ImpermanentGraphDatabase graphdb;
    public @Rule
    TestData<Map<String, Node>> data = TestData.producedThrough( GraphDescription.createGraphFor(
            this, true ) );
    
    public @Rule
    TestData<DocsGenerator> gen = TestData.producedThrough( DocsGenerator.PRODUCER );
    private static WrappingNeoServerBootstrapper server;

    /**
     * A simple query returning all nodes connected to node 1:
     * `start x  = (1) match (x) -- (n) return n, n.name`
     */
    @Test
    @Title("Send a Query - URL encoded")
    @Documented
    @Graph( value = { "I know you", "I know him" } )
    public void testPropertyColumn() throws UnsupportedEncodingException
    {
        String response = gen.get()
        .expectedStatus( Status.OK.getStatusCode() )
        .payload( "query=" + URLEncoder.encode( "start x  = ("+data.get().get( "I" ).getId() +") match (x) -- (n) return n, n.name", "UTF-8"))
        .payloadType( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
        .post( ENDPOINT )
        .entity();
        assertTrue(response.contains( "you" ));
        assertTrue(response.contains( "him" ));
    }

    @BeforeClass
    public static void startDatabase()
    {
        graphdb = new ImpermanentGraphDatabase("target/db"+System.currentTimeMillis());
        
    }

    @AfterClass
    public static void stopDatabase()
    {
    }

    @Override
    public GraphDatabaseService graphdb()
    {
        return graphdb;
    }
    
    @Before
    public void startServer() {
        server = new WrappingNeoServerBootstrapper(
                graphdb );
        server.start();
    }
    
    @After
    public void shutdownServer() {
        server.stop();
    }
}
