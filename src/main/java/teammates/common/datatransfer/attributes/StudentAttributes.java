package teammates.common.datatransfer.attributes;

import static teammates.common.util.Const.EOL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import teammates.common.datatransfer.StudentUpdateStatus;
import teammates.common.util.Assumption;
import teammates.common.util.Config;
import teammates.common.util.Const;
import teammates.common.util.FieldValidator;
import teammates.common.util.JsonUtils;
import teammates.common.util.SanitizationHelper;
import teammates.common.util.StringHelper;
import teammates.storage.entity.CourseStudent;

public class StudentAttributes extends EntityAttributes {

    // Note: be careful when changing these variables as their names are used in *.json files.

    // Required fields
    public String email;
    public String course;
    public String name;

    // Optional values
    public String googleId;
    public String lastName;
    public String comments;
    public String team;
    public String section;
    public String key;

    public transient StudentUpdateStatus updateStatus;

    /*
     * Creation and update time stamps.
     * Updated automatically in Student.java, jdoPreStore()
     */
    private transient Date createdAt;
    private transient Date updatedAt;

    public StudentAttributes() {
        googleId = "";
        section = Const.DEFAULT_SECTION;
        updateStatus = StudentUpdateStatus.UNKNOWN;
        createdAt = Const.TIME_REPRESENTS_DEFAULT_TIMESTAMP;
        updatedAt = Const.TIME_REPRESENTS_DEFAULT_TIMESTAMP;
    }

    public static StudentAttributes valueOf(CourseStudent student) {
        return builder(student.getCourseId(), student.getName(), student.getEmail())
                .withLastName(student.getLastName())
                .withComments(student.getComments())
                .withTeam(student.getTeamName())
                .withSection(student.getSectionName())
                .withGoogleId(student.getGoogleId())
                .withKey(student.getRegistrationKey())
                .withCreatedAt(student.getCreatedAt())
                .withUpdatedAt(student.getUpdatedAt())
                .build();
    }

    /**
     * Return new builder instance with default values for optional fields.
     *
     * <p>Following default values are set to corresponding attributes:
     * <ul>
     * <li>{@code googleId = ""}</li>
     * <li>{@code section = Const.DEFAULT_SECTION}</li>
     * <li>{@code updateStatus = StudentUpdateStatus.UNKNOWN}</li>
     * <li>{@code createdAt = Const.TIME_REPRESENTS_DEFAULT_TIMESTAMP}</li>
     * <li>{@code updatedAt = Const.TIME_REPRESENTS_DEFAULT_TIMESTAMP}</li>
     * <li>{@code lastName = SanitizationHelper.sanitizeName(StringHelper.splitName(name)[1])}</li>
     * </ul>
     */
    public static Builder builder(String courseId, String name, String email) {
        return new Builder(courseId, name, email);
    }

    public StudentAttributes getCopy() {
        return builder(course, name, email)
                .withComments(comments)
                .withCreatedAt(createdAt)
                .withUpdatedAt(updatedAt)
                .withGoogleId(googleId)
                .withKey(key)
                .withLastName(lastName)
                .withSection(section)
                .withTeam(team)
                .withUpdateStatus(updateStatus)
                .build();
    }

    public String toEnrollmentString() {
        String enrollmentStringSeparator = "|";

        return this.section + enrollmentStringSeparator
             + this.team + enrollmentStringSeparator
             + this.name + enrollmentStringSeparator
             + this.email + enrollmentStringSeparator
             + this.comments;
    }

    public boolean isRegistered() {
        return googleId != null && !googleId.isEmpty();
    }

    public String getRegistrationUrl() {
        return Config.getAppUrl(Const.ActionURIs.STUDENT_COURSE_JOIN_NEW)
                                           .withRegistrationKey(StringHelper.encrypt(key))
                                           .withStudentEmail(email)
                                           .withCourseId(course)
                                           .toString();
    }

    public String getPublicProfilePictureUrl() {
        return Config.getAppUrl(Const.ActionURIs.STUDENT_PROFILE_PICTURE)
                           .withStudentEmail(StringHelper.encrypt(email))
                           .withCourseId(StringHelper.encrypt(course))
                           .toString();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getKey() {
        return key;
    }

    /**
     * Format: email%courseId e.g., adam@gmail.com%cs1101.
     */
    public String getId() {
        return email + "%" + course;
    }

    public String getSection() {
        return section;
    }

    public String getTeam() {
        return team;
    }

    public String getComments() {
        return comments;
    }

    public boolean isEnrollInfoSameAs(StudentAttributes otherStudent) {
        return otherStudent != null && otherStudent.email.equals(this.email)
               && otherStudent.course.equals(this.course)
               && otherStudent.name.equals(this.name)
               && otherStudent.comments.equals(this.comments)
               && otherStudent.team.equals(this.team)
               && otherStudent.section.equals(this.section);
    }

    @Override
    public List<String> getInvalidityInfo() {
        // id is allowed to be null when the student is not registered
        Assumption.assertTrue(team != null);
        Assumption.assertTrue(comments != null);

        FieldValidator validator = new FieldValidator();
        List<String> errors = new ArrayList<String>();

        if (isRegistered()) {
            addNonEmptyError(validator.getInvalidityInfoForGoogleId(googleId), errors);
        }

        addNonEmptyError(validator.getInvalidityInfoForCourseId(course), errors);

        addNonEmptyError(validator.getInvalidityInfoForEmail(email), errors);

        addNonEmptyError(validator.getInvalidityInfoForTeamName(team), errors);

        addNonEmptyError(validator.getInvalidityInfoForSectionName(section), errors);

        addNonEmptyError(validator.getInvalidityInfoForStudentRoleComments(comments), errors);

        addNonEmptyError(validator.getInvalidityInfoForPersonName(name), errors);

        return errors;
    }

    public static void sortBySectionName(List<StudentAttributes> students) {
        Collections.sort(students, new Comparator<StudentAttributes>() {
            @Override
            public int compare(StudentAttributes student1, StudentAttributes student2) {
                String sect1 = student1.section;
                String sect2 = student2.section;

                // If the section name is the same, reorder by team name
                if (sect1.compareTo(sect2) == 0) {
                    if (student1.team.compareTo(student2.team) == 0) {
                        return student1.name.compareTo(student2.name);
                    }

                    return student1.team.compareTo(student2.team);
                }

                return sect1.compareTo(sect2);
            }
        });
    }

    public static void sortByTeamName(List<StudentAttributes> students) {
        Collections.sort(students, new Comparator<StudentAttributes>() {
            @Override
            public int compare(StudentAttributes student1, StudentAttributes student2) {
                String team1 = student1.team;
                String team2 = student2.team;

                // If the team name is the same, reorder by student name
                if (team1.compareTo(team2) == 0) {
                    return student1.name.compareTo(student2.name);
                }

                return team1.compareTo(team2);
            }
        });
    }

    public static void sortByNameAndThenByEmail(List<StudentAttributes> students) {
        Collections.sort(students, new Comparator<StudentAttributes>() {
            @Override
            public int compare(StudentAttributes student1, StudentAttributes student2) {
                int result = student1.name.compareTo(student2.name);

                if (result == 0) {
                    result = student1.email.compareTo(student2.email);
                }

                return result;
            }
        });
    }

    public void updateWithExistingRecord(StudentAttributes originalStudent) {
        if (this.email == null) {
            this.email = originalStudent.email;
        }

        if (this.name == null) {
            this.name = originalStudent.name;
        }

        if (this.googleId == null) {
            this.googleId = originalStudent.googleId;
        }

        if (this.team == null) {
            this.team = originalStudent.team;
        }

        if (this.comments == null) {
            this.comments = originalStudent.comments;
        }

        if (this.section == null) {
            this.section = originalStudent.section;
        }
    }

    @Override
    public Object toEntity() {
        return new CourseStudent(email, name, googleId, comments, course, team, section);
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        String indentString = StringHelper.getIndent(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(indentString + "Student:" + name + "[" + email + "]" + EOL);

        return sb.toString();
    }

    @Override
    public String getIdentificationString() {
        return this.course + "/" + this.email;
    }

    @Override
    public String getEntityTypeAsString() {
        return "Student";
    }

    @Override
    public String getBackupIdentifier() {
        return Const.SystemParams.COURSE_BACKUP_LOG_MSG + course;
    }

    @Override
    public String getJsonString() {
        return JsonUtils.toJson(this, StudentAttributes.class);
    }

    @Override
    public void sanitizeForSaving() {
        googleId = SanitizationHelper.sanitizeGoogleId(googleId);
        name = SanitizationHelper.sanitizeName(name);
        comments = SanitizationHelper.sanitizeTextField(comments);
    }

    public String getStudentStatus() {
        if (isRegistered()) {
            return Const.STUDENT_COURSE_STATUS_JOINED;
        }
        return Const.STUDENT_COURSE_STATUS_YET_TO_JOIN;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns true if section value has changed from its original value.
     */
    public boolean isSectionChanged(StudentAttributes originalStudentAttribute) {
        return this.section != null && !this.section.equals(originalStudentAttribute.section);
    }

    /**
     * Returns true if team value has changed from its original value.
     */
    public boolean isTeamChanged(StudentAttributes originalStudentAttribute) {
        return this.team != null && !this.team.equals(originalStudentAttribute.team);
    }

    /**
     * Returns true if email value has changed from its original value.
     */
    public boolean isEmailChanged(StudentAttributes originalStudentAttribute) {
        return this.email != null && !this.email.equals(originalStudentAttribute.email);
    }

    /**
     * A Builder class for {@link StudentAttributes}.
     */
    public static class Builder {
        private final StudentAttributes studentAttributes;

        public Builder(String courseId, String name, String email) {
            studentAttributes = new StudentAttributes();

            studentAttributes.course = courseId;
            studentAttributes.name = SanitizationHelper.sanitizeName(name);
            studentAttributes.email = email;
            studentAttributes.lastName = processLastName(null);
        }

        public Builder withGoogleId(String googleId) {
            studentAttributes.googleId = googleId == null
                    ? ""
                    : SanitizationHelper.sanitizeGoogleId(googleId);
            return this;
        }

        public Builder withLastName(String lastName) {
            studentAttributes.lastName = processLastName(lastName);
            return this;
        }

        private String processLastName(String lastName) {
            if (lastName != null) {
                return lastName;
            }

            if (StringHelper.isEmpty(studentAttributes.name)) {
                return "";
            }

            String[] nameParts = StringHelper.splitName(studentAttributes.name);
            return nameParts.length < 2 ? "" : SanitizationHelper.sanitizeName(nameParts[1]);
        }

        public Builder withComments(String comments) {
            studentAttributes.comments = SanitizationHelper.sanitizeTextField(comments);
            return this;
        }

        public Builder withTeam(String team) {
            studentAttributes.team = team;
            return this;
        }

        public Builder withSection(String section) {
            studentAttributes.section = section == null ? Const.DEFAULT_SECTION : section;
            return this;
        }

        public Builder withKey(String key) {
            studentAttributes.key = key;
            return this;
        }

        public Builder withUpdateStatus(StudentUpdateStatus updateStatus) {
            studentAttributes.updateStatus = updateStatus == null
                    ? StudentUpdateStatus.UNKNOWN
                    : updateStatus;
            return this;
        }

        public Builder withCreatedAt(Date createdAt) {
            Date dateToAdd = (createdAt == null)
                    ? Const.TIME_REPRESENTS_DEFAULT_TIMESTAMP
                    : createdAt;
            studentAttributes.setCreatedAt(dateToAdd);
            return this;
        }

        public Builder withUpdatedAt(Date updatedAt) {
            Date dateToAdd = updatedAt == null
                    ? Const.TIME_REPRESENTS_DEFAULT_TIMESTAMP
                    : updatedAt;
            studentAttributes.setUpdatedAt(dateToAdd);
            return this;
        }

        public StudentAttributes build() {
            return studentAttributes;
        }
    }
}
