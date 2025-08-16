#!/bin/bash

# Integration Tests Validation Script
# This script performs static analysis to identify potential issues

echo "🔍 Validating Integration Tests..."
echo "=================================="

# Check if all required files exist
echo "📁 Checking test files..."
test_files=(
    "src/test/java/com/personalblog/repository/integration/BaseIntegrationTest.java"
    "src/test/java/com/personalblog/repository/integration/BlogPostRepositoryIntegrationTest.java"
    "src/test/java/com/personalblog/repository/integration/CategoryRepositoryIntegrationTest.java"
    "src/test/java/com/personalblog/repository/integration/TagRepositoryIntegrationTest.java"
    "src/test/java/com/personalblog/repository/integration/CommentRepositoryIntegrationTest.java"
    "src/test/java/com/personalblog/repository/integration/RefreshTokenRepositoryIntegrationTest.java"
    "src/test/java/com/personalblog/repository/integration/RepositoryIntegrationTestSuite.java"
    "src/test/java/com/personalblog/repository/integration/IntegrationTestRunner.java"
    "src/test/resources/application-integration-test.yml"
)

missing_files=0
for file in "${test_files[@]}"; do
    if [ ! -f "$file" ]; then
        echo "❌ Missing: $file"
        missing_files=$((missing_files + 1))
    else
        echo "✅ Found: $file"
    fi
done

if [ $missing_files -eq 0 ]; then
    echo "✅ All test files present"
else
    echo "❌ $missing_files test files missing"
fi

echo ""

# Check for common syntax issues
echo "🔍 Checking for syntax issues..."

# Check for unclosed parentheses or brackets
echo "Checking for unclosed parentheses..."
for file in src/test/java/com/personalblog/repository/integration/*.java; do
    if [ -f "$file" ]; then
        # Count opening and closing parentheses
        open_parens=$(grep -o '(' "$file" | wc -l)
        close_parens=$(grep -o ')' "$file" | wc -l)
        
        if [ "$open_parens" -ne "$close_parens" ]; then
            echo "⚠️  Potential parentheses mismatch in $(basename "$file"): $open_parens open, $close_parens close"
        fi
    fi
done

# Check for missing imports
echo "Checking for missing imports..."
for file in src/test/java/com/personalblog/repository/integration/*.java; do
    if [ -f "$file" ]; then
        # Check if file uses assertThat but doesn't import it
        if grep -q "assertThat" "$file" && ! grep -q "import static org.assertj.core.api.Assertions" "$file"; then
            echo "⚠️  $(basename "$file") uses assertThat but may be missing AssertJ import"
        fi
        
        # Check if file uses @Test but doesn't import it
        if grep -q "@Test" "$file" && ! grep -q "import org.junit.jupiter.api.Test" "$file"; then
            echo "⚠️  $(basename "$file") uses @Test but may be missing JUnit import"
        fi
    fi
done

# Check TestContainers configuration
echo "Checking TestContainers configuration..."
if grep -q "testcontainers" pom.xml; then
    echo "✅ TestContainers dependency found in pom.xml"
else
    echo "❌ TestContainers dependency missing in pom.xml"
fi

# Check for proper test annotations
echo "Checking test annotations..."
for file in src/test/java/com/personalblog/repository/integration/*Test.java; do
    if [ -f "$file" ]; then
        if ! grep -q "@SpringBootTest\|@DataJpaTest" "$file" && ! grep -q "extends BaseIntegrationTest" "$file"; then
            echo "⚠️  $(basename "$file") may be missing Spring test annotations"
        fi
    fi
done

echo ""
echo "🏁 Validation complete!"
echo ""
echo "📋 Summary:"
echo "- Test files: $((${#test_files[@]} - missing_files))/${#test_files[@]} present"
echo "- Check the output above for any warnings or issues"
echo ""
echo "🚀 To run the tests (once Java environment is set up):"
echo "   ./mvnw test -Dtest=IntegrationTestRunner"
echo ""
echo "📖 For detailed issue analysis, see:"
echo "   src/test/java/com/personalblog/repository/integration/TestIssuesFix.md"