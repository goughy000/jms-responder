package com.testingsyndicate.jms.responder.repository;

import com.testingsyndicate.jms.responder.matcher.Matcher;
import com.testingsyndicate.jms.responder.model.RequestInfo;
import com.testingsyndicate.jms.responder.model.MatchableStubbedResponse;
import com.testingsyndicate.jms.responder.model.StubbedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FixedResponseRepository implements ResponseRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FixedResponseRepository.class);

    private final List<MatchableStubbedResponse> responses;

    public FixedResponseRepository(List<MatchableStubbedResponse> responses) {
        this.responses = new ArrayList<>(responses);
    }

    public Optional<StubbedResponse> findMatch(RequestInfo requestInfo) {
        LOG.trace("Looking for a match for {}", requestInfo);
        Optional<StubbedResponse> match = responses.stream()
                .filter(r -> matches(r, requestInfo))
                .map(r -> (StubbedResponse)r)
                .findFirst();

        if (match.isPresent()) {
            LOG.debug("Found match {}", match.get());
        } else {
            LOG.warn("No matches found for {}", requestInfo);
        }

        return match;
    }

    private static boolean matches(MatchableStubbedResponse response, RequestInfo requestInfo) {
        List<Matcher> matchers = response.getMatchers();
        return null == matchers || matchers.stream().allMatch(m -> m.matches(requestInfo));
    }

}
