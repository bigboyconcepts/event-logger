package com.newtecsolutions.floorball.model;

import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.utils.LocaleManager;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


/**
 * Created by pedja on 6/27/17 3:30 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */

public class MemberTest
{
    //static data, same for every test
    //roles
    private static final Role SUPER_ADMIN_ROLE = new Role();
    private static final Role ADMIN_ROLE = new Role();
    private static final Role NORMAL_MEMBER_ROLE = new Role();

    //rpr
    private static final RolePermissionRole RPR_SA_SA = new RolePermissionRole();
    private static final RolePermissionRole RPR_SA_A = new RolePermissionRole();
    private static final RolePermissionRole RPR_SA_NM = new RolePermissionRole();
    private static final RolePermissionRole RPR_A_NM = new RolePermissionRole();

    static
    {
        SUPER_ADMIN_ROLE.setId(1);
        SUPER_ADMIN_ROLE.setKey("superadmin");
        SUPER_ADMIN_ROLE.setPermissions("view_back");

        ADMIN_ROLE.setId(2);
        ADMIN_ROLE.setKey("admin");
        ADMIN_ROLE.setPermissions("view_back");

        NORMAL_MEMBER_ROLE.setId(3);
        NORMAL_MEMBER_ROLE.setKey("trainer");
        NORMAL_MEMBER_ROLE.setPermissions("view_front,can_register");

        RPR_SA_A.setPermissions("delete_member,create_member,update_member");
        RPR_SA_A.setSource(SUPER_ADMIN_ROLE);
        RPR_SA_A.setTarget(ADMIN_ROLE);

        RPR_SA_SA.setPermissions("delete_member,create_member,update_member");
        RPR_SA_SA.setSource(SUPER_ADMIN_ROLE);
        RPR_SA_SA.setTarget(SUPER_ADMIN_ROLE);

        RPR_SA_NM.setPermissions("delete_member,create_member,update_member");
        RPR_SA_NM.setSource(SUPER_ADMIN_ROLE);
        RPR_SA_NM.setTarget(NORMAL_MEMBER_ROLE);

        RPR_A_NM.setPermissions("create_member,update_member");
        RPR_A_NM.setSource(ADMIN_ROLE);
        RPR_A_NM.setTarget(NORMAL_MEMBER_ROLE);
    }

    //members
    private Member superAdmin, superAdmin2;
    private Member admin, admin2;
    private Member member1;
    private Member member2;

    @Before
    public void beforeTest()
    {
        MockitoAnnotations.initMocks(this);

        //create members
        superAdmin = new Member();
        superAdmin.setId(1);
        superAdmin.setRole(SUPER_ADMIN_ROLE);
        superAdmin.setEmail("admin@gmail.com");

        superAdmin2 = new Member();
        superAdmin2.setId(2);
        superAdmin2.setRole(SUPER_ADMIN_ROLE);

        admin = new Member();
        admin.setId(3);
        admin.setRole(ADMIN_ROLE);
        admin.setLastName("Peric");

        admin2 = new Member();
        admin2.setId(7);
        admin2.setRole(ADMIN_ROLE);

        member1 = new Member();
        member1.setId(4);
        member1.setRole(NORMAL_MEMBER_ROLE);
        member1.setFirstName("Pera");
        member1.setLastName("Peric");
        member1.setEmail("peraperic@gmail.com");

        member2 = new Member();
        member2.setId(5);
        member2.setRole(NORMAL_MEMBER_ROLE);
        member2.setFirstName("Pera");
    }


    private Session session;
    private Query query;

    @Before
    public void setup()
    {
        session = Mockito.mock(Session.class);
        query = Mockito.mock(Query.class);

        Mockito.when(session.createQuery(Member.rprQuery)).thenReturn(query);
    }

    @Test()
    public void testThatMemberHasPermission()
    {
        //DELETE

        //test that superadmin can delete other superadmin, should not throw exception
        _doTest(RPR_SA_SA, Permission.delete_member, superAdmin, superAdmin2, null, false);

        //test that super admin cannot delete self, should throw exception
        _doTest(RPR_SA_SA, Permission.delete_member, superAdmin, superAdmin, null, true);

        //test that superadmin can delete admin, should not throw exception
        _doTest(RPR_SA_A, Permission.delete_member, superAdmin, admin, null, false);

        //test that superadmin can delete normal member, should not throw exception
        _doTest(RPR_SA_A, Permission.delete_member, superAdmin, admin, null, false);

        //test no RolePermissionRole in db, should throw exception
        _doTest(null, Permission.delete_member, superAdmin, admin, null, true);

        //test admin cannot delete superadmin, should throw exception, same as above, not rpr in db
        _doTest(null, Permission.delete_member, admin, superAdmin, null, true);

        //test that admin cant delete normal member, should throw exception
        _doTest(RPR_A_NM, Permission.delete_member, admin, member1, null, true);

        //test that admin cant delete other admins, should throw exception
        _doTest(null, Permission.delete_member, admin, admin2, null, true);

        //test that normal member cant delete other members
        _doTest(null, Permission.delete_member, member1, member2, null, true);

        //DELETE END

        //UPDATE

        //test that superadmin can update other superadmins
        _doTest(RPR_SA_SA, Permission.update_member, superAdmin, superAdmin2, null, false);
        //test that superadmin can update admins
        _doTest(RPR_SA_A, Permission.update_member, superAdmin, admin, null, false);
        //test that superadmin can update normal members
        _doTest(RPR_SA_NM, Permission.update_member, superAdmin, member1, null, false);
        //test that superadmin can update self
        _doTest(null, Permission.update_member, superAdmin, superAdmin, null, false);

        //test that admin cant update superadmin
        _doTest(null, Permission.update_member, admin, superAdmin, null, true);
        //test that admin cant update other admins
        _doTest(null, Permission.update_member, admin, admin2, null, true);
        //test that admin can update normal members
        _doTest(RPR_A_NM, Permission.update_member, admin, member1, null, false);
        //test that admin can update self
        _doTest(null, Permission.update_member, admin, admin, null, false);

        //test that normal member cant update superadmins
        _doTest(null, Permission.update_member, member1, superAdmin, null, true);
        //test that normal member cant update admins
        _doTest(null, Permission.update_member, member1, admin, null, true);
        //test that normal member cant update other members
        _doTest(null, Permission.update_member, member1, member2, null, true);
        //test that normal member can update self
        _doTest(null, Permission.update_member, member1, member1, null, false);

        //UPDATE END

        //CREATE

        //test that superadmin can create superadmins
        _doTest(RPR_SA_SA, Permission.create_member, superAdmin, null, SUPER_ADMIN_ROLE, false);
        //test that superadmin can create admins
        _doTest(RPR_SA_A, Permission.create_member, superAdmin, null, ADMIN_ROLE, false);
        //test that superadmin can create members
        _doTest(RPR_SA_NM, Permission.create_member, superAdmin, null, NORMAL_MEMBER_ROLE, false);

        //test that admin cant create superadmins
        _doTest(null, Permission.create_member, admin, null, SUPER_ADMIN_ROLE, true);
        //test that admin cant create admins
        _doTest(null, Permission.create_member, admin, null, ADMIN_ROLE, true);
        //test that admin cant create members
        _doTest(null, Permission.create_member, admin, null, NORMAL_MEMBER_ROLE, true);

        //test that member cant create superadmins
        _doTest(null, Permission.create_member, member1, null, SUPER_ADMIN_ROLE, true);
        //test that member cant create admins
        _doTest(null, Permission.create_member, member1, null, ADMIN_ROLE, true);
        //test that member cant create other members
        _doTest(null, Permission.create_member, member1, null, SUPER_ADMIN_ROLE, true);

        //CREATE END
    }

    private void _doTest(RolePermissionRole rprToReturnFromQuery, Permission permission, Member source, Member target, Role targetRole, boolean throwExpected)
    {
        try
        {
            Mockito.when(query.getSingleResult()).thenReturn(rprToReturnFromQuery);
            Mockito.when(query.setParameter("source", source.getRole())).thenReturn(query);
            Mockito.when(query.setParameter("target", target == null ? targetRole : target.getRole())).thenReturn(query);

            Member.checkPermissionTo(session, permission, source, target, targetRole, null, new LocaleManager());
            if(throwExpected)
                Assert.fail(buildErrorStringWithDetails("Expected to throw FBException with ErrorCode permission_error", rprToReturnFromQuery, permission, source, target, targetRole, throwExpected));
        }
        catch (FBException e)
        {
            if(!throwExpected)
                Assert.fail(buildErrorStringWithDetails("Exception not expected", rprToReturnFromQuery, permission, source, target, targetRole, throwExpected));
            else
                Assert.assertEquals(e.getErrorCode(), MyResponse.ErrorCode.permission_error);
        }
    }

    private String buildErrorStringWithDetails(String error, RolePermissionRole rprToReturnFromQuery, Permission permission, Member source, Member target, Role targetRole, boolean throwExpected)
    {
        return String.format("%s. rpr: %s, permission: %s, sourceRole: %s, targetRole: %s, targetRole: %s, throwExpected: %b", error,
                rprToReturnFromQuery, permission.toString(), source.getRole().getKey(), target.getRole().getKey(), targetRole == null ? null : targetRole.getKey(), throwExpected);
    }

    @Test
    public void testFullName()
    {
        Assert.assertEquals("Pera Peric", member1.getFullName(false));
        Assert.assertEquals("admin@gmail.com", superAdmin.getFullName(true));

        Assert.assertEquals("Pera ", member2.getFullName(false));
        Assert.assertEquals("Peric", admin.getFullName(false));
    }
}
