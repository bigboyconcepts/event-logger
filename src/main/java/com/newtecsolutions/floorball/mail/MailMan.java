package com.newtecsolutions.floorball.mail;

import com.newtecsolutions.floorball.model.Mail;

/**
 * Created by pedja on 9/18/16.
 *
 * Interface for sending mails
 */

public interface MailMan
{
    boolean send(Mail mail);
}
