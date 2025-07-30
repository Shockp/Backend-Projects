package com.personalblog.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(BlogPostController.class)
@ActiveProfiles("test")
class BlogPostControllerTest {

    @Test
    void testBlogPostController() {
        // TODO: Implement blog post controller tests
    }

}