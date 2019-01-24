package com.addteq.confluence.plugin.excellentable.filter;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class AuthorizationFilter implements Filter {
    private final UserManager userManager;

    @Autowired
    public AuthorizationFilter(@ComponentImport UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
        
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        UserKey userKey = userManager.getRemoteUserKey(httpServletRequest);
        // Check if the currently loggedin user is Confluence System Admin
        if (!userManager.isSystemAdmin(userKey)) {
            httpServletResponse.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "You are not authorized to do this operation"
            );
        } else {
            chain.doFilter(request, response);
        }
    } 
        
    /**
     * @param filterConfig
     * @throws javax.servlet.ServletException
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig)throws ServletException{
        // TODO Auto-generated method stub
    }
    
    /**
     * @see Filter#destroy()
     */
    @Override
    public void destroy(){
        // TODO Auto-generated method stub
    }

}