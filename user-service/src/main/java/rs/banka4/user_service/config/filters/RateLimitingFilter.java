package rs.banka4.user_service.config.filters;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.banka4.user_service.exceptions.RateLimitExceeded;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Filter that applies rate limiting to incoming requests based on the client's IP address.
 * This filter ensures that a client cannot exceed a predefined number of requests within a specified time period.
 * <p>
 * The rate limiting is implemented using a token bucket algorithm provided by the Bucket4j library.
 * Each client is assigned a bucket that refills at a fixed rate, and tokens are consumed with each request.
 * <p>
 * The filter uses a Guava cache to store the buckets for each client IP address. The cache entries expire
 * after one hour of inactivity.
 * <p>
 * @see OncePerRequestFilter
 * @see Bucket4j
 * @see Cache
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> cache;

    public RateLimitingFilter() {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();
    }

    /**
     * Filters the incoming request to apply rate limiting based on the client's IP address.
     * If the client exceeds the rate limit, a {@link RateLimitExceeded} exception is thrown.
     *
     * @param request the incoming HTTP request
     * @param response the HTTP response that will be sent back to the client
     * @param filterChain the chain of filters to be applied after this filter
     * @throws ServletException if a servlet-related error occurs during the filtering process
     * @throws IOException if an I/O error occurs during request or response handling
     */
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ip = getClientIP(request);
        Bucket bucket;
        try {
            bucket = cache.get(ip, () -> newBucket(ip));
        } catch (ExecutionException e) {
            throw new ServletException("Internal server error", e);
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        }
        else {
            throw new RateLimitExceeded();
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    /**
     * Creates a new token bucket for the given key (client IP address).
     *
     * @param key the key for the bucket, typically the client's IP address
     * @return a new token bucket
     */
    private Bucket newBucket(String key) {
        return Bucket4j.builder()
                .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                .build();
    }
}