package com.personalblog.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for Comment entity.
 * 
 * Tests cover:
 * - Entity validation with Bean Validation API
 * - Business logic methods
 * - Relationship management
 * - Object methods (equals, hashCode, toString)
 * - Constructor behavior
 * - Edge cases and boundary conditions
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DisplayName("Comment Entity Tests")
class CommentTest {

    private Validator validator;
    private Comment validComment;
    private User testUser;
    private BlogPost testBlogPost;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        
        // Create test blog post
        testBlogPost = new BlogPost();
        testBlogPost.setTitle("Test Blog Post");
        testBlogPost.setContent("Test content");
        testBlogPost.setSlug("test-blog-post");
        
        // Create a valid comment for testing
        validComment = new Comment();
        validComment.setContent("This is a test comment with sufficient length to pass validation.");
        validComment.setAuthorUser(testUser);
        validComment.setAuthorEmail("test@example.com");
        validComment.setBlogPost(testBlogPost);
        validComment.setStatus(CommentStatus.PENDING);
        validComment.setIpAddress("192.168.1.1");
        validComment.setUserAgent("Mozilla/5.0 Test Browser");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should initialize with default values")
        void defaultConstructor_ShouldInitializeWithDefaults() {
            Comment comment = new Comment();
            
            assertThat(comment.getContent()).isNull();
            assertThat(comment.getAuthorUser()).isNull();
            assertThat(comment.getAuthorName()).isEqualTo("Anonymous");
            assertThat(comment.getAuthorEmail()).isNull();
            assertThat(comment.getBlogPost()).isNull();
            assertThat(comment.getParentComment()).isNull();
            assertThat(comment.getStatus()).isEqualTo(CommentStatus.PENDING);
            assertThat(comment.getIpAddress()).isNull();
            assertThat(comment.getUserAgent()).isNull();
            assertThat(comment.getReplies()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid comment should pass validation")
        void validComment_ShouldPassValidation() {
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Content should not be null, empty, or blank")
        void content_ShouldNotBeNullEmptyOrBlank(String content) {
            validComment.setContent(content);
            
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            
            if (content != null && content.isEmpty()) {
                // For empty strings, both @NotBlank and @Size are triggered
                assertThat(violations).hasSize(2);
                assertThat(violations.stream().map(ConstraintViolation::getMessage))
                    .containsExactlyInAnyOrder(
                        "Comment content is required",
                        "Comment content must be between 1 and 2000 characters"
                    );
            } else {
                // For null or blank strings (whitespace only), only @NotBlank is triggered
                assertThat(violations).hasSize(1);
                assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Comment content is required");
            }
        }

        @Test
        @DisplayName("Content should not be empty")
        void content_ShouldNotBeEmpty() {
            validComment.setContent(""); // Empty string
            
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            
            assertThat(violations).hasSize(2);
            assertThat(violations.stream().map(ConstraintViolation::getMessage))
                .containsExactlyInAnyOrder(
                    "Comment content is required",
                    "Comment content must be between 1 and 2000 characters"
                );
        }

        @Test
        @DisplayName("Content should not exceed maximum length")
        void content_ShouldNotExceedMaximum() {
            String longContent = "a".repeat(2001); // More than 2000 characters
            validComment.setContent(longContent);
            
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Comment content must be between 1 and 2000 characters");
        }

        @Test
        @DisplayName("Author email should be valid when provided")
        void authorEmail_ShouldBeValidWhenProvided() {
            validComment.setAuthorEmail("invalid-email");
            
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Author email must be valid");
        }

        @Test
        @DisplayName("Author name should not exceed maximum length")
        void authorName_ShouldNotExceedMaximum() {
            String longName = "a".repeat(101); // More than 100 characters
            validComment.setAuthorName(longName);
            
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Author name must not exceed 100 characters");
        }

        @Test
        @DisplayName("IP address should not exceed maximum length")
        void ipAddress_ShouldNotExceedMaximum() {
            String longIp = "a".repeat(46); // More than 45 characters
            validComment.setIpAddress(longIp);
            
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            
            assertThat(violations).hasSize(2); // Both size and pattern violations
            assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                    "IP address must not exceed 45 characters",
                    "IP address must be a valid IPv4 or IPv6 address"
                );
        }

        @Test
        @DisplayName("User agent should not exceed maximum length")
        void userAgent_ShouldNotExceedMaximum() {
            String longUserAgent = "a".repeat(501); // More than 500 characters
            validComment.setUserAgent(longUserAgent);
            
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("User agent must not exceed 500 characters");
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("isFromRegisteredUser should return true when authorUser is set")
        void isFromRegisteredUser_ShouldReturnTrueWhenAuthorUserIsSet() {
            validComment.setAuthorUser(testUser);
            
            assertThat(validComment.isFromRegisteredUser()).isTrue();
        }

        @Test
        @DisplayName("isFromRegisteredUser should return false when authorUser is null")
        void isFromRegisteredUser_ShouldReturnFalseWhenAuthorUserIsNull() {
            validComment.setAuthorUser(null);
            
            assertThat(validComment.isFromRegisteredUser()).isFalse();
        }

        @Test
        @DisplayName("isGuestComment should return true when authorUser is null")
        void isGuestComment_ShouldReturnTrueWhenAuthorUserIsNull() {
            validComment.setAuthorUser(null);
            
            assertThat(!validComment.isFromRegisteredUser()).isTrue();
        }

        @Test
        @DisplayName("isGuestComment should return false when authorUser is set")
        void isGuestComment_ShouldReturnFalseWhenAuthorUserIsSet() {
            validComment.setAuthorUser(testUser);
            
            assertThat(!validComment.isFromRegisteredUser()).isFalse();
        }

        @Test
        @DisplayName("isReply should return true when parentComment is set")
        void isReply_ShouldReturnTrueWhenParentCommentIsSet() {
            Comment parentComment = new Comment();
            validComment.setParentComment(parentComment);
            
            assertThat(validComment.isReply()).isTrue();
        }

        @Test
        @DisplayName("isReply should return false when parentComment is null")
        void isReply_ShouldReturnFalseWhenParentCommentIsNull() {
            validComment.setParentComment(null);
            
            assertThat(validComment.isReply()).isFalse();
        }

        @Test
        @DisplayName("hasReplies should return true when replies exist")
        void hasReplies_ShouldReturnTrueWhenRepliesExist() {
            Comment reply = new Comment();
            reply.setParentComment(validComment);
            validComment.getReplies().add(reply);
            
            assertThat(validComment.hasReplies()).isTrue();
        }

        @Test
        @DisplayName("hasReplies should return false when no replies exist")
        void hasReplies_ShouldReturnFalseWhenNoRepliesExist() {
            assertThat(validComment.hasReplies()).isFalse();
        }

        @Test
        @DisplayName("getAuthorName should return username when authorUser is set")
        void getAuthorName_ShouldReturnUsernameWhenAuthorUserIsSet() {
            validComment.setAuthorUser(testUser);
            validComment.setAuthorName("Guest Name");
            
            assertThat(validComment.getAuthorName()).isEqualTo(testUser.getUsername());
        }

        @Test
        @DisplayName("getAuthorName should return authorName when authorUser is null")
        void getAuthorName_ShouldReturnAuthorNameWhenAuthorUserIsNull() {
            validComment.setAuthorUser(null);
            validComment.setAuthorName("Guest Name");
            
            assertThat(validComment.getAuthorName()).isEqualTo("Guest Name");
        }

        @Test
        @DisplayName("getAuthorName should return Anonymous when both are null")
        void getAuthorName_ShouldReturnAnonymousWhenBothAreNull() {
            validComment.setAuthorUser(null);
            validComment.setAuthorName(null);
            
            assertThat(validComment.getAuthorName()).isEqualTo("Anonymous");
        }

        @Test
        @DisplayName("getAuthorDisplayName should return username when authorUser is set")
        void getAuthorDisplayName_ShouldReturnUsernameWhenAuthorUserIsSet() {
            validComment.setAuthorUser(testUser);
            validComment.setAuthorName("Guest Name");
            
            assertThat(validComment.getAuthorDisplayName()).isEqualTo(testUser.getUsername());
        }

        @Test
        @DisplayName("getAuthorDisplayName should return authorName when authorUser is null")
        void getAuthorDisplayName_ShouldReturnAuthorNameWhenAuthorUserIsNull() {
            validComment.setAuthorUser(null);
            validComment.setAuthorName("Guest Name");
            
            assertThat(validComment.getAuthorDisplayName()).isEqualTo("Guest Name");
        }

        @Test
        @DisplayName("getAuthorDisplayName should return Anonymous when both are null")
        void getAuthorDisplayName_ShouldReturnAnonymousWhenBothAreNull() {
            validComment.setAuthorUser(null);
            validComment.setAuthorName(null);
            
            assertThat(validComment.getAuthorDisplayName()).isEqualTo("Anonymous");
        }

        @ParameterizedTest
        @EnumSource(CommentStatus.class)
        @DisplayName("isApproved should work correctly for all statuses")
        void isApproved_ShouldWorkCorrectlyForAllStatuses(CommentStatus status) {
            validComment.setStatus(status);
            
            boolean expectedApproved = (status == CommentStatus.APPROVED);
            assertThat(validComment.isApproved()).isEqualTo(expectedApproved);
        }

        @Test
        @DisplayName("approve should set status to APPROVED")
        void approve_ShouldSetStatusToApproved() {
            validComment.setStatus(CommentStatus.PENDING);
            
            validComment.approve();
            
            assertThat(validComment.getStatus()).isEqualTo(CommentStatus.APPROVED);
        }

        @Test
        @DisplayName("reject should set status to REJECTED")
        void reject_ShouldSetStatusToRejected() {
            validComment.setStatus(CommentStatus.PENDING);
            
            validComment.reject();
            
            assertThat(validComment.getStatus()).isEqualTo(CommentStatus.REJECTED);
        }

        @Test
        @DisplayName("markAsSpam should set status to SPAM")
        void markAsSpam_ShouldSetStatusToSpam() {
            validComment.setStatus(CommentStatus.APPROVED);
            
            validComment.markAsSpam();
            
            assertThat(validComment.getStatus()).isEqualTo(CommentStatus.SPAM);
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Adding reply should establish bidirectional relationship")
        void addingReply_ShouldEstablishBidirectionalRelationship() {
            Comment reply = new Comment();
            reply.setContent("This is a reply comment with sufficient length.");
            reply.setAuthorName("Reply Author");
            reply.setAuthorEmail("reply@example.com");
            reply.setBlogPost(testBlogPost);
            reply.setStatus(CommentStatus.PENDING);
            
            validComment.getReplies().add(reply);
            reply.setParentComment(validComment);
            
            assertThat(validComment.getReplies()).contains(reply);
            assertThat(reply.getParentComment()).isEqualTo(validComment);
            assertThat(validComment.hasReplies()).isTrue();
            assertThat(reply.isReply()).isTrue();
        }

        @Test
        @DisplayName("Setting blog post should establish relationship")
        void settingBlogPost_ShouldEstablishRelationship() {
            validComment.setBlogPost(testBlogPost);
            
            assertThat(validComment.getBlogPost()).isEqualTo(testBlogPost);
        }

        @Test
        @DisplayName("Setting author user should establish relationship")
        void settingAuthorUser_ShouldEstablishRelationship() {
            validComment.setAuthorUser(testUser);
            
            assertThat(validComment.getAuthorUser()).isEqualTo(testUser);
            assertThat(validComment.isFromRegisteredUser()).isTrue();
        }
    }

    @Nested
    @DisplayName("Object Methods Tests")
    class ObjectMethodsTests {

        @Test
        @DisplayName("toString should include key information")
        void toString_ShouldIncludeKeyInformation() {
            String result = validComment.toString();
            
            assertThat(result)
                .contains("Comment")
                .contains(validComment.getContent().substring(0, 20))
                .contains(validComment.getStatus().toString());
        }

        @Test
        @DisplayName("equals should work correctly with same content")
        void equals_ShouldWorkCorrectlyWithSameContent() {
            Comment comment1 = new Comment();
            comment1.setContent("Test comment content with sufficient length.");
            comment1.setAuthorEmail("test@example.com");
            comment1.setBlogPost(testBlogPost);
            comment1.setStatus(CommentStatus.PENDING);
            
            Comment comment2 = new Comment();
            comment2.setContent("Test comment content with sufficient length.");
            comment2.setAuthorEmail("test@example.com");
            comment2.setBlogPost(testBlogPost);
            comment2.setStatus(CommentStatus.PENDING);
            
            // Since equals is based on BaseEntity (likely ID-based), 
            // new entities without IDs should not be equal
            assertThat(comment1).isNotEqualTo(comment2);
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCode_ShouldBeConsistent() {
            int hashCode1 = validComment.hashCode();
            int hashCode2 = validComment.hashCode();
            
            assertThat(hashCode1).isEqualTo(hashCode2);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Comment with minimum valid content length should pass validation")
        void commentWithMinimumValidContentLength_ShouldPassValidation() {
            validComment.setContent("1234567890"); // Exactly 10 characters
            
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Comment with maximum valid content length should pass validation")
        void commentWithMaximumValidContentLength_ShouldPassValidation() {
            String maxContent = "a".repeat(2000); // Exactly 2000 characters
            validComment.setContent(maxContent);
            
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Comment without author user or name should still be valid")
        void commentWithoutAuthorUserOrName_ShouldStillBeValid() {
            validComment.setAuthorUser(null);
            validComment.setAuthorName(null);
            
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            
            assertThat(violations).isEmpty();
            assertThat(validComment.getAuthorName()).isEqualTo("Anonymous");
        }

        @Test
        @DisplayName("Comment with null status should be invalid")
        void commentWithNullStatus_ShouldBeInvalid() {
            validComment.setStatus(null);
            
            Set<ConstraintViolation<Comment>> violations = validator.validate(validComment);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Comment status is required");
        }
    }
}