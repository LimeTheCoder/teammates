package teammates.common.datatransfer.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.google.appengine.api.datastore.Text;

import teammates.common.datatransfer.FeedbackParticipantType;
import teammates.common.util.Const;
import teammates.common.util.FieldValidator;
import teammates.common.util.JsonUtils;
import teammates.common.util.SanitizationHelper;
import teammates.storage.entity.FeedbackResponseComment;

/**
 * Represents a data transfer object for {@link FeedbackResponseComment} entities.
 */
public class FeedbackResponseCommentAttributes extends EntityAttributes<FeedbackResponseComment> {

    private static final String REQUIRED_FIELD_CANNOT_BE_NULL = "Required field cannot be null";

    // Required fields
    public String courseId;
    public String feedbackSessionName;
    public String feedbackQuestionId;
    public String giverEmail;
    public String feedbackResponseId;

    // Optional fields
    public List<FeedbackParticipantType> showCommentTo;
    public List<FeedbackParticipantType> showGiverNameTo;
    public boolean isVisibilityFollowingFeedbackQuestion;
    public Date createdAt;
    public Text commentText;
    public String lastEditorEmail;
    public Date lastEditedAt;
    public Long feedbackResponseCommentId;
    public String giverSection;
    public String receiverSection;

    FeedbackResponseCommentAttributes() {
        giverSection = Const.DEFAULT_SECTION;
        receiverSection = Const.DEFAULT_SECTION;
        showCommentTo = new ArrayList<>();
        showGiverNameTo = new ArrayList<>();
        isVisibilityFollowingFeedbackQuestion = true;
        commentText = new Text("");
        createdAt = new Date();
    }

    public static FeedbackResponseCommentAttributes valueOf(FeedbackResponseComment comment) {
        return builder(comment.getCourseId(), comment.getFeedbackSessionName(),
                comment.getFeedbackQuestionId(), comment.getFeedbackResponseId(), comment.getGiverEmail())
                .withFeedbackResponseCommentId(comment.getFeedbackResponseCommentId())
                .withCreatedAt(comment.getCreatedAt())
                .withCommentText(comment.getCommentText())
                .withGiverSection(comment.getGiverSection())
                .withReceiverSection(comment.getReceiverSection())
                .withLastEditorEmail(comment.getLastEditorEmail())
                .withLastEditedAt(comment.getLastEditedAt())
                .withVisibilityFollowingFeedbackQuestion(comment.getIsVisibilityFollowingFeedbackQuestion())
                .withShowCommentTo(comment.getShowCommentTo())
                .withShowGiverNameTo(comment.getShowGiverNameTo())
                .build();
    }

    /**
     * Return new builder instance with default values for optional fields.
     *
     * <p>Following default values are set to corresponding attributes:
     * <ul>
     * <li>{@code giverSection = "None"}</li>
     * <li>{@code receiverSection = "None"}</li>
     * <li>{@code showCommentTo = new ArrayList<>()}</li>
     * <li>{@code showGiverNameTo = new ArrayList<>()}</li>
     * <li>{@code isVisibilityFollowingFeedbackQuestion = true}</li>
     * <li>{@code commentText = new Text("")}</li>
     * </ul>
     */
    public static Builder builder(String courseId, String feedbackSessionName, String feedbackQuestionId,
                                  String feedbackResponseId, String giverEmail) {
        return new Builder(courseId, feedbackSessionName, feedbackQuestionId, feedbackResponseId, giverEmail);
    }

    public boolean isVisibleTo(FeedbackParticipantType viewerType) {
        return showCommentTo.contains(viewerType);
    }

    public Long getId() {
        return feedbackResponseCommentId;
    }

    /**
     * Use only to match existing and known Comment.
     */
    public void setId(Long id) {
        this.feedbackResponseCommentId = id;
    }

    @Override
    public List<String> getInvalidityInfo() {
        FieldValidator validator = new FieldValidator();
        List<String> errors = new ArrayList<>();

        addNonEmptyError(validator.getInvalidityInfoForCourseId(courseId), errors);

        addNonEmptyError(validator.getInvalidityInfoForFeedbackSessionName(feedbackSessionName), errors);

        addNonEmptyError(validator.getInvalidityInfoForEmail(giverEmail), errors);

        //TODO: handle the new attributes showCommentTo and showGiverNameTo

        return errors;
    }

    @Override
    public FeedbackResponseComment toEntity() {
        return new FeedbackResponseComment(courseId, feedbackSessionName, feedbackQuestionId, giverEmail,
                feedbackResponseId, createdAt, commentText, giverSection, receiverSection,
                showCommentTo, showGiverNameTo, lastEditorEmail, lastEditedAt);
    }

    @Override
    public String getIdentificationString() {
        return toString();
    }

    @Override
    public String getEntityTypeAsString() {
        return "FeedbackResponseComment";
    }

    @Override
    public String getBackupIdentifier() {
        return Const.SystemParams.COURSE_BACKUP_LOG_MSG + courseId;
    }

    @Override
    public String getJsonString() {
        return JsonUtils.toJson(this, FeedbackResponseCommentAttributes.class);
    }

    @Override
    public void sanitizeForSaving() {
        this.commentText = SanitizationHelper.sanitizeForRichText(this.commentText);
    }

    @Override
    public String toString() {
        //TODO: print visibilityOptions also
        return "FeedbackResponseCommentAttributes ["
                + "feedbackResponseCommentId = " + feedbackResponseCommentId
                + ", courseId = " + courseId
                + ", feedbackSessionName = " + feedbackSessionName
                + ", feedbackQuestionId = " + feedbackQuestionId
                + ", giverEmail = " + giverEmail
                + ", feedbackResponseId = " + feedbackResponseId
                + ", commentText = " + commentText.getValue()
                + ", createdAt = " + createdAt
                + ", lastEditorEmail = " + lastEditorEmail
                + ", lastEditedAt = " + lastEditedAt + "]";
    }

    public static void sortFeedbackResponseCommentsByCreationTime(List<FeedbackResponseCommentAttributes> frcs) {
        Collections.sort(frcs, new Comparator<FeedbackResponseCommentAttributes>() {
            @Override
            public int compare(FeedbackResponseCommentAttributes frc1, FeedbackResponseCommentAttributes frc2) {
                return frc1.createdAt.compareTo(frc2.createdAt);
            }
        });
    }

    /**
     * A Builder for {@link FeedbackResponseCommentAttributes}.
     */
    public static class Builder {
        private final FeedbackResponseCommentAttributes frca;

        public Builder(String courseId, String feedbackSessionName, String feedbackQuestionId,
                       String feedbackResponseId, String giverEmail) {
            frca = new FeedbackResponseCommentAttributes();

            validateRequiredFields(courseId, feedbackSessionName, feedbackQuestionId, feedbackResponseId, giverEmail);

            frca.courseId = courseId;
            frca.feedbackSessionName = feedbackSessionName;
            frca.feedbackQuestionId = feedbackQuestionId;
            frca.feedbackResponseId = feedbackResponseId;
            frca.giverEmail = giverEmail;
        }

        public Builder withShowCommentTo(List<FeedbackParticipantType> showCommentTo) {
            frca.showCommentTo = showCommentTo == null ? new ArrayList<FeedbackParticipantType>() : showCommentTo;
            return this;
        }

        public Builder withShowGiverNameTo(List<FeedbackParticipantType> showGiverNameTo) {
            frca.showGiverNameTo = showGiverNameTo == null ? new ArrayList<FeedbackParticipantType>() : showGiverNameTo;
            return this;
        }

        public Builder withVisibilityFollowingFeedbackQuestion(Boolean visibilityFollowingFeedbackQuestion) {
            frca.isVisibilityFollowingFeedbackQuestion = visibilityFollowingFeedbackQuestion == null
                    || visibilityFollowingFeedbackQuestion; // true as default value if param is null
            return this;
        }

        public Builder withCreatedAt(Date createdAt) {
            if (createdAt != null) {
                frca.createdAt = createdAt;
            }

            return this;
        }

        public Builder withCommentText(Text commentText) {
            frca.commentText = commentText == null ? new Text("") : commentText;
            return this;
        }

        public Builder withLastEditorEmail(String lastEditorEmail) {
            frca.lastEditorEmail = lastEditorEmail == null
                    ? frca.giverEmail
                    : lastEditorEmail;
            return this;
        }

        public Builder withLastEditedAt(Date lastEditedAt) {
            frca.lastEditedAt = lastEditedAt == null
                    ? frca.createdAt
                    : lastEditedAt;
            return this;
        }

        public Builder withFeedbackResponseCommentId(Long feedbackResponseCommentId) {
            if (feedbackResponseCommentId != null) {
                frca.feedbackResponseCommentId = feedbackResponseCommentId;
            }
            return this;
        }

        public Builder withGiverSection(String giverSection) {
            frca.giverSection = giverSection == null ? Const.DEFAULT_SECTION : giverSection;
            return this;
        }

        public Builder withReceiverSection(String receiverSection) {
            frca.receiverSection = receiverSection == null
                    ? Const.DEFAULT_SECTION
                    : receiverSection;
            return this;
        }

        public FeedbackResponseCommentAttributes build() {
            return frca;
        }

        private void validateRequiredFields(Object... objects) {
            for (Object object : objects) {
                Objects.requireNonNull(object, REQUIRED_FIELD_CANNOT_BE_NULL);
            }
        }
    }

}
