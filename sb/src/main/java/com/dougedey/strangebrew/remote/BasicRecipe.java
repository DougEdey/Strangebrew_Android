package com.dougedey.strangebrew.remote;

/**
 * Created by doug on 26/04/14.
 */
public class BasicRecipe {
    public long id;
    public String brewer;
    public String style;
    public String title;
    public int iteration;
    public String search;

    public BasicRecipe(long ID, String Brewer, String Type, String Title, int Iteration, String search) {
        this.id = ID;
        this.brewer = Brewer;
        this.style = Type;
        this.title = Title;
        this.iteration = Iteration;
        this.search = search;
    }
}
