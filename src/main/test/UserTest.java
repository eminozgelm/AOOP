

import com.example.demo.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {

    @Test
    public void testUserBuilder() {
        String username = "testUser";
        String password = "testPassword";
        String firstName = "John";
        String lastName = "Doe";
        String[] profileInfos = {"info1", "info2"};
        int[] friendsArray = {1, 2, 3};
        int isHidden = 0;

        User user = new User.UserBuilder(username, password)
                .firstName(firstName)
                .lastName(lastName)
                .profileInfos(profileInfos)
                .friendsArray(friendsArray)
                .isHidden(isHidden)
                .build();

        // Check user attributes
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(profileInfos, user.getProfileInfos());
        assertEquals(friendsArray, user.getFriendsArray());
        assertEquals(isHidden, user.getIsHidden());
    }
}
