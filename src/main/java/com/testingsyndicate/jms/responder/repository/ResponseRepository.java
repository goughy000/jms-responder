package com.testingsyndicate.jms.responder.repository;

import com.testingsyndicate.jms.responder.model.Request;
import com.testingsyndicate.jms.responder.model.Response;
import java.util.Optional;

public interface ResponseRepository {

  Optional<Response> findResponse(Request request);
}
