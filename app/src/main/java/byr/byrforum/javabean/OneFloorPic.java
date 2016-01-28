package byr.byrforum.javabean;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by suiyue on 2016/1/27 0027.
 */
public class OneFloorPic {
    private String author;
    private String content;
    private String floor;
    private ArrayList<Bitmap> pics;

    public OneFloorPic(){}

    public OneFloorPic(String author, String content, String floor, ArrayList<Bitmap> pics) {

        this.author = author;
        this.content = content;
        this.floor = floor;
        this.pics = pics;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public ArrayList<Bitmap> getPics() {
        return pics;
    }

    public void setPics(ArrayList<Bitmap> pics) {
        this.pics = pics;
    }
}
