# Personal Blog - Security Guidelines

## Overview

This document outlines comprehensive security guidelines for the Personal Blog application, implementing defense-in-depth strategies using Spring Security 6.5.2, modern authentication patterns, and industry best practices for 2025.

## Security Philosophy

### Core Principles
- **Security by Design**: Security considerations integrated from the beginning
- **Defense in Depth**: Multiple layers of security controls
- **Principle of Least Privilege**: Minimal access rights for users and systems
- **Zero Trust Architecture**: Never trust, always verify
- **Fail Secure**: System fails to a secure state
- **Security Transparency**: Clear security policies and procedures

### Compliance Standards
- **OWASP Top 10 2021**: Address all critical security risks
- **GDPR**: Data protection and privacy compliance
- **SOC 2 Type II**: Security, availability, and confidentiality
- **ISO 27001**: Information security management

## Authentication & Authorization

### JWT Implementation

#### JWT Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:3600000}") // 1 hour
    private long jwtExpiration;
    
    @Value("${jwt.refresh-expiration:86400000}") // 24 hours
    private long refreshExpiration;
    
    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(jwtSecret, jwtExpiration, refreshExpiration);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // Using JWT, CSRF not needed
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/posts/**").permitAll()
                .requestMatchers("/api/v1/categories").permitAll()
                .requestMatchers("/api/v1/tags").permitAll()
                .requestMatchers("/api/v1/search").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/refresh").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                
                // Admin endpoints
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                .accessDeniedHandler(jwtAccessDeniedHandler())
            )
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                )
            )
            .build();
    }
}
```

#### Secure JWT Token Provider

```java
@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String AUTHORITIES_KEY = "auth";
    
    private final String jwtSecret;
    private final long jwtExpiration;
    private final long refreshExpiration;
    private final SecretKey key;
    
    public JwtTokenProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration}") long jwtExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        
        // Validate secret strength
        if (jwtSecret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }
        
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        String authoritiesString = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
            
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
            .setSubject(username)
            .claim(AUTHORITIES_KEY, authoritiesString)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .setIssuer("personal-blog")
            .setAudience("personal-blog-users")
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }
    
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);
        
        return Jwts.builder()
            .setSubject(username)
            .claim("type", "refresh")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .setIssuer("personal-blog")
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer("personal-blog")
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
    
    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
            
        String authorities = claims.get(AUTHORITIES_KEY, String.class);
        
        return Arrays.stream(authorities.split(","))
            .filter(auth -> !auth.trim().isEmpty())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
}
```

### Password Security

#### Password Encoding

```java
@Configuration
public class PasswordConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Use Argon2 for new passwords (most secure)
        return new Argon2PasswordEncoder(
            16,    // saltLength
            32,    // hashLength
            1,     // parallelism
            4096,  // memory (4MB)
            3      // iterations
        );
    }
    
    @Bean
    public DelegatingPasswordEncoder delegatingPasswordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("argon2", new Argon2PasswordEncoder());
        encoders.put("bcrypt", new BCryptPasswordEncoder(12));
        encoders.put("scrypt", new SCryptPasswordEncoder());
        
        DelegatingPasswordEncoder passwordEncoder = 
            new DelegatingPasswordEncoder("argon2", encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        
        return passwordEncoder;
    }
}
```

#### Password Policy

```java
@Component
public class PasswordPolicyValidator {
    
    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 128;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':,.<>?]");
    
    public void validatePassword(String password) {
        List<String> violations = new ArrayList<>();
        
        if (password == null || password.length() < MIN_LENGTH) {
            violations.add("Password must be at least " + MIN_LENGTH + " characters long");
        }
        
        if (password != null && password.length() > MAX_LENGTH) {
            violations.add("Password must not exceed " + MAX_LENGTH + " characters");
        }
        
        if (password != null) {
            if (!UPPERCASE.matcher(password).find()) {
                violations.add("Password must contain at least one uppercase letter");
            }
            
            if (!LOWERCASE.matcher(password).find()) {
                violations.add("Password must contain at least one lowercase letter");
            }
            
            if (!DIGIT.matcher(password).find()) {
                violations.add("Password must contain at least one digit");
            }
            
            if (!SPECIAL.matcher(password).find()) {
                violations.add("Password must contain at least one special character");
            }
            
            // Check for common passwords
            if (isCommonPassword(password)) {
                violations.add("Password is too common, please choose a different one");
            }
        }
        
        if (!violations.isEmpty()) {
            throw new PasswordPolicyViolationException(violations);
        }
    }
    
    private boolean isCommonPassword(String password) {
        // Check against common passwords list
        Set<String> commonPasswords = Set.of(
            "password123", "admin123", "qwerty123", "password1",
            "123456789", "welcome123", "letmein123"
        );
        return commonPasswords.contains(password.toLowerCase());
    }
}
```

## Input Validation & Sanitization

### Request Validation

```java
@RestController
@RequestMapping("/api/v1/admin/posts")
@Validated
public class BlogPostController {
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BlogPostResponse>> createPost(
            @Valid @RequestBody CreateBlogPostRequest request,
            Authentication authentication) {
        
        // Additional security validation
        validateContentSecurity(request.getContent());
        
        BlogPostResponse response = blogPostService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Post created successfully"));
    }
    
    private void validateContentSecurity(String content) {
        // Check for potential XSS attempts
        if (containsPotentialXSS(content)) {
            throw new SecurityViolationException("Content contains potentially malicious scripts");
        }
        
        // Check content length to prevent DoS
        if (content.length() > 1_000_000) { // 1MB limit
            throw new ValidationException("Content exceeds maximum allowed length");
        }
    }
    
    private boolean containsPotentialXSS(String content) {
        String[] xssPatterns = {
            "<script", "javascript:", "onload=", "onerror=",
            "onclick=", "onmouseover=", "<iframe", "<object",
            "<embed", "<link", "<meta", "<style"
        };
        
        String lowerContent = content.toLowerCase();
        return Arrays.stream(xssPatterns)
            .anyMatch(lowerContent::contains);
    }
}
```

### DTO Validation

```java
public class CreateBlogPostRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]+$", 
             message = "Title contains invalid characters")
    private String title;
    
    @NotBlank(message = "Content is required")
    @Size(min = 10, max = 1000000, message = "Content must be between 10 and 1,000,000 characters")
    private String content;
    
    @Size(max = 500, message = "Excerpt cannot exceed 500 characters")
    private String excerpt;
    
    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;
    
    @Valid
    private List<@Positive(message = "Tag ID must be positive") Long> tagIds;
    
    @NotNull(message = "Status is required")
    private PostStatus status;
    
    @Future(message = "Published date must be in the future")
    private LocalDateTime publishedAt;
    
    @Valid
    private MetaTagsRequest metaTags;
    
    // Constructors, getters, setters...
}
```

### Custom Validators

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeHtmlValidator.class)
public @interface SafeHtml {
    String message() default "Content contains unsafe HTML";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Component
public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, String> {
    
    private final Safelist safelist = Safelist.relaxed()
        .addTags("code", "pre")
        .addAttributes("code", "class")
        .addAttributes("pre", "class");
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        String cleaned = Jsoup.clean(value, safelist);
        return cleaned.equals(value);
    }
}
```

## SQL Injection Prevention

### Repository Security

```java
@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    
    // Safe: Using Spring Data JPA method names
    List<BlogPost> findByStatusAndPublishedAtBefore(PostStatus status, LocalDateTime date);
    
    // Safe: Using @Query with named parameters
    @Query("SELECT p FROM BlogPost p WHERE p.category.slug = :categorySlug " +
           "AND p.status = :status ORDER BY p.publishedAt DESC")
    Page<BlogPost> findPublishedPostsByCategory(
        @Param("categorySlug") String categorySlug,
        @Param("status") PostStatus status,
        Pageable pageable);
    
    // Safe: Using Criteria API for dynamic queries
    @Query("SELECT p FROM BlogPost p WHERE " +
           "(:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:content IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "p.status = 'PUBLISHED'")
    Page<BlogPost> searchPosts(
        @Param("title") String title,
        @Param("content") String content,
        @Param("categoryId") Long categoryId,
        Pageable pageable);
}
```

### Dynamic Query Security

```java
@Service
public class BlogPostSearchService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public Page<BlogPost> searchPosts(BlogPostSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BlogPost> query = cb.createQuery(BlogPost.class);
        Root<BlogPost> root = query.from(BlogPost.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Always filter by published status
        predicates.add(cb.equal(root.get("status"), PostStatus.PUBLISHED));
        
        // Safe parameter binding
        if (criteria.getTitle() != null && !criteria.getTitle().trim().isEmpty()) {
            String titlePattern = "%" + criteria.getTitle().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get("title")), titlePattern));
        }
        
        if (criteria.getCategoryId() != null) {
            predicates.add(cb.equal(root.get("category").get("id"), criteria.getCategoryId()));
        }
        
        if (criteria.getTagIds() != null && !criteria.getTagIds().isEmpty()) {
            Join<BlogPost, Tag> tagJoin = root.join("tags");
            predicates.add(tagJoin.get("id").in(criteria.getTagIds()));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("publishedAt")));
        
        TypedQuery<BlogPost> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<BlogPost> results = typedQuery.getResultList();
        long total = countSearchResults(criteria);
        
        return new PageImpl<>(results, pageable, total);
    }
}
```

## Cross-Site Scripting (XSS) Prevention

### Content Sanitization

```java
@Component
public class ContentSanitizer {
    
    private final Safelist blogContentSafelist;
    private final Safelist commentSafelist;
    
    public ContentSanitizer() {
        // Configure safelist for blog content
        this.blogContentSafelist = Safelist.relaxed()
            .addTags("h1", "h2", "h3", "h4", "h5", "h6")
            .addTags("code", "pre", "kbd", "samp")
            .addTags("mark", "del", "ins")
            .addAttributes("code", "class")
            .addAttributes("pre", "class")
            .addAttributes("a", "target")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addEnforcedAttribute("a", "rel", "noopener noreferrer");
            
        // More restrictive safelist for comments
        this.commentSafelist = Safelist.basic()
            .addTags("code")
            .addAttributes("code", "class");
    }
    
    public String sanitizeBlogContent(String content) {
        if (content == null) {
            return null;
        }
        return Jsoup.clean(content, blogContentSafelist);
    }
    
    public String sanitizeComment(String content) {
        if (content == null) {
            return null;
        }
        return Jsoup.clean(content, commentSafelist);
    }
    
    public String sanitizeForDisplay(String content) {
        if (content == null) {
            return null;
        }
        // Additional encoding for display
        return HtmlUtils.htmlEscape(content);
    }
}
```

### Response Headers

```java
@Component
public class SecurityHeadersFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // XSS Protection
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Content Type Options
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        
        // Frame Options
        httpResponse.setHeader("X-Frame-Options", "DENY");
        
        // Content Security Policy
        httpResponse.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline'; " +
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
            "font-src 'self' https://fonts.gstatic.com; " +
            "img-src 'self' data: https:; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'; " +
            "base-uri 'self'; " +
            "form-action 'self'");
        
        // Referrer Policy
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions Policy
        httpResponse.setHeader("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=()");
        
        chain.doFilter(request, response);
    }
}
```

## Cross-Site Request Forgery (CSRF) Protection

### CSRF Configuration for Web Forms

```java
@Configuration
public class WebSecurityConfig {
    
    @Bean
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**") // API uses JWT
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/", "/posts/**", "/categories/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/admin/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .build();
    }
}
```

### Double Submit Cookie Pattern

```java
@Component
public class CsrfTokenService {
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    public String generateCsrfToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    public boolean validateCsrfToken(String headerToken, String cookieToken) {
        return headerToken != null && 
               cookieToken != null && 
               MessageDigest.isEqual(headerToken.getBytes(), cookieToken.getBytes());
    }
}
```

## Rate Limiting & DDoS Protection

### Rate Limiting Implementation

```java
@Component
public class RateLimitingFilter implements Filter {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    
    // Different limits for different endpoints
    private static final Map<String, Integer> RATE_LIMITS = Map.of(
        "/api/v1/auth/login", 5,      // 5 attempts per minute
        "/api/v1/posts", 100,         // 100 requests per minute
        "/api/v1/admin", 200,         // 200 requests per minute for admin
        "/api/v1/search", 50          // 50 searches per minute
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIpAddress(httpRequest);
        String endpoint = getEndpointPattern(httpRequest.getRequestURI());
        
        if (isRateLimited(clientIp, endpoint)) {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"success\": false, \"message\": \"Rate limit exceeded\"}");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isRateLimited(String clientIp, String endpoint) {
        Integer limit = RATE_LIMITS.get(endpoint);
        if (limit == null) {
            limit = 1000; // Default limit
        }
        
        String key = "rate_limit:" + endpoint + ":" + clientIp;
        String currentCount = redisTemplate.opsForValue().get(key);
        
        if (currentCount == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
            return false;
        }
        
        int count = Integer.parseInt(currentCount);
        if (count >= limit) {
            return true;
        }
        
        redisTemplate.opsForValue().increment(key);
        return false;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
```

### Request Size Limiting

```java
@Configuration
public class RequestSizeLimitConfig {
    
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(10));      // 10MB per file
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));   // 50MB total request
        return factory.createMultipartConfig();
    }
    
    @Bean
    public TomcatServletWebServerFactory tomcatEmbedded() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(connector -> {
            connector.setMaxPostSize(50 * 1024 * 1024); // 50MB
            connector.setMaxHttpHeaderSize(8192);        // 8KB headers
        });
        return factory;
    }
}
```

## Data Protection & Privacy

### Sensitive Data Handling

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    @JsonIgnore // Never serialize email in responses
    private String email;
    
    @Column(nullable = false)
    @JsonIgnore // Never serialize password
    private String password;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_login_ip")
    @JsonIgnore // Sensitive information
    private String lastLoginIp;
    
    // Getters and setters...
}
```

### Data Encryption

```java
@Component
public class DataEncryptionService {
    
    private final AESUtil aesUtil;
    
    @Value("${app.encryption.key}")
    private String encryptionKey;
    
    public String encryptSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        try {
            return aesUtil.encrypt(data, encryptionKey);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt sensitive data", e);
        }
    }
    
    public String decryptSensitiveData(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        try {
            return aesUtil.decrypt(encryptedData, encryptionKey);
        } catch (Exception e) {
            throw new DecryptionException("Failed to decrypt sensitive data", e);
        }
    }
}

@Component
public class AESUtil {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    public String encrypt(String plainText, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        
        byte[] encryptedText = cipher.doFinal(plainText.getBytes());
        
        byte[] encryptedWithIv = new byte[iv.length + encryptedText.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encryptedText, 0, encryptedWithIv, iv.length, encryptedText.length);
        
        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }
    
    public String decrypt(String encryptedText, String key) throws Exception {
        byte[] decodedText = Base64.getDecoder().decode(encryptedText);
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(decodedText, 0, iv, 0, iv.length);
        
        byte[] encrypted = new byte[decodedText.length - GCM_IV_LENGTH];
        System.arraycopy(decodedText, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
        
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        
        byte[] decryptedText = cipher.doFinal(encrypted);
        return new String(decryptedText);
    }
}
```

## Security Monitoring & Logging

### Security Event Logging

```java
@Component
public class SecurityEventLogger {
    
    private static final Logger securityLogger = 
        LoggerFactory.getLogger("SECURITY");
    
    public void logAuthenticationSuccess(String username, String ipAddress) {
        securityLogger.info("Authentication successful - User: {}, IP: {}", 
            username, ipAddress);
    }
    
    public void logAuthenticationFailure(String username, String ipAddress, String reason) {
        securityLogger.warn("Authentication failed - User: {}, IP: {}, Reason: {}", 
            username, ipAddress, reason);
    }
    
    public void logAuthorizationFailure(String username, String resource, String action) {
        securityLogger.warn("Authorization failed - User: {}, Resource: {}, Action: {}", 
            username, resource, action);
    }
    
    public void logSuspiciousActivity(String username, String ipAddress, String activity) {
        securityLogger.error("Suspicious activity detected - User: {}, IP: {}, Activity: {}", 
            username, ipAddress, activity);
    }
    
    public void logDataAccess(String username, String dataType, String action) {
        securityLogger.info("Data access - User: {}, DataType: {}, Action: {}", 
            username, dataType, action);
    }
    
    public void logSecurityViolation(String violation, String details) {
        securityLogger.error("Security violation - Type: {}, Details: {}", 
            violation, details);
    }
}
```

### Audit Trail

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "action")
    private String action;
    
    @Column(name = "resource")
    private String resource;
    
    @Column(name = "resource_id")
    private String resourceId;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "success")
    private Boolean success;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    // Constructors, getters, setters...
}

@Service
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    public void logAction(String username, String action, String resource, 
                         String resourceId, boolean success, String errorMessage) {
        
        HttpServletRequest request = getCurrentRequest();
        
        AuditLog auditLog = AuditLog.builder()
            .username(username)
            .action(action)
            .resource(resource)
            .resourceId(resourceId)
            .ipAddress(getClientIpAddress(request))
            .userAgent(request.getHeader("User-Agent"))
            .timestamp(LocalDateTime.now())
            .success(success)
            .errorMessage(errorMessage)
            .build();
            
        auditLogRepository.save(auditLog);
    }
}
```

## Security Testing

### Security Test Cases

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    @DisplayName("Should prevent SQL injection in search")
    void shouldPreventSqlInjection() {
        String maliciousQuery = "'; DROP TABLE blog_posts; --";
        
        webTestClient.get()
            .uri("/api/v1/search?q={query}", maliciousQuery)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.results").isArray();
        
        // Verify table still exists by making another request
        webTestClient.get()
            .uri("/api/v1/posts")
            .exchange()
            .expectStatus().isOk();
    }
    
    @Test
    @DisplayName("Should sanitize XSS attempts in content")
    void shouldSanitizeXssAttempts() {
        String xssContent = "<script>alert('XSS')</script>Hello World";
        
        CreateBlogPostRequest request = CreateBlogPostRequest.builder()
            .title("Test Post")
            .content(xssContent)
            .categoryId(1L)
            .build();
            
        String adminToken = getAdminToken();
        
        webTestClient.post()
            .uri("/api/v1/admin/posts")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.data.content").value(not(containsString("<script>")));
    }
    
    @Test
    @DisplayName("Should enforce rate limiting")
    void shouldEnforceRateLimiting() {
        // Make multiple requests quickly
        for (int i = 0; i < 10; i++) {
            webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest("invalid", "invalid"))
                .exchange();
        }
        
        // Next request should be rate limited
        webTestClient.post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new LoginRequest("invalid", "invalid"))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
    
    @Test
    @DisplayName("Should validate JWT token properly")
    void shouldValidateJwtTokenProperly() {
        String invalidToken = "invalid.jwt.token";
        
        webTestClient.get()
            .uri("/api/v1/admin/posts")
            .header("Authorization", "Bearer " + invalidToken)
            .exchange()
            .expectStatus().isUnauthorized();
    }
    
    @Test
    @DisplayName("Should enforce HTTPS in production")
    void shouldEnforceHttpsInProduction() {
        // This test would be environment-specific
        // In production, all HTTP requests should redirect to HTTPS
    }
}
```

## Security Configuration Checklist

### Application Security
- [ ] JWT tokens use strong secrets (minimum 256 bits)
- [ ] Password encoding uses Argon2 or bcrypt with high cost
- [ ] Input validation on all endpoints
- [ ] Output encoding/sanitization
- [ ] SQL injection prevention
- [ ] XSS protection headers
- [ ] CSRF protection for web forms
- [ ] Rate limiting implemented
- [ ] Request size limits configured
- [ ] Security headers configured
- [ ] Audit logging enabled
- [ ] Error handling doesn't leak information

### Infrastructure Security
- [ ] HTTPS enforced in production
- [ ] Database connections encrypted
- [ ] Environment variables for secrets
- [ ] Regular security updates
- [ ] Firewall rules configured
- [ ] Monitoring and alerting setup
- [ ] Backup encryption
- [ ] Access controls implemented

### Compliance
- [ ] GDPR compliance for EU users
- [ ] Data retention policies
- [ ] Privacy policy published
- [ ] Terms of service updated
- [ ] Cookie consent implemented
- [ ] Data breach response plan
- [ ] Regular security assessments
- [ ] Penetration testing scheduled

## Incident Response

### Security Incident Response Plan

1. **Detection & Analysis**
   - Monitor security logs
   - Analyze suspicious activities
   - Determine incident severity

2. **Containment**
   - Isolate affected systems
   - Prevent further damage
   - Preserve evidence

3. **Eradication**
   - Remove malicious code
   - Patch vulnerabilities
   - Update security controls

4. **Recovery**
   - Restore systems from clean backups
   - Monitor for recurring issues
   - Validate system integrity

5. **Lessons Learned**
   - Document incident details
   - Update security procedures
   - Improve detection capabilities

### Emergency Contacts
- Security Team: security@personalblog.com
- System Administrator: admin@personalblog.com
- Legal Team: legal@personalblog.com
- External Security Consultant: [Contact Info]

This comprehensive security guidelines document ensures the Personal Blog application maintains the highest security standards while protecting user data and system integrity.