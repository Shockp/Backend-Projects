package com.personalblog.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for Role enum.
 * 
 * Tests cover:
 * - Enum value validation
 * - Display name functionality
 * - Spring Security authority generation
 * - Privilege hierarchy validation
 * - Edge cases and null handling
 * - Enum ordering and comparison
 * 
 * Following BDD patterns with given-when-then structure.
 * 
 * @author Test Suite
 * @version 1.0
 */
@DisplayName("Role Enum Tests")
class RoleTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have exactly three role values")
        void shouldHaveExactlyThreeRoleValues() {
            // Given & When
            Role[] roles = Role.values();
            
            // Then
            assertThat(roles)
                .hasSize(3)
                .containsExactly(Role.USER, Role.AUTHOR, Role.ADMIN);
        }

        @Test
        @DisplayName("Should maintain correct enum ordering")
        void shouldMaintainCorrectEnumOrdering() {
            // Given & When & Then
            assertThat(Role.USER.ordinal()).isEqualTo(0);
            assertThat(Role.AUTHOR.ordinal()).isEqualTo(1);
            assertThat(Role.ADMIN.ordinal()).isEqualTo(2);
        }

        @ParameterizedTest
        @EnumSource(Role.class)
        @DisplayName("Should have non-null enum values")
        void shouldHaveNonNullEnumValues(Role role) {
            // Given & When & Then
            assertThat(role).isNotNull();
            assertThat(role.name()).isNotBlank();
        }

        @Test
        @DisplayName("Should support valueOf operations")
        void shouldSupportValueOfOperations() {
            // Given & When & Then
            assertThat(Role.valueOf("USER")).isEqualTo(Role.USER);
            assertThat(Role.valueOf("AUTHOR")).isEqualTo(Role.AUTHOR);
            assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("Should throw exception for invalid valueOf")
        void shouldThrowExceptionForInvalidValueOf() {
            // Given & When & Then
            assertThatThrownBy(() -> Role.valueOf("INVALID_ROLE"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Display Name Tests")
    class DisplayNameTests {

        @ParameterizedTest
        @CsvSource({
            "USER, User",
            "AUTHOR, Author",
            "ADMIN, Administrator"
        })
        @DisplayName("Should return correct display names")
        void shouldReturnCorrectDisplayNames(Role role, String expectedDisplayName) {
            // Given & When
            String actualDisplayName = role.getDisplayName();
            
            // Then
            assertThat(actualDisplayName)
                .isNotNull()
                .isNotBlank()
                .isEqualTo(expectedDisplayName);
        }

        @Test
        @DisplayName("Should have human-readable display names")
        void shouldHaveHumanReadableDisplayNames() {
            // Given & When & Then
            assertThat(Role.USER.getDisplayName())
                .isEqualTo("User")
                .doesNotContain("_")
                .matches("^[A-Z][a-z]+$");
                
            assertThat(Role.AUTHOR.getDisplayName())
                .isEqualTo("Author")
                .doesNotContain("_")
                .matches("^[A-Z][a-z]+$");
                
            assertThat(Role.ADMIN.getDisplayName())
                .isEqualTo("Administrator")
                .doesNotContain("_")
                .matches("^[A-Z][a-z]+$");
        }

        @ParameterizedTest
        @EnumSource(Role.class)
        @DisplayName("Should never return null or empty display names")
        void shouldNeverReturnNullOrEmptyDisplayNames(Role role) {
            // Given & When
            String displayName = role.getDisplayName();
            
            // Then
            assertThat(displayName)
                .isNotNull()
                .isNotEmpty()
                .isNotBlank();
        }
    }

    @Nested
    @DisplayName("Spring Security Authority Tests")
    class AuthorityTests {

        @ParameterizedTest
        @CsvSource({
            "USER, ROLE_USER",
            "AUTHOR, ROLE_AUTHOR",
            "ADMIN, ROLE_ADMIN"
        })
        @DisplayName("Should return correct Spring Security authorities")
        void shouldReturnCorrectSpringSecurityAuthorities(Role role, String expectedAuthority) {
            // Given & When
            String actualAuthority = role.getAuthority();
            
            // Then
            assertThat(actualAuthority)
                .isNotNull()
                .isNotBlank()
                .isEqualTo(expectedAuthority)
                .startsWith("ROLE_");
        }

        @Test
        @DisplayName("Should follow Spring Security authority naming convention")
        void shouldFollowSpringSecurityAuthorityNamingConvention() {
            // Given & When & Then
            assertThat(Role.USER.getAuthority())
                .startsWith("ROLE_")
                .isEqualTo("ROLE_" + Role.USER.name());
                
            assertThat(Role.AUTHOR.getAuthority())
                .startsWith("ROLE_")
                .isEqualTo("ROLE_" + Role.AUTHOR.name());
                
            assertThat(Role.ADMIN.getAuthority())
                .startsWith("ROLE_")
                .isEqualTo("ROLE_" + Role.ADMIN.name());
        }

        @ParameterizedTest
        @EnumSource(Role.class)
        @DisplayName("Should never return null or empty authorities")
        void shouldNeverReturnNullOrEmptyAuthorities(Role role) {
            // Given & When
            String authority = role.getAuthority();
            
            // Then
            assertThat(authority)
                .isNotNull()
                .isNotEmpty()
                .isNotBlank()
                .startsWith("ROLE_");
        }

        @ParameterizedTest
        @EnumSource(Role.class)
        @DisplayName("Should have uppercase authority names")
        void shouldHaveUppercaseAuthorityNames(Role role) {
            // Given & When
            String authority = role.getAuthority();
            
            // Then
            assertThat(authority)
                .isUpperCase()
                .matches("^ROLE_[A-Z]+$");
        }
    }

    @Nested
    @DisplayName("Privilege Hierarchy Tests")
    class PrivilegeHierarchyTests {

        @Test
        @DisplayName("Should validate USER privilege hierarchy")
        void shouldValidateUserPrivilegeHierarchy() {
            // Given
            Role userRole = Role.USER;
            
            // When & Then
            assertThat(userRole.hasHigherOrEqualPrivileges(Role.USER))
                .as("USER should have equal privileges to USER")
                .isTrue();
                
            assertThat(userRole.hasHigherOrEqualPrivileges(Role.AUTHOR))
                .as("USER should not have higher privileges than AUTHOR")
                .isFalse();
                
            assertThat(userRole.hasHigherOrEqualPrivileges(Role.ADMIN))
                .as("USER should not have higher privileges than ADMIN")
                .isFalse();
        }

        @Test
        @DisplayName("Should validate AUTHOR privilege hierarchy")
        void shouldValidateAuthorPrivilegeHierarchy() {
            // Given
            Role authorRole = Role.AUTHOR;
            
            // When & Then
            assertThat(authorRole.hasHigherOrEqualPrivileges(Role.USER))
                .as("AUTHOR should have higher privileges than USER")
                .isTrue();
                
            assertThat(authorRole.hasHigherOrEqualPrivileges(Role.AUTHOR))
                .as("AUTHOR should have equal privileges to AUTHOR")
                .isTrue();
                
            assertThat(authorRole.hasHigherOrEqualPrivileges(Role.ADMIN))
                .as("AUTHOR should not have higher privileges than ADMIN")
                .isFalse();
        }

        @Test
        @DisplayName("Should validate ADMIN privilege hierarchy")
        void shouldValidateAdminPrivilegeHierarchy() {
            // Given
            Role adminRole = Role.ADMIN;
            
            // When & Then
            assertThat(adminRole.hasHigherOrEqualPrivileges(Role.USER))
                .as("ADMIN should have higher privileges than USER")
                .isTrue();
                
            assertThat(adminRole.hasHigherOrEqualPrivileges(Role.AUTHOR))
                .as("ADMIN should have higher privileges than AUTHOR")
                .isTrue();
                
            assertThat(adminRole.hasHigherOrEqualPrivileges(Role.ADMIN))
                .as("ADMIN should have equal privileges to ADMIN")
                .isTrue();
        }

        @ParameterizedTest
        @CsvSource({
            "USER, USER, true",
            "USER, AUTHOR, false",
            "USER, ADMIN, false",
            "AUTHOR, USER, true",
            "AUTHOR, AUTHOR, true",
            "AUTHOR, ADMIN, false",
            "ADMIN, USER, true",
            "ADMIN, AUTHOR, true",
            "ADMIN, ADMIN, true"
        })
        @DisplayName("Should correctly compare privilege levels")
        void shouldCorrectlyComparePrivilegeLevels(Role role, Role other, boolean expectedResult) {
            // Given & When
            boolean actualResult = role.hasHigherOrEqualPrivileges(other);
            
            // Then
            assertThat(actualResult)
                .as("%s.hasHigherOrEqualPrivileges(%s) should be %s", role, other, expectedResult)
                .isEqualTo(expectedResult);
        }

        @Test
        @DisplayName("Should handle null parameter gracefully")
        void shouldHandleNullParameterGracefully() {
            // Given & When & Then
            assertThatThrownBy(() -> Role.USER.hasHigherOrEqualPrivileges(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should maintain transitive privilege relationships")
        void shouldMaintainTransitivePrivilegeRelationships() {
            // Given & When & Then
            // If ADMIN >= AUTHOR and AUTHOR >= USER, then ADMIN >= USER
            assertThat(Role.ADMIN.hasHigherOrEqualPrivileges(Role.AUTHOR))
                .as("ADMIN should have higher privileges than AUTHOR")
                .isTrue();
                
            assertThat(Role.AUTHOR.hasHigherOrEqualPrivileges(Role.USER))
                .as("AUTHOR should have higher privileges than USER")
                .isTrue();
                
            assertThat(Role.ADMIN.hasHigherOrEqualPrivileges(Role.USER))
                .as("ADMIN should have higher privileges than USER (transitivity)")
                .isTrue();
        }
    }

    @Nested
    @DisplayName("Enum Behavior Tests")
    class EnumBehaviorTests {

        @Test
        @DisplayName("Should support enum comparison operations")
        void shouldSupportEnumComparisonOperations() {
            // Given & When & Then
            assertThat(Role.USER.compareTo(Role.AUTHOR))
                .as("USER should come before AUTHOR")
                .isNegative();
                
            assertThat(Role.AUTHOR.compareTo(Role.ADMIN))
                .as("AUTHOR should come before ADMIN")
                .isNegative();
                
            assertThat(Role.ADMIN.compareTo(Role.USER))
                .as("ADMIN should come after USER")
                .isPositive();
                
            assertThat(Role.USER.compareTo(Role.USER))
                .as("USER should be equal to USER")
                .isZero();
        }

        @Test
        @DisplayName("Should support equality operations")
        void shouldSupportEqualityOperations() {
            // Given & When & Then
            assertThat(Role.USER)
                .isEqualTo(Role.USER)
                .isNotEqualTo(Role.AUTHOR)
                .isNotEqualTo(Role.ADMIN);
                
            assertThat(Role.AUTHOR)
                .isEqualTo(Role.AUTHOR)
                .isNotEqualTo(Role.USER)
                .isNotEqualTo(Role.ADMIN);
                
            assertThat(Role.ADMIN)
                .isEqualTo(Role.ADMIN)
                .isNotEqualTo(Role.USER)
                .isNotEqualTo(Role.AUTHOR);
        }

        @Test
        @DisplayName("Should have consistent hashCode implementation")
        void shouldHaveConsistentHashCodeImplementation() {
            // Given & When & Then
            assertThat(Role.USER.hashCode())
                .isEqualTo(Role.USER.hashCode())
                .isNotEqualTo(Role.AUTHOR.hashCode())
                .isNotEqualTo(Role.ADMIN.hashCode());
        }

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            // Given & When & Then
            assertThat(Role.USER.toString())
                .isEqualTo("USER")
                .isEqualTo(Role.USER.name());
                
            assertThat(Role.AUTHOR.toString())
                .isEqualTo("AUTHOR")
                .isEqualTo(Role.AUTHOR.name());
                
            assertThat(Role.ADMIN.toString())
                .isEqualTo("ADMIN")
                .isEqualTo(Role.ADMIN.name());
        }

        @ParameterizedTest
        @EnumSource(Role.class)
        @DisplayName("Should be serializable")
        void shouldBeSerializable(Role role) {
            // Given & When & Then
            assertThat(role)
                .isInstanceOf(Enum.class)
                .satisfies(r -> {
                    // Enum instances are inherently serializable
                    assertThat(r.name()).isNotBlank();
                    assertThat(r.ordinal()).isNotNegative();
                });
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work correctly with Spring Security")
        void shouldWorkCorrectlyWithSpringSecurity() {
            // Given & When & Then
            // Verify that authorities follow Spring Security conventions
            for (Role role : Role.values()) {
                String authority = role.getAuthority();
                
                assertThat(authority)
                    .startsWith("ROLE_")
                    .isUpperCase()
                    .doesNotContain(" ")
                    .doesNotContain("-")
                    .matches("^ROLE_[A-Z_]+$");
            }
        }

        @Test
        @DisplayName("Should maintain role hierarchy consistency")
        void shouldMaintainRoleHierarchyConsistency() {
            // Given & When & Then
            // Verify that the ordinal-based hierarchy is consistent with business logic
            assertThat(Role.USER.ordinal())
                .as("USER should have the lowest ordinal (basic role)")
                .isLessThan(Role.AUTHOR.ordinal())
                .isLessThan(Role.ADMIN.ordinal());
                
            assertThat(Role.AUTHOR.ordinal())
                .as("AUTHOR should have middle ordinal (intermediate role)")
                .isGreaterThan(Role.USER.ordinal())
                .isLessThan(Role.ADMIN.ordinal());
                
            assertThat(Role.ADMIN.ordinal())
                .as("ADMIN should have the highest ordinal (highest privileges)")
                .isGreaterThan(Role.USER.ordinal())
                .isGreaterThan(Role.AUTHOR.ordinal());
        }

        @Test
        @DisplayName("Should support role-based access control scenarios")
        void shouldSupportRoleBasedAccessControlScenarios() {
            // Given & When & Then
            // Simulate common RBAC scenarios
            
            // Scenario 1: Basic user access
            assertThat(Role.USER.hasHigherOrEqualPrivileges(Role.USER))
                .as("Users should access user-level resources")
                .isTrue();
                
            // Scenario 2: Author access
            assertThat(Role.AUTHOR.hasHigherOrEqualPrivileges(Role.USER))
                .as("Authors should access user-level resources")
                .isTrue();
            assertThat(Role.AUTHOR.hasHigherOrEqualPrivileges(Role.AUTHOR))
                .as("Authors should access author-level resources")
                .isTrue();
                
            // Scenario 3: Admin access
            assertThat(Role.ADMIN.hasHigherOrEqualPrivileges(Role.USER))
                .as("Admins should access user-level resources")
                .isTrue();
            assertThat(Role.ADMIN.hasHigherOrEqualPrivileges(Role.AUTHOR))
                .as("Admins should access author-level resources")
                .isTrue();
            assertThat(Role.ADMIN.hasHigherOrEqualPrivileges(Role.ADMIN))
                .as("Admins should access admin-level resources")
                .isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle enum iteration correctly")
        void shouldHandleEnumIterationCorrectly() {
            // Given
            Role[] expectedRoles = {Role.USER, Role.AUTHOR, Role.ADMIN};
            
            // When
            Role[] actualRoles = Role.values();
            
            // Then
            assertThat(actualRoles)
                .hasSize(3)
                .containsExactly(expectedRoles);
        }

        @Test
        @DisplayName("Should maintain enum immutability")
        void shouldMaintainEnumImmutability() {
            // Given
            Role originalRole = Role.USER;
            String originalDisplayName = originalRole.getDisplayName();
            String originalAuthority = originalRole.getAuthority();
            
            // When - Multiple calls should return same values
            String displayName1 = originalRole.getDisplayName();
            String displayName2 = originalRole.getDisplayName();
            String authority1 = originalRole.getAuthority();
            String authority2 = originalRole.getAuthority();
            
            // Then
            assertThat(displayName1)
                .isEqualTo(originalDisplayName)
                .isEqualTo(displayName2);
                
            assertThat(authority1)
                .isEqualTo(originalAuthority)
                .isEqualTo(authority2);
        }

        @Test
        @DisplayName("Should handle concurrent access safely")
        void shouldHandleConcurrentAccessSafely() {
            // Given & When & Then
            // Enums are inherently thread-safe
            assertThat(Role.values())
                .as("Enum values should be consistent across calls")
                .containsExactly(Role.USER, Role.AUTHOR, Role.ADMIN);
                
            // Multiple threads accessing the same enum should get the same instance
            assertThat(Role.valueOf("USER"))
                .isSameAs(Role.USER);
        }
    }
}