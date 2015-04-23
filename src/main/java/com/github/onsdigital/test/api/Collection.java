package com.github.onsdigital.test.api;

import com.github.davidcarboni.cryptolite.Random;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.http.Endpoint;
import com.github.onsdigital.http.Http;
import com.github.onsdigital.http.Response;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.api.oneliners.OneLineSetups;
import com.github.onsdigital.zebedee.json.CollectionDescription;
import com.github.onsdigital.zebedee.json.serialiser.IsoDateSerializer;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;


@Api
@DependsOn(Login.class)
public class Collection {

    private static Http http = Login.httpAdministrator;

    public Collection() {
        // Set ISO date formatting in Gson to match Javascript Date.toISODate()
        Serialiser.getBuilder().registerTypeAdapter(Date.class, new IsoDateSerializer());
    }


    /**
     * Test basic functionality
     *
     * Create with publisher permissions should return {@link HttpStatus#OK_200}
     *
     */
    @POST
    @Test
    public void canCreateCollectionWithPublisherPermissions() throws IOException {
        // Given
        // a collection description
        CollectionDescription roundabout = createCollectionDescription();

        // When
        // we post as a publisher
        Response<String> response = post(roundabout, Login.httpPublisher);

        // Expect
        // a response of 200 - success
        assertEquals(HttpStatus.OK_200, response.statusLine.getStatusCode());
    }
    /**
     * Creating an unnamed collection should return {@link HttpStatus#BAD_REQUEST_400}
     *
     */
    @POST
    @Test
    public void shouldReturn400IfNoNameSpecifiedForCreateCollection() throws IOException {
        // Given
        // an incomplete collection description
        CollectionDescription anon = new CollectionDescription();

        // When
        // we post using valid credentials
        Response<String> response = post(anon, Login.httpPublisher);

        // Expect
        // a response of 400 - Bad request
        assertEquals(HttpStatus.BAD_REQUEST_400, response.statusLine.getStatusCode());
    }
    /**
     * written
     */
    @POST
    @Test
    public void shouldReturn409IfCollectionNameAlreadyExists() throws IOException {

        // Given
        // an existing collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        // we try and create an identical collection
        Response<String> response = post(collection, Login.httpPublisher);

        // Expect
        // a reponse of 409 - Conflict
        assertEquals(HttpStatus.CONFLICT_409, response.statusLine.getStatusCode());
    }


    /**
     * Create without publisher permissions should return {@link HttpStatus#UNAUTHORIZED_401}
     *
     */
    @POST
    @Test
    public void shouldReturn401WithoutPublisherPermissions() throws IOException {

        // Given
        // a collection description
        CollectionDescription collection = createCollectionDescription();

        // When
        // we post as anyone but a publisher
        Response<String> responseAdmin = post(collection, Login.httpAdministrator);
        Response<String> responseScallywag = post(collection, Login.httpScallywag);
        Response<String> responseViewer = post(collection, Login.httpViewer);

        // Expect
        // a response of 401 - unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseAdmin.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseScallywag.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, responseViewer.statusLine.getStatusCode());
    }

    /**
     * Viewer permissions should return {@link HttpStatus#OK_200} for any permitted collection, {@link HttpStatus#UNAUTHORIZED_401} otherwise
     *
     * TODO implement once we have mapping from user to collection access
     *
     */
    @GET
    @Test
    public void shouldReturn200ForViewerWithPermissionsOtherwise401() throws IOException {

    }
    /**
     * Admins should return {@link HttpStatus#UNAUTHORIZED_401}
     *
     * TODO implement once we have mapping from user to collection access
     *
     */
    @GET
//    @Test
    public void shouldReturn401WithAdminPermissions() throws IOException {
        // Given
        // a collection
        CollectionDescription collection = createCollectionDescription();
        post(collection, Login.httpPublisher);

        // When
        // we attempt to retrieve it as an admin
        Response<CollectionDescription> response = get(collection.name, Login.httpAdministrator);

        // We expect
        // a response of 401 - unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED_401, response.statusLine.getStatusCode());
    }

    /**
     * Publisher permissions should return {@link HttpStatus#OK_200} for any collection
     *
     */
    @DELETE
    @Test
    public void collectionShouldBeDeletedWithPublisherPermissions() throws IOException {
        // Given
        //...a collection
        CollectionDescription collection = OneLineSetups.publishedCollection();

        // When
        //...we delete it
        delete(collection.name, Login.httpPublisher);

        // We expect
        //...it to be entirely deleted
        Response<CollectionDescription> response = get(collection.name, Login.httpPublisher);
        assertEquals(HttpStatus.NOT_FOUND_404, response.statusLine.getStatusCode());
    }

    /**
     * All other permissions should return {@link HttpStatus#UNAUTHORIZED_401} for any collection
     *
     */
    @DELETE
    @Test
    public void deleteShouldReturn401WithoutPublisherPermissions() throws IOException {
        // Given
        // a collection
        CollectionDescription collection1 = OneLineSetups.publishedCollection();
        CollectionDescription collection2 = OneLineSetups.publishedCollection();
        CollectionDescription collection3 = OneLineSetups.publishedCollection();

        // When
        //...we we try and delete them delete it
        Response<String> deleteResponseScallywag = delete(collection1.name, Login.httpScallywag);
        Response<String> deleteResponseAdministrator = delete(collection2.name, Login.httpAdministrator);
        Response<String> deleteResponseViewer = delete(collection3.name, Login.httpViewer);

        // Then
        // delete should fail with unauthorized returned
        // + the collections should still exist
        assertEquals(HttpStatus.UNAUTHORIZED_401, deleteResponseScallywag.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, deleteResponseAdministrator.statusLine.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED_401, deleteResponseViewer.statusLine.getStatusCode());

        assertEquals(HttpStatus.OK_200, get(collection1.name, Login.httpPublisher).statusLine.getStatusCode());
        assertEquals(HttpStatus.OK_200, get(collection2.name, Login.httpPublisher).statusLine.getStatusCode());
        assertEquals(HttpStatus.OK_200, get(collection3.name, Login.httpPublisher).statusLine.getStatusCode());
    }

    public static Response<String> delete(String name, Http http) throws IOException {
        Endpoint endpoint = ZebedeeHost.collection.addPathSegment(name);
        return http.delete(endpoint, String.class);
    }

    public static Response<String> post(CollectionDescription collection, Http http) throws IOException {
        return http.post(ZebedeeHost.collection, collection, String.class);
    }

    public static CollectionDescription createCollectionDescription() {
        CollectionDescription collection = new CollectionDescription();
        collection.name = "Rusty_" + Random.id();
        collection.publishDate = new Date();
        return collection;
    }

    public static Response<CollectionDescription> get(String name, Http http) throws IOException {
        Endpoint idUrl = ZebedeeHost.collection.addPathSegment(name);
        return http.get(idUrl, CollectionDescription.class);

    }
}
