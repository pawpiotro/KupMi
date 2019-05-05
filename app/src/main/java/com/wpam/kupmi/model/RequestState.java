package com.wpam.kupmi.model;

public enum RequestState
{
    NEW(0),
    ACCEPTED(1),
    DONE(2),
    UNDONE(3),
    UNKNOWN(-1);

    private int stateId;

    RequestState(int stateId)
    {
        this.stateId = stateId;
    }

    public int getStateId()
    {
        return stateId;
    }

    public static RequestState getInstance(int stateId)
    {
        switch (stateId)
        {
            case 0:
                return NEW;
            case 1:
                return ACCEPTED;
            case 2:
                return DONE;
            case 3:
                return UNDONE;
            default:
                return UNKNOWN;
        }
    }
}
