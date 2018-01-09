package com.newtecsolutions.floorball;

import com.newtecsolutions.floorball.auth.OAuthUnauthorizedError;
import com.newtecsolutions.floorball.mail.PostOffice;
import com.newtecsolutions.floorball.model.Log;
import com.newtecsolutions.floorball.utils.ConfigManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;
import com.newtecsolutions.floorball.utils.LocaleManager;
import com.newtecsolutions.floorball.utils.LogUtils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.logging.Level;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Catches all exceptions and returns formatted json response*/
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable>
{
    @Context
    HttpServletRequest request;

    private LocaleManager localeManager;

    @Inject
    public GlobalExceptionMapper(LocaleManager localeManager)
    {
        this.localeManager = localeManager;
    }

    public Response toResponse(Throwable ex)
    {
        LogUtils.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        int httpCode = 500;
        String message = ex.getMessage();
        MyResponse.ErrorCode errorCode = MyResponse.ErrorCode.server_error;
        //oauth error returns wrongly formatted message, so fix it
        if (ex instanceof OAuthUnauthorizedError || (ex.getCause() instanceof OAuthUnauthorizedError))
        {
            if (ex.getCause() != null)
            {
                Throwable throwable = ex.getCause();
                if(throwable instanceof OAuthProblemException)
                    message = ((OAuthProblemException)throwable).getDescription();
                else
                    message = throwable.getMessage();
            }

            httpCode = 401;//unauthorized
            errorCode = MyResponse.ErrorCode.oauth_error;//set oauth error
        }
        else if (ex instanceof WebApplicationException)
        {
            WebApplicationException wex = (WebApplicationException) ex;
            httpCode = wex.getResponse().getStatus();
            if (ex instanceof NotFoundException && "OPTIONS".equalsIgnoreCase(request.getMethod()))
            {
                //for CORS preflight, but i thing its unnecessary, jersey handles this
                httpCode = 200;
            }
        }
        else if (ex instanceof FBException)
        {
            FBException pex = (FBException) ex;
            httpCode = pex.getHttpCode();
            errorCode = pex.getErrorCode();
        }

        //if error is loggable, log it to db and send email to dev
        if(errorCode.isLoggable())
        {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Transaction transaction = session.beginTransaction();
            Log log = new Log();
            log.setErrorCode(errorCode);
            log.setHttpCode(httpCode);
            log.setRemoteAddress(request.getRemoteAddr());
            log.setUserAgent(request.getHeader("User-Agent"));
            log.setRequestUri(request.getRequestURI());
            log.setStacktrace(ExceptionUtils.getStackTrace(ex));
            session.save(log);
            try
            {
                transaction.commit();
            }
            catch (Throwable ignore)
            {
            }
            if (ConfigManager.getInstance().getBoolean(ConfigManager.CONFIG_MAIL_SEND_ERROR_REPORT, false))
            {
                PostOffice.getInstance().sendExceptionMail(log, localeManager);
            }
        }
        return MyResponse.errorResponse(errorCode, message, httpCode);
    }
}