package com.example.csit314sdm;

import java.util.Date;

// ENTITY: Represents a match between a CSR and a PIN.
public class Match {
    private String pinId;
    private String pinName;
    private int matchCount;
    private Date lastInteraction;

    public Match(String pinId, String pinName, int matchCount, Date lastInteraction) {
        this.pinId = pinId;
        this.pinName = pinName;
        this.matchCount = matchCount;
        this.lastInteraction = lastInteraction;
    }

    public String getPinId() {
        return pinId;
    }

    public String getPinName() {
        return pinName;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public Date getLastInteraction() {
        return lastInteraction;
    }
}
