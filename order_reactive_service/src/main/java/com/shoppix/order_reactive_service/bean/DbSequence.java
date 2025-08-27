package com.shoppix.order_reactive_service.bean;

import org.springframework.stereotype.Component;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection="db_sequence")
@Component
public class DbSequence {

    @Id
    private String id;

    @Field
    private long seq;

    public DbSequence(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }
}
