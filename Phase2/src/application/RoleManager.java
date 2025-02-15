package application;

public class RoleManager {

    public enum Role {
        ADMIN, INSTRUCTOR, STAFF, USER
    }

    // Check if the user can edit any question (Admin & Instructor only)
    public static boolean canEditAnyQuestion(Role role) {
        return role == Role.ADMIN || role == Role.INSTRUCTOR;
    }

    // Check if the user can delete any question (Only Admin)
    public static boolean canDeleteAnyQuestion(Role role) {
        return role == Role.ADMIN;
    }

    // Check if the user can answer a question (All roles except STAFF)
    public static boolean canAnswerQuestion(Role role) {
        return role == Role.ADMIN || role == Role.INSTRUCTOR || role == Role.USER;
    }

    // Check if the user can moderate content (Admin & Staff)
    public static boolean canModerate(Role role) {
        return role == Role.ADMIN || role == Role.STAFF;
    }

    // Check if the user can edit their own content (All roles)
    public static boolean canEditOwnContent(Role role) {
        return true;
    }
}
