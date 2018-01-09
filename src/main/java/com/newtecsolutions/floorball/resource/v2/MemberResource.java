package com.newtecsolutions.floorball.resource.v2;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("/v2/members")
public class MemberResource
{
    @Context
    HttpServletRequest request;
}