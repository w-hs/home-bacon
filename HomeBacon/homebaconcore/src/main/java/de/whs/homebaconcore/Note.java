package de.whs.homebaconcore;

import java.io.Serializable;

/**
 * Created by Chris on 25.11.2015.
 */
public class Note implements Serializable{
    private String title;
    private String text;
    private EventType eventType;
    private long timestamp;
    private int noteId;

    public Note(){

    }

    public Note(int noteId, String title, String text, EventType eventType, long timestamp){
        this.noteId = noteId;
        this.title = title;
        this.text = text;
        this.eventType = eventType;
        this.timestamp = timestamp;
    }

    public Note(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public EventType getEventType() { return eventType;}

    public void setEventType(EventType eventType) {this.eventType = eventType; }

    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) {this.timestamp = timestamp;  }

    public int getNoteId() { return noteId;  }

    public void setNoteId(int noteId) {       this.noteId = noteId;   }
}
