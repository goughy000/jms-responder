package com.testingsyndicate.jms.responder.repository;

import com.testingsyndicate.jms.responder.matcher.Matcher;
import com.testingsyndicate.jms.responder.model.Request;
import com.testingsyndicate.jms.responder.model.MatchableResponse;
import com.testingsyndicate.jms.responder.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FixedResponseRepository implements ResponseRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FixedResponseRepository.class);

    private final List<MatchableResponse> responses;

    public FixedResponseRepository(List<MatchableResponse> responses) {
        this.responses = new ArrayList<>(responses);
    }

    public Optional<Response> findResponse(Request request) {
        LOG.trace("Looking for a match for {}", request);
        Optional<Response> match = responses.stream()
                .filter(r -> matches(r, request))
                .map(r -> (Response)r)
                .findFirst();

        if (match.isPresent()) {
            LOG.debug("Found match {}", match.get());
        } else {
            LOG.warn("No matches found for {}", request);
        }

        return match;
    }

    private static boolean matches(MatchableResponse response, Request request) {
        List<Matcher> matchers = response.getMatchers();
        return null == matchers || matchers.stream().allMatch(m -> m.matches(request));
    }

}
