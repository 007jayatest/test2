package ut.com.addteq.confluence.plugin.excellentable.filter;

import com.addteq.confluence.plugin.excellentable.filter.AuthorizationFilter;
import com.atlassian.sal.api.user.UserManager;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import com.atlassian.sal.api.user.UserKey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationFilterTest {

    @Mock
    HttpServletRequest httpServletRequest;
    @Mock
    HttpServletResponse httpServletResponse;
    @Mock
    UserManager userManager;
    
    AuthorizationFilter authorizationFilter;
    @Mock
    FilterChain filterChain;

    UserKey userKeyNotAdmin = new UserKey("notAdmin");
    UserKey userKeyAdmin = new UserKey("admin");
    

    @Before
    public void setup() {
        authorizationFilter = new AuthorizationFilter(userManager);
    }

    @After
    public void tearDown() {
        authorizationFilter.destroy();
    }

    /**
     * Test case when user is not system admin
     * @throws IOException
     * @throws ServletException 
     */
    @Test
    public void isUserNonSystemAdmin() throws IOException, ServletException {
        
            when(userManager.getRemoteUserKey(httpServletRequest)).thenReturn(userKeyAdmin);
            authorizationFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
            verify(httpServletResponse).sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "You are not authorized to do this operation"
            );
    }
    
    /**
     * Test case when user is system admin.
     * @throws IOException
     * @throws ServletException 
     */
    @Test
    public void isUserSystemAdmin() throws IOException, ServletException {
        
        when(userManager.getRemoteUserKey(httpServletRequest)).thenReturn(userKeyAdmin);
        when(userManager.isSystemAdmin(userKeyAdmin)).thenReturn(Boolean.TRUE);
        
        authorizationFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        
        
    }
    
    /**
     * Test case when user does not exist
     * @throws IOException
     * @throws ServletException 
     */
    @Test
    public void userDoesNotExist() throws IOException, ServletException {
        /**
         * Return null for the given user so that it can satisfy the condition
         * USER_DOES_NOT_EXISTS.
         */
        when(userManager.getRemoteUserKey(httpServletRequest)).thenReturn(null);
        authorizationFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        verify(httpServletResponse).sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "You are not authorized to do this operation"
        );
    }
}
