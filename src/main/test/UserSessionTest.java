import com.example.demo.UserSession;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserSessionTest {

    @Test
    void getInstanceTest() {
        // Test that getInstance returns a non-null instance
        UserSession instance = UserSession.getInstance();
        assertNotNull(instance);

        // Test that getInstance always returns the same instance
        UserSession secondInstance = UserSession.getInstance();
        assertEquals(instance, secondInstance);
    }

    @Test
    void getUserIdTest() {
        UserSession userSession = UserSession.getInstance();

        // Initially, userId should be 0
        assertEquals(0, userSession.getUserId());

        // Set userId to a specific value
        int userId = 123;
        userSession.setUserId(userId);
        assertEquals(userId, userSession.getUserId());
    }

    @Test
    void setUserIdTest() {
        UserSession userSession = UserSession.getInstance();

        // Initially, userId should be 0


        // Set userId to a specific value
        int userId = 456;
        userSession.setUserId(userId);
        assertEquals(userId, userSession.getUserId());

        // Set userId to another value
        int newUserId = 789;
        userSession.setUserId(newUserId);
        assertEquals(newUserId, userSession.getUserId());
    }
}
