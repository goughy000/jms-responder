package com.testingsyndicate.jms.responder.repository;

import com.testingsyndicate.jms.responder.model.RequestInfo;
import com.testingsyndicate.jms.responder.model.StubbedResponse;

import java.util.Optional;

public interface ResponseRepository {

    Optional<StubbedResponse> findMatch(RequestInfo requestInfo);

}
