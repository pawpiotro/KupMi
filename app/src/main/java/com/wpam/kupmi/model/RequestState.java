package com.wpam.kupmi.model;

public enum RequestState
{
    ACTIVE(1),
    ACCEPTED(0),
    DONE(2),
    UNDONE(3),
    UNKNOWN(-1);

    private int stateId;

    RequestState(int stateId)
    {
        this.stateId = stateId;
    }

    public int getStateId() { return stateId; }

    public String lowerCaseName() { return name().toLowerCase(); }

    public String firstCapitalLetterName()
    {
        String lowerCaseName = lowerCaseName();

        return lowerCaseName.substring(0, 1).toUpperCase() + lowerCaseName.substring(1);
    }

    public static RequestState getInstance(int stateId)
    {
        switch (stateId)
        {
            case 0:
                return ACCEPTED;
            case 1:
                return ACTIVE;
            case 2:
                return DONE;
            case 3:
                return UNDONE;
            default:
                return UNKNOWN;
        }
    }
}
