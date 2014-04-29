package com.dougedey.strangebrew.recipe;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dougedey.strangebrew.remote.BasicRecipe;
import com.dougedey.strangebrew.remote.Content;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ca.strangebrew.Debug;
import ca.strangebrew.Options;

/**
 * Created by doug on 26/04/14.
 */
public class RemoteRecipes {

    public static String cloudURL = "http://strangebrewcloud.appspot.com";

    public void loadRecipesByStyle(String style) {

        if (cloudURL == null || cloudURL.equals(""))
            return;

        Content.clear();
        try
        {
            //URL url = new URL(baseURL+"/style/" + style);
            URI rURI = new URI("http", null, RemoteRecipes.cloudURL,
                    80, "/styles/"+style, null, null);
            URL url = rURI.toURL();

            InputStream response = url.openStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setValidating(false);
            dbf.setIgnoringComments(false);
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setNamespaceAware(true);
            // dbf.setCoalescing(true);
            // dbf.setExpandEntityReferences(true);

            DocumentBuilder db = null;
            db = dbf.newDocumentBuilder();
            //db.setEntityResolver(new NullResolver());
            // db.setErrorHandler( new MyErrorHandler());
            Document readXML = db.parse(response);
            NodeList childNodes = readXML.getElementsByTagName("recipe");

            Debug.print("Loading recipes from online: " + childNodes.getLength());
            for(int x = 0; x < childNodes.getLength(); x++ ) {
                Node child = childNodes.item(x);
                NamedNodeMap childAttr = child.getAttributes();

                // generate the recipe list
                int ID = Integer.parseInt( childAttr.getNamedItem("id").getNodeValue() );
                String Brewer = childAttr.getNamedItem("brewer").getNodeValue().toString();
                String Title = childAttr.getNamedItem("name").getNodeValue().toString();
                String Style = childAttr.getNamedItem("style").getNodeValue().toString();
                int iteration = 0;//Integer.parseInt(childAttr.getNamedItem("iteration").getNodeValue());
                Debug.print("Loading: " + Title);
                BasicRecipe rRecipe = new BasicRecipe(ID, Brewer, Style, Title, iteration);
                Content.addRecipe(rRecipe);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadRecipesByBrewer(String brewer) {

        Content.clear();
        try
        {
            URI rURI = new URI("http", null, cloudURL, 80, "/brewer/"+brewer, null, null);
            URL url = rURI.toURL();
            InputStream response = url.openStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setValidating(false);
            dbf.setIgnoringComments(false);
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setNamespaceAware(true);
            // dbf.setCoalescing(true);
            // dbf.setExpandEntityReferences(true);

            DocumentBuilder db = null;
            db = dbf.newDocumentBuilder();
            //db.setEntityResolver(new NullResolver());

            // db.setErrorHandler( new MyErrorHandler());

            Document readXML = db.parse(response);
            NodeList childNodes = readXML.getElementsByTagName("recipe");

            Debug.print("Loading recipes from online: "+ childNodes.getLength());
            for(int x = 0; x < childNodes.getLength(); x++ ) {
                Node child = childNodes.item(x);
                NamedNodeMap childAttr = child.getAttributes();

                // generate the recipe list
                int ID = Integer.parseInt( childAttr.getNamedItem("id").getNodeValue() );
                String Brewer = childAttr.getNamedItem("brewer").getNodeValue().toString();
                String Title = childAttr.getNamedItem("name").getNodeValue().toString();
                String Style = childAttr.getNamedItem("style").getNodeValue().toString();
                int iteration = 0;//Integer.parseInt(childAttr.getNamedItem("iteration").getNodeValue());
                Debug.print("Loading: " + Title);
                BasicRecipe rRecipe = new BasicRecipe(ID, Brewer, Style, Title, iteration);
                Content.addRecipe(rRecipe);


            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void getListOfStyles() {

        try
        {
            URI rURI = new URI("http", null, cloudURL, 80, "/styles/", null, null);
            URL url = rURI.toURL();
            //URL url = new URL(baseURL+"/styles/");
            InputStream response = url.openStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setValidating(false);
            dbf.setIgnoringComments(false);
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setNamespaceAware(true);
            // dbf.setCoalescing(true);
            // dbf.setExpandEntityReferences(true);

            DocumentBuilder db = null;
            db = dbf.newDocumentBuilder();

            Document readXML = db.parse(response);
            NodeList childNodes = readXML.getElementsByTagName("style");


            Debug.print("Loading Styles from online: "+ childNodes.getLength());

            for(int x = 0; x < childNodes.getLength(); x++ ) {


                Node child = childNodes.item(x);

                // generate the style list
                Debug.print("Style: " + child.getTextContent());
                if(child.getTextContent() != null) {
                    //styles.add(child.getTextContent());

                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }

    }
}
