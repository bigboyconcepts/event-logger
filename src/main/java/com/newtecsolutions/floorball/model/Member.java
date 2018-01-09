package com.newtecsolutions.floorball.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.newtecsolutions.floorball.utils.HibernateUtil;
import com.newtecsolutions.floorball.utils.LocaleManager;
import com.newtecsolutions.floorball.utils.LogUtils;
import com.newtecsolutions.floorball.utils.PasswordUtils;

import org.hibernate.Session;
import org.skynetsoftware.jutils.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by pedja on 16.9.16. 08.03.
 * This class is part of the Floorball
 * Copyright © 2016 ${OWNER}
 */

@Entity
@Table(
        indexes = {
                @Index(columnList = "email", name = "email_hidx"),
                @Index(columnList = "id", name = "id_hidx")
        }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Member extends HibernateModel
{
    public enum Status
    {
        active, pending, inactive
    }

    private String email;
    private String password;
    @JsonIgnore
    private String passwordHash;
    private String firstName, lastName;
    private Date dataOfBirth;
    private Status status = Status.pending;
    @JsonIgnore
    private String verificationToken, resetPasswordToken;

    private Role role;
    private Club club;
    private File avatar;

    private Set<Track> tracks;

    @Column()
    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    @Column()
    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public Date getDataOfBirth()
    {
        return dataOfBirth;
    }

    public void setDataOfBirth(Date dataOfBirth)
    {
        this.dataOfBirth = dataOfBirth;
    }

    @Column(nullable = false, unique = true, length = 64)
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @Column()
    public String getPasswordHash()
    {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash)
    {
        this.passwordHash = passwordHash;
    }

    @Transient
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
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

    @Column(unique = true)
    public String getVerificationToken()
    {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken)
    {
        this.verificationToken = verificationToken;
    }

    public String getResetPasswordToken()
    {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken)
    {
        this.resetPasswordToken = resetPasswordToken;
    }

    @ManyToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.EAGER)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Role getRole()
    {
        return role;
    }

    public void setRole(Role role)
    {
        this.role = role;
    }

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Club getClub()
    {
        return club;
    }

    public void setClub(Club club)
    {
        this.club = club;
    }

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "MemberTrack", joinColumns = { @JoinColumn(name = "member_id") }, inverseJoinColumns = { @JoinColumn(name = "track_id") })
    public Set<Track> getTracks()
    {
        return tracks;
    }

    public void setTracks(Set<Track> tracks)
    {
        this.tracks = tracks;
    }

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    public File getAvatar()
    {
        return avatar;
    }

    public void setAvatar(File avatar)
    {
        this.avatar = avatar;
    }

    /**
     * Build members full name considering empty first or last name
     * @param returnEmailIfEmpty if true, email will be returned if both email and password are empty*/
    public String getFullName(boolean returnEmailIfEmpty)
    {
        if (!StringUtils.isStringValid(firstName, true) && !StringUtils.isStringValid(lastName, true))
            return returnEmailIfEmpty ? email : null;
        return (StringUtils.isStringValid(firstName) ? firstName + " " : "") + (StringUtils.isStringValid(lastName) ? lastName : "");
    }

    /**
     * Find member by specific filed. Used mostly to find member by email*/
    public static Member findMemberByField(String field, String value)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Member> criteriaQuery = criteriaBuilder.createQuery(Member.class);
        Root<Member> root = criteriaQuery.from(Member.class);
        criteriaQuery.select(root);

        ParameterExpression<String> params = criteriaBuilder.parameter(String.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get(field), params));

        TypedQuery<Member> query = session.createQuery(criteriaQuery);
        query.setParameter(params, value);

        try
        {
            return query.getSingleResult();
        }
        catch (Exception e)
        {
            LogUtils.getLogger().log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }

    public void setProfileDataFrom(Member from)
    {
        firstName = from.firstName;
        lastName = from.lastName;
        dataOfBirth = from.dataOfBirth;
    }

    @Override
    public long getId()
    {
        return super.getId();
    }

    static String rprQuery = "from RolePermissionRole where target = :target and source = :source";

    /**
     * <pre>
     * Check if member has permission to create/modify other member
     * If method returns successfully it is considered that permission is granted
     * </pre>
     * @param permission one of {@link Permission#create_member}, {@link Permission#update_member}, {@link Permission#delete_member}. If not one of those 3 permissions, method just returns
     * @param source member that is performing some action
     * @param targetMember member that is being modified/created. If permission is {@link Permission#update_member} or {@link Permission#delete_track} this cannot be null
     * @param targetRole role of the target member, required if permission is {@link Permission#create_member}*/
    public static void checkPermissionTo(Session session, @Nonnull Permission permission, @Nonnull Member source, Member targetMember, Role targetRole, @Nullable HttpServletRequest request, LocaleManager localeManager)
    {
        if (permission != Permission.create_member && permission != Permission.update_member && permission != Permission.delete_member)
            return;
        if (permission == Permission.create_member)
        {
            if (targetRole == null)
                throw new IllegalArgumentException("targetRole cannot be null if permission == create_member");
        }
        else //if (permission == Permission.update_member || permission == Permission.delete_member)
        {
            if (targetMember == null)
                throw new IllegalArgumentException("targetMember cannot be null if permission == update_member or delete_member");
            if (permission == Permission.update_member && source.getId() == targetMember.getId())
                return;
            targetRole = targetMember.getRole();
        }
        if(permission == Permission.delete_member && source.getId() == targetMember.getId())
            throw Permission.getNoPermissionException(request, localeManager);//no one can delete self
        Role sourceRole = source.getRole();
        RolePermissionRole rpr = (RolePermissionRole) session
                .createQuery(rprQuery)
                .setParameter("source", sourceRole)
                .setParameter("target", targetRole)
                .getSingleResult();
        if (rpr == null)
            throw Permission.getNoPermissionException(request, localeManager);
        List<Permission> permissions = rpr.getPermissionsList();
        if (!permissions.contains(permission))
            throw Permission.getNoPermissionException(request, localeManager);
    }

    /**
     * Find member with username and password and active = 1*/
    public static Member checkUserPass(String username, String password)
    {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
            return null;

        Member member = Member.findMemberByField("email", username);
        if (member == null || member.getStatus() != Member.Status.active)
        {
            return null;
        }
        try
        {
            if (PasswordUtils.validatePassword(password, member.getPasswordHash()))
            {
                return member;
            }
        }
        catch (Exception e)
        {
            LogUtils.getLogger().log(Level.INFO, e.getMessage(), e);
            return null;
        }
        return null;
    }

    /**
     * Find member with email and active = 1*/
    public static Member checkUser(String email)
    {
        if (StringUtils.isEmpty(email))
            return null;

        Member member = Member.findMemberByField("email", email);
        if (member == null || member.getStatus() != Member.Status.active)
        {
            return null;
        }
        return member;
    }

    @Override
    public String toString()
    {
        return "Member{" +
                "id='" + getId() + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dataOfBirth=" + dataOfBirth +
                ", status=" + status +
                ", verificationToken='" + verificationToken + '\'' +
                '}';
    }
}
