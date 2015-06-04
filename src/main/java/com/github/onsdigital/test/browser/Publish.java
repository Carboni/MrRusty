package com.github.onsdigital.test.browser;

import com.github.davidcarboni.cryptolite.Random;
import com.github.onsdigital.junit.DependsOn;
import com.github.onsdigital.test.SetupBeforeTesting;
import com.github.onsdigital.test.browser.PageObjects.*;
import com.github.onsdigital.zebedee.json.Credentials;
import org.junit.Test;

@DependsOn({Create.class})
public class Publish {

    Credentials publisher = SetupBeforeTesting.publisherCredentials;

    @Test
    public void shouldPublishContent() {

        // login and create a collection
        String collectionName = Random.id().substring(0, 5);
        CollectionsPage collectionsPage = new LoginPage().login(publisher.email, publisher.password);
        BrowsePage browsePage = collectionsPage.createCollection(collectionName);

        // create a new page
        String pageName = Random.id().substring(0, 5);
        CreatePage createPage = browsePage.NavigateToT3Page().clickCreateMenuItem();
        EditPage editPage = createPage.createPage(ContentType.article, pageName);

        // add a new section to the page and add markdown content.
        MarkdownEditorPage editorPage = editPage.addContentSection(Random.id().substring(0, 5)).clickEditPage();
//        editPage = editorPage.typeContent("omg this is content!").clickSaveAndExit();
//
//        // submit the page for review.
//        collectionsPage = editPage.clickSubmitForReview();
//
//        // login as the second set of eyes.
//        collectionsPage.clickLogoutMenuLink().typeUsername("p2@t.com").clickLogin();
//
//        // select the page to be reviewed.
//        collectionsPage.clickCollectionByName(collectionName);
//        collectionsPage.clickCollectionPageByName(pageName)
//                .clickEditFile()
//                .clickSubmitForApproval();

        // approve collection

        // go to publish screen and select publish for the collection
    }
}
