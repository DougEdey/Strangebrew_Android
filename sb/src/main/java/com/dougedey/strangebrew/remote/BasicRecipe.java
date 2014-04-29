package com.dougedey.strangebrew.remote;

/**
 * Created by doug on 26/04/14.
 */
public class BasicRecipe {
    public int id;
    public String brewer;
    public String style;
    public String title;
    public int iteration;

    public BasicRecipe(int ID, String Brewer, String Type, String Title, int Iteration) {
        id = ID;
        brewer = Brewer;
        style = Type;
        title = Title;
        iteration = Iteration;
    }
}
