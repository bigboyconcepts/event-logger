package com.newtecsolutions.floorball.mail;

import com.newtecsolutions.floorball.model.Log;
import com.newtecsolutions.floorball.model.Mail;
import com.newtecsolutions.floorball.model.Member;
import com.newtecsolutions.floorball.utils.ConfigManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;
import com.newtecsolutions.floorball.utils.LocaleManager;
import com.newtecsolutions.floorball.utils.LogUtils;
import com.newtecsolutions.floorball.utils.TemplateManager;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

/**
 * Created by pedja on 9/18/16.
 * <p>
 * Responsible for sending all mails
 */

public class PostOffice
{
    private static PostOffice instance;

    public static PostOffice getInstance()
    {
        if (instance == null)
        {
            instance = new PostOffice();
        }
        return instance;
    }

    private MailMan mailMan;

    private BlockingQueue<Mail> mMailQueue;
    private List<Bicycle> mWorkers;

    public PostOffice()
    {
        //set mail provider
        //mail provider is implementation of MailMain
        //reads provider from config, if invalid uses default SMTPMailMan
        String providerClassName = ConfigManager.getInstance().getString(ConfigManager.CONFIG_MAIL_PROVIDER_CLASS_NAME);
        try
        {
            Class<? extends com.newtecsolutions.floorball.mail.MailMan> providerClass = (Class<? extends com.newtecsolutions.floorball.mail.MailMan>) Class.forName(providerClassName);
            mailMan = providerClass.newInstance();
        }
        catch (Exception e)
        {
            LogUtils.warning("Invalid mail provider in config: " + e.getMessage());
            mailMan = new SMTPMailMan();
        }
        //create mail workers queue, and start mail sending workers
        mMailQueue = new LinkedBlockingQueue<>();
        int workerThreadCount = ConfigManager.getInstance().getInt(ConfigManager.CONFIG_MAIL_WORKER_THREAD_COUNT, 2);
        mWorkers = new ArrayList<>(workerThreadCount);
        for (int i = 0; i < workerThreadCount; i++)
        {
            Bicycle worker = new Bicycle(this);
            mWorkers.add(worker);
            worker.start();
        }

        if (ConfigManager.getInstance().getBoolean(ConfigManager.CONFIG_MAIL_QUEUE_ADD_FROM_DB, false))
        {
            //get all mails from database, and add them to queue
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.getTransaction();
            if (!transaction.isActive())
            {
                transaction = session.beginTransaction();
            }

            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Mail> criteriaQuery = criteriaBuilder.createQuery(Mail.class);
            Root<Mail> root = criteriaQuery.from(Mail.class);
            criteriaQuery.select(root);

            ParameterExpression<Mail.Status> params = criteriaBuilder.parameter(Mail.Status.class);
            criteriaQuery.where(criteriaBuilder.equal(root.get("status"), params));

            TypedQuery<Mail> query = session.createQuery(criteriaQuery);
            query.setParameter(params, Mail.Status.pending);

            List<Mail> mails = query.getResultList();

            mMailQueue.addAll(mails);

            transaction.commit();
            session.close();
        }
    }

    /**
     * Send mail. Mail will be added to queue for sending and saved in db
     *
     * @param newTransaction if true, start new transaction for saving mail to db
     */
    public void sendMail(final Mail mail, boolean newTransaction, boolean addToDb)
    {

        if (addToDb)
        {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Transaction transaction = null;
            if (newTransaction)
            {
                transaction = session.beginTransaction();
            }
            Long id = (Long) session.save(mail);
            mail.setId(id);
            if (transaction != null) transaction.commit();
        }
        mMailQueue.add(mail);
    }

    public boolean sendMailDirectly(final Mail mail)
    {
        return mailMan.send(mail);
    }

    /**
     * Clear queue and stop workers
     */
    public static void shutdownIfRunning()
    {
        if (instance != null)
        {
            for (Bicycle worker : instance.mWorkers)
            {
                worker.quit();
            }
            instance.mWorkers.clear();
        }
    }

    /**
     * */
    public void sendEmailVerificationMail(String baseUrl, Member member, LocaleManager localeManager, Locale locale)
    {
        Mail mail = new Mail();
        mail.setStatus(Mail.Status.pending);
        mail.setFrom(ConfigManager.getInstance().getString(ConfigManager.CONFIG_MAIL_FROM, "noreply@localhost"));
        mail.setTo(member.getEmail());
        mail.setSubject(localeManager.getString("Confirm Your New Floorball Account", locale));

        // a little hack using viewable, instead of manually configuring freemarker
        Map<String, Object> map = new HashMap<>();
        map.put("member", member);
        map.put("link", String.format(baseUrl + "members/verify/%d/%s", member.getId(), member.getVerificationToken()));
        map.put("base_url", baseUrl.substring(0, baseUrl.length() - 4));
        String text = TemplateManager.getInstance().processTemplate("verify/verify_email.ftl", map);
        mail.setMessage(text);

        sendMail(mail, false, true);
    }

    public void sendResetPasswordMail(String baseUrl, Member member, LocaleManager localeManager, Locale locale)
    {
        Mail mail = new Mail();
        mail.setStatus(Mail.Status.pending);
        mail.setFrom(ConfigManager.getInstance().getString(ConfigManager.CONFIG_MAIL_FROM, "noreply@localhost"));
        mail.setTo(member.getEmail());
        mail.setSubject(localeManager.getString("Floorball - Password reset", locale));

        // a little hack using viewable, instead of manually configuring freemarker
        Map<String, Object> map = new HashMap<>();
        map.put("member", member);
        map.put("link", String.format(baseUrl + "members/new_password/%d/%s", member.getId(), member.getResetPasswordToken()));
        map.put("base_url", baseUrl.substring(0, baseUrl.length() - 4));
        String text = TemplateManager.getInstance().processTemplate("verify/reset_password_email.ftl", map);
        mail.setMessage(text);

        sendMail(mail, false, true);
    }

    public void sendNewPasswordMail(String baseUrl, String password, Member member, LocaleManager localeManager, Locale locale)
    {
        Mail mail = new Mail();
        mail.setStatus(Mail.Status.pending);
        mail.setFrom(ConfigManager.getInstance().getString(ConfigManager.CONFIG_MAIL_FROM, "noreply@localhost"));
        mail.setTo(member.getEmail());
        mail.setSubject(localeManager.getString("Floorball - New password", locale));

        // a little hack using viewable, instead of manually configuring freemarker
        Map<String, Object> map = new HashMap<>();
        map.put("member", member);
        map.put("password", password);
        map.put("base_url", baseUrl.substring(0, baseUrl.length() - 4));
        String text = TemplateManager.getInstance().processTemplate("verify/reset_password_new_password_email.ftl", map);
        mail.setMessage(text);

        sendMail(mail, false, true);
    }

    public void sendExceptionMail(Log log, LocaleManager localeManager)
    {
        Mail mail = new Mail();
        mail.setStatus(Mail.Status.pending);
        mail.setFrom(ConfigManager.getInstance().getString(ConfigManager.CONFIG_MAIL_FROM, "noreply@localhost"));
        mail.setTo(ConfigManager.getInstance().getString(ConfigManager.CONFIG_MAIL_DEV_ERR_MAIL, "predragcokulov@gmail.com"));
        mail.setSubject(localeManager.getString("Floorball - Server Application Exception", null));

        // a little hack using viewable, instead of manually configuring freemarker
        Map<String, Object> map = new HashMap<>();
        map.put("stackTrace", "<pre>" + log.getStacktrace() + "</pre>");
        map.put("errorCode", log.getErrorCode());
        map.put("httpCode", log.getHttpCode());
        map.put("userAgent", log.getUserAgent());
        map.put("ipAddress", log.getRemoteAddress());
        map.put("requestUri", log.getRequestUri());
        String text = TemplateManager.getInstance().processTemplate("dev/app_exception.ftl", map);
        mail.setMessage(text);

        mMailQueue.add(mail);
    }

    public Map<String, Object> info()
    {
        Map<String, Object> map = new HashMap<>();

        map.put("providerClass", mailMan != null ? mailMan.getClass() : null);
        map.put("queue", mMailQueue.size());

        return map;
    }

    private static class Bicycle extends Thread
    {
        private boolean mQuit;

        private PostOffice manager;

        Bicycle(PostOffice manager)
        {
            this.manager = manager;
        }

        @Override
        public void run()
        {
            while (true)
            {
                final Mail mail;
                try
                {
                    // Take a request from the queue.
                    mail = manager.mMailQueue.take();
                }
                catch (InterruptedException e)
                {
                    // We may have been interrupted because it was time to quit.
                    if (mQuit)
                    {
                        return;
                    }
                    continue;
                }

                try
                {
                    boolean sent = manager.mailMan.send(mail);
                    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
                    Transaction transaction = session.beginTransaction();
                    if (sent)
                    {
                        LogUtils.info("Mail sent: " + mail.getTo());
                        session.remove(mail);
                    }
                    else
                    {
                        LogUtils.info("Mail not sent: " + mail.getTo());
                        session.save(mail);
                    }
                    transaction.commit();
                }
                catch (HibernateException e)
                {
                    LogUtils.getLogger().log(Level.WARNING, e.getMessage(), e);
                    LogUtils.info("Mail not sent: " + mail.getTo());
                }
            }
        }

        void quit()
        {
            mQuit = false;
            interrupt();
        }
    }
}
