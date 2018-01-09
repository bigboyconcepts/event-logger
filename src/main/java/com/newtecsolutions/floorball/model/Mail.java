package com.newtecsolutions.floorball.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by pedja on 9/18/16.
 */

@Entity
public class Mail extends HibernateModel
{
    public enum Status
    {
        pending, failed
    }

    private String to, from, subject, message, statusMessage, statusTrace;
    private Status status = Status.pending;

    @Column(name = "_to")
    public String getTo()
    {
        return to;
    }

    @Column(name = "_from")
    public String getFrom()
    {
        return from;
    }

    public String getSubject()
    {
        return subject;
    }

    @Column(length = 4000)
    public String getMessage()
    {
        return message;
    }

    @Enumerated(EnumType.STRING)
    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage)
    {
        this.statusMessage = statusMessage;
    }

    @Column(length = 4000)
    public String getStatusTrace()
    {
        return statusTrace;
    }

    public void setStatusTrace(String statusTrace)
    {
        this.statusTrace = statusTrace;
    }
}
