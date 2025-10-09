package application;

import databasePart1.DatabaseHelper;
import java.util.ArrayList;

public final class Authorization {
    private Authorization() {}

    // check if user isAdmin
    public static boolean isAdmin(User user, DatabaseHelper db) {
        if (user == null) return false;
        try {
            if (user.getRoles() != null && user.getRoles().contains(Role.ADMIN)) return true;
        } catch (Exception ignore) {}
        try {
            var roles = db.getUserRoles(user.getUserName());
            return roles != null && roles.contains(Role.ADMIN);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // DELETE permissions
    public static boolean hasAdminPowers(User user, DatabaseHelper databaseHelper, boolean adminFlag) {
    	return adminFlag && isAdmin(user, databaseHelper);
    }
    
    public static boolean canDeleteQuestion(User user, DatabaseHelper databaseHelper, Question q, boolean adminFlag) {
        return hasAdminPowers(user, databaseHelper, adminFlag) || isOwner(user, q.getAuthor());
    }
    public static boolean canDeleteAnswer(User user, DatabaseHelper databaseHelper, Answer a, boolean adminFlag) {
        return hasAdminPowers(user, databaseHelper, adminFlag) || isOwner(user, a.getAuthor());
    }
    public static boolean canDeleteComment(User user, DatabaseHelper databaseHelper, Comment c, boolean adminFlag) {
        return hasAdminPowers(user, databaseHelper, adminFlag) || isOwner(user, c.getAuthor());
    }

    //EDIT permissions (Admins cannot edit other people's posts/comments unless you remove the // ) 
    public static boolean canEditQuestion(User user, DatabaseHelper databaseHelper, Question q) {
        return isOwner(user, q.getAuthor()); // || isAdmin(user, databaseHelper)
    }
    public static boolean canEditAnswer(User user, DatabaseHelper databaseHelper, Answer a) {
        return isOwner(user, a.getAuthor()); // || isAdmin(user, databaseHelper)
    }
    public static boolean canEditComment(User user, DatabaseHelper databaseHelper, Comment c) {
        return isOwner(user, c.getAuthor()); // || isAdmin(user, databaseHelper)
    }

    private static boolean isOwner(User current, User author) {
        return current != null && author != null
               && current.getUserName() != null
               && current.getUserName().equals(author.getUserName());
    }
}
