//package com.meru.inventory_service.service;
//
//import com.meru.inventory_service.entity.DbSequence;
//import org.springframework.data.mongodb.core.MongoOperations;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.stereotype.Service;
//
//import java.util.Objects;
//
//import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
//import static org.springframework.data.mongodb.core.query.Criteria.where;
//
//@Service
//public class SequenceGeneratorService {
//
//    private MongoOperations mongoOperations;
//
//    public SequenceGeneratorService(MongoOperations mongoOperations){
//        this.mongoOperations = mongoOperations;
//    }
//
//    public int generateSequence(String seqName){
//
//        DbSequence counter = mongoOperations.findAndModify(new Query(where("_id").is(seqName)),
//                new Update().inc("seq",1),options().returnNew(true).upsert(true),DbSequence.class);
//
//        return !Objects.isNull(counter) ? (int) counter.getSeq() : 1;
//    }
//}
