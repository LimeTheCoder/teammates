package teammates.common.datatransfer.attributes;

import java.util.ArrayList;
import java.util.List;

import teammates.common.datatransfer.InstructorPrivileges;
import teammates.common.util.Const;
import teammates.common.util.FieldValidator;
import teammates.common.util.JsonUtils;
import teammates.common.util.SanitizationHelper;
import teammates.storage.entity.Instructor;

/**
 * The data transfer class for Instructor entities.
 */
public class InstructorAttributes extends EntityAttributes {

    public static final String DEFAULT_DISPLAY_NAME = "Instructor";

    // Note: be careful when changing these variables as their names are used in *.json files.
    public String googleId;
    public String courseId;
    public String name;
    public String email;
    public String key;
    public String role = Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER;
    public String displayedName = DEFAULT_DISPLAY_NAME;
    public Boolean isArchived = false;
    public boolean isDisplayedToStudents = true;

    public InstructorPrivileges privileges = new InstructorPrivileges(
            Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER);

    /**
     * Builder class to simplify creation of object.
     */
    public static class Builder {
        private final InstructorAttributes instructorAttributes = new InstructorAttributes();

        public Builder googleId(String googleId) {
            instructorAttributes.googleId = SanitizationHelper.sanitizeGoogleId(googleId);
            return this;
        }

        public Builder courseId(String courseId) {
            instructorAttributes.courseId = SanitizationHelper.sanitizeTitle(courseId);
            return this;
        }

        public Builder name(String name) {
            instructorAttributes.name = SanitizationHelper.sanitizeName(name);
            return this;
        }

        public Builder email(String email) {
            instructorAttributes.email = email;
            return this;
        }

        public Builder key(String key) {
            instructorAttributes.key = key;
            return this;
        }

        public Builder role(String role) {
            instructorAttributes.role = (role == null)
                    ? Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER
                    : SanitizationHelper.sanitizeName(role);
            return this;
        }

        public Builder displayedName(String displayedName) {
            instructorAttributes.displayedName = (displayedName == null)
                    ? DEFAULT_DISPLAY_NAME
                    : SanitizationHelper.sanitizeName(displayedName);
            return this;
        }

        public Builder isArchived(Boolean isArchived) {
            instructorAttributes.isArchived = isArchived != null && isArchived;
            return this;
        }

        public Builder isDisplayedToStudents(boolean isDisplayedToStudents) {
            instructorAttributes.isDisplayedToStudents = isDisplayedToStudents;
            return this;
        }

        public Builder privileges(InstructorPrivileges privileges) {
            instructorAttributes.privileges = (privileges == null)
                    ? new InstructorPrivileges(Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER)
                    : privileges;
            return this;
        }

        public Builder privileges(String privilegesAsText) {
            instructorAttributes.privileges = (privilegesAsText == null)
                    ? new InstructorPrivileges(Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER)
                    : InstructorAttributes.getInstructorPrivilegesFromText(privilegesAsText);
            return this;
        }

        public InstructorAttributes build() {
            return instructorAttributes;
        }
    }

    /** Return new builder instance. */
    public static Builder builder() {
        return new Builder();
    }

    public static InstructorAttributes valueOf(Instructor instructor) {
        return builder()
                .googleId(instructor.getGoogleId())
                .courseId(instructor.getCourseId())
                .name(instructor.getName())
                .email(instructor.getEmail())
                .key(instructor.getRegistrationKey())
                .role(instructor.getRole())
                .displayedName(instructor.getDisplayedName())
                .privileges(instructor.getInstructorPrivilegesAsText())
                .isDisplayedToStudents(instructor.isDisplayedToStudents())
                .isArchived(instructor.getIsArchived())
                .build();
    }

    // Do we really need that method? It creates only shallow copy of object.
    // If we need exactly that behavior and not deep copy of object we can implement cloneable interface
    // and use clone() method instead.
    public InstructorAttributes getCopy() {
        return builder()
                .googleId(googleId).courseId(courseId).name(name).email(email).key(key)
                .role(role).displayedName(displayedName).privileges(privileges)
                .isDisplayedToStudents(isDisplayedToStudents).isArchived(isArchived)
                .build();
    }

    public String getTextFromInstructorPrivileges() {
        return JsonUtils.toJson(privileges, InstructorPrivileges.class);
    }

    private static InstructorPrivileges getInstructorPrivilegesFromText(String instructorPrivilegesAsText) {
        return JsonUtils.fromJson(instructorPrivilegesAsText, InstructorPrivileges.class);
    }

    public String getName() {
        return name;
    }

    public String getDisplayedName() {
        return displayedName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isDisplayedToStudents() {
        return isDisplayedToStudents;
    }

    public boolean isRegistered() {
        return googleId != null;
    }

    @Override
    public Instructor toEntity() {
        if (key != null) {
            return new Instructor(googleId, courseId, isArchived, name, email, key, role,
                                  isDisplayedToStudents, displayedName, getTextFromInstructorPrivileges());
        }
        return new Instructor(googleId, courseId, isArchived, name, email, role,
                              isDisplayedToStudents, displayedName, getTextFromInstructorPrivileges());
    }

    @Override
    public List<String> getInvalidityInfo() {
        FieldValidator validator = new FieldValidator();
        List<String> errors = new ArrayList<String>();

        if (googleId != null) {
            addNonEmptyError(validator.getInvalidityInfoForGoogleId(googleId), errors);
        }

        addNonEmptyError(validator.getInvalidityInfoForCourseId(courseId), errors);

        addNonEmptyError(validator.getInvalidityInfoForPersonName(name), errors);

        addNonEmptyError(validator.getInvalidityInfoForEmail(email), errors);

        addNonEmptyError(validator.getInvalidityInfoForPersonName(displayedName), errors);

        return errors;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this, InstructorAttributes.class);
    }

    @Override
    public String getIdentificationString() {
        return courseId + "/" + email;
    }

    @Override
    public String getEntityTypeAsString() {
        return "Instructor";
    }

    @Override
    public String getBackupIdentifier() {
        return Const.SystemParams.COURSE_BACKUP_LOG_MSG + courseId;
    }

    @Override
    public String getJsonString() {
        return JsonUtils.toJson(this, InstructorAttributes.class);
    }

    @Override
    public void sanitizeForSaving() {
        googleId = SanitizationHelper.sanitizeGoogleId(googleId);
        name = SanitizationHelper.sanitizeForHtml(SanitizationHelper.sanitizeName(name));
        email = SanitizationHelper.sanitizeEmail(email);
        courseId = SanitizationHelper.sanitizeTitle(courseId);

        if (role == null) {
            role = Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER;
        } else {
            role = SanitizationHelper.sanitizeForHtml(SanitizationHelper.sanitizeName(role));
        }

        if (displayedName == null) {
            displayedName = Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER;
        } else {
            displayedName = SanitizationHelper.sanitizeForHtml(SanitizationHelper.sanitizeName(displayedName));
        }

        if (privileges == null) {
            privileges = new InstructorPrivileges(Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER);
        }
    }

    public boolean isAllowedForPrivilege(String privilegeName) {
        if (privileges == null) {
            privileges = new InstructorPrivileges(Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER);
        }
        return privileges.isAllowedForPrivilege(privilegeName);
    }

    public boolean isAllowedForPrivilege(String sectionName, String privilegeName) {
        if (privileges == null) {
            privileges = new InstructorPrivileges(Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER);
        }
        return privileges.isAllowedForPrivilege(sectionName, privilegeName);
    }

    public boolean isAllowedForPrivilege(String sectionName, String sessionName, String privilegeName) {
        if (privileges == null) {
            privileges = new InstructorPrivileges(Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER);
        }
        return privileges.isAllowedForPrivilege(sectionName, sessionName, privilegeName);
    }

    /**
     * Returns true if privilege for session is present for any section.
     */
    public boolean isAllowedForPrivilegeAnySection(String sessionName, String privilegeName) {
        if (privileges == null) {
            privileges = new InstructorPrivileges(Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER);
        }
        return privileges.isAllowedForPrivilegeAnySection(sessionName, privilegeName);
    }

    public boolean hasCoownerPrivileges() {
        return privileges.hasCoownerPrivileges();
    }

    public boolean hasManagerPrivileges() {
        return privileges.hasManagerPrivileges();
    }

    public boolean hasObserverPrivileges() {
        return privileges.hasObserverPrivileges();
    }

    public boolean hasTutorPrivileges() {
        return privileges.hasTutorPrivileges();
    }

    /**
     * Returns true if this instructor object is equal with the given {@code instructor} object.
     *
     * @param instructor
     *            the {@link InstructorAttributes} of an instructor, cannot be
     *            {@code null}
     * @return true if this {@link InstructorAttributes} is equal to
     *         {@code instructor}, otherwise false
     */
    public boolean isEqualToAnotherInstructor(InstructorAttributes instructor) {
        // JsonParser is used instead of
        // this.getJsonString().equals(instructor.getJsonString) so that the
        // comparison ignores the order of key-value pairs in the json strings.
        return JsonUtils.parse(getJsonString()).equals(JsonUtils.parse(instructor.getJsonString()));
    }

    public boolean isCustomRole() {
        return Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_CUSTOM.equals(role);
    }

    public String getCourseId() {
        return courseId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getRole() {
        return role;
    }
}
