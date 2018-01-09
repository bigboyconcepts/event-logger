package com.newtecsolutions.floorball.intercept;

import com.newtecsolutions.floorball.auth.OAuthUnauthorizedError;
import com.newtecsolutions.floorball.model.OAuthAccessToken;
import com.newtecsolutions.floorball.model.Permission;
import com.newtecsolutions.floorball.utils.AnnotationUtils;
import com.newtecsolutions.floorball.utils.HibernateUtil;
import com.newtecsolutions.floorball.utils.LocaleManager;
import com.newtecsolutions.floorball.utils.LogUtils;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Michal Gajdos
 */
public class MyResourceInterceptor implements MethodInterceptor
{
    public static final String ATTR_TOKEN = "com.newtecsolutions.floorball.attr_token";

    private LocaleManager localeManager;

    MyResourceInterceptor(LocaleManager localeManager)
    {
        this.localeManager = localeManager;
    }

    @Override
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable
    {
        //this will intercept all methods annotated with @Transactional
        //all method invocations will be wrapped in transaction
        //this will also check if member has permission to call this resource
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        /// Begin the transaction.
        Transaction transaction = session.beginTransaction();

        try
        {
            Object resource = methodInvocation.getThis();
            Transactional transactional = AnnotationUtils.getInheritedAnnotation(Transactional.class, methodInvocation.getMethod());
            if (transactional.requiresAuth())
            {
                List<Permission> permissions = Arrays.asList(transactional.requiredPermissions());

                HttpServletRequest request = getRequestReflect(resource);
                OAuthAccessToken token = authorize(request);

                if (!permissions.isEmpty())
                {
                    boolean noElementsInCommon = Collections.disjoint(permissions, token.getMember().getRole().getPermissionsList());
                    if (noElementsInCommon)
                        throw Permission.getNoPermissionException(request, localeManager);
                }

                request.setAttribute(ATTR_TOKEN, token);
            }

            // Invoke JAX-RS resource method.
            final Object result = methodInvocation.proceed();

            // Commit the transaction.
            HibernateUtil.commitTransactionIgnoringConstraintViolation(transaction);

            return result;
        }
        catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e)
        {
            transaction.rollback();
            throw Permission.getNoPermissionException(null, localeManager);
        }
        catch (final Exception re)
        {
            // Something bad happened, rollback;
            transaction.rollback();

            // Rethrow the Exception.
            throw re;
        }
    }

    /**
     * Get HttpServletRequest from resource by reflection*/
    private static HttpServletRequest getRequestReflect(Object o) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException
    {
        Method fieldGetter = o.getClass().getMethod("getRequest");
        return (HttpServletRequest) fieldGetter.invoke(o);
    }

    /**
     * Authorize user, check if token is valid*/
    private OAuthAccessToken authorize(HttpServletRequest request)
    {
        OAuthAccessToken accessToken;
        try
        {
            OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request, ParameterStyle.HEADER);

            String token = oauthRequest.getAccessToken();

            Session session = HibernateUtil.getSessionFactory().getCurrentSession();

            accessToken = session.get(OAuthAccessToken.class, token);
            if (accessToken == null)
                throw new OAuthUnauthorizedError(localeManager.getString("Invalid token: '%s'", request.getLocale(), token));
            else if(accessToken.getExpires().getTime() < System.currentTimeMillis())
                throw new OAuthUnauthorizedError(localeManager.getString("Token has expired: '%s'", request.getLocale(), token));
        }
        catch (OAuthSystemException | OAuthProblemException e)
        {
            LogUtils.getLogger().log(Level.INFO, e.getMessage(), e);
            throw new OAuthUnauthorizedError(e);
        }
        return accessToken;
    }
}