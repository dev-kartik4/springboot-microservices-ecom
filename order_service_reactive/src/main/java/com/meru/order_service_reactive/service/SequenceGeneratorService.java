package com.meru.order_service_reactive.service;

import com.meru.order_service_reactive.bean.DbSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class SequenceGeneratorService {

    public MongoOperations mongoOperations;

    public long generateSequence(String seqName){

        DbSequence counter = mongoOperations.findAndModify(new Query(where("_id").is(seqName)),
                new Update().inc("seq",1),options().returnNew(true).upsert(true), DbSequence.class);

        return !Objects.isNull(counter) ? counter.getSeq() : 1;
    }
}
